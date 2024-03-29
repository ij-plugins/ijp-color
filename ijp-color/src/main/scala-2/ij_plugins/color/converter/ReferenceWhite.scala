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

import enumeratum.{Enum, EnumEntry}

import scala.collection.immutable

/** Reference white. */
sealed abstract class ReferenceWhite(override val entryName: String, val x: Double, val z: Double) extends EnumEntry {
  val name: String = entryName

  final val y: Double = 1

  override def toString: String = entryName
}

/** Concrete reference values values for selected illuminants. */
case object ReferenceWhite extends Enum[ReferenceWhite] {

  /** A (ASTM E308-01) */
  case object A extends ReferenceWhite("A", x = 1.09850, z = 0.35585)

  /** B (Wyszecki & Stiles, p. 769) */
  case object B extends ReferenceWhite("B", x = 0.99072, z = 0.85223)

  /** C (ASTM E308-01) */
  case object C extends ReferenceWhite("C", x = 0.98074, z = 1.18232)

  /** D50 (ASTM E308-01) */
  case object D50 extends ReferenceWhite("D50", x = 0.96422, z = 0.82521)

  /** D55 (ASTM E308-01) */
  case object D55 extends ReferenceWhite("D55", x = 0.95682, z = 0.92149)

  /** D65 (ASTM E308-01) */
  case object D65 extends ReferenceWhite("D65", x = 0.95047, z = 1.08883)

  /** D75 (ASTM E308-01) */
  case object D75 extends ReferenceWhite("D70", x = 0.94972, z = 1.22638)

  /** E (ASTM E308-01) */
  case object E extends ReferenceWhite("E", x = 1.00000, z = 1.00000)

  /** F2 (ASTM E308-01) */
  case object F2 extends ReferenceWhite("F2", x = 0.99186, z = 0.67393)

  /** F7 (ASTM E308-01) */
  case object F7 extends ReferenceWhite("F7", x = 0.95041, z = 1.08747)

  /** F11 (ASTM E308-01) */
  case object F11 extends ReferenceWhite("F11", x = 1.00962, z = 0.64350)

  /** All predefined reference whites. */
  val values: immutable.IndexedSeq[ReferenceWhite] = findValues
}
