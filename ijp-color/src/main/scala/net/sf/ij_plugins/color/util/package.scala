/*
 * Image/J Plugins
 * Copyright (C) 2002-2020 Jarek Sacha
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

package net.sf.ij_plugins.color

/** Various utility methods. */
package object util {
  /**
    * Distance between points. For points in CIE L*a*b* color space it is equivalent to delta E 1974.
    *
    * @param a first point.
    * @param b second point.
    * @return distance
    */
  def delta(a: Array[Double], b: Array[Double]): Double = {
    assert(a != null)
    assert(b != null)
    assert(a.length == b.length)
    var sum: Double = 0
    for (i <- a.indices) {
      val d = a(i) - b(i)
      sum += d * d
    }
    math.sqrt(sum)
  }

  /** Clip all elements of the input array to range suitable for an 8-bit unsigned integer: 0 to 255.
    *
    * Values in the range will be rounded to the closest integer.
    * Values less than 0 will be set to 0.
    * Values greater than 255 will be set to 255.
    */
  def clipUInt8(a: Array[Double]): Array[Int] = {
    assert(a != null)
    val r = new Array[Int](a.length)
    for (i <- a.indices) {
      r(i) = clipUInt8(a(i))
    }
    r
  }

  /** Clip input value to the range if an 8-bit unsigned integer: 0 to 255, but do not modify decimal places within the range.
    *
    * Values in the range will not be changed.
    * Values less than 0 will be set to 0.
    * Values greater than 255 will be set to 255.
    */
  def clipUInt8D(a: Array[Double]): Array[Double] = {
    assert(a != null)
    val r = new Array[Double](a.length)
    for (i <- a.indices) {
      r(i) = clipUInt8D(a(i))
    }
    r
  }

  /** Clip input value to range suitable for an 8-bit unsigned integer: 0 to 255.
    *
    * Values in the range will be rounded to the closest integer.
    * Values less than 0 will be set to 0.
    * Values greater than 255 will be set to 255.
    */
  @inline
  def clipUInt8(v: Double): Int = math.max(math.min(math.round(v).toInt, 255), 0)

  /** Clip input value to the range if an 8-bit unsigned integer: 0 to 255, but do not modify decimal places within the range.
    *
    * Values in the range will not be changed.
    * Values less than 0 will be set to 0.
    * Values greater than 255 will be set to 255.
    */
  @inline
  def clipUInt8D(v: Double): Double = math.max(math.min(v, 255), 0)

}
