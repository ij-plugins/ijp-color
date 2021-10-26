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
import ij.process.FloatProcessor
import ij.{IJ, ImagePlus}
import ij_plugins.color.DeltaE
import ij_plugins.color.calibration.CalibrationUtils.toPolygonROI
import ij_plugins.color.calibration._
import ij_plugins.color.calibration.chart.{ColorChip, GridColorChart, ReferenceColorSpace}
import ij_plugins.color.calibration.regression.MappingMethod
import ij_plugins.color.converter.ColorTriple.Lab
import ij_plugins.color.converter.ReferenceWhite
import ij_plugins.color.ui.calibration.tasks.CalibrateTask.{OutputConfig, showScatterChart}
import ij_plugins.color.ui.calibration.{CalibrationUtils, IJPError}
import ij_plugins.color.ui.fx.ColorFXUI
import ij_plugins.color.ui.util.PlotUtils.ValueEntry
import ij_plugins.color.ui.util.{IJPrefs, PlotUtils}
import ij_plugins.color.util.ImagePlusType
import ij_plugins.color.util.Utils.delta
import javafx.scene.{chart => jfxsc}
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import org.scalafx.extras.BusyWorker.SimpleTask
import org.scalafx.extras.ShowMessage
import scalafx.Includes._
import scalafx.application.Platform
import scalafx.beans.property.ObjectProperty
import scalafx.collections.ObservableBuffer
import scalafx.scene.Scene
import scalafx.scene.chart.{NumberAxis, ScatterChart, XYChart}
import scalafx.scene.layout.StackPane
import scalafx.scene.paint.Color
import scalafx.stage.{Stage, Window}

import java.net.URL

object CalibrateTask {

  object OutputConfig {
    private val ReferencePrefix = classOf[OutputConfig].getName

    def loadFromIJPref(): Option[OutputConfig] = {
      for {
        imageInSRGB <- IJPrefs.getBooleanOption(ReferencePrefix + ".imageInSRGB")
        imageInReferenceColorSpace <- IJPrefs.getBooleanOption(ReferencePrefix + ".imageInReferenceColorSpace")
        imageInLab <- IJPrefs.getBooleanOption(ReferencePrefix + ".imageInLab")
        plotScatterFit <- IJPrefs.getBooleanOption(ReferencePrefix + ".plotScatterFit")
        plotIndividualChipError <- IJPrefs.getBooleanOption(ReferencePrefix + ".plotIndividualChipError")
        tableExpectedVsCorrected <- IJPrefs.getBooleanOption(ReferencePrefix + ".tableExpectedVsCorrected")
        tableRegressionResults <- IJPrefs.getBooleanOption(ReferencePrefix + ".tableRegressionResults")
        tableIndividualChipDeltaInLab <- IJPrefs.getBooleanOption(ReferencePrefix + ".tableIndividualChipDeltaInLab")
        logDeltaInReferenceColorSpace <- IJPrefs.getBooleanOption(ReferencePrefix + ".logDeltaInReferenceColorSpace")
      } yield {
        OutputConfig(
          imageInSRGB,
          imageInReferenceColorSpace,
          imageInLab,
          plotScatterFit,
          plotIndividualChipError,
          tableExpectedVsCorrected,
          tableRegressionResults,
          tableIndividualChipDeltaInLab,
          logDeltaInReferenceColorSpace
        )
      }
    }
  }

  case class OutputConfig(
                           imageInSRGB: Boolean = true,
                           imageInReferenceColorSpace: Boolean = false,
                           imageInLab: Boolean = false,
                           plotScatterFit: Boolean = false,
                           plotIndividualChipError: Boolean = false,
                           tableExpectedVsCorrected: Boolean = false,
                           tableRegressionResults: Boolean = false,
                           tableIndividualChipDeltaInLab: Boolean = false,
                           logDeltaInReferenceColorSpace: Boolean = false
                         ) {

    def saveToIJPref(): Unit = {
      import OutputConfig.ReferencePrefix
      IJPrefs.set(ReferencePrefix + ".imageInSRGB", imageInSRGB)
      IJPrefs.set(ReferencePrefix + ".imageInReferenceColorSpace", imageInReferenceColorSpace)
      IJPrefs.set(ReferencePrefix + ".imageInLab", imageInLab)
      IJPrefs.set(ReferencePrefix + ".plotScatterFit", plotScatterFit)
      IJPrefs.set(ReferencePrefix + ".plotIndividualChipError", plotIndividualChipError)
      IJPrefs.set(ReferencePrefix + ".tableExpectedVsCorrected", tableExpectedVsCorrected)
      IJPrefs.set(ReferencePrefix + ".tableRegressionResults", tableRegressionResults)
      IJPrefs.set(ReferencePrefix + ".tableIndividualChipDeltaInLab", tableIndividualChipDeltaInLab)
      IJPrefs.set(ReferencePrefix + ".logDeltaInReferenceColorSpace", logDeltaInReferenceColorSpace)
    }

  }

