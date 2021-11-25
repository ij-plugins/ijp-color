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

import scala.collection.immutable

/** Concrete reference values values for selected illuminants. */
enum ReferenceWhite(val name: String, val x: Double, val z: Double) {

  val y: Double = 1

  /** A (ASTM E308-01) */
  case A extends ReferenceWhite("A", x = 1.09850, z = 0.35585)

  /** B (Wyszecki & Stiles, p. 769) */
  case B extends ReferenceWhite("B", x = 0.99072, z = 0.85223)

  /** C (ASTM E308-01) */
  case C extends ReferenceWhite("C", x = 0.98074, z = 1.18232)

  /** D50 (ASTM E308-01) */
  case D50 extends ReferenceWhite("D50", x = 0.96422, z = 0.82521)

  /** D55 (ASTM E308-01) */
  case D55 extends ReferenceWhite("D55", x = 0.95682, z = 0.92149)

  /** D65 (ASTM E308-01) */
  case D65 extends ReferenceWhite("D65", x = 0.95047, z = 1.08883)

  /** D75 (ASTM E308-01) */
  case D75 extends ReferenceWhite("D70", x = 0.94972, z = 1.22638)

  /** E (ASTM E308-01) */
  case E extends ReferenceWhite("E", x = 1.00000, z = 1.00000)

  /** F2 (ASTM E308-01) */
  case F2 extends ReferenceWhite("F2", x = 0.99186, z = 0.67393)

  /** F7 (ASTM E308-01) */
  case F7 extends ReferenceWhite("F7", x = 0.95041, z = 1.08747)

  /** F11 (ASTM E308-01) */
  case F11 extends ReferenceWhite("F11", x = 1.00962, z = 0.64350)

  override def toString: String = name
}

object ReferenceWhite {

  /**
    * Tries to get an item by the supplied name.
    * @param name
    *   name of the item
    * @throws NoSuchElementException
    *   if enum has no item with given name
    */
  def withName(name: String): ReferenceWhite =
    withNameOption(name).getOrElse(throw new NoSuchElementException(s"No ReferenceWhite with name: $name"))

  /**
    * Optionally returns an item for a given name.
    * @param name
    *   name of the item
    */
  def withNameOption(name: String): Option[ReferenceWhite] =
    ReferenceWhite.values.find(_.name == name)
}
