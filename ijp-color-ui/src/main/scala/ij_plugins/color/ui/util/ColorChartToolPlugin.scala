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

package ij_plugins.color.ui.util

import ij.gui._
import ij.measure.ResultsTable
import ij.plugin.PlugIn
import ij.process.ImageStatistics
import ij.{IJ, ImagePlus}
import ij_plugins.color.calibration.chart.{GridChartFrame, GridChartFrameUtils}
import ij_plugins.color.util.{IJTools, PerspectiveTransform}
import scalafx.beans.property.ObjectProperty

import java.awt.AWTEvent
import java.awt.event.{WindowAdapter, WindowEvent}
import scala.collection.immutable.ListMap

object ColorChartToolPlugin {

  private var sendToROIManager: Boolean = true
  private var measureChips: Boolean = true
}

/**
  * Send tiles of the chart to ROI Manager
  * User indicates chart location by pointing to chart corners.
  */
class ColorChartToolPlugin
  extends PlugIn
    with DialogListener
    with ImageListenerHelper
    with LiveChartROIHelper {

  import ColorChartToolPlugin._

  private val Title = "Color Chart ROI Tool"
  private val Description = "" +
    "Converts color chart ROI to individual chip ROIs.<br>" +
    "Measures color of each chip."

  private var dialog: Option[NonBlockingGenericDialog] = None

  private val referenceChartOption = {
    val chart = new GridChartFrame(6, 4, chipMargin = 0.1, new PerspectiveTransform())
    new ObjectProperty(this, "chart", Option(chart))
  }

  private def referenceChart: GridChartFrame = referenceChartOption.value.get

  override def run(arg: String): Unit = {

    image = Option(IJ.getImage)
    val imp = image match {
      case Some(v) => v
      case None =>
        IJ.noImage()
        return
    }

    val gd = createDialog()
    dialog = Option(gd)

    setupImageListener()
    setupROIListener(new LiveChartROI(image.get, referenceChartOption))

    // Show dialog and wait
    gd.showDialog()

    if (gd.wasOKed()) {
      val chart = chartOption match {
        case Some(chart) => chart
        case None =>
          IJ.error(Title, "Chart is not defined")
          return
      }

      if (sendToROIManager) {
        doSendToROIManager(chart)
      }

      if (measureChips) {
        doMeasureChips(imp, chart)
      }
    }
  }

  private def createDialog(): NonBlockingGenericDialog = {
    val gd = new NonBlockingGenericDialog(Title) {
      addPanel(IJPUtils.createInfoPanel(Title, Description))
      addMessage("Chart Layout")
      addNumericField("Rows", referenceChart.nbRows, 0, 3, "")
      addNumericField("Columns", referenceChart.nbColumns, 0, 3, "")
      addSlider("Chip margin", 0, 0.49, referenceChart.chipMargin, 0.01)
      addMessage("")
      addCheckbox("Send chip ROI to ROI Manager", sendToROIManager)
      addCheckbox("Measure chips", measureChips)
      addHelp("https://github.com/ij-plugins/ijp-color/wiki/Color-Chart-ROI-Tool")
    }
    gd.addDialogListener(this)
    gd.addWindowListener(new WindowAdapter {
      override def windowClosed(e: WindowEvent): Unit = {
        super.windowClosed(e)
        removeImageListener()
        removeROIListener()
        // TODO Cleanup
      }
    })

    // Set dialog icon that is not set in NonBlockingGenericDialog by default
    Option(IJ.getInstance()).foreach(ij => gd.setIconImage(ij.getIconImage))

    gd
  }

  override def dialogItemChanged(gd: GenericDialog, e: AWTEvent): Boolean = {
    val nbRows = {
      val v = gd.getNextNumber
      math.max(1, math.round(v).toInt)
    }
    val nbCols = {
      val v = gd.getNextNumber
      math.max(1, math.round(v).toInt)
    }
    val chipMargin = math.max(0, math.min(0.49, gd.getNextNumber))
    referenceChartOption.value = Some(new GridChartFrame(nbCols, nbRows, chipMargin, referenceChart.alignmentTransform))

    sendToROIManager = gd.getNextBoolean
    measureChips = gd.getNextBoolean

    chartOption.nonEmpty
  }


  override protected def handleImageUpdated(): Unit = {
    // TODO: Review correctness, this code seems to do nothing
    liveChartROIOption.foreach(v => v.locatedChart)
  }

  override protected def handleImageClosed(): Unit = {
    // Image was closed, so close the dialog associated with that image
    dialog.foreach { d =>
      d.setVisible(false)
      d.dispose()
    }
    dialog = None
  }

  private def chartOption: Option[GridChartFrame] = {
    liveChartROIOption.flatMap(_.locatedChart.value)
  }

  private def doSendToROIManager(chart: GridChartFrame): Unit = {
    IJTools.addToROIManager(chart.alignedChipROIs)
  }

  private def doMeasureChips(imp: ImagePlus, chart: GridChartFrame): Unit = {

    val roiBandStats = GridChartFrameUtils.measureRois(imp, chart)

    val rt = new ResultsTable()

    for ((roi, bandStats) <- roiBandStats) {
      for ((bandName, bandStats) <- bandStats) {
        rt.incrementCounter()

        rt.addValue("Chip", roi.getName)
        rt.addValue("Band", bandName)

        val measurements = statsToMap(bandStats)
        measurements.foreach { case (label, value) => rt.addValue(label, value) }
      }
    }

    rt.show("Chip Measurements")
  }

  private def statsToMap(stats: ImageStatistics): ListMap[String, Double] = {
    ListMap(
      "Area" -> stats.area,
      "Mean" -> stats.mean,
      "Median" -> stats.median,
      "Min" -> stats.min,
      "Max" -> stats.max,
      "StdDev" -> stats.stdDev,
      "Kurtosis" -> stats.kurtosis,
      "Skewness" -> stats.skewness,
    )
  }
}
