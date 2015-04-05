/*
 * Image/J Plugins
 * Copyright (C) 2002-2015 Jarek Sacha
 * Author's email: jsacha at users dot sourceforge dot net
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
 * Latest release available at http://sourceforge.net/projects/ij-plugins/
 */

package net.sf.ij_plugins.color.converter.ui

import java.text.DecimalFormat

import net.sf.ij_plugins.color.converter.ColorTriple
import net.sf.ij_plugins.color.converter.ColorTriple.Color123

import scalafx.Includes._
import scalafx.beans.property.ObjectProperty
import scalafx.geometry.Pos
import scalafx.scene.layout.HBox


/** Row of number text fields holding color band values. */
class ColorValueUI {

  private val format = new DecimalFormat("0.000000")
  private val band1NTF = new DoubleNumberTextField(0, format)
  private val band2NTF = new DoubleNumberTextField(0, format)
  private val band3NTF = new DoubleNumberTextField(0, format)
  private val allBands = List(band1NTF, band2NTF, band3NTF)
  private var updating = false

  allBands.foreach(_.prefColumnCount = 6)
  allBands.foreach(_.setAlignment(Pos.CenterRight))
  allBands.foreach(_.numberProperty().onChange(updateColor()))

  val color = new ObjectProperty[ColorTriple](this, "color", new Color123())
  color.onChange {
    updating = true
    try {
      band1NTF.setNumber(color()(0))
      band2NTF.setNumber(color()(1))
      band3NTF.setNumber(color()(2))
    } finally {
      updating = false
    }
  }


  val control = new HBox {
    children +=(band1NTF, band2NTF, band3NTF)
  }

  private def updateColor() {
    if (!updating) {
      color() = new Color123(band1NTF.getNumber, band2NTF.getNumber, band3NTF.getNumber)
    }
  }
}
