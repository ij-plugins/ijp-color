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

package ij_plugins.color.ui.calibration

import ij.process.FloatProcessor
import ij.{CompositeImage, ImagePlus, ImageStack}
import ij_plugins.color.calibration.CalibrationUtils.convertToSRGB
import ij_plugins.color.calibration.CorrectionRecipe
import ij_plugins.color.calibration.chart.ReferenceColorSpace
import ij_plugins.color.converter.ReferenceWhite

object CalibrationUtils {

  case class ApplyCalibrationOutput(
                                     correctedInReferenceSpace: Option[CompositeImage],
                                     correctedInSRGB: Option[ImagePlus],
                                     correctedBands: Array[FloatProcessor]
                                   )

  def applyCorrection(
                       recipe: CorrectionRecipe,
                       imp: ImagePlus,
                       computeInReference: Boolean,
                       computeInSRGB: Boolean
                     ): Either[IJPError, ApplyCalibrationOutput] = {

    val correctedBandsE =
      try {
        Right(recipe.corrector.map(imp))
      } catch {
        case t: Throwable =>
          Left(IJPError("Error while color correcting the image.", t))
      }

    correctedBandsE.map { correctedBands =>
      // Show floating point stack in the reference color space
      val correctedInReference =
        if (computeInReference) {
          val stack = new ImageStack(imp.getWidth, imp.getHeight)
          (recipe.referenceColorSpace.bandsNames zip correctedBands).foreach(v => stack.addSlice(v._1, v._2))
          val mode =
            if (recipe.referenceColorSpace == ReferenceColorSpace.sRGB) CompositeImage.COMPOSITE
            else CompositeImage.GRAYSCALE
          Option(new CompositeImage(
            new ImagePlus(imp.getTitle + "+corrected_" + recipe.referenceColorSpace, stack),
            mode
          ))
        } else {
          None
        }

      // Convert corrected image to sRGB
      val correctedInSRGB =
        if (computeInSRGB) {
          val img = convertToSRGB(correctedBands, recipe.referenceColorSpace, recipe.colorConverter)
          img.setTitle(imp.getTitle + "+corrected_" + recipe.referenceColorSpace + "+sRGB")
          Option(img)
        } else {
          None
        }

      ApplyCalibrationOutput(correctedInReference, correctedInSRGB, correctedBands)
    }
  }

  def showImageInLab(colorSpace: ReferenceColorSpace,
                     refWhite: ReferenceWhite,
                     correctedBands: Array[FloatProcessor],
                     title: String
                    ): Unit = {
    toLab(colorSpace, refWhite, correctedBands, title).show()
  }

  def toLab(colorSpace: ReferenceColorSpace, refWhite: ReferenceWhite, correctedBands: Array[FloatProcessor], title: String): ImagePlus = {
    val labFPs = colorSpace.toLab(correctedBands, refWhite)
    val stack = new ImageStack(labFPs(0).getWidth, labFPs(0).getHeight)
    stack.addSlice("L*", labFPs(0))
    stack.addSlice("a*", labFPs(1))
    stack.addSlice("b*", labFPs(2))
    new CompositeImage(new ImagePlus(title, stack), CompositeImage.GRAYSCALE)
  }

}
