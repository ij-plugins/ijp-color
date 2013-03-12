/*
 * Image/J Plugins
 * Copyright (C) 2002-2013 Jarek Sacha
 * Author's email: jsacha at users dot sourceforge dot net
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
 * Latest release available at http://sourceforge.net/projects/ij-plugins/
 */

package net.sf.ij_plugins.color.calibration

import ij.process._
import net.sf.ij_plugins.color.calibration.ColorCalibrator._
import net.sf.ij_plugins.color.calibration.MappingMethod._
import net.sf.ij_plugins.color.calibration.chart.{ReferenceColorSpace, ColorChart}
import net.sf.ij_plugins.util._

/** Color calibration helper methods */
object ColorCalibrator {

  /** Results of computing color calibration.
    *
    * @param reference reference color values
    * @param observed observed color values
    * @param corrected color values after calibration
    * @param mapping coefficients of the polynomial mapping functions.
    */
  case class CalibrationFit(reference: Array[Array[Double]],
                            observed: Array[Array[Double]],
                            corrected: Array[Array[Double]],
                            mapping: CubicPolynomialTriple)

  /** Compute coefficients of a polynomial color mapping between the reference and observed colors.
    *
    * @param reference Desired color values.
    * @param observed Actually observed color values.
    * @param method   Type of the polynomial to use.
    * @return color mapping coefficients.
    * @throws net.sf.ij_plugins.color.ColorException when the reference and observed values are not sufficient
    *                                                to compute mapping polynomial coefficients, for instance,
    *                                                if the desired polynomial order is
    *                                                too high given the number of reference colors.
    * @see #applyCorrection(CubicPolynomialTriple, double[][])
    * @see #applyCorrection(CubicPolynomialTriple, double[])
    */
  def computeColorMapping(reference: Array[Array[Double]],
                          observed: Array[Array[Double]],
                          method: MappingMethod.Value): CubicPolynomialTriple = {
    MappingFactory.createCubicPolynomialTriple(reference, observed, method)
  }

  /** Apply color mapping function to a color triplet. This is not color space specific.
    *
    * @param mapping color mapping function.
    * @param src color triplet to map.
    * @return mapped color.
    * @see #computeColorCorrection(double[][], double[][], MappingMethod)
    * @see #applyCorrection(CubicPolynomialTriple, double[][])
    */
  def applyMapping(mapping: CubicPolynomialTriple, src: Array[Double]): Array[Double] = {
    assert(src != null)

    val corrector = new CubicPolynomialTripleMapper(mapping)
    corrector.map(src)
  }

  /** Apply color mapping to an array of color triplets. This is not color space specific.
    *
    * @param mapping color mapping function.
    * @param src color triplets to map.
    * @return Mapped color triplets
    * @see #computeColorCorrection(double[][], double[][], MappingMethod)
    * @see #applyCorrection(CubicPolynomialTriple, double[])
    */
  def applyCorrection(mapping: CubicPolynomialTriple, src: Array[Array[Double]]): Array[Array[Double]] = {
    assert(src != null)

    val corrector = new CubicPolynomialTripleMapper(mapping)
    val dest = new Array[Array[Double]](src.length)

    for (i <- 0 until src.length) {
      dest(i) = corrector.map(src(i))
    }
    dest
  }

  /** Suggest optimal, most robust, mapping method.
    * The method is selected using leave-one-out cross-validation approach.
    *
    * @param reference reference values.
    * @param observed observed values.
    * @return method with lowest leave-one-out cross-validation error.
    * @throws ColorException if computation of a color mapping function fails.
    */
  def bestMethod(reference: Array[Array[Double]], observed: Array[Array[Double]]): MappingMethod = {
    assert(reference != null)
    assert(observed != null)
    assert(reference.length == observed.length)
    assert(reference.length > 15, "At least 15 chips needed to test cubic cross-band mapping, got" + reference.length + ".")

    var minError: Double = Double.PositiveInfinity
    var bestMethod: MappingMethod = null
    for (method <- MappingMethod.values) {
      var maxDelta: Double = Double.NegativeInfinity
      for (i <- 0 until reference.length) {
        val ss = exclude(i, reference)
        val oo = exclude(i, observed)
        val t = computeColorMapping(ss, oo, method)
        val d = delta(reference(i), applyMapping(t, observed(i)))
        maxDelta = math.max(d, maxDelta)
      }
      // TODO: return results for each tested method
      if (maxDelta < minError) {
        minError = maxDelta
        bestMethod = method
      }
    }
    bestMethod
  }

