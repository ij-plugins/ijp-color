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

package net.sf.ij_plugins.color.calibration.chart

import ij.process.{ImageProcessor, ColorProcessor}
import net.sf.ij_plugins.color.converter.{RGBWorkingSpace, ColorConverter, ReferenceWhite}
import net.sf.ij_plugins.util.{PerspectiveTransform, IJTools}
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
  final val refWhite: ReferenceWhite = ReferenceWhite.D65

  /** Default converter for transforming color values between different color spaces.
    *
    * Use it to ensure proper scaling for XYZ and RGB color space.
    */
  final val colorConverter = new ColorConverter(refWhite = refWhite,
    rgbSpace = RGBWorkingSpace.sRGB,
    xyzScale = 100,
    rgbScale = 255
  )

  private var _alignmentTransform = new PerspectiveTransform()

  /** Alignment between the reference chips and chips found in the actual chart. */
  final def alignmentTransform: PerspectiveTransform = _alignmentTransform

  final def alignmentTransform_=(transform: PerspectiveTransform) {
    require(transform != null, "Argument 'transform' cannot be null.")
    _alignmentTransform = transform
  }


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
    * @param enabled array with indexes corresponding to ones retured by `referenceColor` methods.
    *                If value is `true` chip with corresponding index is enabled, if `false` it is disabled.
    * @return
    */
  def copyWithEnableChips(enabled: Array[Boolean]): ColorChart

  /** Reference chips without transform applied to their coordinates. */
  def referenceChips: immutable.IndexedSeq[ColorChip]

  /** Color chips with alignment transform applied to their outline. */
  def alignedChips: immutable.IndexedSeq[ColorChip]
}
