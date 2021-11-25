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

import ij_plugins.color.util.ImagePlusType

enum ColorChartType(val name: String) {
  case GretagMacbethColorChecker extends ColorChartType("GretagMacbeth ColorChecker")

  case XRitePassportColorChecker extends ColorChartType("X-Rite Passport")

  case ImageScienceColorGaugeMatte extends ColorChartType("Image Science ColorGauge Matte")

  case Custom extends ColorChartType("Custom")

  override def toString: String = name
}

object ColorChartType {

  /**
   * Tries to get an item by the supplied name.
   * @param name
   *   name of the item
   * @throws NoSuchElementException
   *   if enum has no item with given name
   */
  def withName(name: String): ColorChartType =
    withNameOption(name).getOrElse(throw new NoSuchElementException(s"No ImagePlusType with name: $name"))

  /**
   * Optionally returns an item for a given name.
   * @param name
   *   name of the item
   */
  def withNameOption(name: String): Option[ColorChartType] =
    ColorChartType.values.find(_.name == name)
}
