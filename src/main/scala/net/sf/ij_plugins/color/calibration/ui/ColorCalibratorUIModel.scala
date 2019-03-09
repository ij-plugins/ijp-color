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
 * Latest release available at http://sourceforge.net/projects/ij-plugins/
 */

package net.sf.ij_plugins.color.calibration.ui

import java.net.URL

import ij.measure.ResultsTable
import ij.plugin.BrowserLauncher
import ij.process.FloatProcessor
import ij.{CompositeImage, IJ, ImagePlus, ImageStack}
import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.scene.{chart => jfxsc}
import net.sf.ij_plugins.color.calibration.chart.{ColorCharts, ReferenceColorSpace}
import net.sf.ij_plugins.color.calibration.regression.MappingMethod
import net.sf.ij_plugins.color.calibration.{ColorCalibrator, Corrector, LOOCrossValidation, toPolygonROI}
import net.sf.ij_plugins.color.converter.ColorTriple.Lab
import net.sf.ij_plugins.color.{ColorFXUI, DeltaE}
import net.sf.ij_plugins.util.PlotUtils.createBarErrorPlot
import net.sf.ij_plugins.util._
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import org.scalafx.extras.mvcfx.ModelFX
import org.scalafx.extras.{BusyWorker, ShowMessage}
import scalafx.Includes._
import scalafx.application.Platform
import scalafx.beans.property._
import scalafx.collections.ObservableBuffer
import scalafx.scene.Scene
import scalafx.scene.chart._
import scalafx.scene.layout.StackPane
import scalafx.stage.{Stage, Window}

object ColorCalibratorUIModel {

  val HelpURL = "https://github.com/ij-plugins/ijp-color"

  def applyCorrection(recipe: CorrectionRecipe,
                      imp: ImagePlus,
                      showError: (String, String, Throwable) => Unit): Option[Array[FloatProcessor]] = {
    val correctedBands = try {
      recipe.corrector.map(imp)
    } catch {
      case t: Throwable =>
        showError("Error while color correcting the image.", t.getMessage, t)
        return None
    }

    // Show floating point stack in the reference color space
    val correctedInReference = {
      val stack = new ImageStack(imp.getWidth, imp.getHeight)
      (recipe.referenceColorSpace.bandsNames zip correctedBands).foreach(v => stack.addSlice(v._1, v._2))
      val mode = if (recipe.referenceColorSpace == ReferenceColorSpace.sRGB) CompositeImage.COMPOSITE else CompositeImage.GRAYSCALE
      new CompositeImage(new ImagePlus(imp.getTitle + "+corrected_" + recipe.referenceColorSpace, stack), mode)
    }
    correctedInReference.show()

    // Convert corrected image to sRGB
    val correctedImage: ImagePlus = convertToSRGB(correctedBands, recipe.referenceColorSpace, recipe.colorConverter)
    correctedImage.setTitle(imp.getTitle + "+corrected_" + recipe.referenceColorSpace + "+sRGB")
    correctedImage.show()

    Option(correctedBands)
  }
}

/**
  * Model for color calibrator UI.
  */
class ColorCalibratorUIModel(val image: ImagePlus, parentWindow: Window) extends ModelFX with ShowMessage {

  import ColorCalibratorUIModel._

  require(parentWindow != null, "Argument `parentStage` cannot be null.")

  val imageTitle = new StringProperty(this, "imageTitle", image.getTitle)
  val referenceColorSpace = new ObjectProperty[ReferenceColorSpace](this, "referenceColorSpace", ReferenceColorSpace.sRGB)
  val referenceChart = new ObjectProperty(this, "chart", ColorCharts.GretagMacbethColorChecker)
  // Convenience variable to feed/link to `liveChartROI`
  val referenceChartOption = new ObjectProperty(this, "chart", Option(referenceChart()))
  val chipMarginPercent = new ObjectProperty[Integer](this, "chipMargin", 20)
  val mappingMethod = new ObjectProperty(this, "mappingMethod", MappingMethod.LinearCrossBand)
  val clipReferenceRGB = new BooleanProperty(this, "clipReferenceRGB", true)
  val showExtraInfo = new BooleanProperty(this, "showExtraInfo", false)
  val correctionRecipe = new ObjectProperty[Option[CorrectionRecipe]](this, "correctionRecipe", None)

  val liveChartROI = new LiveChartROI(image, referenceChartOption, chipMarginPercent)

