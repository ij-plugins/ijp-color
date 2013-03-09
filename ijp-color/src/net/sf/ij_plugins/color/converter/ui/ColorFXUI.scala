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

/** Helper methods for creation of UI in `net.sf.ij_plugins.color` module. */
object ColorFXUI {
  /** Return string representing URL to stylesheet used by `ColorFXUI` user interface.
    *
    * Returns empty string if stylesheet is not present.
    */
  def stylesheet: Seq[String] = {
    val stylesheetFileName = "modena.css"
    val stylesheetURL = getClass.getResource(stylesheetFileName)
    if (stylesheetURL != null)
      Array(stylesheetURL.toExternalForm)
    else {
      println("Cannot load stylesheet: " + stylesheetFileName + " relative  class: " + getClass + ".")
      Array[String]()
    }
  }
}
