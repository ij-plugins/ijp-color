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

import ij.IJ
import ij.io.RoiDecoder
import ij_plugins.color.calibration.LOOCrossValidation.CrossValidationData
import ij_plugins.color.calibration.chart._
import ij_plugins.color.calibration.regression.MappingMethod
import ij_plugins.color.converter.ReferenceWhite

import java.io.File

object LOOCrossValidation1Demo {

  def main(args: Array[String]): Unit = {

    val dataDir = new File("../test/data")
    require(dataDir.exists(), s"Input directory must exist: ${dataDir.getCanonicalPath}")

    // Image to calibrate
    val imageFile = new File(dataDir, "Color_Gauge_Sample_3.png")
    val roiFile = new File(dataDir, "Color_Gauge_Sample_3.roi")

    // Custom chart params
    val nbRows = 5
    val nbColumns = 6
    val chipMargin = 0.2
    val refWhite = ReferenceWhite.D50
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

    // LOO-CV with all chips enabled
    val crossValidations =
      LOOCrossValidation.crossValidationStatsAll(chart, imp, ReferenceColorSpace.values, MappingMethod.values)

    println("All chips enables")
    printBestInfo(crossValidations)

    // LOO-CV with invalid-reference-chips disabled
    val enabled2 = chart.enabled.toArray
    enabled2(6) = false
    val chart2 = chart.copyWithEnableChips(enabled2)
    val crossValidations2 =
      LOOCrossValidation.crossValidationStatsAll(chart2, imp, ReferenceColorSpace.values, MappingMethod.values)
    println("\nWith invalid reference disabled")
    printBestInfo(crossValidations2)
  }

  private def printBestInfo(crossValidations: Seq[CrossValidationData]): Unit = {
    val bestByMean = crossValidations.minBy(v => v.statsDeltaE.getMean)
    println(
      s"Best by mean  : ${bestByMean.referenceColorSpace} - ${bestByMean.method}: ${bestByMean.statsDeltaE.getMean} "
    )

    val bestByMedian = crossValidations.minBy(v => v.statsDeltaE.getPercentile(50))
    println(
      s"Best by median: ${bestByMedian.referenceColorSpace} - ${bestByMedian.method}: ${bestByMedian.statsDeltaE.getPercentile(50)} "
    )

    val bestBy95 = crossValidations.minBy(v => v.statsDeltaE.getPercentile(95))
    println(
      s"Best by 95%   : ${bestBy95.referenceColorSpace} - ${bestBy95.method}: ${bestBy95.statsDeltaE.getPercentile(95)} "
    )
  }
}
