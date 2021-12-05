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
import ij_plugins.color.calibration.CalibrationUtils.point2D
import ij_plugins.color.util.{ImageJUtils, PerspectiveTransform}

import java.awt.geom.Point2D
import scala.collection.compat.immutable.ArraySeq

/**
 * Represents only layout of chips similar to `GridColorChart`: a regular grid of square chips, arranged in rows and
 * columns.
 *
 * @param nbColumns
 *   number of columns
 * @param nbRows
 *   number of rows
 * @param chipMargin
 *   reduction of chip from their maximum size on the grid (as fraction of its width or height). Value of the margin
 *   must be between 0 and 0.5.
 * @param alignmentTransform
 *   Alignment between the reference chips and chips found in the actual chart.
 */
class GridChartFrame(
  val nbColumns: Int,
  val nbRows: Int,
  val chipMargin: Double,
  val alignmentTransform: PerspectiveTransform = new PerspectiveTransform()
) {

  private val chipOutlines: IndexedSeq[Seq[Point2D]] = {
    for {
      row    <- 0 until nbRows
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
   * Creates a copy of this chart that has its chip outline aligned gto given ROI.
   *
   * @param roi
   *   desired chip outline ROI.
   */
  def copyAlignedTo(roi: Roi): GridChartFrame = {
    val t = GridChartFrameUtils.computeAlignmentTransform(roi, this)
    this.copyWith(t)
  }

  /**
   * Creates a copy of this chart with different `chipMargin`. Value of the margin must be between 0 and 0.5.
   */
  def copyWithChipMargin(newChipMargin: Double): GridChartFrame =
    new GridChartFrame(nbColumns, nbRows, newChipMargin, alignmentTransform)

  /**
   * Creates a copy of this chart with different `alignmentTransform`.
   */
  def copyWith(newAlignmentTransform: PerspectiveTransform): GridChartFrame =
    new GridChartFrame(nbColumns, nbRows, chipMargin, newAlignmentTransform)

  /**
   * Chips ROIs with alignment transform applied to their outline.
   */
  def alignedChipROIs: IndexedSeq[Roi] = {
    for ((oSrc, i) <- chipOutlines.zipWithIndex) yield {
      val oDst = alignmentTransform.transform(oSrc)
      val roi  = ImageJUtils.toRoi(oDst)
      roi.setName(s"${i + 1}")
      roi
    }
  }
}
