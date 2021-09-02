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

import ij.IJ
import ij_plugins.color.calibration.chart.{ColorCharts, GridColorChart}
import ij_plugins.color.converter.ReferenceWhite
import ij_plugins.color.ui.fx.GenericDialogFX
import org.scalafx.extras.BusyWorker.SimpleTask
import scalafx.stage.Window

import java.io.File
import scala.util.control.NonFatal

class EditCustomChartTask(customChartOption: Option[GridColorChart], parentWindow: Option[Window])
  extends SimpleTask[Option[GridColorChart]] {

  private val Title = "Edit Custom Reference Chart"

  override def call(): Option[GridColorChart] = {

    val _nbRows = customChartOption.map(_.nbRows).getOrElse(5)
    val _nbColumns = customChartOption.map(_.nbColumns).getOrElse(6)
    val _refWhite = customChartOption.map(_.refWhite).getOrElse(ReferenceWhite.D50)
    // TODO Save last path in preferences
    val _defaultPath = ""

    // TODO Implement using JavaFX to preserve correct window order when dialog opens,
    //  it should open in front of its parent dialog, but not be forced to be always on top of all other windows

    val gd =
      new GenericDialogFX(
        Title,
        Option("Define chart layout and select a CSV file with CIE L*a*b* reference colors."),
        parentWindow
      ) {
        addNumericField("Rows", _nbRows, 0, 3, "")
        addNumericField("Columns", _nbColumns, 0, 3, "")
        addChoice("Reference_White", ReferenceWhite.values.map(_.toString).toArray, _refWhite.toString)
        addFileField("Reference_values_file", _defaultPath)
      }

    gd.showDialog()

    if (gd.wasOKed) {
      try {
        val nbRows = {
          val v = gd.getNextNumber()
          math.max(1, math.round(v).toInt)
        }

        val nbCols = {
          val v = gd.getNextNumber()
          math.max(1, math.round(v).toInt)
        }

        val refWhite: ReferenceWhite = {
          val v = gd.getNextChoice()
          ReferenceWhite.withName(v)
        }

        val filePath = gd.getNextString()
        val file = new File(filePath)

        val chipsOpt =
          try {
            Option(ColorCharts.loadReferenceValues(file))
          } catch {
            case NonFatal(ex) =>
              IJ.error(Title, s"Error loading reference chart values. ${Option(ex.getMessage).getOrElse("")}")
              None
          }

        chipsOpt.map { chips =>
          new GridColorChart(
            s"Custom - ${file.getName}",
            nbColumns = nbCols,
            nbRows = nbRows,
            chips = chips,
            chipMargin = 0.2,
            refWhite = refWhite
          )
        }
      } catch {
        case NonFatal(ex) =>
          IJ.error(Title, s"Error when creating custom chart. ${Option(ex.getMessage).getOrElse("")}")
          None
      }
    } else
      None
  }
}
