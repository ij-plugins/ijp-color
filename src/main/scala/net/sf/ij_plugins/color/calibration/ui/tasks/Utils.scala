/*
 * Image/J Plugins
 * Copyright (C) 2002-2019 Jarek Sacha
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

package net.sf.ij_plugins.color.calibration.ui.tasks

import ij.process.FloatProcessor
import ij.{CompositeImage, ImagePlus, ImageStack}
import net.sf.ij_plugins.color.calibration.chart.ReferenceColorSpace
import net.sf.ij_plugins.color.calibration.ui.{CorrectionRecipe, convertToSRGB}

object Utils {
  def applyCorrection(recipe: CorrectionRecipe,
                      imp: ImagePlus,
                      showError: (String, String, Throwable) => Unit): Option[Array[FloatProcessor]] = {
    val correctedBands = try {
      recipe.corrector.map(imp)
    } catch {
      case t: Throwable =>
        showError("Error while color correcting the image.", t.getMessage, t)
        return None
    }

    // Show floating point stack in the reference color space
    val correctedInReference = {
      val stack = new ImageStack(imp.getWidth, imp.getHeight)
      (recipe.referenceColorSpace.bandsNames zip correctedBands).foreach(v => stack.addSlice(v._1, v._2))
      val mode = if (recipe.referenceColorSpace == ReferenceColorSpace.sRGB) CompositeImage.COMPOSITE else CompositeImage.GRAYSCALE
      new CompositeImage(new ImagePlus(imp.getTitle + "+corrected_" + recipe.referenceColorSpace, stack), mode)
    }
    correctedInReference.show()

    // Convert corrected image to sRGB
    val correctedImage: ImagePlus = convertToSRGB(correctedBands, recipe.referenceColorSpace, recipe.colorConverter)
    correctedImage.setTitle(imp.getTitle + "+corrected_" + recipe.referenceColorSpace + "+sRGB")
    correctedImage.show()

    Option(correctedBands)
  }

}
