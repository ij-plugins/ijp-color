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
import scalafx.stage.{FileChooser, Window}

import java.io.File

class FileSelectionField(val title: String, val ownerWindow: Option[Window]) {
  private lazy val fileChooser: FileChooser = new FileChooser() {
    this.title = FileSelectionField.this.title
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
          val file = new File(initialPath)
          fileChooser.initialFileName = file.getName
          if (file.getParentFile.exists()) {
            fileChooser.initialDirectory = file.getParentFile
          }
        } else {
          val parent = new File(OpenDialog.getDefaultDirectory)
          if (parent.exists())
            fileChooser.initialDirectory = parent
        }

        val selection = fileChooser.showOpenDialog(ownerWindow.orNull)

        Option(selection).foreach { s =>
          path.value = s.getCanonicalPath
          OpenDialog.setLastDirectory(s.getParent)
          OpenDialog.setDefaultDirectory(s.getParent)
        }
      }
    }

    new HBox(3, textField, button)
  }

}
