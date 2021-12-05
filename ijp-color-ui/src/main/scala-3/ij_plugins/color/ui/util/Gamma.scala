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

enum Gamma(val name: String, val value: Double) {

  case v10 extends Gamma("1.0", 1.0)

  case v18 extends Gamma("1.8", 1.8)

  case v22 extends Gamma("2.2", 2.2)

  case sRGB extends Gamma("sRGB", -2.2)

  case L extends Gamma("L*", 0.0)

  override def toString: String = name
}

object Gamma {

  /**
   * Tries to get an item by the supplied name.
   * @param name
   *   name of the item
   * @throws NoSuchElementException
   *   if enum has no item with given name
   */
  def withName(name: String): Gamma =
    withNameOption(name).getOrElse(throw new NoSuchElementException(s"No Gamma with name: $name"))

  /**
   * Optionally returns an item for a given name.
   * @param name
   *   name of the item
   */
  def withNameOption(name: String): Option[Gamma] =
    Gamma.values.find(_.name == name)
}
