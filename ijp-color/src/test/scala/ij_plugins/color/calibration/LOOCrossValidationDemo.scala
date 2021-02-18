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

package ij_plugins.color.calibration

import ij.measure.ResultsTable
import ij_plugins.color.calibration.chart.{ColorCharts, ReferenceColorSpace}
import ij_plugins.color.calibration.regression.MappingMethod

import java.io.File

object LOOCrossValidationDemo extends App {

  main()

  def main(): Unit = {
    // Read chart values
    val tableFile = "../test/data/Passport-linear-25_color_values.csv"
    val srcRT = ResultsTable.open(tableFile)

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
    val crossValidations = LOOCrossValidation.crossValidationStatsAll(chart, observed, ReferenceColorSpace.values, MappingMethod.values)

    val bestByMean = crossValidations.minBy(v => v.statsDeltaE.getMean)
    println(s"Best by mean: ${bestByMean.method} - ${bestByMean.referenceColorSpace}: ${bestByMean.statsDeltaE.getMean} ")

    val bestByMedian = crossValidations.minBy(v => v.statsDeltaE.getPercentile(50))
    println(s"Best by median: ${bestByMedian.method} - ${bestByMedian.referenceColorSpace}: ${bestByMedian.statsDeltaE.getPercentile(50)} ")

    val bestBy95 = crossValidations.minBy(v => v.statsDeltaE.getPercentile(95))
    println(s"Best by 95%: ${bestBy95.method} - ${bestBy95.referenceColorSpace}: ${bestBy95.statsDeltaE.getPercentile(95)} ")


    // Convert to a table for saving
    val hSorted = crossValidations.toArray.sortBy(-_.statsDeltaE.getMean)
    val dstRT = new ResultsTable()
    for ((v, i) <- hSorted.reverse.zipWithIndex) {
      dstRT.setValue("Reference", i, v.referenceColorSpace.toString)
      dstRT.setValue("Method", i, v.method.toString)
      dstRT.setValue("Mean DeltaE", i, v.statsDeltaE.getMean)
      dstRT.setValue("Min DeltaE", i, v.statsDeltaE.getMin)
      dstRT.setValue("Max DeltaE", i, v.statsDeltaE.getMax)
      dstRT.setValue("Median DeltaE", i, v.statsDeltaE.getPercentile(50))
      dstRT.setValue("95% DeltaE", i, v.statsDeltaE.getPercentile(95))
      dstRT.setValue("StandardDeviation DeltaE", i, v.statsDeltaE.getStandardDeviation)
    }

    val dstFile = new File("../tmp/LOOCrossValidationDemo_Poly_.csv").getCanonicalFile
    println(dstFile)
    assert(dstFile.getParentFile.exists(), dstFile.getParent)
    dstRT.saveAs("../tmp/LOOCrossValidationDemo_Poly_.csv")
  }
}
