/*
 * Image/J Plugins
 * Copyright (C) 2002-2022 Jarek Sacha
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

import ij.ImagePlus
import ij_plugins.color.util.EnumCompanion.{WithName, WithNameCompanion, WithValue, WithValueCompanion}

import scala.collection.immutable

/**
 * Type-safe dealing with ImagePlus image types (integer IDs).
 */
enum ImagePlusType(val name: String, val value: Int) extends WithName with WithValue[Int]:

  /** ImagePlus.GRAY8 */
  case Gray8 extends ImagePlusType("8-bit gray", ImagePlus.GRAY8)

  /** ImagePlus.GRAY16 */
  case Gray16 extends ImagePlusType("16-bit gray", ImagePlus.GRAY16)

  /** ImagePlus.GRAY32 */
  case Gray32 extends ImagePlusType("32-bit gray", ImagePlus.GRAY32)

  /** ImagePlus.COLOR_256 */
  case Color256 extends ImagePlusType("Indexed color 256", ImagePlus.COLOR_256)

  /** ImagePlus.COLOR_RGB */
  case ColorRGB extends ImagePlusType("24-bit color", ImagePlus.COLOR_RGB)

  override def toString: String = name

object ImagePlusType extends WithNameCompanion[ImagePlusType] with WithValueCompanion[Int, ImagePlusType]
