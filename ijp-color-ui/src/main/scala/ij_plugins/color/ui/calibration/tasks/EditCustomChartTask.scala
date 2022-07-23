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
import ij_plugins.color.ui.fx.GenericDialogFXIJ
import ij_plugins.color.ui.util.IJPrefs
import org.scalafx.extras.BusyWorker.SimpleTask
import scalafx.stage.Window

import java.io.File
import scala.util.control.NonFatal

class EditCustomChartTask(customChartOption: Option[GridColorChart], parentWindow: Option[Window])
    extends SimpleTask[Option[GridColorChart]] {

  private val Title               = "Edit Custom Reference Chart"
  private val ReferencePrefix     = classOf[EditCustomChartTask].getName
  private val defaultPathPrefName = ReferencePrefix + ".defaultPath"

  override def call(): Option[GridColorChart] = {

    val _nbRows    = customChartOption.map(_.nbRows).getOrElse(5)
    val _nbColumns = customChartOption.map(_.nbColumns).getOrElse(6)
    val _refWhite  = customChartOption.map(_.refWhite).getOrElse(ReferenceWhite.D50)
    // Load last path from ImageJ preferences
    val _defaultPath = IJPrefs.getStringOption(defaultPathPrefName).getOrElse("")

    // Implement using JavaFX to preserve correct window order when dialog opens
    val gd =
      new GenericDialogFXIJ(
        Title,
        "Define chart layout and select a CSV file with CIE L*a*b* reference colors.",
        parentWindow
      ) {
        addNumericField("Rows", _nbRows, 0, 3, "")
        addNumericField("Columns", _nbColumns, 0, 3, "")
        addChoice("Reference_White", ReferenceWhite.values.map(_.toString).toArray, _refWhite.toString)
        addFileField("Reference_values_file", _defaultPath)
        addHelp("https://github.com/ij-plugins/ijp-color/wiki/Custom-Color-Chart")
      }

    gd.showDialog()

    if (gd.wasOKed) {
      try {
        val nbRows = {
          val v = gd.nextNumber()
          math.max(1, math.round(v).toInt)
        }

        val nbCols = {
          val v = gd.nextNumber()
          math.max(1, math.round(v).toInt)
        }

        val refWhite: ReferenceWhite = {
          val v = gd.nextChoice()
          ReferenceWhite.withName(v)
        }

        val filePath = gd.nextString()
        val file = new File(filePath)

        val chipsOpt =
          try {
            Option(ColorCharts.loadReferenceValues(file))
          } catch {
            case NonFatal(ex) =>
              IJ.error(Title, s"Error loading reference chart values. ${Option(ex.getMessage).getOrElse("")}")
              None
          }

        val chart = chipsOpt.map { chips =>
          new GridColorChart(
            s"Custom - ${file.getName}",
            nbColumns = nbCols,
            nbRows = nbRows,
            chips = chips,
            chipMargin = 0.2,
            refWhite = refWhite
          )
        }

        // Save defaultPath one we know that it can be used to create a chart
        IJPrefs.set(defaultPathPrefName, file.getPath)

        chart
      } catch {
        case NonFatal(ex) =>
          IJ.error(Title, s"Error when creating custom chart. ${Option(ex.getMessage).getOrElse("")}")
          None
      }
    } else
      None
  }
}
