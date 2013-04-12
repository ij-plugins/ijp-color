/*
 * Image/J Plugins
 * Copyright (C) 2002-2013 Jarek Sacha
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

import ij.gui.{Roi, PolygonRoi}
import ij.measure.ResultsTable
import ij.process.ColorProcessor
import ij.{ImageStack, IJ, ImagePlus}
import java.awt.{BasicStroke, Polygon, Color}
import javafx.beans.property.ReadOnlyBooleanWrapper
import javafx.scene.control.Dialogs
import javafx.scene.{chart => jfxsc}
import net.sf.ij_plugins.color.ColorFXUI
import net.sf.ij_plugins.color.calibration.chart.{ReferenceColorSpace, ColorCharts}
import net.sf.ij_plugins.color.calibration.{ColorCalibrator, MappingMethod}
import net.sf.ij_plugins.util._
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import scalafx.Includes._
import scalafx.beans.property._
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Point2D
import scalafx.scene.Scene
import scalafx.scene.chart.{ScatterChart, NumberAxis, XYChart}
import scalafx.scene.layout.StackPane
import scalafx.stage.Stage


/** Model for color calibrator UI. */
class ColorCalibratorModel(val image: ImagePlus, parentStage: Stage) {

  require(parentStage != null, "Argument `parentStage` cannot be null.")

  val imageTitle          = new StringProperty(this, "imageTitle", image.getTitle)
  val referenceColorSpace = new ObjectProperty(this, "referenceColorSpace", ReferenceColorSpace.sRGB)
  val chart               = new ObjectProperty(this, "chart", ColorCharts.GretagMacbethColorChecker)
  val chipMarginPercent   = new ObjectProperty[Int](this, "chipMargin", 20)
  val mappingMethod       = new ObjectProperty(this, "mappingMethod", MappingMethod.LinearCrossBand)
  val clipReferenceRGB    = new BooleanProperty(this, "clipReferenceRGB", true)

  private val chipValuesObservedWrapper = new ReadOnlyBooleanWrapper(this, "chipValuesObserved", false)
  val chipValuesObserved = chipValuesObservedWrapper.getReadOnlyProperty