  val chipValuesObserved: ReadOnlyBooleanProperty = {
    val p = new ReadOnlyBooleanWrapper(this, "chipValuesObserved", false)
    // Chip values are observed when `locatedChart` is available
    p <== liveChartROI.locatedChart =!= None
    p.getReadOnlyProperty
  }

  // True when correction parameters are available and can be applied to another image
  val correctionRecipeAvailable: ReadOnlyBooleanProperty = {
    val p = new ReadOnlyBooleanWrapper(this, "correctionRecipeAvailable", false)
    p <== correctionRecipe =!= None
    p.getReadOnlyProperty
  }

  private def currentChart = liveChartROI.locatedChart.value.get

  private val busyWorker: BusyWorker = new BusyWorker("Color Calibrator", parentWindow)


  referenceChart.onChange((_, _, newValue) => referenceChartOption() = Option(newValue))


  def onRenderReferenceChart(): Unit = busyWorker.doTask("onRenderReferenceChart") { () =>
    renderReferenceChart(referenceChart()).show()
  }

  def onShowReferenceColors(): Unit = busyWorker.doTask("onShowReferenceColors") { () =>
    val rt = new ResultsTable()
    val chips = referenceChart().referenceChips
    for (i <- chips.indices) {
      rt.incrementCounter()
      rt.setLabel(chips(i).name, i)
      val lab = chips(i).color
      val xyz = referenceChart().colorConverter.toXYZ(lab)
      val rgb = referenceChart().colorConverter.toRGB(xyz)
      rt.setValue("CIE L*", i, lab.l)
      rt.setValue("CIE a*", i, lab.a)
      rt.setValue("CIE b*", i, lab.b)
      rt.setValue("CIE X", i, xyz.x)
      rt.setValue("CIE Y", i, xyz.y)
      rt.setValue("CIE Z", i, xyz.z)
      rt.setValue("sRGB R", i, rgb.r)
      rt.setValue("sRGB G", i, rgb.g)
      rt.setValue("sRGB B", i, rgb.b)
    }
    rt.show(referenceChart().name + " / " + referenceChart().refWhite)
  }

  def onSuggestCalibrationOptions(): Unit = busyWorker.doTask("onSuggestCalibrationOptions") { () =>
    val chart = currentChart

    val methods = MappingMethod.values.toList

    val refSpaceMethods = for (rcs <- ReferenceColorSpace.values; method <- methods) yield (rcs, method)

    val crossValidations = for (((rcs, _method), i) <- refSpaceMethods.zipWithIndex) yield {
      IJ.showStatus("Checking " + rcs + " + " + _method)
      IJ.showProgress(i, refSpaceMethods.length)

      val stats = new DescriptiveStatistics()
      val deltas = LOOCrossValidation.crossValidation(chart, rcs, _method, image)
      deltas.foreach(v => stats.addValue(v))
      val _mean = deltas.sum / deltas.length

      (rcs, _method, _mean, deltas.min, deltas.max, stats.getPercentile(50))
      new {
        val referenceColorSpace = rcs
        val method = _method
        val mean = _mean
        val min = deltas.min
        val max = deltas.max
        val median = stats.getPercentile(50)
        val stdDev = stats.getStandardDeviation
      }
    }
    IJ.showProgress(1, 1)


    val best = crossValidations.minBy(_.mean)
    IJ.showStatus("Best: " + best.referenceColorSpace + ":" + best.method + " -> " + best.mean)

    // Sort, worst first
    val hSorted = crossValidations.toArray.sortBy(_.mean)

    // Show as results table
    val rt = new ResultsTable()
    for ((v, i) <- hSorted.reverse.zipWithIndex) {
      rt.setValue("Reference", i, v.referenceColorSpace.toString)
      rt.setValue("Method", i, v.method.toString)
      rt.setValue("Mean", i, v.mean)
      rt.setValue("Min", i, v.min)
      rt.setValue("Max", i, v.max)
      rt.setValue("Median", i, v.median)
      rt.setValue("StandardDeviation", i, v.stdDev)
    }
    rt.show(image.getTitle + " Method LOO Cross Validation Error")


    // Show chart with comparison of results
    val data: Seq[(String, Double, Double)] = hSorted.map(m => (m.referenceColorSpace + " + " + m.method, m.mean, m.stdDev))
    createBarErrorPlot(
      title = "Cross-Validation: " + imageTitle(),
      data = data,
      categoryAxisLabel = "Method",
      valueAxisLabel = "Mean Delta E (smaller is better)"
    )
  }

