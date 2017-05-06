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

package net.sf.ij_plugins.color.converter.ui

import ij.plugin.PlugIn
import net.sf.ij_plugins.color.ColorFXUI
import net.sf.ij_plugins.fx._
import net.sf.ij_plugins.util.IJTools
import org.scalafx.extras.onFX

import scalafx.Includes._
import scalafx.scene.Scene
import scalafx.stage.Stage

object ColorConverterPlugin {
  private var dialogStage: Option[Stage] = None
}

/** Simple color conversion calculator. */
class ColorConverterPlugin extends PlugIn {

  private final val Title = "IJP Color Calculator"


  def run(arg: String): Unit = {

    initializeFX()
    onFX {
      if (ColorConverterPlugin.dialogStage.isDefined) {
        ColorConverterPlugin.dialogStage.foreach(_.show())
      } else {
        ColorConverterPlugin.dialogStage = Some(
          new Stage {
            title = Title
            scene = new Scene {
              title = Title
              icons += IJTools.imageJIconAsFXImage
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
