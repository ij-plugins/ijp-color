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

package ij_plugins.color.ui.calibration.tasks

import ij_plugins.color.calibration.chart.GridColorChart
import ij_plugins.color.converter.ColorConverter
import ij_plugins.color.converter.ColorTriple.{Lab, RGB, XYZ}
import ij_plugins.color.ui.fx.GenericDialogFX
import org.scalafx.extras.BusyWorker.SimpleTask
import org.scalafx.extras.onFXAndWait
import scalafx.beans.property.{BooleanProperty, ObjectProperty, StringProperty}
import scalafx.beans.value.ObservableValue
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.cell.CheckBoxTableCell
// Explicit import of sfxTableColumn2jfx is to cover implicit resolution limitation in Scala 2.12
import scalafx.scene.control.TableColumn.sfxTableColumn2jfx
import scalafx.scene.control.{TableColumn, TableView}
import scalafx.scene.paint.Color
import scalafx.scene.shape.Rectangle
import scalafx.stage.Window

object SelectEnabledChipsTask {

  class ColorChipData(
                       val index: Int,
                       val name: String,
                       val colorLab: Lab,
                       val enabled0: Boolean,
                       colorConverter: ColorConverter
                     ) {
    val enabled = new BooleanProperty(this, "selected", enabled0)
    val colorRGB: RGB = colorConverter.copyWith(rgbScale = 1).toRGB(colorLab)
    val colorXYZ: XYZ = colorConverter.copyWith(xyzScale = 100).toXYZ(colorLab)
  }

  def createTableView(
                       data: ObservableBuffer[ColorChipData]
                     ): TableView[ColorChipData] = {

    def bandColumn(
                    header: String,
                    f: ColorChipData => Double
                  ): TableColumn[ColorChipData, Number] =
      new TableColumn[ColorChipData, Number] {
        text = header
        cellValueFactory =
          (v: TableColumn.CellDataFeatures[ColorChipData, Number]) => new ObjectProperty[Number](this, "", f(v.value))
        cellFactory = (cell, value) => {
          cell.text = f"${value.doubleValue()}%8.3f"
          cell.setStyle("-fx-alignment: CENTER-RIGHT;")
        }
      }

    new TableView[ColorChipData](data) {
      editable = true
      columns ++= Seq(
        new TableColumn[ColorChipData, Number] {
          cellValueFactory =
            (v: TableColumn.CellDataFeatures[ColorChipData, Number]) => ObjectProperty[Number](v.value.index)
          cellFactory = (cell, value) => {
            cell.text = s"${value.intValue()}"
            cell.setStyle("-fx-alignment: CENTER-RIGHT;")
          }
        },
        new TableColumn[ColorChipData, java.lang.Boolean] {
          text = "Enabled"
          cellValueFactory = _.value.enabled.asInstanceOf[ObservableValue[java.lang.Boolean, java.lang.Boolean]]
          cellFactory = CheckBoxTableCell.forTableColumn[ColorChipData](this)
          editable = true
        },
        new TableColumn[ColorChipData, String] {
          text = "Name"
          cellValueFactory = v => StringProperty(v.value.name)
          delegate.setStyle("-fx-alignment: CENTER;")
        },
        new TableColumn[ColorChipData, RGB] {
          text = "Color"
          cellValueFactory = v => ObjectProperty(v.value.colorRGB)
          cellFactory = (tc, rgb) => {
            val rgbColor: Color = {
              Color.color(red = clip01(rgb.r), green = clip01(rgb.g), blue = clip01(rgb.b))
            }
            tc.graphic = new Rectangle {
              fill = rgbColor
              width = 16
              height = 16
            }
            tc.setStyle("-fx-alignment: CENTER;")
          }
        },
        bandColumn("sRGB R", _.colorRGB.r * 255),
        bandColumn("sRGB G", _.colorRGB.g * 255),
        bandColumn("sRGB B", _.colorRGB.b * 255),
        bandColumn("CIE L*", _.colorLab.l),
        bandColumn("CIE a*", _.colorLab.a),
        bandColumn("CIE b*", _.colorLab.b),
        bandColumn("CIE X", _.colorXYZ.x),
        bandColumn("CIE Y", _.colorXYZ.y),
        bandColumn("CIE Z*", _.colorXYZ.z)
      )
    }
  }

  private def clip01(v: Double): Double = math.max(0, math.min(1, v))
}

class SelectEnabledChipsTask(chart: GridColorChart, parentWindow: Option[Window])
  extends SimpleTask[Option[GridColorChart]] {

  import SelectEnabledChipsTask._

  private val Title = "Select Enabled Chips"

  override def call(): Option[GridColorChart] = {

    val chips =
      chart
        .referenceChips
        .zip(chart.enabled)
        .zipWithIndex
        .map { case ((chip, enabled), i) =>
          new ColorChipData(i, chip.name, chip.color, enabled, chart.colorConverter)
        }

    val data: ObservableBuffer[ColorChipData] = ObservableBuffer.from[ColorChipData](chips)

    onFXAndWait {

      val dialog =
        new GenericDialogFX(
          title = Title,
          header = Option("An attempt to emulate ImageJ's GenericDialog."),
          parentWindowOption = parentWindow
        ) {
          addNode(createTableView(data))
          addHelp("https://github.com/ij-plugins/ijp-color/wiki/Select-Enabled-Chips")
        }

      dialog.showDialog()

      if (dialog.wasOKed) {
        val enabled: Array[Boolean] = data.sortBy(_.index).map(_.enabled.value).toArray
        val c = chart.copyWithEnabled(enabled)
        Option(c)
      } else {
        None
      }
    }
  }
}
