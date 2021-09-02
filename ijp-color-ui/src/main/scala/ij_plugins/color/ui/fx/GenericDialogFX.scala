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

import org.scalafx.extras.{initFX, onFXAndWait}
import scalafx.Includes._
import scalafx.application.Platform
import scalafx.beans.property.StringProperty
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Insets
import scalafx.scene.Node
import scalafx.scene.control._
import scalafx.scene.layout.{ColumnConstraints, GridPane, Priority}
import scalafx.scene.text.Font
import scalafx.stage.Window

import scala.collection.mutable.ListBuffer

object GenericDialogFX {
  // Take a chance to initialize JavaFX Toolkit
  initFX()

  case class Result(checkboxes: Seq[Boolean])
}

/**
  * A helper for crating input dialogs. It emulates basic behaviour of ImageJ's GenericDialog.
  *
  * {{{
  *    val dialog =
  *      new GenericDialogFX(
  *        title = "GenericDialogFX Demo",
  *        header = Option("An attempt to emulate ImageJ's GenericDialog.")
  *      ) {
  *        addCheckbox("Check me out!", defaultValue = false)
  *        addCheckbox("Check me too!", defaultValue = true)
  *      }
  *
  *    dialog.showDialog()
  *
  *    if (dialog.wasOKed) {
  *      val select1 = dialog.getNextBoolean()
  *      val select2 = dialog.getNextBoolean()
  *
  *      IJ.log(s"Selection 1: $select1")
  *      IJ.log(s"Selection 2: $select2")
  *    }
  * }}}
  */
