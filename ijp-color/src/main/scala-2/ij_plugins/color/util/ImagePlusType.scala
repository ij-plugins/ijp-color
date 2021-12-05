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

package ij_plugins.color.util

import enumeratum.values.{IntEnum, IntEnumEntry}

import scala.collection.immutable

sealed abstract class ImagePlusType(val value: Int, val name: String) extends IntEnumEntry

/**
 * Make ImagePlus image types integer IDs to be type-safe.
 */
object ImagePlusType extends IntEnum[ImagePlusType] {

  /**
   * ImagePlus.GRAY8
   */
  case object Gray8 extends ImagePlusType(0, "8-bit gray")

  /**
   * ImagePlus.GRAY16
   */
  case object Gray16 extends ImagePlusType(1, "16-bit gray")

  /**
   * ImagePlus.GRAY32
   */
  case object Gray32 extends ImagePlusType(2, "32-bit gray")

  /**
   * ImagePlus.COLOR_256
   */
  case object Color256 extends ImagePlusType(3, "Indexed color 256")

  /**
   * ImagePlus.COLOR_RGB
   */
  case object ColorRGB extends ImagePlusType(4, "24-bit color")

  override def values: immutable.IndexedSeq[ImagePlusType] = findValues
}