  /** Exclude element with given `index` from array `a`.
    *
    * @param index index
    * @param a     input array
    * @return array with one element less.
    */
  private def exclude(index: Int, a: Array[Array[Double]]): Array[Array[Double]] = {
    assert(a != null)
    assert(index >= 0)
    assert(index < a.length)

    val r = for (i <- 0 until a.length; if i != index) yield a(i)
    r.toArray
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
  * @param chart color chart providing reference color values, location of chips, and the alignment transform
  *              to spatially map color chip locations fro the reference to input image.
  * @param referenceColorSpace assumption about the color space of the input image.
  *                            Reference color values will be generated in that color space.
  * @param mappingMethod type of polynomial function used to map from input to the reference color space.
  * @param clipReferenceRGB if the reference was selected as RGB, the reference color values can be outside the gamut
  *                         of that color space (lower than 0 or larger than 255). This parameter gives an option
  *                         to clip reference color value to fit within the gamut.
  */
class ColorCalibrator(val chart: ColorChart,
                      val referenceColorSpace: ReferenceColorSpace,
                      val mappingMethod: MappingMethod.Value,
                      val clipReferenceRGB: Boolean = true) {

  private var _mapping: Option[CubicPolynomialTriple] = None

  /** Estimate calibration coefficient. This method does not clip reference color values.
    *
    * @param bands input image bands to measure observed color value of chart's chips.
    * @param chipMargin border excluded from each chip while measuring observed color, as a fraction of chip length/width.
    * @throws ColorException if one of the calibration mapping functions cannot be computed.
    */
  def computeCalibrationMapping[T <: ImageProcessor](chipMargin: Double, bands: Array[T]): CalibrationFit = {
    require((bands.forall(_.isInstanceOf[ByteProcessor]) |
        bands.forall(_.isInstanceOf[ShortProcessor]) |
        bands.forall(_.isInstanceOf[FloatProcessor])
        ) & (bands.length == 3), "Expecting 3 gray-level images, got " + bands.length
    )

    clear()
    val reference = chart.referenceColor(referenceColorSpace)
    val observed = chart.averageChipColor(chipMargin, bands)
    _mapping = Some(computeColorMapping(reference, observed, mappingMethod))
    val corrected = observed.map(o => applyMapping(_mapping.get, o))

    CalibrationFit(reference = reference, observed = observed, corrected = corrected, _mapping.get)
  }

  /** Estimate calibration coefficients. Reference color values will be clipped if reference color space is RGB and
    * `clipReferenceRGB` is true.
    *
    * @param image input image to measure observed color value of chart's chips.
    * @param chipMargin border excluded from each chip while measuring observed color, as a fraction of chip length/width.
    * @throws ColorException if one of the calibration mapping functions cannot be computed.
    */
  def computeCalibrationMapping(chipMargin: Double, image: ColorProcessor): CalibrationFit = {
    clear()
    val reference = chart.referenceColor(referenceColorSpace)
    if (clipReferenceRGB && ReferenceColorSpace.sRGB == referenceColorSpace) {
      reference.foreach(r => {
        r(0) = clipUInt8(r(0))
        r(1) = clipUInt8(r(1))
        r(2) = clipUInt8(r(2))
      })
    }
    val observed = chart.averageChipColor(chipMargin, image)
    _mapping = Some(computeColorMapping(reference, observed, mappingMethod))
    val corrected = observed.map(o => applyMapping(_mapping.get, o))

    CalibrationFit(reference = reference, observed = observed, corrected = corrected, _mapping.get)
  }

  /** Color calibrate input image `src`, use default color space.
    *
    * Calibration mapping must be computed before calling this method.
    * It is critical to only use this method on the same type of an image as it was used for
    * computing the calibration mapping.
    *
    * @param src image to be calibrated.
    * @return calibrated image in the `referenceColorSpace`.
    * @throws IllegalArgumentException if the mapping was not yet computed or
    *                                  the images in the array are not of the same dimension.
    * @see #computeCalibrationMapping[T <: ImageProcessor](chipMargin: Double, bands: Array[T])
    */
  def map[T <: ImageProcessor](src: Array[T]): Array[FloatProcessor] = {
    // Sanity checks
    validate()
    IJTools.validateSameDimensions(src, 3)

    val width = src(0).getWidth
    val height = src(0).getHeight
    val dest = Array.range(0, 3).map(_ => new FloatProcessor(width, height))

    val n = width * height
    for (i <- 0 until n) {
      val x = Array[Double](src(0).getf(i), src(1).getf(i), src(2).getf(i))
      val corrected = applyMapping(_mapping.get, x)
      dest(0).setf(i, corrected(0).toFloat)
      dest(1).setf(i, corrected(1).toFloat)
      dest(2).setf(i, corrected(2).toFloat)
    }
    dest
  }

  /** Color calibrate input image `src`, convert result to sRGB color space.
    *
    * Calibration mapping must be computed before calling this method.
    * It is critical to only use this method on the same type of an image as it was used for
    * computing the calibration mapping.

    * @param src image to be calibrated.
    * @return converted image in sRGB color space.
    * @throws IllegalArgumentException if the mapping was not yet computed or
    *                                  the images in the array are not of the same dimension.
    * @see #computeCalibrationMapping(chipMargin: Double, image: ColorProcessor)
    */
  def map(src: ColorProcessor): ColorProcessor = {
    validate()
    val destDefault = map(IJTools.splitRGB(src))
    referenceColorSpace match {
      case ReferenceColorSpace.sRGB => IJTools.mergeRGB(destDefault)
      case ReferenceColorSpace.XYZ => {
        // Convert XYZ to sRGB
        val converter = chart.colorConverter
        val n = destDefault(0).getWidth * destDefault(0).getHeight
        for (i <- 0 until n) {
          val rgb = converter.xyz2RGB(destDefault(0).getf(i), destDefault(1).getf(i), destDefault(2).getf(i))
          destDefault(0).setf(i, rgb.r.toFloat)
          destDefault(1).setf(i, rgb.g.toFloat)
          destDefault(2).setf(i, rgb.b.toFloat)
        }
        IJTools.mergeRGB(destDefault)
      }
      case _ => throw new IllegalArgumentException("Unsupported reference color space '" + referenceColorSpace + "'.")
    }
  }

  private def clear() {
    _mapping = None
  }

  /** Check if the object is properly initialized to perform corrections. */
  private def validate() {
    require(_mapping != null, "Correction transforms not initialized call computeCalibrationMapping() first.")
  }
}
