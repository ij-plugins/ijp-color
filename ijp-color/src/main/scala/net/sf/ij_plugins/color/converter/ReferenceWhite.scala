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

package net.sf.ij_plugins.color.converter

/** Reference white. */
sealed case class ReferenceWhite(name: String, x: Double, z: Double) {
  final val y: Double = 1

  override def toString: String = name
}

/** Concrete reference values values for selected illuminants. */
object ReferenceWhite {

  /** A (ASTM E308-01) */
  val A = ReferenceWhite("A", x = 1.09850, z = 0.35585)

  /** B (Wyszecki & Stiles, p. 769) */
  val B = ReferenceWhite("B", x = 0.99072, z = 0.85223)

  /** C (ASTM E308-01) */
  val C = ReferenceWhite("C", x = 0.98074, z = 1.18232)

  /** D50 (ASTM E308-01) */
  val D50 = ReferenceWhite("D50", x = 0.96422, z = 0.82521)

  /** D55 (ASTM E308-01) */
  val D55 = ReferenceWhite("D55", x = 0.95682, z = 0.92149)

  /** D65 (ASTM E308-01) */
  val D65 = ReferenceWhite("D65", x = 0.95047, z = 1.08883)

  /** D75 (ASTM E308-01) */
  val D75 = ReferenceWhite("D70", x = 0.94972, z = 1.22638)

  /** E (ASTM E308-01) */
  val E = ReferenceWhite("E", x = 1.00000, z = 1.00000)

  /** F2 (ASTM E308-01) */
  val F2 = ReferenceWhite("F2", x = 0.99186, z = 0.67393)

  /** F7 (ASTM E308-01) */
  val F7 = ReferenceWhite("F7", x = 0.95041, z = 1.08747)

  /** F11 (ASTM E308-01) */
  val F11 = ReferenceWhite("F11", x = 1.00962, z = 0.64350)

  /** All predefined reference whites. */
  val values = List(A, B, C, D50, D55, D65, D75, E, F2, F7, F11)
}


