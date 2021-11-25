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

import enumeratum.{Enum, EnumEntry}

import scala.collection.immutable

sealed abstract class ColorChartType(override val entryName: String) extends EnumEntry {
  override def toString: String = entryName

  def name: String = entryName
}

case object ColorChartType extends Enum[ColorChartType] {
  case object GretagMacbethColorChecker extends ColorChartType("GretagMacbeth ColorChecker")

  case object XRitePassportColorChecker extends ColorChartType("X-Rite Passport")

  case object ImageScienceColorGaugeMatte extends ColorChartType("Image Science ColorGauge Matte")

  case object Custom extends ColorChartType("Custom")

  /** All refined reference color spaces. */
  val values: immutable.IndexedSeq[ColorChartType] = findValues
}