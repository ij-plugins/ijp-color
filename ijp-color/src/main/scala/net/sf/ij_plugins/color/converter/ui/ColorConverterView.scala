/*
 * Image/J Plugins
 * Copyright (C) 2002-2015 Jarek Sacha
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

import javafx.beans.{binding => jfxbb}

import net.sf.ij_plugins.color.converter._
import net.sf.ij_plugins.color.converter.ui.ColorConverterModel.Update

import scalafx.Includes._
import scalafx.beans.property.ObjectProperty
import scalafx.collections.ObservableBuffer
import scalafx.event.ActionEvent
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Node
import scalafx.scene.control.{Button, ChoiceBox, Label}
import scalafx.scene.layout.GridPane
import scalafx.util.StringConverter


class ColorConverterView(val model: ColorConverterModel) {

  private val nbColumns = 5
  private val xyzUI     = new ColorValueUI()
  private val labUI     = new ColorValueUI()
  private val rgbUI     = new ColorValueUI()

  lazy val pane = createGidPane()

  private val buttonXYZ = new Button {
    id = "ijp-button"
    text = "XYZ"
    maxWidth = Double.MaxValue
    onAction = (e: ActionEvent) => {
      val colors = model.updateFromXYZ(xyzUI.color())
      update(colors)
    }
  }

  private val buttonLab = new Button {
    id = "ijp-button"
    text = "L*a*b*"
    maxWidth = Double.MaxValue
    onAction = (e: ActionEvent) => {
      val colors = model.updateFromLab(labUI.color())
      update(colors)
    }
  }

  private val buttonRGB = new Button {
    id = "ijp-button"
    text = "RGB"
    maxWidth = Double.MaxValue
    onAction = (e: ActionEvent) => {
      val colors = model.updateFromRGB(rgbUI.color())
      update(colors)
    }
  }

  private def createGidPane(): GridPane = {
    val gp = new GridPane() {
      hgap = 10
      vgap = 5
      padding = Insets(10)
      // gridLinesVisible = true
    }

    def addColorUI(row: Int, n1: Node, n2: Node, xLabel: Option[String] = None) {
      gp.add(n1, 0, row, 1, 1)
      if (None == xLabel) {
        gp.add(n2, 1, row, nbColumns - 1, 1)
      } else {
        gp.add(n2, 1, row, nbColumns - 2, 1)
        gp.add(new Label {
          id = "ijp-label"
          text = xLabel.get
        }, nbColumns - 1, row, 1, 1)
      }
    }

    def addChoiceBox[T <: AnyRef](row: Int, label: String, property: ObjectProperty[T], values: List[T],
                                  converter: Option[StringConverter[T]] = None) {
      val v = ObservableBuffer(values)
      val cb = new ChoiceBox[T] {
        value = property()
        items = v
      }
      converter.map(cb.converter = _)
      gp.add(
        new Label {
          id = "ijp-label"
          text = label
          alignment = Pos.CenterRight
          alignmentInParent = Pos.CenterRight
        },
        0, row
      )
      gp.add(cb, 1, row)

      property <== cb.value
    }

    var row = 1
    addColorUI(row, buttonXYZ, xyzUI.control, Some("[0-100]"))
    row += 1

    addColorUI(row, buttonLab, labUI.control)
    row += 1

    addColorUI(row, buttonRGB, rgbUI.control, Some("[0-255]"))
    row += 1

    addChoiceBox(row, "Ref. White", model.referenceWhite, ReferenceWhite.values)
    row += 1

    addChoiceBox(row, "RGB Model", model.rgbWorkingSpace, RGBWorkingSpace.values)
    gp.add(new Label {
      id = "ijp-label"
      text = "Gamma"
      alignment = Pos.CenterRight
    }, 2, row)
    gp.add(
      new Label {
        id = "ijp-label"
        text <== new jfxbb.StringBinding {
          super.bind(model.rgbWorkingSpace)

          protected def computeValue(): String = model.rgbWorkingSpace().gamma match {
            case g if (g > 0) => model.rgbWorkingSpace().gamma.toString
            case g if (g < 0) => "sRGB"
            case _ => "L*"
          }
        }
        alignment = Pos.CenterLeft
      },
      3, row
    )
    row += 1

    addChoiceBox(row, "Adaptation", model.chromaticAdaptation,
      None :: ChromaticAdaptation.values.map(Some(_)).toList,
      Some(
        StringConverter.toStringConverter[Option[ChromaticAdaptation]] {
          case Some(o) => o.toString
          case None    => "None"
        }
      )
    )
    row += 1

    gp
  }

  private def update(colors: Update) {
    labUI.color() = colors.lab
    xyzUI.color() = colors.xyz
    rgbUI.color() = colors.rgb
  }
}
