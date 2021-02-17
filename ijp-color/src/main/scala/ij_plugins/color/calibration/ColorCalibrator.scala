/*
 * Image/J Plugins
 * Copyright (C) 2002-2021 Jarek Sacha
 * Author's email: jpsacha at gmail dot com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Latest release available at https://github.com/ij-plugins/ijp-color/
 */

package ij_plugins.color.calibration

import ij.ImagePlus
import ij.ImagePlus._
import ij.process._
import ij_plugins.color.calibration.chart.{ColorChart, ReferenceColorSpace}
import ij_plugins.color.calibration.regression.{CubicPolynomialTriple, MappingFactory, MappingMethod}
import ij_plugins.color.util.{clipUInt8D, delta}

/** Color calibration helper methods */
object ColorCalibrator {

  /** Results of computing color calibration.
    *
    * @param reference reference color values
    * @param observed  observed color values
    * @param corrected color values after calibration
    * @param corrector coefficients of the polynomial mapping functions.
    */
  case class CalibrationFit(reference: Array[Array[Double]],
                            observed: Array[Array[Double]],
                            corrected: Array[Array[Double]],
                            corrector: CubicPolynomialTriple) {
    // Validate inputs
    require(reference.length > 0)
    require(reference.forall(_.length == 3))
    require(observed.length == reference.length)
    require(observed.forall(_.length == 3))
    require(corrected.length == reference.length)
    require(corrected.forall(_.length == 3))

    /** */
    def correctedDeltas: Array[Double] = reference zip corrected map (p => delta(p._1, p._2))
  }

  /** Create instance of ColorCalibrator */
  def apply(chart: ColorChart,
            referenceColorSpace: ReferenceColorSpace,
            mappingMethod: MappingMethod,
            clipReferenceRGB: Boolean = true): ColorCalibrator = {
    new ColorCalibrator(chart, referenceColorSpace, mappingMethod, clipReferenceRGB)
  }
}


/** Performs color calibration using a color chart.
  *
  * The calibration in performed in the specified reference color space.
  * For the best results the reference color space should be similar to the color space of the input image.
  * That is, in optimal conditions function mapping from the actual color space to the reference color space
  * should be close to linear.
  * For instance for raw images the CIE XYZ color space is better reference than sRGB since mapping between the input and
  * the reference can be done with good accuracy using low order polynomial.
  * If the input image is a typical JPEG image it is best to select sRGB as a reference color space.
  *
  * @param chart               color chart providing reference color values, location of chips, and the alignment transform
  *                            to spatially map color chip locations fro the reference to input image.
  * @param referenceColorSpace assumption about the color space of the input image.
  *                            Reference color values will be generated in that color space.
  * @param mappingMethod       type of polynomial function used to map from input to the reference color space.
  * @param clipReferenceRGB    if the reference was selected as RGB, the reference color values can be outside the gamut
  *                            of that color space (lower than 0 or larger than 255). This parameter gives an option
  *                            to clip reference color value to fit within the gamut.
  */
class ColorCalibrator(val chart: ColorChart,
                      val referenceColorSpace: ReferenceColorSpace,
                      val mappingMethod: MappingMethod,
                      val clipReferenceRGB: Boolean) {

  import ColorCalibrator._

  /** Compute coefficients of a polynomial color mapping between the reference and observed colors.
    *
    * @param observed Actually observed color values.
    * @return color mapping coefficients.
    * @throws ij_plugins.color.ColorException when the reference and observed values are not sufficient
    *                                         to compute mapping polynomial coefficients, for instance,
    *                                         if the desired polynomial order is
    *                                         too high given the number of reference colors.
    */
  def computeCalibrationMapping(observed: Array[Array[Double]]): CalibrationFit = {
    require(observed.length == chart.referenceChips.length,
      s"Expecting ${chart.referenceChips.length} observations, got ${observed.length}.")
    require(observed.forall(_.length == 3))

    val reference = chart.referenceColor(referenceColorSpace).clone()
    if (clipReferenceRGB && ReferenceColorSpace.sRGB == referenceColorSpace) {
      reference.foreach(r => {
        // Values should be clipped, but decimal precision should net be truncated
        r(0) = clipUInt8D(r(0))
        r(1) = clipUInt8D(r(1))
        r(2) = clipUInt8D(r(2))
      })
    }

    val corrector = MappingFactory.createCubicPolynomialTriple(reference, observed, mappingMethod)
    val corrected = observed.map(corrector.map)

    CalibrationFit(reference = reference, observed = observed, corrected = corrected, corrector)
  }

  /** Estimate calibration coefficient. This method does not clip reference color values.
    *
    * @param bands input image bands to measure observed color value of chart's chips.
    * @throws ColorException if one of the calibration mapping functions cannot be computed.
    */
  def computeCalibrationMapping[T <: ImageProcessor](bands: Array[T]): CalibrationFit = {
    require(
      (bands.forall(_.isInstanceOf[ByteProcessor]) |
        bands.forall(_.isInstanceOf[ShortProcessor]) |
        bands.forall(_.isInstanceOf[FloatProcessor])
        ) & (bands.length == 3),
      "Expecting 3 gray-level images, got " + bands.length
    )

    val observed = chart.averageChipColor(bands)
    computeCalibrationMapping(observed)
  }

  /** Estimate calibration coefficients. Reference color values will be clipped if reference color space is RGB and
    * `clipReferenceRGB` is true.
    *
    * @param image input image to measure observed color value of chart's chips.
    * @throws ColorException if one of the calibration mapping functions cannot be computed.
    */
  def computeCalibrationMapping(image: ColorProcessor): CalibrationFit = {
    val observed = chart.averageChipColor(image)
    computeCalibrationMapping(observed)
  }

  /** Estimate calibration coefficients. Reference color values will be clipped if reference color space is RGB and
    * `clipReferenceRGB` is true.
    *
    * @param image input image to measure observed color value of chart's chips.
    * @throws ColorException           if one of the calibration mapping functions cannot be computed.
    * @throws IllegalArgumentException if input image is not RGB or not a three slice stack of gray level images.
    */
  def computeCalibrationMapping(image: ImagePlus): CalibrationFit = {
    (image.getType, image.getStackSize) match {
      case (COLOR_RGB, 1) =>
        val src = image.getProcessor.asInstanceOf[ColorProcessor]
        computeCalibrationMapping(src)
      case (GRAY8, 3) | (GRAY16, 3) | (GRAY32, 3) =>
        val src = (1 to 3).map(image.getStack.getProcessor).toArray
        computeCalibrationMapping(src)
      case _ =>
        throw new IllegalArgumentException("Input image must be either single slice RGB image or three slice gray level image.")
    }
  }
}