  def onCalibrate(): Unit = busyWorker.doTask("onCalibrate") { () =>

    correctionRecipe() = None

    val chart = currentChart

    // Compute color mapping coefficients
    val colorCalibrator = new ColorCalibrator(chart, referenceColorSpace(), mappingMethod())

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

    val correctedBands = applyCorrection(
      recipe,
      image,
      showException
    ).getOrElse(return)

    if (showExtraInfo()) {
      // Show table with expected measured and corrected values
      val rtColor = new ResultsTable()
      val chips = chart.referenceChips
      for (i <- chips.indices) {
        rtColor.incrementCounter()
        rtColor.setLabel(chips(i).name, i)
        rtColor.setValue("Reference 0", i, IJ.d2s(fit.reference(i)(0), 4))
        rtColor.setValue("Reference 1", i, IJ.d2s(fit.reference(i)(1), 4))
        rtColor.setValue("Reference 2", i, IJ.d2s(fit.reference(i)(2), 4))
        rtColor.setValue("Observed 0", i, IJ.d2s(fit.observed(i)(0), 4))
        rtColor.setValue("Observed 1", i, IJ.d2s(fit.observed(i)(1), 4))
        rtColor.setValue("Observed 2", i, IJ.d2s(fit.observed(i)(2), 4))
        rtColor.setValue("Corrected 0", i, IJ.d2s(fit.corrected(i)(0), 4))
        rtColor.setValue("Corrected 1", i, IJ.d2s(fit.corrected(i)(1), 4))
        rtColor.setValue("Corrected 2", i, IJ.d2s(fit.corrected(i)(2), 4))
        rtColor.setValue("Delta", i, IJ.d2s(delta(fit.reference(i), fit.corrected(i)), 4))
      }
      rtColor.show("Color Values")

      // Show table with regression results
      val rtFit = new ResultsTable()
      List(
        ("Band 1", fit.mapping.band1),
        ("Band 2", fit.mapping.band2),
        ("Band 3", fit.mapping.band3)
      ).foreach {
        case (name, b) =>
          rtFit.incrementCounter()
          rtFit.addLabel(name)
          rtFit.addValue("R Squared", b.regressionResult.get.rSquared)
          b.toMap.foreach {
            case (l, v) => rtFit.addValue(l, IJ.d2s(v, 8))
          }
      }
      rtFit.show("Regression Coefficients")

      showScatterChart(fit.reference, fit.observed, referenceColorSpace().bandsNames, "Reference vs. Observed")
      showScatterChart(fit.reference, fit.corrected, referenceColorSpace().bandsNames, "Reference vs. Corrected")
      showResidualScatterChart(fit.reference, fit.corrected, "Reference vs. Corrected Residual")

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

  def onApplyToCurrentImage(): Unit = busyWorker.doTask("onApplyToCurrentImage") { () =>
    val errorTitle = "Cannot apply Correction"

    // Check that calibration recipe is computed
    val recipe = correctionRecipe().getOrElse {
      showError(errorTitle, "Correction parameters not available.")
      return
    }

    // Get current image
    val imp = IJ.getImage
    if (imp == null) {
      IJ.noImage()
      return
    }


    // Verify that image is of correct type
    if (imp.getType != recipe.imageType) {
      showError(errorTitle, "Image type does not match expected: [" + recipe.imageType + "]")
      return
    }

    // Run calibration on the current image
    val correctedBands = applyCorrection(recipe, imp, showException).getOrElse(return)
    if (showExtraInfo()) {
      val labFPs = referenceColorSpace().toLab(correctedBands)
      val stack = new ImageStack(labFPs(0).getWidth, labFPs(0).getHeight)
      stack.addSlice("L*", labFPs(0))
      stack.addSlice("a*", labFPs(1))
      stack.addSlice("b*", labFPs(2))
      new CompositeImage(new ImagePlus(imp.getShortTitle + "-L*a*b*", stack), CompositeImage.GRAYSCALE).show()
    }
  }

  def onHelp(): Unit = busyWorker.doTask("onHelp") { () =>
    BrowserLauncher.openURL(HelpURL)
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
      val chips = referenceChart().referenceChips
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
