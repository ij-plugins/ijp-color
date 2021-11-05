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

import ij.io.OpenDialog
import scalafx.application.Platform
import scalafx.beans.property.StringProperty
import scalafx.scene.Node
import scalafx.scene.control.{Button, TextField}
import scalafx.scene.layout.{HBox, Priority}
import scalafx.stage.{DirectoryChooser, Window}

import java.io.File
import scala.annotation.tailrec

object DirectorySelectionField {

  /**
    * Find existing part of the input file path.
    * If the input file exists return that file otherwise look for first existing parent
    */
  @tailrec
  def existingOrParent(file: File): File =
    if (file.exists()) file
    else existingOrParent(file.getCanonicalFile.getParentFile)
}

/**
  * Directory selection control, accessible through `view`.
  * The text field shows the path, the button allow browsing to select the directory.
  */
class DirectorySelectionField(val title: String, val ownerWindow: Option[Window]) {

  import DirectorySelectionField._

  private lazy val chooser: DirectoryChooser = new DirectoryChooser() {
    title = DirectorySelectionField.this.title
  }

  private var _view: Option[Node] = None
  val path: StringProperty = new StringProperty("")

  def view: Node = {
    if (_view.isEmpty) {
      _view = Option(buildView())
    }
    _view.get
  }

  private def buildView(): Node = {

    val textField = new TextField() {
      hgrow = Priority.Always
      maxWidth = Double.MaxValue
      text <==> path
    }

    // Make sure that end of the file name is visible
    textField.text.onChange { (_, _, _) =>
      val location = textField.text.length.get()
      Platform.runLater {
        textField.positionCaret(location)
      }
    }

    val button = new Button("Browse") {
      onAction = _ => {
        val initialPath = path.value
        if (initialPath.trim.nonEmpty) {
          chooser.initialDirectory = existingOrParent(new File(initialPath))
        } else {
          val file = new File(OpenDialog.getDefaultDirectory)
          chooser.initialDirectory = file
        }

        val selection = chooser.showDialog(ownerWindow.orNull)

        Option(selection).foreach { s =>
          path.value = s.getCanonicalPath
          OpenDialog.setLastDirectory(s.getCanonicalPath)
          OpenDialog.setDefaultDirectory(s.getCanonicalPath)
        }
      }
    }

    new HBox(3, textField, button)
  }

}
