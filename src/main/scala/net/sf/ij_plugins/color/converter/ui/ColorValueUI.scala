/*
 * Image/J Plugins
 * Copyright (C) 2002-2019 Jarek Sacha
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

package net.sf.ij_plugins.color.converter.ui

import java.text.DecimalFormat

import net.sf.ij_plugins.color.converter.ColorTriple
import net.sf.ij_plugins.color.converter.ColorTriple.Color123
import net.sf.ij_plugins.color.converter.ui.ColorValueUI.NumberTextField

import scalafx.Includes._
import scalafx.beans.property.ObjectProperty
import scalafx.geometry.Pos
import scalafx.scene.control.{TextField, TextFormatter}
import scalafx.scene.layout.HBox
import scalafx.util.converter.FormatStringConverter

object ColorValueUI {
  private class NumberTextField extends TextField {
    private val format = new DecimalFormat("0.000000")
    private val converter = new FormatStringConverter[Number](format)
    val model = new TextFormatter(converter)
    textFormatter = model
  }
}

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


  val control = new HBox {
    children +=(band1NTF, band2NTF, band3NTF)
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
