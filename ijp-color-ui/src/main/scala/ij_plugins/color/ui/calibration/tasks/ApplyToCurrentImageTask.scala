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

package ij_plugins.color.ui.calibration.tasks

import ij.IJ
import ij_plugins.color.calibration.CorrectionRecipe
import ij_plugins.color.ui.calibration.CalibrationUtils
import ij_plugins.color.ui.calibration.tasks.CalibrateTask.OutputConfig
import ij_plugins.color.util.ImagePlusType
import org.scalafx.extras.BusyWorker.SimpleTask
import org.scalafx.extras.ShowMessage
import scalafx.beans.property.ObjectProperty
import scalafx.stage.Window

/**
  * Apply color correction to the current selected image in ImageJ UI
  *
  * @param correctionRecipe correction recipe
  * @param parentWindow     parent window for dialogs
  */
class ApplyToCurrentImageTask(
                               correctionRecipe: ObjectProperty[Option[CorrectionRecipe]],
                               outputConfig: OutputConfig,
                               val parentWindow: Option[Window]
                             ) extends SimpleTask[Unit]
  with ShowMessage {

  def call(): Unit = {
    val errorTitle = "Cannot apply Correction"

    // Check that calibration recipe is computed
    val recipe = correctionRecipe() match {
      case Some(r) => r
      case None =>
        showError(errorTitle, "Correction parameters not available.", "")
        return
    }

    // Get current image
    val imp = IJ.getImage
    if (imp == null) {
      IJ.noImage()
      return
    }

    // Verify that image is of correct type
    if (imp.getType != recipe.imageType.value) {
      showError(
        errorTitle,
        s"Image type does not match expected: ${recipe.imageType} [${recipe.imageType.value}],  " +
          s"got ${ImagePlusType.withValueOpt(imp.getType).getOrElse("???")} [${imp.getType}]",
        ""
      )
      return
    }

    // Run calibration on the current image
    val correctionOutputLR =
      CalibrationUtils.applyCorrection(recipe, imp, outputConfig.imageInReferenceColorSpace, outputConfig.imageInSRGB)

    correctionOutputLR match {
      case Right(correctionOutput) =>
        correctionOutput.correctedInSRGB.foreach(_.show())
        correctionOutput.correctedInReferenceSpace.foreach(_.show())

        if (outputConfig.imageInLab) {
          val refWhite = recipe.colorConverter.refWhite
          CalibrationUtils.showImageInLab(
            recipe.referenceColorSpace,
            refWhite,
            correctionOutput.correctedBands,
            imp.getShortTitle + s" - CIE L*a*b* ${refWhite.entryName}"
          )
        }

      case Left(error) =>
        showException(error.message, error.t.getMessage, error.t)
    }
  }
}
