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

package net.sf.ij_plugins.color.calibration.chart

/** Color spaces used for creation of reference color values. */
sealed case class ReferenceColorSpace(name: String) {
  override def toString = name

  def bands: Array[String] = toString.toArray.takeRight(3).map(_.toString)
}

/** Enumeration of supported reference color spacesa. */
object ReferenceColorSpace {
  /** CIE XYZ color space */
  val XYZ  = ReferenceColorSpace("XYZ")
  /** sRGB color space */
  val sRGB = ReferenceColorSpace("sRGB")

  /** All refined reference color spaces. */
  val values = List(XYZ, sRGB)
}
