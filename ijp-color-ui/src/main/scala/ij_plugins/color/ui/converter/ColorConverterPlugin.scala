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

package ij_plugins.color.ui.converter

import ij.IJ
import ij.plugin.PlugIn
import ij_plugins.color.ui.fx.{ColorFXUI, imageJIconAsFXImage, initializeFX}
import org.scalafx.extras.onFX
import scalafx.Includes.*
import scalafx.scene.Scene
import scalafx.stage.Stage

import scala.util.control.NonFatal

object ColorConverterPlugin {
  private var dialogStage: Option[Stage] = None
}

/** Simple color conversion calculator. */
class ColorConverterPlugin extends PlugIn {

  private final val Title = "IJP Color Calculator"

  def run(arg: String): Unit = {

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
      if (ColorConverterPlugin.dialogStage.isDefined) {
        ColorConverterPlugin.dialogStage.foreach(_.show())
      } else {
        ColorConverterPlugin.dialogStage = Some(
          new Stage {
            title = Title
            scene = new Scene {
              title = Title
              imageJIconAsFXImage.foreach(icons += _)
              root = new ColorConverterView(new ColorConverterModel()).pane
              stylesheets ++= ColorFXUI.stylesheets
            }
          }
        )
        ColorConverterPlugin.dialogStage.foreach(_.show())
      }
    }
  }
}
