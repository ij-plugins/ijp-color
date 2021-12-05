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

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.*

class GridChartFrameTest extends AnyFlatSpec {

  it should "have 4 point outline" in {
    val nbColumns = 3
    val nbRows    = 2
    val chart     = new GridChartFrame(nbColumns, nbRows, 0.1)

    val ot = chart.referenceOutline

    ot.length should be(4)

    val tol = 1e-6
    ot(0).getX should be(0.0 +- tol)
    ot(0).getY should be(0.0 +- tol)
    ot(1).getX should be(nbColumns)
    ot(1).getY should be(0.0 +- tol)
    ot(2).getX should be(nbColumns)
    ot(2).getY should be(nbRows)
    ot(3).getX should be(0.0 +- tol)
    ot(3).getY should be(nbRows)
  }

  it should "have default chips at unit locations" in {
    val nbColumns = 3
    val nbRows    = 2
    val chart     = new GridChartFrame(nbColumns, nbRows, 0.1)

    val rois = chart.alignedChipROIs
    rois.length should be(nbColumns * nbRows)

    val tol = 1e-6
    for (r <- 0 until nbRows) {
      for (c <- 0 until nbColumns) {
        val indx = c + r * nbColumns
        val roi  = rois(indx)
        val cent = roi.getContourCentroid
        cent(0) should be(c + 0.5 +- tol)
        cent(1) should be(r + 0.5 +- tol)
      }
    }
  }

}
