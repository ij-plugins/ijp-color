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

package ij_plugins.color.ui.converter

import ij_plugins.color.converter.ColorTriple
import ij_plugins.color.converter.ColorTriple.Color123
import org.scalafx.extras.generic_pane.NumberTextField
import scalafx.Includes.*
import scalafx.beans.property.ObjectProperty
import scalafx.geometry.Pos
import scalafx.scene.layout.HBox

object ColorValueUI {}

/** Row of number text fields holding color band values. */
class ColorValueUI {

  private val band1NTF = new NumberTextField()
  private val band2NTF = new NumberTextField()
  private val band3NTF = new NumberTextField()
  private val allBands = List(band1NTF, band2NTF, band3NTF)
  private var updating = false

  allBands.foreach(_.prefColumnCount = 6)
  allBands.foreach(_.setAlignment(Pos.CenterRight))
  allBands.foreach(_.textFormatter().value.onChange(updateColor()))

  val color = new ObjectProperty[ColorTriple](this, "color", Color123())
  color.onChange {
    updating = true
    try {
      band1NTF.model.value() = color()(0)
      band2NTF.model.value() = color()(1)
      band3NTF.model.value() = color()(2)
    } finally {
      updating = false
    }
  }

  val control: HBox = new HBox {
    children ++= Seq(band1NTF, band2NTF, band3NTF)
  }

  private def updateColor(): Unit = {
    if (!updating) {
      val numbers = allBands.map(_.model.value())
      // Check if ready
      if (numbers.contains(null)) return

      val doubles = numbers.map(_.doubleValue())
      color() = Color123(doubles(0), doubles(1), doubles(2))
    }
  }
}
