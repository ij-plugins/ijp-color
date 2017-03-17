/*
 * Image/J Plugins
 * Copyright (C) 2002-2017 Jarek Sacha
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

import org.scalatest.FlatSpec

import scalafx.geometry.Point2D

/**
 * @author Jarek Sacha 
 */
class ChartGridSpec extends FlatSpec {

  "ChartGrid" should "locate chips with 0 margin" in {

    val margin = 0
    val expected = Array(
      new Point2D(1 + margin, 1 + margin),
      new Point2D(2 - margin, 1 + margin),
      new Point2D(2 - margin, 2 - margin),
      new Point2D(1 + margin, 2 - margin)
    )

    val chartGrid = new ChartGrid(6, 4)
    val chip = chartGrid.chipAt(1, 1, margin)

    assert(expected.length === chip.length)

    expected.zip(chip).foreach(v => assertEquals(v._1, v._2, 0.0001))
  }

  it should "locate chips with 0.1 margin" in {

    val margin = 0.1
    val expected = Array(
      new Point2D(1 + margin, 1 + margin),
      new Point2D(2 - margin, 1 + margin),
      new Point2D(2 - margin, 2 - margin),
      new Point2D(1 + margin, 2 - margin)
    )

    val chartGrid = new ChartGrid(6, 4)
    val chip = chartGrid.chipAt(1, 1, margin)

    assert(expected.length === chip.length)

    expected.zip(chip).foreach(v => assertEquals(v._1, v._2, 0.0001))
  }

  it should "locate chips with different column and row margin" in {

    val c = 1
    val r = 2
    val cMargin = 0.1
    val rMargin = 0.25
    val expected = Array(
      new Point2D(c + cMargin, r + rMargin),
      new Point2D(c + 1 - cMargin, r + rMargin),
      new Point2D(c + 1 - cMargin, r + 1 - rMargin),
      new Point2D(c + cMargin, r + 1 - rMargin)
    )

    val chartGrid = new ChartGrid(6, 4)
    val chip = chartGrid.chipAt(c, r, cMargin, rMargin)

    assert(expected.length === chip.length)

    expected.zip(chip).foreach(v => assertEquals(v._1, v._2, 0.0001))
  }


  def assertEquals(expected: Point2D, actual: Point2D, tolerance: Double): Unit = {
    assert(math.abs(expected.x - actual.x) <= tolerance, "Expecting x=" + expected.x + ", got " + actual.x)
    assert(math.abs(expected.y - actual.y) <= tolerance, "Expecting y=" + expected.y + ", got " + actual.y)
  }

}