  def onRenderReferenceChart() {
    val scale = 80
    val margin = 0.1 * scale

    val maxX = chart().referenceOutline().map(_.x).max * scale
    val maxY = chart().referenceOutline().map(_.y).max * scale
    val width: Int = (maxX + 2 * margin).toInt
    val height: Int = (maxY + 2 * margin).toInt
    val cp = new ColorProcessor(width, height)
    cp.setColor(Color.BLACK)
    cp.fill()
    val converter = chart().colorConverter
    for (chip <- chart().referenceChips(0.1)) {
      // ROI
      val outline = chip.outline
      val xPoints = outline.map(p => (margin + p.x * scale).toInt).toArray
      val yPoints = outline.map(p => (margin + p.y * scale).toInt).toArray
      val roi = new PolygonRoi(new Polygon(xPoints, yPoints, xPoints.length), Roi.POLYGON)
      cp.setRoi(roi)
      // Color
      val color = {
        val rgb = converter.xyz2RGB(converter.lab2XYZ(chip.color))
        new Color(clipUInt8(rgb.r), clipUInt8(rgb.g), clipUInt8(rgb.b))
      }
      cp.setColor(color)
      cp.fill()
    }

    val imp = new ImagePlus(chart().name, cp)
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

  def onShowReferenceColors() {
    val rt = new ResultsTable()
    val chips = chart().referenceChips(0)
    for (i <- 0 until chips.length) {
      rt.incrementCounter()
      rt.setLabel(chips(i).name, i)
      val lab = chips(i).color
      val xyz = chart().colorConverter.lab2XYZ(lab)
      val rgb = chart().colorConverter.xyz2RGB(xyz)
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
    rt.show(chart().name + " / " + chart().refWhite)
  }


  def onLoadLocationFromROI() {

    // Load ROI
    val points = loadROI()
    if (points.isEmpty) return

    // Create alignment transform
    chart().alignmentTransform = PerspectiveTransform.quadToQuad(
      chart().referenceOutline().toArray,
      points.get.toArray
    )

    // Display chart overlay
    val shape = toShape(chart().alignedChips(chipMarginPercent() / 100d))
    image.setOverlay(shape, Color.MAGENTA, new BasicStroke(2))

    chipValuesObservedWrapper.set(true)
  }

  def resetROI() {
    // Create alignment transform
    chart().alignmentTransform = new PerspectiveTransform()
    image.setOverlay(null)
    chipValuesObservedWrapper.set(false)
  }

  def onCalibrate() {

    // Compute color mapping coefficients
    val corrector = new ColorCalibrator(chart(), referenceColorSpace(), mappingMethod())

    val fit = try {
      corrector.computeCalibrationMapping(chipMarginPercent() / 100d, image)
    } catch {
      case t: Throwable => {
        showError("Error while computing color calibration.", t.getMessage, t)
        return
      }
    }

    val correctedBands = try {
      corrector.map(image)
    } catch {
      case t: Throwable => {
        showError("Error while color correcting the image.", t.getMessage, t)
        return
      }
    }

    // Show floating point stack in the reference color space
    val correctedInReference = {
      val stack = new ImageStack(image.getWidth, image.getHeight)
      (corrector.referenceColorSpace.bands zip correctedBands).foreach(v => stack.addSlice(v._1, v._2))
      new ImagePlus(image.getTitle + "+corrected_" + referenceColorSpace(), stack)
    }
    correctedInReference.show()

    // Convert corrected image to sRGB
    val correctedImage: ImagePlus = referenceColorSpace() match {
      case ReferenceColorSpace.sRGB => new ImagePlus("", IJTools.mergeRGB(correctedBands))
      case ReferenceColorSpace.XYZ => {
        // Convert XYZ to sRGB
        val cp = new ColorProcessor(correctedBands(0).getWidth, correctedBands(0).getHeight)
        val converter = corrector.chart.colorConverter
        val n = correctedBands(0).getWidth * correctedBands(0).getHeight
        for (i <- (0 until n).par) {
          val rgb = converter.xyz2RGB(correctedBands(0).getf(i), correctedBands(1).getf(i), correctedBands(2).getf(i))
          val r = clipUInt8(rgb.r)
          val g = clipUInt8(rgb.g)
          val b = clipUInt8(rgb.b)
          val color = ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | ((b & 0xFF) << 0)
          cp.set(i, color)
        }
        new ImagePlus("", cp)
      }
      case _ => throw new IllegalArgumentException("Unsupported reference color space '" + referenceColorSpace() + "'.")
    }
    correctedImage.setTitle(image.getTitle + "+corrected_" + referenceColorSpace() + "+sRGB")
    correctedImage.show()

    if (IJ.debugMode) {
      // Show table with expected measured and corrected values
      val rtColor = new ResultsTable()
      val chips = chart().referenceChips(0)
      for (i <- 0 until chips.length) {
        rtColor.incrementCounter()
        rtColor.setLabel(chips(i).name, i)
        rtColor.setValue("Reference 0", i, fit.reference(i)(0))
        rtColor.setValue("Reference 1", i, fit.reference(i)(1))
        rtColor.setValue("Reference 2", i, fit.reference(i)(2))
        rtColor.setValue("Observed 0", i, fit.observed(i)(0))
        rtColor.setValue("Observed 1", i, fit.observed(i)(1))
        rtColor.setValue("Observed 2", i, fit.observed(i)(2))
        rtColor.setValue("Corrected 0", i, fit.corrected(i)(0))
        rtColor.setValue("Corrected 1", i, fit.corrected(i)(1))
        rtColor.setValue("Corrected 2", i, fit.corrected(i)(2))
      }
      rtColor.show("Color Values")

      // Show table with regression results
      val rtFit = new ResultsTable()
      List(
        ("Band 1", fit.mapping.band1),
        ("Band 2", fit.mapping.band2),
        ("Band 3", fit.mapping.band3)
      ).foreach {
        case (name, b) => {
          rtFit.incrementCounter()
          rtFit.addLabel(name)
          rtFit.addValue("R Squared", b.regressionResult.get.rSquared)
          b.toMap.foreach {
            case (l, v) => rtFit.addValue(l, v)
          }
        }
      }
      rtFit.show("Regression Coefficients")

      showScatterChart(fit.reference, fit.observed, "Reference vs. Observed")
      showScatterChart(fit.reference, fit.corrected, "Reference vs. Corrected")

      // Delta E
      val deltaE = {
        val chips = chart().referenceChips(0)
        //      val before = new DescriptiveStatistics()
        val after = new DescriptiveStatistics()
        for (i <- 0 until chips.length) {
          //        before.addValue(ColorUtils.delta(fit.reference(i), fit.observed(i)))
          after.addValue(delta(fit.reference(i), fit.corrected(i)))
        }
        after
      }
      IJ.log("" +
          "Mean color chip delta after calibration:\n" +
          "  mean   = " + deltaE.getMean + "\n" +
          "  min    = " + deltaE.getMin + "\n" +
          "  max    = " + deltaE.getMax + "\n" +
          "  median = " + deltaE.getPercentile(.5) + "\n" +
          "\n"
      )
    }
  }

  private def showScatterChart(x: Array[Array[Double]], y: Array[Array[Double]], chartTitle: String) {
    // Create plot
    val xAxis = new NumberAxis()
    val yAxis = new NumberAxis()
    val scatterChart = ScatterChart(xAxis, yAxis)
    scatterChart.data = {
      val answer = new ObservableBuffer[jfxsc.XYChart.Series[Number, Number]]()
      val bands = (0 to 2).map {
        b => new XYChart.Series[Number, Number] {
          name = "Band " + b
        }
      }
      val chips = chart().referenceChips(0)
      for (i <- 0 until chips.length; b <- 0 to 2) {
        bands(b).data.get += XYChart.Data[Number, Number](x(i)(b), y(i)(b))
      }
      bands.foreach(answer.add(_))
      answer
    }
    val dialogStage = new Stage() {
      title = chartTitle
      scene = new Scene {
        root = new StackPane {
          content = scatterChart
          stylesheets ++= ColorFXUI.stylesheets
        }
      }
    }
    dialogStage.show
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
        case n => {
          showError("Not a valid ROI", "Expecting polygonal selection with 4 points, got " + n + " points.")
          return None
        }
      }
      case _ => {
        showError("Not a valid ROI", "Polygon or Segmented Line selection required.")
        return None
      }
    }

    // Get location of the chart corners from the selected poly-line
    val p0 = new Point2D(polygon.xpoints(0), polygon.ypoints(0))
    val p1 = new Point2D(polygon.xpoints(1), polygon.ypoints(1))
    val p2 = new Point2D(polygon.xpoints(2), polygon.ypoints(2))
    val p3 = new Point2D(polygon.xpoints(3), polygon.ypoints(3))
    Some(List(p0, p1, p2, p3))
  }

  private def showError(summary: String, message: String, t: Throwable) {
    if (t != null) t.printStackTrace()
    Dialogs.showErrorDialog(parentStage, message, summary, "Error", t)
  }

  private def showError(summary: String, message: String) {
    showError(summary, message, null)
  }

}
