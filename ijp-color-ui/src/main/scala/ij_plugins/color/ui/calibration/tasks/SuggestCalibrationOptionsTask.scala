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

import ij.measure.ResultsTable
import ij.{IJ, ImagePlus}
import ij_plugins.color.calibration.LOOCrossValidation
import ij_plugins.color.calibration.chart.{GridColorChart, ReferenceColorSpace}
import ij_plugins.color.calibration.regression.MappingMethod
import ij_plugins.color.ui.util.PlotUtils
import ij_plugins.color.ui.util.PlotUtils.ValueErrorEntry
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import org.scalafx.extras.BusyWorker.SimpleTask
import org.scalafx.extras.ShowMessage
import scalafx.stage.Window

object SuggestCalibrationOptionsTask {

  private case class CrossValidationData(referenceColorSpace: ReferenceColorSpace,
                                         method: MappingMethod,
                                         statsDeltaE: DescriptiveStatistics,
                                         statsDeltaL: DescriptiveStatistics,
                                         statsDeltaA: DescriptiveStatistics,
                                         statsDeltaB: DescriptiveStatistics)

}

class SuggestCalibrationOptionsTask(chart: GridColorChart,
                                    image: ImagePlus,
                                    val parentWindow: Option[Window]) extends SimpleTask[Unit] with ShowMessage {

  def call(): Unit = {


    IJ.showStatus("Computing cross validations...")
    val crossValidations = LOOCrossValidation.crossValidationStatsAll(chart, image,
      ReferenceColorSpace.values, MappingMethod.values)

    val best = crossValidations.minBy(_.statsDeltaE.getMean)
    IJ.showStatus("Best: " + best.referenceColorSpace + ":" + best.method + " -> " + best.statsDeltaE.getMean)

    // Sort, worst first
    val hSorted = crossValidations.toArray.sortBy(_.statsDeltaE.getMean)

    // Show as results table
    val rt = new ResultsTable()
    for ((v, i) <- hSorted.reverse.zipWithIndex) {
      rt.setValue("Reference", i, v.referenceColorSpace.toString)
      rt.setValue("Method", i, v.method.toString)
      rt.setValue("Mean DeltaE", i, v.statsDeltaE.getMean)
      rt.setValue("Min DeltaE", i, v.statsDeltaE.getMin)
      rt.setValue("Max DeltaE", i, v.statsDeltaE.getMax)
      rt.setValue("Median DeltaE", i, v.statsDeltaE.getPercentile(50))
      rt.setValue("StandardDeviation DeltaE", i, v.statsDeltaE.getStandardDeviation)
    }
    rt.show(image.getTitle + " Method LOO Cross Validation Error")


    // Show chart with comparison of results
    //    val data: Seq[ValueErrorEntry] = hSorted.flatMap { m =>
    //      Seq(ValueErrorEntry("Delta E", m.referenceColorSpace + " + " + m.method, m.statsDeltaE.getMean, m.statsDeltaE.getStandardDeviation),
    //        ValueErrorEntry("Delta L*", m.referenceColorSpace + " + " + m.method, m.statsDeltaL.getMean, m.statsDeltaL.getStandardDeviation),
    //        ValueErrorEntry("Delta a*", m.referenceColorSpace + " + " + m.method, m.statsDeltaA.getMean, m.statsDeltaA.getStandardDeviation),
    //        ValueErrorEntry("Delta b*", m.referenceColorSpace + " + " + m.method, m.statsDeltaB.getMean, m.statsDeltaB.getStandardDeviation))
    //    }
    val data = hSorted.map { m =>
      ValueErrorEntry("Delta E", s"${m.referenceColorSpace} + ${m.method}", m.statsDeltaE.getMean, m.statsDeltaE.getStandardDeviation)
    }.toIndexedSeq
    PlotUtils.createBarErrorPlot(
      title = "Method Comparison by Cross-Validation: " + image.getTitle,
      data = data,
      categoryAxisLabel = "Method",
      valueAxisLabel = "Mean Delta E (smaller is better)"
    )

  }

}