class GenericDialogFX(
                       val title: String,
                       val header: Option[String] = None,
                       val parentWindowOption: Option[Window] = None
                     ) {

  import GenericDialogFX._

  private var _wasOKed = false
  private var _rowIndex = 0
  private val _labeledControls = ListBuffer.empty[(String, Node)]
  private val _checkBoxes = ListBuffer.empty[CheckBox]
  private var _checkBoxNextIndex = 0
  private val _choiceBoxes = ListBuffer.empty[ChoiceBox[String]]
  private var _choiceBoxNextIndex = 0
  private val _numberTextFields = ListBuffer.empty[NumberTextField]
  private var _numberTextFieldNextIndex = 0
  private val _stringProperties = ListBuffer.empty[StringProperty]
  private var _stringPropertyNextIndex = 0

  private val _grid: GridPane = new GridPane() {
    hgap = 5
    vgap = 5
    padding = Insets(10, 10, 10, 10)
    maxWidth = Double.MaxValue
    hgrow = Priority.Always
    val constrains = Seq(
      new ColumnConstraints(),
      new ColumnConstraints(),
      new ColumnConstraints(),
      new ColumnConstraints() {
        hgrow = Priority.Always
      }
    )
    columnConstraints.addAll(constrains.map(_.delegate): _*)
  }

  /**
    * Adds a checkbox.
    *
    * @param label        the label
    * @param defaultValue the initial state
    */
  def addCheckbox(label: String, defaultValue: Boolean): Unit = {
    val label2 = label.replace('_', ' ')

    val checkBox = new CheckBox()
    checkBox.selected = defaultValue

    _grid.add(new Label(label2), 0, _rowIndex)
    _grid.add(checkBox, 1, _rowIndex)
    _rowIndex += 1

    _labeledControls.append((label, checkBox))
    _checkBoxes += checkBox
  }

  def addChoice(label: String, items: Array[String], defaultItem: String): Unit = {

    require(items.contains(defaultItem))

    val label2 = label.replace('_', ' ')

    val choiceBox = new ChoiceBox[String](ObservableBuffer.from(items))
    choiceBox.selectionModel.value.select(defaultItem)

    _grid.add(new Label(label2), 0, _rowIndex)
    _grid.add(choiceBox, 1, _rowIndex)
    _rowIndex += 1

    _labeledControls.append((label, choiceBox))
    _choiceBoxes += choiceBox

  }

  def addFileField(label: String, defaultPath: String = ""): Unit = {
    val label2 = label.replace('_', ' ')

    val fileSelectionField = new FileSelectionField(label2, parentWindowOption)
    fileSelectionField.path.value = defaultPath

    _grid.add(new Label(label2), 0, _rowIndex)
    _grid.add(fileSelectionField.view, 1, _rowIndex, GridPane.Remaining, 1)
    _rowIndex += 1

    _labeledControls.append((label, fileSelectionField.view))
    _stringProperties += fileSelectionField.path
  }

  /**
    * Adds a message consisting of one or more lines of text.
    */
  def addMessage(message: String, font: Option[Font] = None): Unit = {
    val label = Label(message)
    font.foreach(label.font = _)

    _grid.add(label, 0, _rowIndex, GridPane.Remaining, 1)
    _rowIndex += 1
  }

  def addMessage(message: String, font: Font): Unit = {
    addMessage(message, Option(font))
  }

  def addNumericField(label: String, defaultValue: Double): Unit = {
    val decimalPlaces = if (defaultValue.toInt == defaultValue) 0 else 3
    val columnWidth = if (decimalPlaces == 3) 8 else 6
    addNumericField(label, defaultValue, decimalPlaces, columnWidth, "")
  }

  def addNumericField(
                       label: String,
                       defaultValue: Double,
                       decimalPlaces: Int,
                       columnWidth: Int,
                       units: String
                     ): Unit = {
    require(columnWidth > 0)

    val label2 = label.replace('_', ' ')
    val textField = new NumberTextField(decimalPlaces) {
      prefColumnCount = columnWidth
      model.value = defaultValue
    }

    _grid.add(new Label(label2), 0, _rowIndex)
    _grid.add(textField, 1, _rowIndex)
    _grid.add(new Label(units), 2, _rowIndex)
    _rowIndex += 1

    _labeledControls.append((label, textField))
    _numberTextFields += textField
  }

  /**
    * Returns the state of the next checkbox.
    */
  //noinspection AccessorLikeMethodIsEmptyParen
  def getNextBoolean(): Boolean = {
    require(_checkBoxNextIndex < _checkBoxes.size)

    val next = _checkBoxes(_checkBoxNextIndex).selected.value
    _checkBoxNextIndex += 1

    next
  }

  //noinspection AccessorLikeMethodIsEmptyParen
  def getNextChoice(): String = {
    require(_choiceBoxNextIndex < _choiceBoxes.size)

    val next = _choiceBoxes(_choiceBoxNextIndex).selectionModel.value.selectedItem.value
    _choiceBoxNextIndex += 1

    next
  }

  //noinspection AccessorLikeMethodIsEmptyParen
  def getNextNumber(): Double = {
    require(_numberTextFieldNextIndex < _numberTextFields.size)

    val next = _numberTextFields(_numberTextFieldNextIndex).model.value.value
    _numberTextFieldNextIndex += 1

    next.doubleValue()
  }

  //noinspection AccessorLikeMethodIsEmptyParen
  def getNextString(): String = {
    require(_stringPropertyNextIndex < _stringProperties.size)

    val next = _stringProperties(_stringPropertyNextIndex).value
    _stringPropertyNextIndex += 1

    next
  }

  def showDialog(): Unit = {

    onFXAndWait {

      // Create the custom dialog.
      val dialog = new Dialog[Result]() {
        parentWindowOption.foreach(initOwner)
        title = GenericDialogFX.this.title
        header.foreach(headerText = _)
        resizable = true
      }

      dialog.dialogPane().buttonTypes = Seq(ButtonType.OK, ButtonType.Cancel)

      // Place to add validation to enable OK button
      //    // Enable/Disable login button depending on whether a username was entered.
      //    val loginButton = dialog.dialogPane().lookupButton(loginButtonType)
      //    loginButton.disable = true

      //    // Do some validation (disable when username is empty).
      //    username.text.onChange { (_, _, newValue) =>
      //      loginButton.disable = newValue.trim().isEmpty
      //    }

      dialog.dialogPane().content = _grid

      // Request focus on the username field by default.
      _labeledControls.headOption.foreach(l => Platform.runLater(l._2.requestFocus()))

      // When the login button is clicked, convert the result to a username-password-pair.
      dialog.resultConverter = dialogButton =>
        if (dialogButton == ButtonType.OK)
          Result(_checkBoxes.map(_.selected.value).toSeq)
        else
          null

      // We could use some more digested result
      val result = dialog.showAndWait()

      _wasOKed = result.isDefined
    }
  }

  def wasCanceled: Boolean = !_wasOKed

  def wasOKed: Boolean = _wasOKed
}
