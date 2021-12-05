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

import ij.ImagePlus
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.*

class ImagePlusTypeTest extends AnyFlatSpec {
  it should "match ImagePlus constants" in {
    ImagePlusType.Gray8.value should be(ImagePlus.GRAY8)
    ImagePlusType.Gray16.value should be(ImagePlus.GRAY16)
    ImagePlusType.Gray32.value should be(ImagePlus.GRAY32)
    ImagePlusType.Color256.value should be(ImagePlus.COLOR_256)
    ImagePlusType.ColorRGB.value should be(ImagePlus.COLOR_RGB)
  }

  it should "have unique names" in {
    val names = ImagePlusType.values.map(_.name)

    names.toSet.size should be(ImagePlusType.values.size)
  }

//  it should "lookup by name" in {
//    for (v <- ImagePlusType.values) {
//      ImagePlusType.withName(v.name) should be(v)
//    }
//  }

  it should "lookup by value" in {
    for (v <- ImagePlusType.values) {
      ImagePlusType.withValue(v.value) should be(v)
    }
  }
}
