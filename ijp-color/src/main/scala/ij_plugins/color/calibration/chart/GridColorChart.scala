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
import ij.process.ImageProcessor
import ij_plugins.color.converter.ColorTriple.Lab
import ij_plugins.color.converter.ReferenceWhite
import ij_plugins.color.util.{IJTools, PerspectiveTransform}

import java.awt.geom.Point2D
import scala.collection.immutable


/** Chart consisting of a regular grid of square chips, arranged in rows and columns.
  *
  * @param name       chart's name
  * @param nbColumns  number of columns
  * @param nbRows     number of rows
  * @param chips      chip names and CIE L*a*b* / D65 color values, row by row, starting at (0,0) or top left corner.
  * @param chipMargin reduction of chip from their maximum size on the grid (as fraction of its width or height).
  *                   Value of the margin must be between 0 and 0.5.
  * @param enabled    which chips are active. If value is 'true' chip is active' if 'false' not used in computations.
  */
final class GridColorChart(val name: String,
                           nbColumns: Int,
                           nbRows: Int,
                           val chips: List[(String, Lab)],
                           chipMargin: Double,
                           val enabled: List[Boolean],
                           override val refWhite: ReferenceWhite,
                           alignmentTransform: PerspectiveTransform)
  extends GridChartFrame(nbColumns, nbRows, chipMargin, alignmentTransform)
    with ColorChart {

  // TODO: add requirement messages
  require(chipMargin >= 0 && chipMargin < 0.5, "Margin value must at least 0 but less than 0.5, got " + chipMargin)
  require(nbColumns > 0)
  require(nbRows > 0)
  require(nbColumns * nbRows == chips.size)
  require(chips.size == enabled.size)
  require(refWhite != null)
  require(alignmentTransform != null)

  /** Construct chart with all chips enabled.
    *
    * @param name      chart's name
    * @param nbColumns number of columns
    * @param nbRows    number of rows
    * @param chips     chip names and CIE L*a*b* / D65 color values, row by row, starting at (0,0) or top left corner.
    */
  def this(name: String,
           nbColumns: Int,
           nbRows: Int,
           chips: List[(String, Lab)],
           chipMargin: Double = 0,
           refWhite: ReferenceWhite,
           alignmentTransform: PerspectiveTransform = new PerspectiveTransform()) = {
    this(name, nbColumns, nbRows, chips, chipMargin, List.fill(nbColumns * nbRows)(true), refWhite,
      alignmentTransform)
  }

  private val n = nbColumns * nbRows
  require(chips.length == n)
  require(enabled.length == n)

  /** Reference grid on which reference color chips are located. */
  val referenceGrid: ChartGrid = new ChartGrid(nbColumns, nbRows)

  /** Reference chips with they size reduced by margin (as fraction of its width or height).
    *
    * Value of the margin must be between 0 and 0.5.
    */
  def referenceChips: immutable.IndexedSeq[ColorChip] = {
    val a = chips.toArray
    for {
      row <- 0 until nbRows
      column <- 0 until nbColumns
      i = row * nbColumns + column
      if enabled(i)
    } yield ColorChip(a(i)._1, a(i)._2, column, row, chipMargin)
  }

  override def referenceColorXYZ: Array[Array[Double]] = {
    val enabledChips = chips.zipWithIndex.filter { case (_, i) => enabled(i) }.map(_._1)
    enabledChips.map { v => colorConverter.toXYZ(v._2).toArray }.toArray
  }

  /**
    * Return the aligned outline of a chip in given `column` and `row`.
    *
    * Chip linear size is reduced by a margin expressed as a fraction of width/length.
    */
  @deprecated("Unused method, will be removed", "0.8")
  def outlineChipAt(column: Int, row: Int, margin: Double): Array[Point2D] = {
    val ref = referenceGrid.chipAt(column, row, margin)
    ref.map(p => alignmentTransform.transform(p))
  }

  /**
    * Color chips with alignment transform applied to their outline.
    */
  override def alignedChips: immutable.IndexedSeq[ColorChip] = referenceChips.map {
    c => new ColorChip(c.name, c.color, alignmentTransform.transform(c.outline))
  }

  /**
    * Color chips with alignment transform applied to their outline.
    */
  override def alignedChipROIs: immutable.IndexedSeq[Roi] = alignedChips.map { chip =>
    val roi = IJTools.toRoi(chip.outline)
    roi.setName(chip.name)
    roi
  }

  /**
    * Actual outline of the of the chart (reference outline with alignment transform applied).
    */
  @deprecated("Unused method, will be removed", "0.8")
  def alignedOutline: immutable.IndexedSeq[Point2D] =
    alignmentTransform.transform(referenceOutline).toIndexedSeq


  override def toString: String = name

  override def averageChipColor[T <: ImageProcessor](src: Array[T]): Array[Array[Double]] = {
    val chips = alignedChips
    val r = for (chip <- chips) yield IJTools.measureColor(src, chip.outline.toArray)
    r.toArray
  }

  /** Creates a copy of this chart in which some chips cn be enabled/disabled.
    *
    * @param enabled array with indexes corresponding to ones returned by `referenceColor` methods.
    *                If value is `true` chip with corresponding index is enabled, if `false` it is disabled.
    * @return
    */
  override def copyWithEnableChips(enabled: Array[Boolean]): GridColorChart = {
    require(chips.length == enabled.length,
      "Expecting " + chips.length + " elements in the input array, got " + enabled.length)

    new GridColorChart(name, nbColumns, nbRows, chips, chipMargin, enabled.toList, refWhite, alignmentTransform)
  }

  /** Creates a copy of this chart with different `chipMargin`. Value of the margin must be between 0 and 0.5. */
  override def copyWithNewChipMargin(newChipMargin: Double): GridColorChart =
    new GridColorChart(name, nbColumns, nbRows, chips, newChipMargin, refWhite, alignmentTransform)

  /** Creates a copy of this chart with different `alignmentTransform`. */
  override def copyWith(newAlignmentTransform: PerspectiveTransform): GridColorChart =
    new GridColorChart(name, nbColumns, nbRows, chips, chipMargin, refWhite, newAlignmentTransform)
}
