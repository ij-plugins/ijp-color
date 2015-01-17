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

package net.sf.ij_plugins.color.calibration.ui

import ij.ImagePlus.{COLOR_RGB, GRAY8, GRAY16, GRAY32}
import ij.plugin.PlugIn
import ij.{ImageListener, ImagePlus, IJ}
import javafx.embed.swing.JFXPanel
import net.sf.ij_plugins.color.ColorFXUI
import net.sf.ij_plugins.util.IJTools
import scalafx.Includes._
import scalafx.application.Platform
import scalafx.scene.Scene
import scalafx.stage.Stage

/** ImageJ plugin for running image color calibration. */
class ColorCalibratorPlugin extends PlugIn {

  private final val Title = "Color Calibrator"
  private var image: Option[ImagePlus] = None
  private var model: Option[ColorCalibratorModel] = None
  private var dialogStage: Option[Stage] = None


  def run(arg: String) {

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

    // Create JFXPanel to force initialization of JavaFX.
    new JFXPanel()

    Platform.runLater {
      // Create dialog
      dialogStage = Some(
        new Stage {
          title = Title
          icons += IJTools.imageJIconAsFXImage
        }
      )

      model = Some(new ColorCalibratorModel(image.get, dialogStage.get))
      val mainView = new ColorCalibratorView(model.get)
      dialogStage.get.scene = new Scene {
        stylesheets ++= ColorFXUI.stylesheets
        root = mainView
      }
      dialogStage.get.show()
    }
  }

  def setupImageListener() {
    ImagePlus.addImageListener(new ImageListener {
      def imageUpdated(imp: ImagePlus) {
        if (image.exists(_ == imp)) {handleImageUpdated()}
      }

      def imageClosed(imp: ImagePlus) {
        if (image.exists(_ == imp)) {handleImageClosed()}
      }

      def imageOpened(imp: ImagePlus) {}
    })
  }

  def handleImageUpdated() {
    // Update image title
    Platform.runLater {
      model.foreach(m => {
        m.imageTitle() = image.getOrElse(new ImagePlus("<No Image>")).getTitle
        m.resetROI()
      }
      )
    }
  }

  def handleImageClosed() {
    Platform.runLater {
      model.foreach(_.resetROI())
      dialogStage.foreach(_.hide())
    }
  }
}
