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

import ij_plugins.color.calibration.chart.ReferenceColorSpace
import ij_plugins.color.calibration.regression.CubicPolynomialTriple
import ij_plugins.color.converter.ColorConverter
import ij_plugins.color.util.ImagePlusType

object CorrectionRecipe {
  def apply(
    corrector: CubicPolynomialTriple,
    colorConverter: ColorConverter,
    referenceColorSpace: ReferenceColorSpace,
    imageTypeInt: Int
  ): CorrectionRecipe = {
    val imageType = ImagePlusType.withValue(imageTypeInt)
    CorrectionRecipe(corrector, colorConverter, referenceColorSpace, imageType)
  }
}

/**
 * Parameters needed to perform color correction of an image and convert it to sRGB.
 *
 * @param corrector
 *   color correction mapping. Correction is done in the provided `referenceColorSpace`
 * @param colorConverter
 *   use to convert to color space other than the provided `referenceColorSpace`
 * @param referenceColorSpace
 *   indicates color space in which `corrector` operates
 * @param imageType
 *   ImagePlus image type supported by this correction. Used to enforce matching image type. For instance, is
 *   `imageType` indicates `GRAY32`, it will result in when input is `GRAY16`, due to uncertainty in scaling of image
 *   values
 */
case class CorrectionRecipe(
  corrector: CubicPolynomialTriple,
  colorConverter: ColorConverter,
  referenceColorSpace: ReferenceColorSpace,
  imageType: ImagePlusType
)
