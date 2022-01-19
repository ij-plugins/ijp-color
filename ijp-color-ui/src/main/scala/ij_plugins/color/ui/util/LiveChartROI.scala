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

import ij.ImagePlus
import ij.gui.{Overlay, Roi, RoiListener}
import ij_plugins.color.calibration.chart.ChartFrame
import ij_plugins.color.calibration.chart.ChartFrameUtils.toOutline
import ij_plugins.color.util.PerspectiveTransform
import scalafx.beans.property.*

import java.awt.Color

/**
 * @param imp
 *   Image which ROI is observed
 */
class LiveChartROI[T <: ChartFrame](imp: ImagePlus, referenceChart: ReadOnlyObjectProperty[Option[T]])
    extends RoiListener {

  private val overlyColorProperty = new ObjectProperty(this, "overlyColorProperty", new Color(255, 0, 255, 128))

  private val _status                = new ReadOnlyStringWrapper()
  val status: ReadOnlyStringProperty = _status.readOnlyProperty

  private val _locatedChart                           = new ReadOnlyObjectWrapper[Option[T]](this, "locatedChart", None)
  val locatedChart: ReadOnlyObjectProperty[Option[T]] = _locatedChart.readOnlyProperty

  referenceChart.onChange((_, _, _) => updateChartLocation())
  overlyColorProperty.onChange((_, _, _) => updateOverlay())

  updateChartLocation()

  override def roiModified(imp: ImagePlus, id: Int): Unit = {
    if (imp != this.imp) return

    updateChartLocation()
  }

  def overlyColor: Color = overlyColorProperty.value

  def overlyColor_=(v: Color): Unit = {
    overlyColorProperty.value = v
  }

  /**
   * Check ROI in the current image and if valid update locatedChart. If invalid remove located chart.
   */
  private def updateChartLocation(): Unit = {

    val roi = imp.getRoi

    referenceChart() match {
      case Some(refChart) if roi != null && roi.getType == Roi.POLYGON && roi.getPolygon.npoints == 4 =>
        val points = toOutline(roi.getFloatPolygon)

        // Create alignment transform
        val alignmentTransform = PerspectiveTransform.quadToQuad(refChart.referenceOutline.toArray, points)

        // Display chart overlay
        // TODO: Remove cast below (.asInstanceOf[T])
        val currentChart = refChart.copyWith(alignmentTransform).asInstanceOf[T]
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
