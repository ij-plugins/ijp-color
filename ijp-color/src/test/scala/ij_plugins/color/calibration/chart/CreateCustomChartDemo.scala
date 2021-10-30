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

package ij_plugins.color.calibration.chart

import ij_plugins.color.converter.ReferenceWhite

import java.io.File

object CreateCustomChartDemo {

  def main(args: Array[String]): Unit = {
    val colorChart = createCustomChart()
    println(colorChart)
  }

  def createCustomChart(): GridColorChart = {
    val chartName = "Custom Color Gauge"
    val nbColumns = 6
    val nbRows = 5
    val chipsList = ColorCharts.loadReferenceValues(new File("../test/data/Color_Gauge_Chart_3.csv"))
    val chipMargin = 0
    val referenceWhite = ReferenceWhite.D50

    new GridColorChart(chartName, nbColumns, nbRows, chipsList, chipMargin, referenceWhite)
  }
}
