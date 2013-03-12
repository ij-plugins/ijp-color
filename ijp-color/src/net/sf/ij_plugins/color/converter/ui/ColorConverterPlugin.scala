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

package net.sf.ij_plugins.color.converter.ui

import ij.plugin.PlugIn
import javafx.embed.swing.JFXPanel
import net.sf.ij_plugins.color.ColorFXUI
import net.sf.ij_plugins.util.IJTools
import scalafx.Includes._
import scalafx.application.Platform
import scalafx.scene.Scene
import scalafx.stage.Stage

object ColorConverterPlugin {
  private var dialogStage: Option[Stage] = None
}

/** Simple color convertion calculator. */
class ColorConverterPlugin extends PlugIn {

  private final val Title = "IJP Color Calculator"


  def run(arg: String) {

    // Create JFXPanel to force initialization of JavaFX.
    new JFXPanel()

    if (ColorConverterPlugin.dialogStage.isDefined) {
      Platform.runLater {ColorConverterPlugin.dialogStage.foreach(_.show)}
    } else {
      Platform.runLater {
        ColorConverterPlugin.dialogStage = Some(
          new Stage {
            title = Title
            scene = new Scene {
              title = Title
              icons.add(IJTools.imageJIconAsFXImage)
              root = new ColorConverterView(new ColorConverterModel()).pane
              stylesheets ++= ColorFXUI.stylesheets
            }
          }
        )
        ColorConverterPlugin.dialogStage.foreach(_.show)
      }
    }
  }
}
