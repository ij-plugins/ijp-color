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

import ij_plugins.color.calibration.CalibrationUtils.point2D
import ij_plugins.color.converter.ColorTriple.Lab

import java.awt.geom.Point2D

/** Factory methods for creating ColorChips. */
object ColorChip {

  /**
   * Create a color chip located on a [[ij_plugins.color.calibration.chart.ChartGrid]].
   *
   * Chip size is 1x1, less the margin.
   *
   * @param name
   *   name
   * @param color
   *   chip color in CIE L*a*b* color space
   * @param column
   *   column in a grid chart
   * @param row
   *   row in a grid chart
   * @param margin
   *   size of the chip is reduced ny the margin from its 1x1 size. Margin must be greater or equal 0, and less than
   *   0.5.
   * @see
   *   [[ij_plugins.color.calibration.chart.ChartGrid]]
   */
  def apply(name: String, color: Lab, column: Int, row: Int, margin: Double = 0): ColorChip = {
    require(margin >= 0 && margin < 0.5, "Margin value must at least 0 but less than 0.5, got " + margin)
    new ColorChip(
      name,
      color,
      List(
        point2D(column + margin, row + margin),
        point2D(column + 1 - margin, row + margin),
        point2D(column + 1 - margin, row + 1 - margin),
        point2D(column + margin, row + 1 - margin)
      )
    )
  }
}

/**
 * Color chips, its name, color, and shape (location).
 *
 * @param name
 *   chip's name.
 * @param color
 *   chip's color in CIE L*a*b*.
 * @param outline
 *   outline describing the chip and its location.
 */
class ColorChip(val name: String, val color: Lab, val outline: Seq[Point2D])
