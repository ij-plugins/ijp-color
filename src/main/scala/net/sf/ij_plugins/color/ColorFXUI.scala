/*
 * Image/J Plugins
 * Copyright (C) 2002-2014 Jarek Sacha
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

package net.sf.ij_plugins.color

import java.net.URL

import scalafx.Includes._
import scalafx.scene.layout.StackPane
import scalafx.scene.{Node, Scene}
import scalafx.stage.Stage

/** Helper methods for creation of UI in `net.sf.ij_plugins.color` module. */
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
   * @param node node to show.
   * @param windowTitle window title.
   */
  def showInNewWindow(node: Node, windowTitle: String) {
    val dialogStage = new Stage() {
      title = windowTitle
      scene = new Scene {
        root = new StackPane {
          children = node
          stylesheets ++= ColorFXUI.stylesheets
        }
      }
    }
    dialogStage.show()
  }

  private def check(name: String): Option[URL] = {
    val stylesheetURL = getClass.getResource(name)
    if (stylesheetURL != null)
      Some(stylesheetURL)
    else {
      println("Cannot load stylesheet: '" + name + "' relative  to class: " + getClass + "")
      None
    }
  }
}
