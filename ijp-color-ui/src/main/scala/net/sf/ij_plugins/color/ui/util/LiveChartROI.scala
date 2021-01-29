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

package net.sf.ij_plugins.color.ui.util

import ij.ImagePlus
import ij.gui.{Overlay, Roi, RoiListener}
import net.sf.ij_plugins.color.calibration.chart.GridChartFrame
import net.sf.ij_plugins.color.ui.fx.toAWT
import net.sf.ij_plugins.color.util.PerspectiveTransform
import scalafx.beans.property._
import scalafx.geometry.Point2D

import java.awt.Color

object LiveChartROI {
  def apply[T <: GridChartFrame](imp: ImagePlus,
                                 referenceChart: ObjectProperty[Option[T]]): LiveChartROI = {
    // This a hack so we can pass `referenceChart` argument to `LiveChartROI` constructor without
    // compiler complaining about incorrect types. There may be some smarted way to deal with this. Suggestions welcomed.
    val _referenceChartFrameOption =
    new ObjectProperty[Option[GridChartFrame]](this, "", referenceChart())
    _referenceChartFrameOption <== referenceChart
    new LiveChartROI(imp, _referenceChartFrameOption)
  }
}

/**
  *
  * @param imp Image which ROI is observed
  */
class LiveChartROI(imp: ImagePlus,
                   referenceChart: ObjectProperty[Option[GridChartFrame]])
  extends RoiListener {

  private val overlyColor = new Color(255, 0, 255, 128)

  private val _status = new ReadOnlyStringWrapper()
  val status: ReadOnlyStringProperty = _status.readOnlyProperty

  private val _locatedChart = new ReadOnlyObjectWrapper[Option[GridChartFrame]](this, "locatedChart", None)
  val locatedChart: ReadOnlyObjectProperty[Option[GridChartFrame]] = _locatedChart.readOnlyProperty

  referenceChart.onChange((_, _, _) => updateChartLocation())

  updateChartLocation()

  override def roiModified(imp: ImagePlus, id: Int): Unit = {
    if (imp != this.imp) return

    updateChartLocation()
  }

  /**
    * Check ROI in the current image and if valid update locatedChart. If invalid remove located chart.
    */
  private def updateChartLocation(): Unit = {

    val roi = imp.getRoi

    referenceChart() match {
      case Some(refChart) if roi != null && roi.getType == Roi.POLYGON && roi.getPolygon.npoints == 4 =>
        val polygon = roi.getPolygon
        // Get location of the chart corners from the selected poly-line
        val p0 = new Point2D(polygon.xpoints(0), polygon.ypoints(0))
        val p1 = new Point2D(polygon.xpoints(1), polygon.ypoints(1))
        val p2 = new Point2D(polygon.xpoints(2), polygon.ypoints(2))
        val p3 = new Point2D(polygon.xpoints(3), polygon.ypoints(3))
        val points = Array(p0, p1, p2, p3)

        // Create alignment transform
        val alignmentTransform = PerspectiveTransform.quadToQuad(
          refChart.referenceOutline.toArray,
          points.map(toAWT)
        )

        // Display chart overlay
        val currentChart = refChart.copyWith(alignmentTransform)
        _locatedChart() = Option(currentChart)
      case _ =>
        _locatedChart() = None
    }

    updateOverlay()
  }


  /**
    * Update overlay displayed on ImagePlus
    */
  def updateOverlay(): Unit = {
    locatedChart() match {
      case Some(chart) =>
        val o = new Overlay()
        chart.alignedChipROIs.foreach(o.add)
        o.setStrokeColor(overlyColor)
        o.setStrokeWidth(1d)
        imp.setOverlay(o)
      case None =>
        imp.setOverlay(null)
        imp.setHideOverlay(true)
    }
  }
}
