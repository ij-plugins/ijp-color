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

/**
 * An immutable 3x3 matrix:
 * {{{
 *   m00, m01, m02
 *   m10, m11, m12
 *   m20, m21, m22
 * }}}
 */
final class Matrix3x3(
  val m00: Double,
  val m01: Double,
  val m02: Double,
  val m10: Double,
  val m11: Double,
  val m12: Double,
  val m20: Double,
  val m21: Double,
  val m22: Double
) {

  /** Compute determinant of this matrix. */
  def determinant: Double = {
    m00 * (m22 * m11 - m21 * m12) -
      m10 * (m22 * m01 - m21 * m02) +
      m20 * (m12 * m01 - m11 * m02)
  }

  /**
   * Return inverse of this matrix.
   *
   * @throws java.lang.IllegalArgumentException if the matrix has no inverse.
   */
  def inverse: Matrix3x3 = {
    val d = determinant
    if (d == 0) throw new IllegalArgumentException("Matrix has no inverse, its determinant is 0.")

    new Matrix3x3(
      m00 = (m22 * m11 - m21 * m12) / d,
      m01 = -(m22 * m01 - m21 * m02) / d,
      m02 = (m12 * m01 - m11 * m02) / d,
      m10 = -(m22 * m10 - m20 * m12) / d,
      m11 = (m22 * m00 - m20 * m02) / d,
      m12 = -(m12 * m00 - m10 * m02) / d,
      m20 = (m21 * m10 - m20 * m11) / d,
      m21 = -(m21 * m00 - m20 * m01) / d,
      m22 = (m11 * m00 - m10 * m01) / d
    )
  }

  /** Return a transposed version of this matrix. */
  def transpose: Matrix3x3 = new Matrix3x3(
    m00 = m00,
    m01 = m10,
    m02 = m20,
    m10 = m01,
    m11 = m11,
    m12 = m21,
    m20 = m02,
    m21 = m12,
    m22 = m22
  )
}
