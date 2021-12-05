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

package ij_plugins.color.ui.charttool

import ij.gui.*
import ij.measure.ResultsTable
import ij.plugin.PlugIn
import ij.process.ImageStatistics
import ij.{IJ, ImagePlus}
import ij_plugins.color.calibration.chart.{GridChartFrame, GridChartFrameUtils}
import ij_plugins.color.ui.util.*
import ij_plugins.color.util.{ImageJUtils, PerspectiveTransform}
import scalafx.beans.property.ObjectProperty

import java.awt.event.{WindowAdapter, WindowEvent}
import java.awt.{AWTEvent, Color}
import scala.collection.immutable.ListMap

object ColorChartToolPlugin {

  private val ReferencePrefix = classOf[ColorChartToolPlugin].getName

  object Config {
    def loadFromIJPref(): Option[Config] = {
      for {
        nbColumns            <- IJPrefs.getIntOption(ReferencePrefix + ".nbColumns")
        nbRows               <- IJPrefs.getIntOption(ReferencePrefix + ".nbRows")
        chipMargin           <- IJPrefs.getDoubleOption(ReferencePrefix + ".chipMargin")
        chipOverlayColorName <- IJPrefs.getStringOption(ReferencePrefix + ".chipOverlayColorName")
        sendToROIManager     <- IJPrefs.getBooleanOption(ReferencePrefix + ".sendToROIManager")
        measureChips         <- IJPrefs.getBooleanOption(ReferencePrefix + ".measureChips")
        listChipVertices     <- IJPrefs.getBooleanOption(ReferencePrefix + ".listChipVertices")
      } yield Config(
        nbColumns = nbColumns,
        nbRows = nbRows,
        chipMargin = chipMargin,
        chipOverlayColorName = chipOverlayColorName,
        sendToROIManager = sendToROIManager,
        measureChips = measureChips,
        listChipVertices = listChipVertices
      )
    }
  }

  case class Config(
    nbColumns: Int = 6,
    nbRows: Int = 4,
    chipMargin: Double = 0.2,
    chipOverlayColorName: String = "magenta",
    sendToROIManager: Boolean = true,
    measureChips: Boolean = true,
    listChipVertices: Boolean = false
  ) {
    require(ImageJUIColors.listColorNames.contains(chipOverlayColorName))

    def saveToIJPref(): Unit = {
      IJPrefs.set(ReferencePrefix + ".nbColumns", nbColumns)
      IJPrefs.set(ReferencePrefix + ".nbRows", nbRows)
      IJPrefs.set(ReferencePrefix + ".chipMargin", chipMargin)
      IJPrefs.set(ReferencePrefix + ".chipOverlayColorName", chipOverlayColorName)
      IJPrefs.set(ReferencePrefix + ".sendToROIManager", sendToROIManager)
      IJPrefs.set(ReferencePrefix + ".measureChips", measureChips)
      IJPrefs.set(ReferencePrefix + ".listChipVertices", listChipVertices)
    }

    def chipOverlayColor: Color = ImageJUIColors.colorWithNameAWT(chipOverlayColorName)
  }
}

/**
 * Send tiles of the chart to ROI Manager User indicates chart location by pointing to chart corners.
 */
class ColorChartToolPlugin extends PlugIn with DialogListener with ImageListenerHelper with LiveChartROIHelper {

  import ColorChartToolPlugin.Config

  private val Title = "Color Chart ROI Tool"
  private val Description = "" +
    "Converts color chart ROI to individual chip ROIs.<br>" +
    "Measures color of each chip."

  private var dialog: Option[NonBlockingGenericDialog] = None
  private var config: Config                           = Config.loadFromIJPref().getOrElse(Config())

  private val referenceChartOption = {
    val chart =
      new GridChartFrame(config.nbColumns, config.nbRows, chipMargin = config.chipMargin, new PerspectiveTransform())
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
    setupLiveChartROI(new LiveChartROI(image.get, referenceChartOption))

    liveChartROIOption.foreach { l =>
      l.overlyColor = config.chipOverlayColor
    }

    // Show dialog and wait
    gd.showDialog()

    if (gd.wasOKed()) {
      val chart = chartOption match {
        case Some(chart) => chart
        case None =>
          IJ.error(Title, "Chart is not defined")
          return
      }

      if (config.sendToROIManager) {
        doSendToROIManager(chart)
      }

      if (config.measureChips) {
        doMeasureChips(imp, chart)
      }

      if (config.listChipVertices) {
        doListChipVertices(imp.getTitle, chart)
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
      addChoice("Chip_overlay_color:", ImageJUIColors.listColorNames, config.chipOverlayColorName)
      addMessage("")
      addCheckbox("Send chip ROI to ROI Manager", config.sendToROIManager)
      addCheckbox("Measure chips", config.measureChips)
      addCheckbox("List_chip_vertices", config.listChipVertices)

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
    Option(IJ.getInstance())
      .foreach(ij => gd.setIconImage(ij.getIconImage))

    gd
  }

  override def dialogItemChanged(gd: GenericDialog, e: AWTEvent): Boolean = {
    val nbRows = {
      val v = gd.getNextNumber
      math.max(1, math.round(v).toInt)
    }
    val nbColumns = {
      val v = gd.getNextNumber
      math.max(1, math.round(v).toInt)
    }
    val chipMargin = math.max(0, math.min(0.49, gd.getNextNumber))

    val colorName = gd.getNextChoice

    val sendToROIManager = gd.getNextBoolean
    val measureChips     = gd.getNextBoolean
    val listChipVertices = gd.getNextBoolean

    config = Config(
      nbColumns = nbColumns,
      nbRows = nbRows,
      chipMargin = chipMargin,
      chipOverlayColorName = ImageJUIColors.validNameOr(colorName, Config().chipOverlayColorName),
      sendToROIManager = sendToROIManager,
      measureChips = measureChips,
      listChipVertices = listChipVertices
    )

    config.saveToIJPref()

    referenceChartOption.value =
      Option(new GridChartFrame(config.nbColumns, config.nbRows, config.chipMargin, referenceChart.alignmentTransform))

    liveChartROIOption.foreach { l =>
      l.overlyColor = config.chipOverlayColor
    }

    true
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
    ImageJUtils.addToROIManager(chart.alignedChipROIs)
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

    rt.show(imp.getTitle + " - Chip Measurements")
  }

  private def doListChipVertices(imageTitle: String, chart: GridChartFrame): Unit = {
    val rt = new ResultsTable()

    chart.alignedChipROIs.foreach { roi =>
      rt.incrementCounter()
      rt.addValue("Chip", roi.getName)
      val poly = roi.getPolygon
      for (i <- 0 until poly.npoints) {
        rt.addValue(s"x$i", poly.xpoints(i))
        rt.addValue(s"y$i", poly.ypoints(i))
      }
    }

    rt.show(imageTitle + " - Chip Vertices")
  }

  private def statsToMap(stats: ImageStatistics): ListMap[String, Double] = {
    ListMap(
      "Area"     -> stats.area,
      "Mean"     -> stats.mean,
      "Median"   -> stats.median,
      "Min"      -> stats.min,
      "Max"      -> stats.max,
      "StdDev"   -> stats.stdDev,
      "Kurtosis" -> stats.kurtosis,
      "Skewness" -> stats.skewness
    )
  }
}
