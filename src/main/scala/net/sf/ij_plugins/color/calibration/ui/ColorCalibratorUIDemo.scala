/*
 * Image/J Plugins
 * Copyright (C) 2002-2016 Jarek Sacha
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

import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.layout.BorderPane

/**
  * @author Jarek Sacha 
  */
object ColorCalibratorUIDemo extends JFXApp {

  val editableList = new ColorCalibratorUI(null, null) {
//    model.caption() = "Subject"
//    model.items ++= Seq(1, 2, 3)
  }

  stage = new PrimaryStage {
    scene = new Scene(250, 300) {
      title = "Editable List Demo"
      root = new BorderPane {
        padding = Insets(7)
        center = editableList.view
      }
      onCloseRequest = handle {
        println("Closing application")
      }
    }
  }

  editableList.parent = stage
//  editableList.model.items.onChange((_, _) =>
//    println(ConfigurationUtils.asString(editableList.model.toConfiguration))
//  )

}