  private def showScatterChart(
                                x: Array[Array[Double]],
                                y: Array[Array[Double]],
                                seriesLabels: Array[String],
                                chartTitle: String
                              ): Unit = {

    require(x.length == y.length)
    require(seriesLabels.length == 3)

    def check(name: String): Option[URL] = {
      val stylesheetURL = getClass.getResource(name)
      Option(stylesheetURL)
    }

    def myStylesheets: Seq[String] = List(
      "RGBScatterChart.css"
    ).flatMap(check(_).map(_.toExternalForm))

    // Create plot
    val xAxis = new NumberAxis()
    val yAxis = new NumberAxis()
    val scatterChart = ScatterChart(xAxis, yAxis)
    scatterChart.data = {
      val answer = new ObservableBuffer[jfxsc.XYChart.Series[Number, Number]]()
      val bands = (0 to 2).map {
        b =>
          new XYChart.Series[Number, Number] {
            name = seriesLabels(b)
          }
      }
      for (i <- x.indices; b <- 0 to 2) {
        bands(b).data.get += XYChart.Data[Number, Number](x(i)(b), y(i)(b))
      }
      bands.foreach(answer.add(_))
      answer
    }
    Platform.runLater {
      val dialogStage = new Stage() {
        title = chartTitle
        scene = new Scene {
          root = new StackPane {
            children = scatterChart
            stylesheets ++= ColorFXUI.stylesheets
            stylesheets ++= myStylesheets
          }
        }
      }
      dialogStage.show()
    }
  }
}

