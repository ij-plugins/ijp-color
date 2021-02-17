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

package ij_plugins.color.calibration.regression

import enumeratum.{Enum, EnumEntry}

import scala.collection.immutable

/** Enumeration of polynomial function mapping methods used by `MappingFactory`. */
case object MappingMethod extends Enum[MappingMethod] {

  /** First order polynomial, within single color band - single input. */
  case object Linear extends MappingMethod("Linear")

  /** First order polynomial within with three input for each color band. */
  case object LinearCrossBand extends MappingMethod("Linear Cross-band")

  /** Second order polynomial, within single color band - single input. */
  case object Quadratic extends MappingMethod("Quadratic")

  /** Second order polynomial within with three input for each color band. */
  case object QuadraticCrossBand extends MappingMethod("Quadratic Cross-band")

  /** Third order polynomial, within single color band - single input. */
  case object Cubic extends MappingMethod("Cubic")

  /** Third order polynomial within with three input for each color band. */
  case object CubicCrossBand extends MappingMethod("Cubic Cross-band")

  /** All MappingMethod values. */
  val values: immutable.IndexedSeq[MappingMethod] = findValues
}

sealed abstract class MappingMethod(override val entryName: String) extends EnumEntry {
  override def toString: String = entryName
}