/*
 * Image/J Plugins
 * Copyright (C) 2002-2023 Jarek Sacha
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

package ij_plugins.color.util

import scala.reflect.ClassTag

/**
 * Helpers for adding companion objects to enums.
 * The convenience methods of companion objects are inspired by Scala 2 Enumeration library, for instance, `withValue`.
 */
object EnumCompanion:

  /**
   * Marks availability of member variable `name`. To be used in an `enum`
   *
   * Example:
   * {{{
   *   enum ColorChartType(val name: String) extends WithName:
   *     case Custom extends ColorChartType("Custom")
   *     ...
   * }}}
   */
  trait WithName:
    /* Name of the item used for display */
    val name: String

    override def toString: String = name

  /**
   * Trait to create companion object for an enum that has member variable `name`.
   *
   * Example of companion object for `enum` `ColorChartType` (single line)
   * {{{
   *   object ColorChartType extends WithNameCompanion[ColorChartType]
   * }}}
   *
   * @tparam T enum entry type
   */
  trait WithNameCompanion[T <: WithName]:

    def values: Array[T]

    /**
     * Tries to get an case by the supplied name.
     * @param name  name of the item
     * @throws NoSuchElementException if enum has no item with given name
     */
    def withName(name: String): T =
      withNameOption(name).getOrElse(throw new NoSuchElementException(s"No enum case with name: $name"))

    /**
     * Optionally returns case for a given name.
     * @param name name of the item
     */
    def withNameOption(name: String): Option[T] =
      values.find(_.name == name)

  /**
   * Marks availability of member variable `value` of type `V`. To be used in an `enum`.
   *
   * Example:
   * {{{
   *   enum ImagePlusType(val value: Int) extends WithValue[Int]:
   *     case Gray8 extends ImagePlusType(0)
   *     ...
   * }}}
   */
  trait WithValue[V]:
    val value: V

  /**
   * Trait to create companion object for an enum that has member variable `value` of type `V`.
   *
   * Typically the companion object is defined by a single line declaration like this (companion to `enum` `ImagePlusType`)
   * {{{
   *   object ImagePlusType extends WithValueCompanion[Int, ImagePlusType]
   * }}}
   *
   * @tparam T enum entry type
   * @tparam V type of the value
   */
  trait WithValueCompanion[V: ClassTag, T <: WithValue[V]]:

    def values: Array[T]

    /**
     * Tries to get an item by the supplied value `v`.
     * @param v value of the item
     * @throws NoSuchElementException if enum has no item with given value
     */
    def withValue(v: V): T =
      withValueOption(v).getOrElse(throw new NoSuchElementException(s"No enum case with value: $v"))

    /**
     * Optionally returns a case for a given value `v`.
     * @param v value of the item
     */
    def withValueOption(v: V): Option[T] =
      values.find(_.value == v)