class CalibrateTask(
                     referenceColorSpace: ObjectProperty[ReferenceColorSpace],
                     mappingMethod: ObjectProperty[MappingMethod],
                     image: ImagePlus,
                     chart: GridColorChart,
                     outputConfig: OutputConfig,
                     val parentWindow: Option[Window]
                   ) extends SimpleTask[Option[CorrectionRecipe]]
  with ShowMessage {

  def call(): Option[CorrectionRecipe] = {

    // Compute color mapping coefficients
    val clipReferenceRGB = false
    val colorCalibrator = new ColorCalibrator(chart, referenceColorSpace(), mappingMethod(), clipReferenceRGB)

    // Compute calibration mapping
    val fitLR =
      try {
        Right(colorCalibrator.computeCalibrationMapping(image))
      } catch {
        case t: Throwable =>
          Left(IJPError("Error while computing color calibration.", t))
      }

    // Prepare calibration outputs
    val recipeLR = fitLR.map { fit: ColorCalibrator.CalibrationFit =>
      val recipe = CorrectionRecipe(
        corrector = fit.corrector,
        colorConverter = colorCalibrator.chart.colorConverter,
        referenceColorSpace = referenceColorSpace(),
        imageType = ImagePlusType.withValue(image.getType)
      )

      // Applies correction and displays image in reference color space and sRGB color image
      val correctionOutputLR =
        CalibrationUtils.applyCorrection(
          recipe,
          image,
          computeInReference = outputConfig.imageInReferenceColorSpace,
          computeInSRGB = outputConfig.imageInSRGB
        )

      // TODO: properly deal with the error
      correctionOutputLR.foreach { co =>
        val titlePrefix = s"${image.getTitle} - ${colorCalibrator.referenceColorSpace}+${colorCalibrator.mappingMethod}"
        val chips: Seq[ColorChip] = chart.referenceChipsEnabled
        val bands: Array[String] = recipe.referenceColorSpace.bandsNames

        co.correctedInSRGB.foreach { imp =>
          imp.setTitle(s"$titlePrefix - sRGB")
          imp.show()
        }
        co.correctedInReferenceSpace.foreach { imp =>
          imp.setTitle(titlePrefix)
          imp.show()
        }

        val refWhite = recipe.colorConverter.refWhite

        if (outputConfig.imageInLab) {
          CalibrationUtils.showImageInLab(
            recipe.referenceColorSpace,
            refWhite,
            co.correctedBands,
            s"$titlePrefix - CIE L*a*b* ${refWhite.entryName}"
          )
        }

        if (outputConfig.plotScatterFit)
          showFitScatterPlots(fit, titlePrefix)

        if (outputConfig.plotIndividualChipError)
          showColorErrorChart(
            fit.reference,
            fit.corrected,
            chips.map(_.name).toArray,
            referenceColorSpace().bandsNames,
            titlePrefix
          )

        if (outputConfig.tableExpectedVsCorrected)
          showExpectedVsCorrected(chips: Seq[ColorChip], bands: Array[String], fit, titlePrefix)

        if (outputConfig.tableRegressionResults)
          showTableWithRegressionResults(bands, fit, s"$titlePrefix - Regression Coefficients")

        if (outputConfig.tableIndividualChipDeltaInLab)
          tableIndividualChipDeltaInLab(refWhite, co.correctedBands, titlePrefix)

        if (outputConfig.logDeltaInReferenceColorSpace)
          logDeltaInReferenceColorSpace(fit, titlePrefix)
      }

      recipe
    }

    // Deal with error and create return value
    recipeLR match {
      case Right(value) =>
        Option(value)
      case Left(error) =>
        showException(error.message, error.t.getMessage, error.t)
        None
    }
  }

  private def showExpectedVsCorrected(
                                       chips: Seq[ColorChip],
                                       bands: Array[String],
                                       fit: ColorCalibrator.CalibrationFit,
                                       titlePrefix: String
                                     ): Unit = {

    // Show table with expected measured and corrected values
    val rtColor = new ResultsTable()
    for (i <- chips.indices) {
      rtColor.incrementCounter()
      rtColor.setLabel(chips(i).name, i)
      for (b <- bands.indices) rtColor.setValue("Reference " + bands(b), i, IJ.d2s(fit.reference(i)(b), 4))
      for (b <- bands.indices) rtColor.setValue("Observed " + bands(b), i, IJ.d2s(fit.observed(i)(b), 4))
      for (b <- bands.indices) rtColor.setValue("Corrected " + bands(b), i, IJ.d2s(fit.corrected(i)(b), 4))
      rtColor.setValue(
        "Delta " + bands.map(_.toUpperCase.head).mkString(""),
        i,
        IJ.d2s(delta(fit.reference(i), fit.corrected(i)), 4)
      )
      for (b <- bands.indices) rtColor.setValue(
        "Delta " + bands(b).toUpperCase().head,
        i,
        IJ.d2s(math.abs(fit.reference(i)(b) - fit.corrected(i)(b)), 4)
      )
    }
    rtColor.show(titlePrefix + " - Color Values")

  }

  private def showTableWithRegressionResults(
                                              bands: Array[String],
                                              fit: ColorCalibrator.CalibrationFit,
                                              title: String
                                            ): Unit = {

    // Show table with regression results
    val rtFit = new ResultsTable()
    List(
      (bands(0), fit.corrector.band1),
      (bands(1), fit.corrector.band2),
      (bands(2), fit.corrector.band3)
    ).foreach {
      case (name, b) =>
        rtFit.incrementCounter()
        rtFit.addLabel(name)
        rtFit.addValue("R Squared", b.regressionResult.get.rSquared)
        b.toMap.foreach {
          case (_l, v) =>
            val l =
              if (_l.length <= 3)
                _l.replace('a', bands(0).toLowerCase().head).replace('b', bands(1).toLowerCase().head).replace(
                  'c',
                  bands(2).toLowerCase().head
                )
              else
                _l
            rtFit.addValue(l, IJ.d2s(v, 8))
        }
    }
    rtFit.show(title)
  }

  private def showFitScatterPlots(fit: ColorCalibrator.CalibrationFit, titlePrefix: String): Unit = {

    showScatterChart(
      fit.reference,
      fit.observed,
      referenceColorSpace().bandsNames,
      s"$titlePrefix - Reference vs. Observed"
    )
    showScatterChart(
      fit.reference,
      fit.corrected,
      referenceColorSpace().bandsNames,
      s"$titlePrefix - Reference vs. Corrected"
    )
    showResidualScatterChart(fit.reference, fit.corrected, s"$titlePrefix - Reference vs. Corrected Residual")
  }

  private def logDeltaInReferenceColorSpace(fit: ColorCalibrator.CalibrationFit, titlePrefix: String): Unit = {

    // Delta in reference color space
    val deltaStats = {
      val chips = chart.referenceChipsEnabled
      //      val before = new DescriptiveStatistics()
      val after = new DescriptiveStatistics()
      for (i <- chips.indices) {
        //        before.addValue(ColorUtils.delta(fit.reference(i), fit.observed(i)))
        after.addValue(delta(fit.reference(i), fit.corrected(i)))
      }
      after
    }
    IJ.log("\n" +
      titlePrefix + "\n" +
      "Mean color chip delta after calibration:\n" +
      "  mean   = " + deltaStats.getMean + "\n" +
      "  min    = " + deltaStats.getMin + "\n" +
      "  max    = " + deltaStats.getMax + "\n" +
      "  median = " + deltaStats.getPercentile(50) + "\n" +
      "\n")
  }

  private def tableIndividualChipDeltaInLab(
                                             refWhite: ReferenceWhite,
                                             correctedBands: Array[FloatProcessor],
                                             titlePrefix: String
                                           ): Unit = {

    // Delta in L*a*b*
    val deltaEStats = {
      val rt = new ResultsTable()
      val stats = new DescriptiveStatistics()
      val labFPs = referenceColorSpace().toLab(correctedBands, refWhite)
      IJ.log(titlePrefix)
      for (chip <- chart.alignedChips) {
        val poly = toPolygonROI(chip.outline)
        val actualTestColor = for (fp <- labFPs) yield {
          fp.setRoi(poly)
          fp.getStatistics.mean
        }
        val actualTestColorLab = Lab(actualTestColor(0), actualTestColor(1), actualTestColor(2))

        // Delta needs to be computed in a manner independent of the reference color space so different reference
        // color spaces can be compared to each other. We will use CIE L*a*b* as common comparison space.
        val d = DeltaE.e76(chip.color, actualTestColorLab)
        IJ.log(chip.name + " expected = " + chip.color)
        IJ.log(chip.name + " actual   = " + actualTestColorLab)
        IJ.log(chip.name + " Delta E  = " + d)
        stats.addValue(d)

        rt.incrementCounter()
        rt.addValue("Color Name", chip.name)
        rt.addValue("Expected L*", chip.color.l)
        rt.addValue("Expected a*", chip.color.a)
        rt.addValue("Expected b*", chip.color.b)
        rt.addValue("Actual L*", actualTestColorLab.l)
        rt.addValue("Actual a*", actualTestColorLab.a)
        rt.addValue("Actual b*", actualTestColorLab.b)
        rt.addValue("Delta E*", d)
        rt.addValue("Delta L*", actualTestColorLab.l - chip.color.l)
        rt.addValue("Delta a*", actualTestColorLab.a - chip.color.a)
        rt.addValue("Delta b*", actualTestColorLab.b - chip.color.b)
      }
      rt.show(titlePrefix + " - Delta E")
      stats
    }
    IJ.log("" +
      "Mean color chip delta E after calibration:\n" +
      "  mean    = " + deltaEStats.getMean + "\n" +
      "  std dev = " + deltaEStats.getStandardDeviation + "\n" +
      "  min     = " + deltaEStats.getMin + "\n" +
      "  max     = " + deltaEStats.getMax + "\n" +
      "  median  = " + deltaEStats.getPercentile(50) + "\n" +
      "\n")
  }

  private def showColorErrorChart(
                                   x: Array[Array[Double]],
                                   y: Array[Array[Double]],
                                   columnNames: Array[String],
                                   seriesLabels: Array[String],
                                   titlePrefix: String
                                 ): Unit = {

    assert(x.length == y.length)
    assert(x.length == columnNames.length)

    val xxYYcc = x.zip(y).zip(columnNames)
    val data = xxYYcc.flatMap { case ((xx, yy), cc) =>
      Seq(
        ValueEntry(seriesLabels(0), cc, math.abs(xx(0) - yy(0))),
        ValueEntry(seriesLabels(1), cc, math.abs(xx(1) - yy(1))),
        ValueEntry(seriesLabels(2), cc, math.abs(xx(2) - yy(2)))
      )
    }.toIndexedSeq
    val barColors = Seq(Color(1, 0.33, 0.33, 0.75), Color(0.33, 1, 0.33, 0.5), Color(0.33, 0.33, 1, 0.25))
    PlotUtils.createBarPlot(titlePrefix + " - Individual Chip Error", data, "Chip", "Error", barColors)
  }

  private def showResidualScatterChart(x: Array[Array[Double]], y: Array[Array[Double]], chartTitle: String): Unit = {

    val dy = new Array[Array[Double]](x.length)
    for (i <- x.indices) {
      val xi = x(i)
      val yi = y(i)
      val dd = new Array[Double](xi.length)
      for (j <- xi.indices) {
        dd(j) = math.abs(yi(j) - xi(j))
      }
      dy(i) = dd
    }
    showScatterChart(x, dy, referenceColorSpace().bandsNames, chartTitle)
  }

}
