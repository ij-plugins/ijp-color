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

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.*

import java.io.File

class ColorChartsTest extends AnyFlatSpec {

  behavior of "ColorCharts"

  it should "correspond to unique names" in {

    val chartNames = ColorCharts.values.map(_.name)

    chartNames.distinct should contain theSameElementsAs chartNames
  }

  it should "have value for each ColorChartType" in {

    ColorChartType
      .values
      .filter(c => c != ColorChartType.Custom)
      .forall(t => ColorCharts.withColorChartType(t).isDefined) should be(true)
  }

  it should "read reference values from a file" in {
    val srcFile = new File("../test/data/Color_Gauge_Chart_3.csv")
    assert(srcFile.exists())

    val chipsList = ColorCharts.loadReferenceValues(srcFile)

    chipsList.size should be(30)

  }

}
