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

package ij_plugins.color.calibration.chart

import ij.gui.Roi
import ij_plugins.color.calibration.point2D
import ij_plugins.color.util.{IJTools, PerspectiveTransform}

import java.awt.Polygon
import java.awt.geom.Point2D
import scala.collection.compat.immutable.ArraySeq

object GridChartFrame {
  /**
    * Create a chart aligned to provided polygon
    *
    * @param nbColumns  number of columns
    * @param nbRows     number of rows
    * @param chipMargin reduction of chip from their maximum size on the grid (as fraction of its width or height).
    *                   Value of the margin must be between 0 and 0.5.
    * @param polygon    chart vertices' locations (requires 4 vertices for each chart corner)
    * @return `GridChartFrame` with alignment transform corresponding to provided polygon
    */
  def apply(nbColumns: Int,
            nbRows: Int,
            chipMargin: Double,
            polygon: Polygon): GridChartFrame = {

    require(polygon != null, "Argument `polygon` cannot be null")
    require(polygon.npoints == 4, s"Argument `polygon` must have 4 points, got ${polygon.npoints}")

    val chart = new GridChartFrame(nbColumns = nbColumns, nbRows = nbRows, chipMargin = chipMargin)

    // Get location of the chart corners from the selected poly-line
    val p0 = new Point2D.Double(polygon.xpoints(0), polygon.ypoints(0))
    val p1 = new Point2D.Double(polygon.xpoints(1), polygon.ypoints(1))
    val p2 = new Point2D.Double(polygon.xpoints(2), polygon.ypoints(2))
    val p3 = new Point2D.Double(polygon.xpoints(3), polygon.ypoints(3))
    val points = Array[Point2D](p0, p1, p2, p3)

    // Create alignment transform
    val alignmentTransform = PerspectiveTransform.quadToQuad(
      chart.referenceOutline.toArray,
      points
    )

    chart.copyWith(alignmentTransform)
  }
}

/**
  * Represents only layout of chips similar to `GridColorChart`:  a regular grid of square chips, arranged in rows and columns.
  *
  * @param nbColumns          number of columns
  * @param nbRows             number of rows
  * @param chipMargin         reduction of chip from their maximum size on the grid (as fraction of its width or height).
  *                           Value of the margin must be between 0 and 0.5.
  * @param alignmentTransform Alignment between the reference chips and chips found in the actual chart.
  */
class GridChartFrame(val nbColumns: Int,
                     val nbRows: Int,
                     val chipMargin: Double,
                     val alignmentTransform: PerspectiveTransform = new PerspectiveTransform()) {

  private val chipOutlines: IndexedSeq[Seq[Point2D]] = {
    for {
      row <- 0 until nbRows;
      column <- 0 until nbColumns
    } yield {
      Seq(
        point2D(column + chipMargin, row + chipMargin),
        point2D(column + 1 - chipMargin, row + chipMargin),
        point2D(column + 1 - chipMargin, row + 1 - chipMargin),
        point2D(column + chipMargin, row + 1 - chipMargin)
      )
    }.toIndexedSeq
  }

  /**
    * Outline of the reference chart as a sequence of 4 corner points: top-left, top-right, bottom-right, bottom-left.
    */
  def referenceOutline: IndexedSeq[Point2D] = {
    ArraySeq(point2D(0, 0), point2D(nbColumns, 0), point2D(nbColumns, nbRows), point2D(0, nbRows))
  }

  /**
    * Creates a copy of this chart with different `chipMargin`. Value of the margin must be between 0 and 0.5.
    **/
  def copyWithNewChipMargin(newChipMargin: Double): GridChartFrame =
    new GridChartFrame(nbColumns, nbRows, newChipMargin, alignmentTransform)

  /**
    * Creates a copy of this chart with different `alignmentTransform`.
    **/
  def copyWith(newAlignmentTransform: PerspectiveTransform): GridChartFrame =
    new GridChartFrame(nbColumns, nbRows, chipMargin, newAlignmentTransform)

  /**
    * Chips ROIs with alignment transform applied to their outline.
    */
  def alignedChipROIs: IndexedSeq[Roi] = {
    for ((oSrc, i) <- chipOutlines.zipWithIndex) yield {
      val oDst = alignmentTransform.transform(oSrc)
      val roi = IJTools.toRoi(oDst)
      roi.setName(s"${i + 1}")
      roi
    }
  }
}