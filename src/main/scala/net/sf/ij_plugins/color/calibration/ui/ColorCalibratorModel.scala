/*
 * Image/J Plugins
 * Copyright (C) 2002-2017 Jarek Sacha
 * Author's email: jsacha at users dot sourceforge dot net
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

import java.awt.{BasicStroke, Color, Polygon}
import java.io.{PrintWriter, StringWriter}
import javafx.scene.{chart => jfxsc}

import ij.gui.{PolygonRoi, Roi}
import ij.measure.ResultsTable
import ij.process.{ColorProcessor, FloatProcessor}
import ij.{IJ, ImagePlus, ImageStack}
import net.sf.ij_plugins.color.calibration.chart.{ColorCharts, ReferenceColorSpace}
import net.sf.ij_plugins.color.calibration.regression.MappingMethod
import net.sf.ij_plugins.color.calibration.{ColorCalibrator, Corrector, LOOCrossValidation, toPolygonROI}
import net.sf.ij_plugins.color.converter.ColorConverter
import net.sf.ij_plugins.color.converter.ColorTriple.Lab
import net.sf.ij_plugins.color.{ColorFXUI, DeltaE}
import net.sf.ij_plugins.util._
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics

import scalafx.Includes._
import scalafx.beans.property._
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Point2D
import scalafx.scene.Scene
import scalafx.scene.chart._
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.{Alert, Label, TextArea}
import scalafx.scene.layout.{GridPane, Priority, StackPane}
import scalafx.stage.Stage


/** Model for color calibrator UI. */
class ColorCalibratorModel(val image: ImagePlus, parentStage: Stage) {

  require(parentStage != null, "Argument `parentStage` cannot be null.")

  val imageTitle = new StringProperty(this, "imageTitle", image.getTitle)
  val referenceColorSpace = new ObjectProperty[ReferenceColorSpace](this, "referenceColorSpace", ReferenceColorSpace.sRGB)
  val referenceChart = new ObjectProperty(this, "chart", ColorCharts.GretagMacbethColorChecker)
  val chipMarginPercent = new ObjectProperty[Integer](this, "chipMargin", 20)
  val mappingMethod = new ObjectProperty(this, "mappingMethod", MappingMethod.LinearCrossBand)
  val clipReferenceRGB = new BooleanProperty(this, "clipReferenceRGB", true)
  val showExtraInfo = new BooleanProperty(this, "showExtraInfo", false)

  private val chipValuesObservedWrapper = new ReadOnlyBooleanWrapper(this, "chipValuesObserved", false)
  val chipValuesObserved = chipValuesObservedWrapper.getReadOnlyProperty

  private def chipMargin: Double = chipMarginPercent() / 100d
  private def currentChart = referenceChart().copyWithNewChipMargin(chipMargin)


  def onRenderReferenceChart(): Unit = {
    val scale = 80
    val margin = 0.1 * scale

    val chart = referenceChart().copyWithNewChipMargin(0.1)
    val maxX = chart.referenceOutline.map(_.x).max * scale
    val maxY = chart.referenceOutline.map(_.y).max * scale
    val width: Int = (maxX + 2 * margin).toInt
    val height: Int = (maxY + 2 * margin).toInt
    val cp = new ColorProcessor(width, height)
    cp.setColor(Color.BLACK)
    cp.fill()
    val converter = chart.colorConverter
    for (chip <- chart.referenceChips) {
      // ROI
      val outline = chip.outline
      val xPoints = outline.map(p => (margin + p.x * scale).toInt).toArray
      val yPoints = outline.map(p => (margin + p.y * scale).toInt).toArray
      val roi = new PolygonRoi(new Polygon(xPoints, yPoints, xPoints.length), Roi.POLYGON)
      cp.setRoi(roi)
      // Color
      val color = {
        val rgb = converter.toRGB(converter.toXYZ(chip.color))
        new Color(clipUInt8(rgb.r), clipUInt8(rgb.g), clipUInt8(rgb.b))
      }
      cp.setColor(color)
      cp.fill()
    }

    val imp = new ImagePlus(chart.name, cp)
    imp.setRoi(
      new PolygonRoi(
        new Polygon(
          Array(margin.toInt, (maxX + margin).toInt, (maxX + margin).toInt, margin.toInt),
          Array(margin.toInt, margin.toInt, (maxY + margin).toInt, (maxY + margin).toInt),
          4
        ), Roi.POLYGON
      )
    )
    imp.show()
  }

