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

package ij_plugins.color.converter

/** Generic representation of a color as three values, color space independent. */
trait ColorTriple {

  /** Return color component with given index. Valid indices: 0, 1, 2. */
  def apply(index: Int): Double

  /** Convert color representation to an Array */
  def toArray: Array[Double] = Array(apply(0), apply(1), apply(2))
}

/** Color triples with band/channel names specific to color spaces. */
object ColorTriple {

  /** Generic color coordinates in an arbitrary color space.
    *
    * @author Jarek Sacha
    */
  case class Color123(_1: Double = 0, _2: Double = 0, _3: Double = 0) extends ColorTriple {

    def apply(index: Int): Double = index match {
      case 0 => _1
      case 1 => _2
      case 2 => _3
      case _ => throw new IllegalArgumentException("Invalid color band index: " + index)
    }

    /** Convert color representation to an Array */
    @inline
    override def toArray: Array[Double] = Array(_1, _2, _3)
  }

  object Color123 {
    def apply(a: Array[Double]): Color123 = {
      require(a.length == 3)
      new Color123(a(0), a(1), a(2))
    }
  }

  /** An RGB color. */
  case class RGB(r: Double, g: Double, b: Double) extends ColorTriple {
    def apply(index: Int): Double = index match {
      case 0 => r
      case 1 => g
      case 2 => b
      case _ => throw new IllegalArgumentException("Invalid color band index: " + index)
    }

    @inline
    override def toArray: Array[Double] = Array(r, g, b)
  }

  object RGB {
    def apply(rgb: Array[Double]): RGB = {
      require(rgb.length == 3)
      new RGB(rgb(0), rgb(1), rgb(2))
    }
  }

  /** An XYZ color. */
  case class XYZ(x: Double, y: Double, z: Double) extends ColorTriple {
    def apply(index: Int): Double = index match {
      case 0 => x
      case 1 => y
      case 2 => z
      case _ => throw new IllegalArgumentException("Invalid color band index: " + index)
    }

    @inline
    override def toArray: Array[Double] = Array(x, y, z)
  }

  object XYZ {
    def apply(xyz: Array[Double]): XYZ = {
      require(xyz.length == 3)
      new XYZ(xyz(0), xyz(1), xyz(2))
    }
  }

  /** An Lab color. */
  case class Lab(l: Double, a: Double, b: Double) extends ColorTriple {
    def apply(index: Int): Double = index match {
      case 0 => l
      case 1 => a
      case 2 => b
      case _ => throw new IllegalArgumentException("Invalid color band index: " + index)
    }

    @inline
    override def toArray: Array[Double] = Array(l, a, b)
  }

  object Lab {
    def apply(lab: Array[Double]): Lab = {
      require(lab.length == 3)
      new Lab(lab(0), lab(1), lab(2))
    }
  }

}

