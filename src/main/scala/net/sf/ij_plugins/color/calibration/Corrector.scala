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

import ij.ImagePlus
import ij.ImagePlus._
import ij.gui.{Roi, PolygonRoi}
import ij.process._
import net.sf.ij_plugins.color.calibration.regression.CubicPolynomialTriple
import net.sf.ij_plugins.util.IJTools


/** Performs color space mapping using cubic polynomial cross-band functions. */
class Corrector(val poly: CubicPolynomialTriple) {

  /** Map color triplet.
    *
    * @param src input color values. The array must be of size 3.
    * @return new color triplet.
    */
  def map(src: Array[Double]): Array[Double] = {
    require(src.length == 3, "Color space mapping can only be done when input image has 3 color bands")

    val dest: Array[Double] = new Array[Double](3)
    poly.evaluate(src, dest)
    dest
  }

  def map(a: Array[Array[Double]]): Array[Array[Double]] = {
    a.map(map)
  }

  /** Color calibrate input image `src`, convert result to sRGB color space.
    *
    * Calibration mapping must be set before calling this method.
    * It is critical to only use this method on the same type of an image as it was used for
    * computing the calibration mapping.

    * @param src image to be calibrated.
    * @return converted image in sRGB color space.
    * @throws IllegalArgumentException if the mapping was not set.
    * @see #map[T <: ImageProcessor](src: Array[T])
    */
  def mapToFloat(src: ColorProcessor): Array[FloatProcessor] = {
    map(IJTools.splitRGB(src))
  }

  /** Map color space of the input image using this tri-polynomial.
    *
    * @param src input image.
    * @return Mapped image and information about clipped values.
    * @see #map(ij.process.FloatProcessor[])
    * @see #mapToFloat(ij.process.FloatProcessor[])
    */
  def map(src: ColorProcessor): RGBMappingResult = {
    require(src != null, "Argument 'src' cannot be null.")

    map(IJTools.convertToFloat(src))
  }

  /** Color calibrate input image `src` three-band image to the `referenceColorSpace`.
    *
    * Calibration mapping must be computed or set before calling this method.
    * It is critical to only use this method on the same type of an image as it was used for
    * computing the calibration mapping.
    *
    * The input image slices must be of a a grey level type: `ByteProcessor`, `ShortProcessor`, or `FloatProcessor`.
    * Value calibration is ignored.
    *
    * @param src image to be calibrated.
    * @return calibrated image in the `referenceColorSpace`.
    * @throws IllegalArgumentException if the mapping is not set or
    *                                  the images in the array are not of the same type and dimension.
    */
  def map[T <: ImageProcessor](src: Array[T]): Array[FloatProcessor] = {
    // Sanity checks
    IJTools.validateSameTypeAndDimensions(src, 3)
    require(
      src(0).isInstanceOf[ByteProcessor] | src(0).isInstanceOf[ShortProcessor] | src(0).isInstanceOf[FloatProcessor],
      "The input image slices must be of a a grey level type: `ByteProcessor`, `ShortProcessor`, or `FloatProcessor`, " +
        "got: " + src(0).getClass
    )

    val width = src(0).getWidth
    val height = src(0).getHeight
    val dest = Array.range(0, 3).map(_ => new FloatProcessor(width, height))


    val n = width * height
    for (i <- 0 until n) {
      val c = Array[Double](src(0).getf(i), src(1).getf(i), src(2).getf(i))
      val corrected = map(c)
      dest(0).setf(i, corrected(0).toFloat)
      dest(1).setf(i, corrected(1).toFloat)
      dest(2).setf(i, corrected(2).toFloat)
    }
    dest
  }

  /** Color calibrate input `image`.
    *
    * Calibration mapping must be computed before calling this method.
    * It is critical to only use this method on the same type of an image as it was used for
    * computing the calibration mapping.

    * @param image image to be calibrated.
    * @return calibrated image.
    * @throws IllegalArgumentException if the mapping was not yet computed or input image of of incorrect type.
    * @see #computeCalibrationMapping(chipMargin: Double, image: ColorProcessor)
    */
  def map(image: ImagePlus): Array[FloatProcessor] = {
    (image.getType, image.getStackSize) match {
      case (COLOR_RGB, 1) =>
        val src = image.getProcessor.asInstanceOf[ColorProcessor]
        mapToFloat(src)
      case (GRAY8 | ImagePlus.GRAY16 | ImagePlus.GRAY16, 3) =>
        val src = (1 to 3).map(image.getStack.getProcessor).toArray
        map(src)
      case _ => throw new IllegalArgumentException("Unsupported ImagePlusType [" + image.getType + "] " +
        "or stack size [" + image.getStackSize + "].")
    }
  }

