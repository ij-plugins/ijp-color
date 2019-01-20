/*
 * Image/J Plugins
 * Copyright (C) 2002-2017 Jarek Sacha
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
 * Latest release available at http://sourceforge.net/projects/ij-plugins/
 */

package net.sf.ij_plugins.color.calibration.ui

import ij.ImagePlus.{COLOR_RGB, GRAY16, GRAY32, GRAY8}
import ij.plugin.PlugIn
import ij.{IJ, ImageListener, ImagePlus}
import net.sf.ij_plugins.color.ColorFXUI
import net.sf.ij_plugins.fx._
import net.sf.ij_plugins.util.IJTools
import org.scalafx.extras._

import scalafx.Includes._
import scalafx.scene.Scene
import scalafx.stage.Stage

/** ImageJ plugin for running image color calibration. */
class ColorCalibratorPlugin extends PlugIn {

  private final val Title = "Color Calibrator"
  private var image: Option[ImagePlus] = None
  private var model: Option[ColorCalibratorModel] = None
  private var dialogStage: Option[Stage] = None


  def run(arg: String): Unit = {

    // Check is image is available
    image = Some(IJ.getImage)
    if (image.isEmpty) {
      IJ.noImage()
      return
    }

    // Verify the input images is of correct type.
    (image.get.getType, image.get.getStackSize) match {
      case (COLOR_RGB, 1) =>
      case (GRAY8 | GRAY16 | GRAY32, 3) =>
      case _ =>
        IJ.error(Title, "Unsupported image. It must be either single slice RGB image or three slice gray level image.")
        return
    }

    setupImageListener()

    initializeFX()

    onFX {
      // Create dialog
      dialogStage = Some(
        new Stage {
          title = Title
          IJTools.imageJIconAsFXImage.foreach(icons += _)
        }
      )

      val mvvm = new ColorCalibratorMVVM(image.get, dialogStage.get)
      model = Some(mvvm.model)
      val mainView = mvvm.view
      dialogStage.get.scene = new Scene {
        stylesheets ++= ColorFXUI.stylesheets
        root = mainView
      }
      dialogStage.get.show()
    }
  }

  private def setupImageListener(): Unit = {
    ImagePlus.addImageListener(
      new ImageListener {
        def imageUpdated(imp: ImagePlus): Unit = {
          if (image.contains(imp)) {
            handleImageUpdated()
          }
        }

        def imageClosed(imp: ImagePlus): Unit = {
          if (image.contains(imp)) {
            handleImageClosed()
          }
        }

        def imageOpened(imp: ImagePlus): Unit = {}
      }
    )
  }

  private def handleImageUpdated(): Unit = onFX {
    // Update image title
    model.foreach { m =>
      m.imageTitle() = image.getOrElse(new ImagePlus("<No Image>")).getTitle
      m.resetROI()
    }
  }

  private def handleImageClosed(): Unit = onFX {
    model.foreach(_.resetROI())
    dialogStage.foreach(_.hide())
  }
}
