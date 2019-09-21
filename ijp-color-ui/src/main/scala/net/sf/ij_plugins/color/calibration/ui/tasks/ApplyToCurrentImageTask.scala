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

import ij.{CompositeImage, IJ, ImagePlus, ImageStack}
import net.sf.ij_plugins.color.calibration.{CorrectionRecipe, applyCorrection}
import org.scalafx.extras.BusyWorker.SimpleTask
import org.scalafx.extras.ShowMessage
import scalafx.beans.property.{BooleanProperty, ObjectProperty}
import scalafx.stage.Window

/**
  * Apply color correction to the current selected image in ImageJ UI
  *
  * @param correctionRecipe correction recipe
  * @param showExtraInfo    if true a L*a*b* image will be also shown after correction
  * @param parentWindow     parent window for dialogs
  */
class ApplyToCurrentImageTask(correctionRecipe: ObjectProperty[Option[CorrectionRecipe]],
                              showExtraInfo: BooleanProperty,
                              val parentWindow: Option[Window])
  extends SimpleTask[Unit] with ShowMessage {

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
    if (imp.getType != recipe.imageType) {
      showError(errorTitle, "Image type does not match expected: [" + recipe.imageType + "]", "")
      return
    }

    // Run calibration on the current image
    val correctedBands = applyCorrection(recipe, imp, showException) match {
      case Some(r) => r
      case None =>
        return
    }
    if (showExtraInfo()) {
      correctionRecipe.value.foreach { cr =>
        val labFPs = cr.referenceColorSpace.toLab(correctedBands)
        val stack = new ImageStack(labFPs(0).getWidth, labFPs(0).getHeight)
        stack.addSlice("L*", labFPs(0))
        stack.addSlice("a*", labFPs(1))
        stack.addSlice("b*", labFPs(2))
        new CompositeImage(new ImagePlus(imp.getShortTitle + "-L*a*b*", stack), CompositeImage.GRAYSCALE).show()
      }
    }
  }
}
