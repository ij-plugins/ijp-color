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

package net.sf.ij_plugins.color.calibration

import java.awt.Shape
import java.awt.geom.Path2D
import net.sf.ij_plugins.color.calibration.chart.ColorChip
import scalafx.geometry.Point2D

package object ui {
  def toShape(chips: Seq[ColorChip]): Shape = {
    val shape = new Path2D.Double
    for (chip <- chips) shape.append(outlineToShape(chip.outline.toArray), false)
    shape
  }

  def outlineToShape(outline: Array[Point2D]): Shape = {
    val path: Path2D = new Path2D.Double
    path.moveTo(outline.head.x, outline.head.y)
    outline.tail.foreach(point => path.lineTo(point.x, point.y))
    path.closePath()
    path
  }
}
