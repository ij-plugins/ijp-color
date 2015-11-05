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

import scalafx.geometry.Point2D


/** Rectangular grid for color reference chart.
  *
  * Each chip has size of 1x1.
  * The chart top left corner is located at (0,0), bottom right corner at (nbColumns, nbRows).
  *
  * @param nbColumns number of columns in the chart
  * @param nbRows    number of rows in the chart
  */
final class ChartGrid(nbColumns: Int, nbRows: Int) {

  /** Compute outline of a chip located in (column, row).  Chip size will be decreased by the `margin` on ech side.
    *
    * @param column chip's column.
    * @param row  chip's row.
    * @param margin chip border (same on all 4 sides), as fraction of width or height.
    * @return points defining chip outline.
    */
  def chipAt(column: Int, row: Int, margin: Double): Array[Point2D] = chipAt(column, row, margin, margin)

  /** Compute outline of a chip located in (column, row).
    *
    * @param column chip's column.
    * @param row  chip's row.
    * @param columnMargin chip border in column (same on both sides), as fraction of height.
    * @param rowMargin  chip border in row (same on both sides), as fraction of width.
    * @return points defining chip outline.
    */
  def chipAt(column: Int, row: Int, columnMargin: Double, rowMargin: Double): Array[Point2D] = {
    val x0 = column + columnMargin
    val x1 = column + 1 - columnMargin
    val y0 = row + rowMargin
    val y1 = row + 1 - rowMargin
    Array(
      new Point2D(x0, y0),
      new Point2D(x1, y0),
      new Point2D(x1, y1),
      new Point2D(x0, y1)
    )
  }

  /** Points outlining the chart: top-left, top-right, bottom-right, and bottom-left. */
  def outline: Array[Point2D] = Array(
    new Point2D(0, 0),
    new Point2D(nbColumns, 0),
    new Point2D(nbColumns, nbRows),
    new Point2D(0, nbRows)
  )
}