  /** Color calibrate input `image`.
    *
    * Image is cropped to the provided ROI and computation is done only for the cropped part.
    * Calibration mapping must be computed before calling this method.
    * It is critical to only use this method on the same type of an image as it was used for
    * computing the calibration mapping.

    * @param image image to be calibrated.
    * @return pair (calibrated cropped image, cropped roi)
    * @throws IllegalArgumentException if the mapping was not yet computed or input image of of incorrect type.
    * @see #computeCalibrationMapping(chipMargin: Double, image: ColorProcessor)
    */
  def map(image: ImagePlus, roi: PolygonRoi): (Array[FloatProcessor], PolygonRoi) = {
    val fps: Array[FloatProcessor] = (image.getType, image.getStackSize) match {
      case (COLOR_RGB, 1) =>
        val src = image.getProcessor
        src.setRoi(roi)
        mapToFloat(src.crop().asInstanceOf[ColorProcessor])
      case (GRAY8 | ImagePlus.GRAY16 | ImagePlus.GRAY16, 3) =>
        val src = (1 to 3).map(image.getStack.getProcessor).toArray
        val srcCropped = src.map {
          ip =>
            ip.setRoi(roi)
            ip.crop()
        }
        map(srcCropped)
      case _ => throw new IllegalArgumentException("Unsupported ImagePlusType [" + image.getType + "] " +
        "or stack size [" + image.getStackSize + "].")
    }

    // Compute recreate cropped ROI
    val bounds = roi.getBounds
    val poly = roi.getPolygon
    val n = poly.npoints
    val xs, ys = new Array[Int](n)
    for (i <- 0 until n) {
      xs(i) = poly.xpoints(i) - bounds.x
      ys(i) = poly.ypoints(i) - bounds.y
    }

    (fps, new PolygonRoi(xs, ys, n, Roi.POLYGON))
  }


  /** Map color space of the input image consisting of three bands. Each band must be of the same size.
    *
    * @param src input image.
    * @return Mapped image bands.
    * @see #map(ij.process.ColorProcessor)
    * @see #map(ij.process.FloatProcessor[])
    */
  def mapToFloat(src: Array[FloatProcessor]): Array[FloatProcessor] = {
    require(src.length == 3, "Color space mapping can only be done when input image has 3 color bands.")

    val srcPixels = new Array[Array[Float]](3)
    val dest = new Array[FloatProcessor](3)
    val destPixels = new Array[Array[Float]](3)

    for (band <- 0 until src.length) {
      srcPixels(band) = src(band).getPixels.asInstanceOf[Array[Float]]
      dest(band) = new FloatProcessor(src(band).getWidth, src(band).getHeight)
      destPixels(band) = dest(band).getPixels.asInstanceOf[Array[Float]]
    }
    val srcRGB = new Array[Double](3)
    val destRGB = new Array[Double](3)
    val numberOfPixels = srcPixels(0).length
    for (i <- 0 until numberOfPixels) {
      for (band <- 0 until 3) {
        srcRGB(band) = srcPixels(band)(i)
      }
      poly.evaluate(srcRGB, destRGB)
      for (band <- 0 until 3) {
        destPixels(band)(i) = destRGB(band).toFloat
      }
    }
    dest
  }

  /** Map color space of the input image consisting of three bands. Each band must be of the same size.
    *
    * @param src input image.
    * @return Mapped image and information about clipped values.
    * @see #map(ij.process.ColorProcessor)
    * @see #mapToFloat(ij.process.FloatProcessor[])
    */
  private def map(src: Array[FloatProcessor]): RGBMappingResult = {
    require(src != null)
    require(src != null)
    require(src.length == 3, "Image array 'src' has to be of size 3")

    val width = src(0).getWidth
    val height = src(0).getHeight

    for (i <- 0 until src.length) {
      require(width == src(i).getWidth, "All bands must have the same width == " + width)
      require(height == src(i).getHeight, "All bands must have the same height == " + height)
    }

    val destFPs = mapToFloat(src)
    val destFlatPixels = destFPs.map(_.getPixels.asInstanceOf[Array[Float]])
    val nbPixels = width * height
    val destBytes = Array.ofDim[Byte](3, nbPixels)

    val clippingLow = new Array[Long](3)
    val clippingHigh = new Array[Long](3)
    for (pixelNumber <- 0 until nbPixels) {
      for (bandNumber <- 0 until 3) {
        val intValue = math.round(destFlatPixels(bandNumber)(pixelNumber))
        val byteValue: Byte = if (intValue < 0) {
          clippingLow(bandNumber) += 1
          0
        } else if (intValue > 255) {
          clippingHigh(bandNumber) += 1
          (255 & 0xff).toByte
        } else {
          (intValue & 0xff).toByte
        }
        destBytes(bandNumber)(pixelNumber) = byteValue
      }
    }

    val image = new ColorProcessor(width, height)
    image.setRGB(destBytes(0), destBytes(1), destBytes(2))

    RGBMappingResult(image, clippingLow, clippingHigh)
  }
}
