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

package net.sf.ij_plugins.color.calibration


/** Three polynomials representing mapping for three color bands. */
case class CubicPolynomialTriple(band1: CubicPolynomial,
                                 band2: CubicPolynomial,
                                 band3: CubicPolynomial) {

  /** Convert triplet `src` in source color space to triplet `dest` space to destination color space.
    *
    * @param src  array of size 3 representing triplet of colors in source color space.
    * @param dest array of size 3 representing triplet of colors in destination color space.
    */
  def evaluate(src: Array[Double], dest: Array[Double]) {
    dest(0) = band1.evaluate(src)
    dest(1) = band2.evaluate(src)
    dest(2) = band3.evaluate(src)
  }
}
