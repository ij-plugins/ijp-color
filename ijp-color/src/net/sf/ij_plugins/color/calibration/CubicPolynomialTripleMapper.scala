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

import ij.process.{ColorProcessor, FloatProcessor}
import net.sf.ij_plugins.util.IJTools


/** Performs color space mapping using cubic polynomial cross-band functions. */
class CubicPolynomialTripleMapper(val poly: CubicPolynomialTriple) {

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
}
