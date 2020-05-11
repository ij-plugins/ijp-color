/*
 * Image/J Plugins
 * Copyright (C) 2002-2020 Jarek Sacha
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

package net.sf.ij_plugins.color.calibration

import java.io.File

import ij.measure.ResultsTable
import net.sf.ij_plugins.color.calibration.chart.{ColorCharts, ReferenceColorSpace}
import net.sf.ij_plugins.color.calibration.regression.MappingMethod
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._


class LOOCrossValidationSpec extends AnyFlatSpec {

  behavior of "LOOCrossValidation"

  it should "compute cross validation stats" in {

    // Read chart values
    val tableFile = new File("../test/data/Passport-linear-25_color_values.csv").getCanonicalFile
    assert(tableFile.exists(), "Input table file should exist: " + tableFile)
    val srcRT = ResultsTable.open(tableFile.getCanonicalPath)
    // Extract chart values as an array
    val observed = new Array[Array[Double]](srcRT.getCounter)
    for (i <- observed.indices) {
      observed(i) = new Array[Double](3)
      observed(i)(0) = srcRT.getValue("Observed X", i)
      observed(i)(1) = srcRT.getValue("Observed Y", i)
      observed(i)(2) = srcRT.getValue("Observed Z", i)
    }

    // Reference chart
    val chart = ColorCharts.XRitePassportColorChecker

    // Do LOO validation
    val referenceColorSpaces = ReferenceColorSpace.values
    val mappingMethods = MappingMethod.values
    val crossValidations = LOOCrossValidation.crossValidationStatsAll(chart, observed, referenceColorSpaces, mappingMethods)

    crossValidations should have length (referenceColorSpaces.length * mappingMethods.length)

    // Sort, best first
    val bestByMean = crossValidations.toArray.minBy(_.statsDeltaE.getMean)
    println(s"Best by mean: ${bestByMean.method} - ${bestByMean.referenceColorSpace}: ${bestByMean.statsDeltaE.getMean} ")
    bestByMean.referenceColorSpace should be(ReferenceColorSpace.XYZ)
    bestByMean.method should be(MappingMethod.QuadraticCrossBand)
    bestByMean.statsDeltaE.getMean should be(4.854 +- 0.1)
    bestByMean.statsDeltaE.getPercentile(50) should be(4.494 +- 0.1)
    bestByMean.statsDeltaE.getPercentile(95) should be(12.212 +- 0.1)
  }

}
