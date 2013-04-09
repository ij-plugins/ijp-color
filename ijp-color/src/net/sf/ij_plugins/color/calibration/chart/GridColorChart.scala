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

package net.sf.ij_plugins.color.calibration.chart

import ij.process.ImageProcessor
import net.sf.ij_plugins.color.converter.ColorTriple.Lab
import net.sf.ij_plugins.util.IJTools
import scalafx.geometry.Point2D


/** Chart consisting of a regular grid of square chips, arranged in rows and columns.
  *
  * @param name chart's name
  * @param nbColumns number of columns
  * @param nbRows number of rows
  * @param chips chip names and CIE L*a*b* / D65 color values, row by row, starting at (0,0) or top left corner.
  */
final class GridColorChart(val name: String,
                           val nbColumns: Int,
                           val nbRows: Int,
                           val chips: List[(String, Lab)]
                              ) extends ColorChart {

  /** Reference grid on which reference color chips are located. */
  val referenceGrid: ChartGrid = new ChartGrid(nbColumns, nbRows)

  /** Reference chips with they size reduced by margin (as fraction of its width or height).
    *
    * Value of the margin must be between 0 and 0.5.
    */
  def referenceChips(margin: Double): IndexedSeq[ColorChip] = {
    require(margin >= 0 && margin < 0.5, "Margin value must at least 0 but less than 0.5, got " + margin)
    val a = chips.toArray
    for {
      row <- 0 until nbRows; column <- 0 until nbColumns; i = row * nbColumns + column
    } yield ColorChip(a(i)._1, a(i)._2, column, row, margin)
  }

  override def referenceColorXYZ: Array[Array[Double]] = {
    chips.map(v => colorConverter.lab2XYZ(v._2).toArray).toArray
  }


  /** Outline of the reference chart as a sequence of 4 corner points: top-left, top-right, bottom-right, bottom-left.
    */
  def referenceOutline(): Seq[Point2D] = {
    List(new Point2D(0, 0), new Point2D(nbColumns, 0), new Point2D(nbColumns, nbRows), new Point2D(0, nbRows))
  }

  /** Return the aligned outline of a chip in given `column` and `row`.
    *
    * Chip linear size is reduced by a margin expressed as a fraction of width/length.
    */
  def outlineChipAt(column: Int, row: Int, margin: Double): Array[Point2D] = {
    val ref = referenceGrid.chipAt(column, row, margin)
    ref.map(p => alignmentTransform.transform(p))
  }

  /** Color chips with alignment transform applied to their outline. */
  def alignedChips(margin: Double): Array[ColorChip] = referenceChips(margin).map {
    c => new ColorChip(c.name, c.color, alignmentTransform.transform(c.outline))
  }.toArray

  override def toString = name

  override def averageChipColor[T <: ImageProcessor](margin: Double, src: Array[T]): Array[Array[Double]] = {
    val chips = alignedChips(margin)
    for (chip <- chips) yield IJTools.measureColor(src, chip.outline.toArray)
  }

}
