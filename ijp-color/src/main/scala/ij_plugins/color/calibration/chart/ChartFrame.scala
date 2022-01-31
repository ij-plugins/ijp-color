/*
 * Image/J Plugins
 * Copyright (C) 2002-2022 Jarek Sacha
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
import ij_plugins.color.calibration.chart.ChartFrame.deepCopy
import ij_plugins.color.util.{ImageJUtils, PerspectiveTransform}

import java.awt.geom.Point2D

object ChartFrame {
  def deepCopy(src: Seq[Point2D]): Seq[Point2D]               = src.map(_.clone().asInstanceOf[Point2D])
  def deepCopy(src: IndexedSeq[Point2D]): IndexedSeq[Point2D] = src.map(_.clone().asInstanceOf[Point2D])
}

/**
 * Represents a with frame defined by an 4-point polygon and some chips of an an arbitrary polygon
 *
 * @param refOutline the reference outline of the charts frame. It needs to be a 4-point polygon.
 * @param refChipOutlines polygons defining the reference chips outlines.
 *                        They are in the same coordinates as the reference outline. The sequence can be empty.
 * @param alignmentTransform the transformation that si applied to the of the reference outline and
 *                           the reference chips outlines.
 */
class ChartFrame(
  refOutline: IndexedSeq[Point2D],
  refChipOutlines: IndexedSeq[Seq[Point2D]],
  final val alignmentTransform: PerspectiveTransform /*= new PerspectiveTransform()*/
) {

  require(refOutline.length == 4, s"The reference outline must have 4 points, got ${refOutline.length}.")

  private val _referenceOutline: IndexedSeq[Point2D]  = deepCopy(refOutline)
  private val _chipOutlines: IndexedSeq[Seq[Point2D]] = refChipOutlines.map(deepCopy)

  /**
   * Outline of the reference chart as a sequence of 4 corner points: top-left, top-right, bottom-right, bottom-left.
   */
  final def referenceOutline: IndexedSeq[Point2D] = deepCopy(_referenceOutline)

  /**
   * Outlines of the reference chips. OOutlines are polygons, may have 3 or more vertices.
   */
  final def referenceChipOutlines: IndexedSeq[Seq[Point2D]] = _chipOutlines.map(deepCopy)

  /**
   * Creates a copy of this chart that has its chip outline aligned to given ROI.
   * The ROI is expected to be a polygon with 4 vertices.
   *
   * @param roi desired chip outline ROI.
   */
  def copyAlignedTo(roi: Roi): ChartFrame = {
    val t = ChartFrameUtils.computeAlignmentTransform(roi, this)
    this.copyWith(t)
  }

  /**
   * Creates a copy of this chart with different `alignmentTransform`.
   */
  def copyWith(newAlignmentTransform: PerspectiveTransform): ChartFrame =
    new ChartFrame(_referenceOutline, _chipOutlines, newAlignmentTransform)

  final def alignedOutlineROI: Roi = {
    val oDst = alignmentTransform.transform(_referenceOutline)
    val roi  = ImageJUtils.toRoi(oDst)
    roi.setName("Chart Outline")
    roi
  }

  /**
   * Chips ROIs with alignment transform applied to their outline.
   */
  final def alignedChipROIs: IndexedSeq[Roi] = {
    for ((oSrc, i) <- _chipOutlines.zipWithIndex) yield {
      val oDst = alignmentTransform.transform(oSrc)
      val roi  = ImageJUtils.toRoi(oDst)
      roi.setName(s"${i + 1}")
      roi
    }
  }
}
