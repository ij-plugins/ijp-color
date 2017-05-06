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

package net.sf.ij_plugins.util

import scalafx.scene.Node
import scalafx.scene.control.Label
import scalafx.scene.image.ImageView
import scalafx.scene.layout.GridPane

/**
  * Internal utilities.
  */
object IJPUtils {

  def createHeaderNode(title: String, message: String): Node = {
    // Create header with logo, title, and a brief description
    val headerGP = new GridPane {
      vgap = 7
      hgap = 7
    }

    val ijpLogoView = new ImageView("/net/sf/ij_plugins/color/IJP-48.png")
    headerGP.add(ijpLogoView, 0, 0)

    val pluginTitleLabel = new Label {
      text = title
      id = "ijp-header-title"
    }
    headerGP.add(pluginTitleLabel, 1, 0)

    val descriptionLabel = new Label {
      text = message
      id = "ijp-header-message"
      wrapText = true
    }
    headerGP.add(descriptionLabel, 0, 1, GridPane.Remaining, 1)

    headerGP
  }

}
