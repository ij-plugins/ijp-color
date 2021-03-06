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

package ij_plugins.color.calibration.chart

import ij.ImagePlus
import ij.ImagePlus.{COLOR_RGB, GRAY16, GRAY32, GRAY8}
import ij.process.{ColorProcessor, ImageProcessor}
import ij_plugins.color.converter.RGBWorkingSpace.sRGB
import ij_plugins.color.converter.{ColorConverter, ReferenceWhite}
import ij_plugins.color.util.{IJTools, PerspectiveTransform}

import scala.collection.immutable

/** Generic color chart.
  *
  * It represents the reference parameters:
  * <ul>
  * <li>Color chip values</li>
  * <li>Reference color space parameters like, reference, white, color value scaling, etc.</li>
  * <li>Arrangement, location, and names of chips in the chart.</li>
  * </ul>
  *
  * Contains information about location of the actual chart.
  * This information is represented as an alignment transform between original chart coordinates (reference)
  * and actual chart coordinates.
  */
trait ColorChart {

  /** Reference white for for the reference color expressed in CIE XYZ. */
  def refWhite: ReferenceWhite = ReferenceWhite.D65

  /** Default converter for transforming color values between different color spaces.
    *
    * Use it to ensure proper scaling for XYZ and RGB color space.
    */
  final val colorConverter = new ColorConverter(refWhite = refWhite,
    rgbSpace = sRGB,
    xyzScale = 100,
    rgbScale = 255
  )

  /** Alignment between the reference chips and chips found in the actual chart. */
  def alignmentTransform: PerspectiveTransform

  /** Compute average color within aligned chips from an RGB image.
    *
    * @param cp RGB image
    */
  def averageChipColor(cp: ColorProcessor): Array[Array[Double]] =
    averageChipColor(IJTools.splitRGB(cp))

  /** Compute average color within aligned chips from an an image.
    *
    * Input images is represented by 3 bands. There is no assumption made about image color space.
    *
    * @param src image represented by 3 bands
    */
  def averageChipColor[T <: ImageProcessor](src: Array[T]): Array[Array[Double]]

  /** Compute average color within aligned chips from an an image.
    *
    * @param image is either single slice RGB image (ColorProcessor)
    *              or three slices of gray processors representing color bands.
    * @return average for each band in each color chip (array of triples).
    */
  def averageChipColor(image: ImagePlus): Array[Array[Double]] = {
    (image.getType, image.getStackSize) match {
      case (COLOR_RGB, 1) =>
        val src = image.getProcessor.asInstanceOf[ColorProcessor]
        averageChipColor(src)
      case (GRAY8, 3) | (GRAY16, 3) | (GRAY32, 3) =>
        val src = (1 to 3).map(image.getStack.getProcessor).toArray
        averageChipColor(src)
      case _ =>
        throw new IllegalArgumentException("Input image must be either single slice RGB image or three slice gray level image.")
    }
  }

  /** Return reference colors represented in given color space. */
  def referenceColor(colorSpace: ReferenceColorSpace): Array[Array[Double]] = colorSpace match {
    case ReferenceColorSpace.XYZ => referenceColorXYZ
    case ReferenceColorSpace.sRGB => referenceColorSRGB
    case _ => throw new IllegalArgumentException("Unsupported reference color space: '" + colorSpace + "'.")
  }

  /** Return reference color expressed in CIE XYZ color space assuming default reference white for this chart. */
  def referenceColorXYZ: Array[Array[Double]]

  /** Return reference color expressed in sRGB color space. */
  def referenceColorSRGB: Array[Array[Double]] = {
    referenceColorXYZ.map(xyz => colorConverter.xyzToRGB(xyz(0), xyz(1), xyz(2)).toArray)
  }

  /** Creates a copy of this chart in which some chips cn be enabled/disabled.
    *
    * @param enabled array with indexes corresponding to ones returned by `referenceColor` methods.
    *                If value is `true` chip with corresponding index is enabled, if `false` it is disabled.
    * @return
    */
  def copyWithEnableChips(enabled: Array[Boolean]): ColorChart

  /** Reference chips without transform applied to their coordinates. */
  def referenceChips: immutable.IndexedSeq[ColorChip]

  /** Color chips with alignment transform applied to their outline. */
  def alignedChips: immutable.IndexedSeq[ColorChip]
}
