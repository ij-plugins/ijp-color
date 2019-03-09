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
 * Latest release available at https://github.com/ij-plugins/ijp-color/
 */

package net.sf.ij_plugins.util

import org.scalatest.FlatSpec
import org.scalatest.Matchers._

import scalafx.geometry.Point2D

class PerspectiveTransformTest extends FlatSpec {

  "PerspectiveTransform" should "transform points" in {

    // Reference quad
    val referenceQuad = Array(
      new Point2D(0, 0),
      new Point2D(6, 0),
      new Point2D(6, 4),
      new Point2D(0, 4)
    )

    val deformedQuad = Array(
      new Point2D(207, 95),
      new Point2D(461, 132),
      new Point2D(436, 312),
      new Point2D(198, 255)
    )

    val alignmentTransform = PerspectiveTransform.quadToQuad(referenceQuad, deformedQuad)
    val pm = alignmentTransform.transform(new Point2D(3, 2))

    pm.x should be(317.07786 +- 0.00001)
    pm.y should be(199.30959 +- 0.00001)
  }
}