  def onShowReferenceColors(): Unit = {
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


  def onLoadLocationFromROI(): Unit = {

    // Load ROI
    val points = loadROI()
    if (points.isEmpty) return

    // Create alignment transform
    referenceChart().alignmentTransform = PerspectiveTransform.quadToQuad(
      referenceChart().referenceOutline.toArray,
      points.get.toArray
    )

    // Display chart overlay
    val shape = toShape(currentChart.alignedChips)
    image.setOverlay(shape, Color.MAGENTA, new BasicStroke(2))

    chipValuesObservedWrapper.set(true)
  }

  def resetROI(): Unit = {
    // Create alignment transform
    referenceChart().alignmentTransform = new PerspectiveTransform()
    image.setOverlay(null)
    chipValuesObservedWrapper.set(false)
  }

  def onSuggestCalibrationOptions(): Unit = {
    val chart = currentChart

    val methods = MappingMethod.values.toList
    val crossValidations = for (rcs <- ReferenceColorSpace.values; method <- methods) yield {
      IJ.showStatus("Checking " + rcs + " + " + method)
      val stats = new DescriptiveStatistics()
      val deltas = LOOCrossValidation.crossValidation(chart, rcs, method, image)
      deltas.foreach(v => stats.addValue(v))
      val mean = deltas.sum / deltas.length
      println("" + rcs + ": " + method + "mean =" + mean + ", median = " +
        stats.getPercentile(50) + ",  min = " + deltas.min + ", max =" + deltas.max)
      (rcs, method, mean, deltas.min, deltas.max)
    }

    val best = crossValidations.minBy(_._3)
    IJ.showStatus("Best: " + best._1 + ":" + best._2 + " -> " + best._3)
    println("Best: " + best._1 + ":" + best._2 + " -> " + best._3)
    println("Max : " + crossValidations.map(_._3).max)

    // Show chart with comparison of results
    // TODO show chart with error bars
    val hSorted = crossValidations.toArray.sortBy(-_._3)

    createBarChart()

    def createBarChart(): Unit = {
      val categories = hSorted.map(m => m._1 + " + " + m._2).toSeq
      val values = hSorted.map(_._3).toSeq

      val yAxis = CategoryAxis(ObservableBuffer(categories))
      val xAxis = NumberAxis("Mean Delta E (smaller is better)")

      def xyData(xs: Seq[Double]) = ObservableBuffer(
        xs zip categories map (xy => XYChart.Data[Number, String](xy._1, xy._2))
      )

      val series1 = XYChart.Series("Deltas", xyData(values))

      val chart = new BarChart[Number, String](xAxis, yAxis) {
        data = series1
        legendVisible = false
      }

      ColorFXUI.showInNewWindow(chart, "Cross-Validation: " + imageTitle())
    }

  }

  def onCalibrate(): Unit = {

    // Compute color mapping coefficients
    val chart = currentChart
    val colorCalibrator = new ColorCalibrator(chart, referenceColorSpace(), mappingMethod())

    val fit = try {
      colorCalibrator.computeCalibrationMapping(image)
    } catch {
      case t: Throwable =>
        showError("Error while computing color calibration.", t.getMessage, t)
        return
    }
    val corrector = new Corrector(fit.mapping)

    val correctedBands = try {
      corrector.map(image)
    } catch {
      case t: Throwable =>
        showError("Error while color correcting the image.", t.getMessage, t)
        return
    }

    // Show floating point stack in the reference color space
    val correctedInReference = {
      val stack = new ImageStack(image.getWidth, image.getHeight)
      (colorCalibrator.referenceColorSpace.bands zip correctedBands).foreach(v => stack.addSlice(v._1, v._2))
      new ImagePlus(image.getTitle + "+corrected_" + referenceColorSpace(), stack)
    }
    correctedInReference.show()

    // Convert corrected image to sRGB
    val correctedImage: ImagePlus = convertToSRGB(correctedBands, referenceColorSpace(), colorCalibrator.chart.colorConverter)
    correctedImage.setTitle(image.getTitle + "+corrected_" + referenceColorSpace() + "+sRGB")
    correctedImage.show()

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

      showScatterChart(fit.reference, fit.observed, "Reference vs. Observed")
      showScatterChart(fit.reference, fit.corrected, "Reference vs. Corrected")
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
        new ImagePlus("L*a*b*", stack).show()
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
          rt.addLabel("Color Name", chip.name)
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

  private def convertToSRGB(bands: Array[FloatProcessor],
                            colorSpace: ReferenceColorSpace,
                            converter: ColorConverter): ImagePlus = {
    colorSpace match {
      case ReferenceColorSpace.sRGB => new ImagePlus("", IJTools.mergeRGB(bands))
      case ReferenceColorSpace.XYZ =>
        // Convert XYZ to sRGB
        val cp = new ColorProcessor(bands(0).getWidth, bands(0).getHeight)
        val n = bands(0).getWidth * bands(0).getHeight
        for (i <- (0 until n).par) {
          val rgb = converter.xyzToRGB(bands(0).getf(i), bands(1).getf(i), bands(2).getf(i))
          val r = clipUInt8(rgb.r)
          val g = clipUInt8(rgb.g)
          val b = clipUInt8(rgb.b)
          val color = ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | ((b & 0xFF) << 0)
          cp.set(i, color)
        }
        new ImagePlus("", cp)
      case _ => throw new IllegalArgumentException("Unsupported reference color space '" + colorSpace + "'.")
    }
  }


  private def showScatterChart(x: Array[Array[Double]], y: Array[Array[Double]], chartTitle: String): Unit = {
    // Create plot
    val xAxis = new NumberAxis()
    val yAxis = new NumberAxis()
    val scatterChart = ScatterChart(xAxis, yAxis)
    scatterChart.data = {
      val answer = new ObservableBuffer[jfxsc.XYChart.Series[Number, Number]]()
      val bands = (0 to 2).map {
        b =>
          new XYChart.Series[Number, Number] {
            name = "Band " + b
          }
      }
      val chips = referenceChart().referenceChips
      for (i <- chips.indices; b <- 0 to 2) {
        bands(b).data.get += XYChart.Data[Number, Number](x(i)(b), y(i)(b))
      }
      bands.foreach(answer.add(_))
      answer
    }
    val dialogStage = new Stage() {
      title = chartTitle
      scene = new Scene {
        root = new StackPane {
          children = scatterChart
          stylesheets ++= ColorFXUI.stylesheets
        }
      }
    }
    dialogStage.show()
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
    showScatterChart(x, dy, chartTitle)
  }


  private def loadROI(): Option[Seq[Point2D]] = {
    // Validate ROI selection
    if (image == null) {
      showError("No input image available", "Input image needed for ROI selection.")
      return None
    }

    val polygon = image.getRoi match {
      case polyline: PolygonRoi => polyline.getPolygon.npoints match {
        case 4 => polyline.getPolygon
        case n =>
          showError("Not a valid ROI", "Expecting polygonal selection with 4 points, got " + n + " points.")
          return None
      }
      case _ =>
        showError("Not a valid ROI", "Polygon or Segmented Line selection required.")
        return None
    }

    // Get location of the chart corners from the selected poly-line
    val p0 = new Point2D(polygon.xpoints(0), polygon.ypoints(0))
    val p1 = new Point2D(polygon.xpoints(1), polygon.ypoints(1))
    val p2 = new Point2D(polygon.xpoints(2), polygon.ypoints(2))
    val p3 = new Point2D(polygon.xpoints(3), polygon.ypoints(3))
    Some(List(p0, p1, p2, p3))
  }

  private def showError(summary: String, message: String, t: Throwable): Unit = {
    // Extract exception text
    val exceptionText = {
      val sw = new StringWriter()
      val pw = new PrintWriter(sw)
      t.printStackTrace(pw)
      sw.toString
    }
    val label = new Label("The exception stacktrace was:")
    val textArea = new TextArea {
      text = exceptionText
      editable = false
      wrapText = true
      maxWidth = Double.MaxValue
      maxHeight = Double.MaxValue
      vgrow = Priority.Always
      hgrow = Priority.Always
    }
    val expContent = new GridPane {
      maxWidth = Double.MaxValue
      add(label, 0, 0)
      add(textArea, 0, 1)
    }

    new Alert(AlertType.Error) {
      initOwner(parentStage)
      title = "Error"
      headerText = summary
      contentText = message
      // Set expandable Exception into the dialog pane.
      dialogPane().expandableContent = expContent
    }.showAndWait()
  }

  private def showError(summary: String, message: String): Unit = {
    new Alert(AlertType.Error) {
      initOwner(parentStage)
      title = "Error"
      headerText = summary
      contentText = message
    }.showAndWait()
  }

}
