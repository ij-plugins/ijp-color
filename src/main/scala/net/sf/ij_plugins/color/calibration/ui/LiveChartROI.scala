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

import java.awt.{BasicStroke, Color}

import ij.ImagePlus
import ij.gui.{Roi, RoiListener}
import net.sf.ij_plugins.color.calibration.chart.GridColorChart
import net.sf.ij_plugins.util.PerspectiveTransform
import scalafx.beans.property._
import scalafx.geometry.Point2D

/**
  *
  * @param imp Image which ROI is observed
  */
class LiveChartROI(imp: ImagePlus,
                   referenceChart: ObjectProperty[Option[GridColorChart]],
                   chipMarginPercent: ObjectProperty[Integer])
  extends RoiListener {

  // TODO: Handle external closing of imp

  private val color = new Color(255, 0, 255, 128)

  private val _status = new ReadOnlyStringWrapper()
  val status: ReadOnlyStringProperty = _status.readOnlyProperty

  private val _locatedChart = new ReadOnlyObjectWrapper[Option[GridColorChart]](this, "locatedChart", None)
  val locatedChart: ReadOnlyObjectProperty[Option[GridColorChart]] = _locatedChart.readOnlyProperty

  referenceChart.onChange((_, _, _) => updateChartLocation())
  chipMarginPercent.onChange((_, _, _) => updateChartLocation())

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
          points
        )

        // Display chart overlay
        val currentChart = refChart.
          copyWithNewChipMargin(chipMarginPercent() / 100.0).
          copyWith(alignmentTransform)
        _locatedChart() = Option(currentChart)
      case _ =>
        _locatedChart() = None
    }

    updateOverlay()
  }


  private def updateOverlay(): Unit = {
    locatedChart() match {
      case Some(chart) =>
        val shape = toShape(chart.alignedChips)
        imp.setOverlay(shape, color, new BasicStroke(1))
      case None =>
        imp.setOverlay(null)
        imp.setHideOverlay(true)
    }
  }
}
