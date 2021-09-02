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

package ij_plugins.color.ui.util

import enumeratum.{Enum, EnumEntry}

import scala.collection.immutable

sealed abstract class Gamma(override val entryName: String, val value: Double) extends EnumEntry {
  override def toString: String = entryName
}

case object Gamma extends Enum[Gamma] {

  case object v10 extends Gamma("1.0", 1.0)

  case object v18 extends Gamma("1.8", 1.8)

  case object v22 extends Gamma("2.2", 2.2)

  case object sRGB extends Gamma("sRGB", -2.2)

  case object L extends Gamma("L*", 0.0)

  val values: immutable.IndexedSeq[Gamma] = findValues
}
