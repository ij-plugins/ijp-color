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

package ij_plugins.color.ui.util

import ij.Prefs

/** Helper for reading from ImageJ `Prefs` */
object IJPrefs {

  /**
    * Uses the keyword key to retrieve a boolean from the preferences file.
    * Returns `None` if key not found or value is `null`.
    */
  def getBooleanOption(key: String): Option[Boolean] = Option(Prefs.get(key, null.asInstanceOf[Boolean]))

  /**
    * Uses the keyword key to retrieve a double from the preferences file.
    * Returns `None` if key not found or value is `null`.
    */
  def getDoubleOption(key: String): Option[Double] = Option(Prefs.get(key, null.asInstanceOf[Double]))

  /**
    * Uses the keyword key to retrieve a string from the preferences file.
    * Returns `None` if key not found or value is `null`.
    */
  def getStringOption(key: String): Option[String] = Option(Prefs.get(key, null.asInstanceOf[String]))
}
