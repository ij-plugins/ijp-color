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

import ij.ImagePlus.{COLOR_RGB, GRAY16, GRAY32, GRAY8}
import ij.plugin.PlugIn
import ij.{IJ, ImagePlus}
import ij_plugins.color.ui.calibration.ColorCalibratorUIModel.Config
import ij_plugins.color.ui.fx.{ColorFXUI, imageJIconAsFXImage, initializeFX}
import ij_plugins.color.ui.util.{ImageListenerHelper, LiveChartROIHelper}
import org.scalafx.extras._
import scalafx.Includes._
import scalafx.scene.Scene
import scalafx.stage.Stage

import scala.util.control.NonFatal

object ColorCalibratorPlugin {
  private final val Title = "Color Calibrator"
}

/**
  * ImageJ plugin for running image color calibration.
  */
class ColorCalibratorPlugin
  extends PlugIn
    with ImageListenerHelper
    with LiveChartROIHelper {

  import ColorCalibratorPlugin._

  private var model: Option[ColorCalibratorUIModel] = None
  private var dialogStage: Option[Stage] = None

  def run(arg: String): Unit = {
    IJ.showStatus("Preparing UI for " + Title + "...")

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

    try {
      initializeFX()
    } catch {
      // ImageJ has a bug where is may show incorrect error message about missing plugin,
      // se wee need to handle this here
      case NonFatal(ex) =>
        IJ.error(Title, "Cannot initialize JavaFX: " + ex.getMessage)
        return
      case ex: ClassNotFoundException =>
        IJ.error(Title, "Cannot initialize JavaFX. ClassNotFoundException: " + ex.getMessage)
        return
      case ex: NoClassDefFoundError =>
        IJ.error(Title, "Cannot initialize JavaFX. NoClassDefFoundError: " + ex.getMessage)
        return
      case ex: Throwable =>
        IJ.error(Title, "Cannot initialize JavaFX: " + ex.getClass.getName + ": " + ex.getMessage)
        return
    }

    onFX {
      // Create dialog
      dialogStage = Some(
        new Stage {
          title = Title
          imageJIconAsFXImage.foreach(icons += _)
        }
      )

      val mvc = new ColorCalibratorUI(image.get, dialogStage.get)
      model = Option(mvc.model)
      val mainView = mvc.view
      dialogStage.get.scene = new Scene {
        stylesheets ++= ColorFXUI.stylesheets
        root = mainView
      }

      IJ.showStatus("")
      dialogStage.get.show()

      setupImageListener()
      setupROIListener(mvc.model.liveChartROI)

      // Load previous options, if available
      Config
        .loadFromIJPref()
        .foreach { config =>
          mvc.model.fromConfig(config)
        }

      dialogStage.get.onCloseRequest = () => {
        // Remove image listener
        removeImageListener()
        // Remove ROI listener
        removeROIListener()

        // Save current UI selections
        mvc.model.toConfig.saveToIJPref()
      }
    }
  }


  override protected def handleImageUpdated(): Unit = onFX {
    // Update image title
    model.foreach { m =>
      m.imageTitle() = image.getOrElse(new ImagePlus("<No Image>")).getTitle
      //      m.resetROI()
    }
  }

  override protected def handleImageClosed(): Unit = onFX {
    //    model.foreach(_.resetROI())
    dialogStage.foreach(_.hide())
  }

}
