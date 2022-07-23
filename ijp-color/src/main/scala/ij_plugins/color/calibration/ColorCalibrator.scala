/*
 * Image/J Plugins
 * Copyright (C) 2002-2022 Jarek Sacha
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
import ij.ImagePlus.*
import ij.process.*
import ij_plugins.color.calibration.chart.{ColorChart, ReferenceColorSpace}
import ij_plugins.color.calibration.regression.{CubicPolynomialTriple, MappingFactory, MappingMethod}
import ij_plugins.color.util.Utils.{clipUInt8D, delta}

import scala.reflect.ClassTag

/** Color calibration helper methods */
object ColorCalibrator {

  /**
    * Results of computing color calibration.
    *
    * @param reference
    * reference color values
    * @param observed
    * observed color values
    * @param corrected
    * color values after calibration
    * @param corrector
    * coefficients of the polynomial mapping functions.
    */
  case class CalibrationFit(reference: IndexedSeq[IndexedSeq[Double]],
                            observed: IndexedSeq[IndexedSeq[Double]],
                            corrected: IndexedSeq[IndexedSeq[Double]],
                            corrector: CubicPolynomialTriple) {
    // Validate inputs
    require(reference.nonEmpty)
    require(reference.forall(_.length == 3))
    require(observed.length == reference.length)
    require(observed.forall(_.length == 3))
    require(corrected.length == reference.length)
    require(corrected.forall(_.length == 3))

    /**
      */
    def correctedDeltas: IndexedSeq[Double] = reference zip corrected map (p => delta(p._1, p._2))
  }

  /** Create instance of ColorCalibrator */
  def apply(
    chart: ColorChart,
    referenceColorSpace: ReferenceColorSpace,
    mappingMethod: MappingMethod
  ): ColorCalibrator = {
    new ColorCalibrator(chart, referenceColorSpace, mappingMethod, clipReferenceRGB = false)
  }

  def apply(
    chart: ColorChart,
    referenceColorSpaceName: String,
    mappingMethodName: String
  ): ColorCalibrator = {
    val referenceColorSpace = ReferenceColorSpace.withName(referenceColorSpaceName)
    val mappingMethod       = MappingMethod.withName(mappingMethodName)
    new ColorCalibrator(chart, referenceColorSpace, mappingMethod, clipReferenceRGB = false)
  }
}

/**
 * Performs color calibration using a color chart.
 *
 * The calibration in performed in the specified reference color space. For the best results the reference color space
 * should be similar to the color space of the input image. That is, in optimal conditions function mapping from the
 * actual color space to the reference color space should be close to linear. For instance for raw images the CIE XYZ
 * color space is better reference than sRGB since mapping between the input and the reference can be done with good
 * accuracy using low order polynomial. If the input image is a typical JPEG image it is best to select sRGB as a
 * reference color space.
 *
 * @param chart
 *   color chart providing reference color values, location of chips, and the alignment transform to spatially map color
 *   chip locations fro the reference to input image.
 * @param referenceColorSpace
 *   assumption about the color space of the input image. Reference color values will be generated in that color space.
 * @param mappingMethod
 *   type of polynomial function used to map from input to the reference color space.
 * @param clipReferenceRGB
 *   if the reference was selected as RGB, the reference color values can be outside the gamut of that color space
 *   (lower than 0 or larger than 255). This parameter gives an option to clip reference color value to fit within the
 *   gamut.
 */
class ColorCalibrator(
  val chart: ColorChart,
  val referenceColorSpace: ReferenceColorSpace,
  val mappingMethod: MappingMethod,
  val clipReferenceRGB: Boolean
) {

  import ColorCalibrator.*

  /**
    * Compute coefficients of a polynomial color mapping between the reference and observed colors.
    *
    * @param observed
    * Actually observed color values.
    * @return
    * color mapping coefficients.
    * @throws java.lang.IllegalArgumentException
    * when the reference and observed values are not sufficient to compute mapping polynomial coefficients, for
    * instance, if the desired polynomial order is too high given the number of reference colors.
    */
  def computeCalibrationMapping(observed: IndexedSeq[IndexedSeq[Double]]): CalibrationFit = {
    require(
      observed.length == chart.referenceChipsEnabled.length,
      s"Expecting ${chart.referenceChipsEnabled.length} observations, got ${observed.length}."
    )
    require(observed.forall(_.length == 3))


    val referenceClipped = {
      val reference1 = chart.referenceColorEnabled(referenceColorSpace)

      if (clipReferenceRGB && ReferenceColorSpace.sRGB == referenceColorSpace) {
        // Values should be clipped, but decimal precision should net be truncated
        reference1.map(r => r.map(clipUInt8D))
      } else {
        reference1
      }
    }

    require(referenceClipped.length == observed.length)

    val corrector = MappingFactory.createCubicPolynomialTriple(referenceClipped, observed, mappingMethod)
    val corrected = observed.map(corrector.map)

    CalibrationFit(reference = referenceClipped, observed = observed, corrected = corrected, corrector)
  }

  /**
    * Estimate calibration coefficient. This method does not clip reference color values.
    *
    * @param bands
    * input image bands to measure observed color value of chart's chips.
    * @throws java.lang.IllegalArgumentException
    * if one of the calibration mapping functions cannot be computed.
    */
  def computeCalibrationMapping[T <: ImageProcessor : ClassTag](bands: IndexedSeq[T]): CalibrationFit = {
    require(
      (bands.forall(_.isInstanceOf[ByteProcessor]) |
        bands.forall(_.isInstanceOf[ShortProcessor]) |
        bands.forall(_.isInstanceOf[FloatProcessor])) & (bands.length == 3),
      "Expecting 3 gray-level images, got " + bands.length
    )

    val observed = chart.averageChipColorEnabled(bands)
    computeCalibrationMapping(observed)
  }

  /**
   * Estimate calibration coefficients. Reference color values will be clipped if reference color space is RGB and
   * `clipReferenceRGB` is true.
   *
   * @param image
   *   input image to measure observed color value of chart's chips.
   * @throws java.lang.IllegalArgumentException
   *   if one of the calibration mapping functions cannot be computed.
   */
  def computeCalibrationMapping(image: ColorProcessor): CalibrationFit = {
    val observed = chart.averageChipColorEnabled(image)
    computeCalibrationMapping(observed)
  }

  /**
   * Estimate calibration coefficients. Reference color values will be clipped if reference color space is RGB and
   * `clipReferenceRGB` is true.
   *
   * @param image
   *   input image to measure observed color value of chart's chips.
   * @throws java.lang.IllegalArgumentException
   *   if one of the calibration mapping functions cannot be computed or if the input image is not RGB or not a three
   *   slice stack of gray level images.
   */
  def computeCalibrationMapping(image: ImagePlus): CalibrationFit = {
    (image.getType, image.getStackSize) match {
      case (COLOR_RGB, 1) =>
        val src = image.getProcessor.asInstanceOf[ColorProcessor]
        computeCalibrationMapping(src)
      case (GRAY8, 3) | (GRAY16, 3) | (GRAY32, 3) =>
        val src = (1 to 3).map(image.getStack.getProcessor)
        computeCalibrationMapping(src)
      case _ =>
        throw new IllegalArgumentException(
          "Input image must be either single slice RGB image or three slice gray level image."
        )
    }
  }
}
