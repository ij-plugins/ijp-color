/*
 * Image/J Plugins
 * Copyright (C) 2002-2022 Jarek Sacha
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

import ij.io.RoiDecoder
import ij.measure.ResultsTable
import ij.{IJ, ImagePlus}
import ij_plugins.color.calibration.LOOCrossValidation.CrossValidationData
import ij_plugins.color.calibration.chart.{ColorCharts, GridColorChart, ReferenceColorSpace}
import ij_plugins.color.calibration.regression.MappingMethod
import ij_plugins.color.converter.ReferenceWhite
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.*

import java.io.File

class LOOCrossValidationSpec extends AnyFlatSpec {

  behavior of "LOOCrossValidation"

  it should "compute cross validation stats (from a table)" in {

    // Read chart values
    val tableFile = new File("../test/data/Passport-linear-25_color_values.csv").getCanonicalFile
    assert(tableFile.exists(), "Input table file should exist: " + tableFile)
    val srcRT = ResultsTable.open(tableFile.getCanonicalPath)
    // Extract chart values as an array
    val observed =
      (0 until srcRT.getCounter).map { i =>
        IndexedSeq(
          srcRT.getValue("Observed X", i),
          srcRT.getValue("Observed Y", i),
          srcRT.getValue("Observed Z", i)
        )
      }

    // Reference chart
    val chart = ColorCharts.XRitePassportColorChecker

    // Do LOO validation
    val referenceColorSpaces = ReferenceColorSpace.values.toSeq
    val mappingMethods = MappingMethod.values.toSeq
    val crossValidations =
      LOOCrossValidation.crossValidationStatsAll(chart, observed, referenceColorSpaces, mappingMethods)

    crossValidations should have length (referenceColorSpaces.length * mappingMethods.length)

    // Sort, best first
    val bestByMean = crossValidations.toArray.minBy(_.statsDeltaE.getMean)
    //    println(
    //      s"Best by mean: ${bestByMean.method} - ${bestByMean.referenceColorSpace}: ${bestByMean.statsDeltaE.getMean} "
    //    )
    bestByMean.referenceColorSpace should be(ReferenceColorSpace.XYZ)
    bestByMean.method should be(MappingMethod.QuadraticCrossBand)
    bestByMean.statsDeltaE.getMean should be(4.854 +- 0.1)
    bestByMean.statsDeltaE.getPercentile(50) should be(4.494 +- 0.1)
    bestByMean.statsDeltaE.getPercentile(95) should be(12.212 +- 0.1)
  }

  it should "compute cross validation stats (from an image)" in {

    val (imp, chart) = loadImageChart()

    // LOO-CV with all chips enabled
    val crossValidations =
      LOOCrossValidation.crossValidationStatsAll(chart, imp, ReferenceColorSpace.values.toSeq, MappingMethod.values.toSeq)

    //    println("\nLOO Cross-Validation - all chips")
    //    printInfo(crossValidations)

    val bestByMean = crossValidations.toArray.minBy(_.statsDeltaE.getMean)
    bestByMean.referenceColorSpace should be(ReferenceColorSpace.sRGB)
    bestByMean.method should be(MappingMethod.QuadraticCrossBand)
    bestByMean.statsDeltaE.getMean should be(5.321 +- 0.1)
    bestByMean.statsDeltaE.getPercentile(50) should be(3.958 +- 0.1)
    bestByMean.statsDeltaE.getPercentile(95) should be(13.450 +- 0.1)
  }

  it should "compute cross validation stats with a chip disabled (from an image)" in {

    val (imp, chart) = loadImageChart()

    // LOO-CV with invalid-reference-chips disabled
    val enabled = chart.enabled.updated(6, false)
    val chart2 = chart.copyWithEnabled(enabled)
    val crossValidations =
      LOOCrossValidation.crossValidationStatsAll(chart2, imp, ReferenceColorSpace.values.toSeq, MappingMethod.values.toSeq)

    //    println("\nLOO Cross-Validation - invalid reference disabled")
    //    printInfo(crossValidations)

    val bestByMean = crossValidations.toArray.minBy(_.statsDeltaE.getMean)
    bestByMean.referenceColorSpace should be(ReferenceColorSpace.sRGB)
    bestByMean.method should be(MappingMethod.QuadraticCrossBand)
    bestByMean.statsDeltaE.getMean should be(5.132 +- 0.1)
    bestByMean.statsDeltaE.getPercentile(50) should be(3.839 +- 0.1)
    bestByMean.statsDeltaE.getPercentile(95) should be(12.140 +- 0.1)
  }

  def loadImageChart(): (ImagePlus, GridColorChart) = {
    val dataDir = new File("../test/data")
    require(dataDir.exists(), s"Input directory must exist: ${dataDir.getCanonicalPath}")

    // Image to calibrate
    val imageFile = new File(dataDir, "Color_Gauge_Sample_3.png")
    val roiFile   = new File(dataDir, "Color_Gauge_Sample_3.roi")

    // Custom chart params
    val nbRows        = 5
    val nbColumns     = 6
    val chipMargin    = 0.2
    val refWhite      = ReferenceWhite.D50
    val refValuesFile = new File(dataDir, "Color_Gauge_Chart_3.csv")

    // Load image
    val imp = IJ.openImage(imageFile.getPath)
    require(imp != null)

    // Load ROI
    val roi = RoiDecoder.open(roiFile.getPath)
    require(roi != null)

    // Load reference values
    val chipsRefValues = ColorCharts.loadReferenceValues(refValuesFile)

    // Create custom calibration chart aligned to given ROI
    val chart =
      new GridColorChart(
        s"Custom Color Gauge",
        nbColumns = nbColumns,
        nbRows = nbRows,
        chips = chipsRefValues,
        chipMargin = chipMargin,
        refWhite = refWhite
      ).copyAlignedTo(roi)

    (imp, chart)
  }

  def printInfo(crossValidations: Seq[CrossValidationData]): Unit = {
    val sortedByMean = crossValidations.sortBy(v => v.statsDeltaE.getMean)

    println("Space -                Method:      Mean,    Median,       95%")
    sortedByMean.foreach { (cvd: CrossValidationData) =>
      println(
        f"${cvd.referenceColorSpace}%5s - ${cvd.method}%21s:  " +
          f"${cvd.statsDeltaE.getMean}%8.3f,  ${cvd.statsDeltaE.getPercentile(50)}%8.3f,  ${cvd.statsDeltaE.getPercentile(95)}%8.3f"
      )
    }
  }

}
