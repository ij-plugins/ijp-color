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

package ij_plugins.color.ui.fx

import scalafx.application.Platform
import scalafx.scene.layout.StackPane
import scalafx.scene.{Node, Scene}
import scalafx.stage.Stage

import java.net.URL

/** Helper methods for creation of UI in `ij_plugins.color` module. */
object ColorFXUI {

  /**
   * Return string representing URL to stylesheet used by `ColorFXUI` user interface.
   *
   * Returns empty string if stylesheet is not present.
   */
  def stylesheets: Seq[String] = List(
    "ijp-color.css"
  ).flatMap(check(_).map(_.toExternalForm))

  /**
   * Show `node` in a new window.
   *
   * @param node
   *   node to show.
   * @param windowTitle
   *   window title.
   */
  def showInNewWindow(node: Node, windowTitle: String): Unit = {
    Platform.runLater {
      val dialogStage = new Stage() {
        title = windowTitle
        imageJIconAsFXImage.foreach(icons += _)
        scene = new Scene {
          root = new StackPane {
            children = node
            this.stylesheets ++= ColorFXUI.stylesheets
          }
        }
      }
      dialogStage.show()
    }
  }

  private def check(name: String): Option[URL] = {
    val stylesheetURL = getClass.getResource(name)
    Option(stylesheetURL)
  }
}
