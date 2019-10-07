/*
 * Image/J Plugins
 * Copyright (C) 2002-2019 Jarek Sacha
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

package net.sf.ij_plugins.color.calibration.ui.tasks

import java.net.URL

import ij.measure.ResultsTable
import ij.{CompositeImage, IJ, ImagePlus, ImageStack}
import javafx.scene.{chart => jfxsc}
import net.sf.ij_plugins.color.calibration._
import net.sf.ij_plugins.color.calibration.chart.{GridColorChart, ReferenceColorSpace}
import net.sf.ij_plugins.color.calibration.regression.MappingMethod
import net.sf.ij_plugins.color.converter.ColorTriple.Lab
import net.sf.ij_plugins.color.{ColorFXUI, DeltaE}
import net.sf.ij_plugins.util.PlotUtils.{ValueEntry, createBarPlot}
import net.sf.ij_plugins.util.delta
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import org.scalafx.extras.BusyWorker.SimpleTask
import org.scalafx.extras.ShowMessage
import scalafx.Includes._
import scalafx.application.Platform
import scalafx.beans.property.{BooleanProperty, ObjectProperty}
import scalafx.collections.ObservableBuffer
import scalafx.scene.Scene
import scalafx.scene.chart.{NumberAxis, ScatterChart, XYChart}
import scalafx.scene.layout.StackPane
import scalafx.scene.paint.Color
import scalafx.stage.{Stage, Window}


class CalibrateTask(correctionRecipe: ObjectProperty[Option[CorrectionRecipe]],
                    referenceColorSpace: ObjectProperty[ReferenceColorSpace],
                    mappingMethod: ObjectProperty[MappingMethod],
                    image: ImagePlus,
                    chart: GridColorChart,
                    showExtraInfo: BooleanProperty,
                    val parentWindow: Option[Window]) extends SimpleTask[Unit] with ShowMessage {

  def call(): Unit = {

    correctionRecipe() = None

    // Compute color mapping coefficients
    val clipReferenceRGB = false
    val colorCalibrator = new ColorCalibrator(chart, referenceColorSpace(), mappingMethod(), clipReferenceRGB)

    val fit = try {
      colorCalibrator.computeCalibrationMapping(image)
    } catch {
      case t: Throwable =>
        showException("Error while computing color calibration.", t.getMessage, t)
        return
    }

    val recipe = CorrectionRecipe(
      corrector = new Corrector(fit.mapping),
      colorConverter = colorCalibrator.chart.colorConverter,
      referenceColorSpace = referenceColorSpace(),
      imageType = image.getType
    )

    correctionRecipe() = Option(recipe)

    val correctedBands = applyCorrection(recipe, image, showException) match {
      case Some(cb) => cb
      case None =>
        return
    }

    if (showExtraInfo()) {
      // Show table with expected measured and corrected values
      val rtColor = new ResultsTable()
      val chips = chart.referenceChips
      val bands = recipe.referenceColorSpace.bandsNames
      for (i <- chips.indices) {
        rtColor.incrementCounter()
        rtColor.setLabel(chips(i).name, i)
        for (b <- bands.indices) rtColor.setValue("Reference " + bands(b), i, IJ.d2s(fit.reference(i)(b), 4))
        for (b <- bands.indices) rtColor.setValue("Observed " + bands(b), i, IJ.d2s(fit.observed(i)(b), 4))
        for (b <- bands.indices) rtColor.setValue("Corrected " + bands(b), i, IJ.d2s(fit.corrected(i)(b), 4))
        rtColor.setValue("Delta " + bands.map(_.toUpperCase.head).mkString(""), i,
          IJ.d2s(delta(fit.reference(i), fit.corrected(i)), 4))
        for (b <- bands.indices) rtColor.setValue("Delta " + bands(b).toUpperCase().head, i,
          IJ.d2s(math.abs(fit.reference(i)(b) - fit.corrected(i)(b)), 4))
      }
      rtColor.show("Color Values")

      // Show table with regression results
      val rtFit = new ResultsTable()
      List(
        (bands(0), fit.mapping.band1),
        (bands(1), fit.mapping.band2),
        (bands(2), fit.mapping.band3)
      ).foreach {
        case (name, b) =>
          rtFit.incrementCounter()
          rtFit.addLabel(name)
          rtFit.addValue("R Squared", b.regressionResult.get.rSquared)
          b.toMap.foreach {
            case (_l, v) =>
              val l = if (_l.length <= 3)
                _l.
                  replace('a', bands(0).toLowerCase().head).
                  replace('b', bands(1).toLowerCase().head).
                  replace('c', bands(2).toLowerCase().head)
              else
                _l
              rtFit.addValue(l, IJ.d2s(v, 8))
          }
      }
      rtFit.show("Regression Coefficients")

      showScatterChart(fit.reference, fit.observed, referenceColorSpace().bandsNames, "Reference vs. Observed")
      showScatterChart(fit.reference, fit.corrected, referenceColorSpace().bandsNames, "Reference vs. Corrected")
      showResidualScatterChart(fit.reference, fit.corrected, "Reference vs. Corrected Residual")

      showColorErrorChart(fit.reference, fit.corrected,
        chips.map(_.name).toArray, referenceColorSpace().bandsNames)

      // Delta in reference color space
      val deltaStats = {
        val chips = chart.referenceChips
        //      val before = new DescriptiveStatistics()
        val after = new DescriptiveStatistics()
        for (i <- chips.indices) {
          //        before.addValue(ColorUtils.delta(fit.reference(i), fit.observed(i)))
          after.addValue(delta(fit.reference(i), fit.corrected(i)))
        }
        after
      }
      IJ.log("" +
        "Mean color chip delta after calibration:\n" +
        "  mean   = " + deltaStats.getMean + "\n" +
        "  min    = " + deltaStats.getMin + "\n" +
        "  max    = " + deltaStats.getMax + "\n" +
        "  median = " + deltaStats.getPercentile(50) + "\n" +
        "\n"
      )

      // Delta in L*a*b*
      val deltaEStats = {
        val rt = new ResultsTable()
        val stats = new DescriptiveStatistics()
        val labFPs = referenceColorSpace().toLab(correctedBands)
        val stack = new ImageStack(labFPs(0).getWidth, labFPs(0).getHeight)
        stack.addSlice("L*", labFPs(0))
        stack.addSlice("a*", labFPs(1))
        stack.addSlice("b*", labFPs(2))
        new CompositeImage(new ImagePlus("L*a*b*", stack), CompositeImage.GRAYSCALE).show()
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
        rt.show("Delta E")
        stats
      }
      IJ.log("" +
        "Mean color chip delta E after calibration:\n" +
        "  mean    = " + deltaEStats.getMean + "\n" +
        "  std dev = " + deltaEStats.getStandardDeviation + "\n" +
        "  min     = " + deltaEStats.getMin + "\n" +
        "  max     = " + deltaEStats.getMax + "\n" +
        "  median  = " + deltaEStats.getPercentile(50) + "\n" +
        "\n"
      )
    }
  }

  private def showColorErrorChart(x: Array[Array[Double]],
                                  y: Array[Array[Double]],
                                  columnNames: Array[String],
                                  seriesLabels: Array[String]): Unit = {
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
    createBarPlot("Individual Chip Error: " + image.getTitle, data, "Chip", "Error", barColors)

  }


  private def showScatterChart(x: Array[Array[Double]],
                               y: Array[Array[Double]],
                               seriesLabels: Array[String],
                               chartTitle: String): Unit = {

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
      val chips = chart.referenceChips
      for (i <- chips.indices; b <- 0 to 2) {
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
