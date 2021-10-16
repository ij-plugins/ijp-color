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
import ij_plugins.color.calibration.chart.ColorCharts
import ij_plugins.color.calibration.chart.ReferenceColorSpace.XYZ
import ij_plugins.color.calibration.regression.MappingMethod

object PolynomialCorrectorDemo extends App {

  // Read chart values
  //  val tableFile = "../test/data/Passport-linear-25_color_values.csv"
  val tableFile = "../test/data/ColorGauge-01_color_values.csv"
  val srcRT     = ResultsTable.open(tableFile)

  // Extract chart values as an array
  val observed = new Array[Array[Double]](srcRT.getCounter)
  for (i <- observed.indices) {
    observed(i) = new Array[Double](3)
    observed(i)(0) = srcRT.getValue("Observed X", i)
    observed(i)(1) = srcRT.getValue("Observed Y", i)
    observed(i)(2) = srcRT.getValue("Observed Z", i)
  }

  // Reference chart
  //  val chart = ColorCharts.XRitePassportColorChecker
  val chart = ColorCharts.ImageScienceColorGaugeMatte

  val referenceColorSpace = XYZ
  val clipReferenceRGB    = false
  val mappingMethod = MappingMethod.QuadraticCrossBand
  val colorCalibrator = new ColorCalibrator(chart, referenceColorSpace, mappingMethod, clipReferenceRGB)
  val fit = colorCalibrator.computeCalibrationMapping(observed)

  println("Deltas")
  println(fit.correctedDeltas.mkString("\n"))
  println("Delta mean: " + (fit.correctedDeltas.sum / fit.correctedDeltas.length))

  // Evaluate corrector using LOO cross validation
  val stats = LOOCrossValidation.crossValidationStats(chart, referenceColorSpace, mappingMethod, observed)

  println(mappingMethod)
  println(referenceColorSpace)
  println("LOO CV mean deltaE: " + stats.statsDeltaE.getMean)

}
