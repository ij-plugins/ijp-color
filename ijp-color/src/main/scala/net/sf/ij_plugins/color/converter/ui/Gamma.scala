/*
 * Image/J Plugins
 * Copyright (C) 2002-2013 Jarek Sacha
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

sealed case class Gamma(name: String, value: Double) {
  override def toString = name
}

object Gamma {
  val v10  = Gamma("1.0", 1.0)
  val v18  = Gamma("1.8", 1.8)
  val v22  = Gamma("2.2", 2.2)
  val sRGB = Gamma("sRGB", -2.2)
  val L    = Gamma("L*", 0.0)

  val values = List(v10, v18, v22, sRGB, L)
}
