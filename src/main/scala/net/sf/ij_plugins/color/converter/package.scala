/*
 * Image/J Plugins
 * Copyright (C) 2002-2017 Jarek Sacha
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
 * Latest release available at http://sourceforge.net/projects/ij-plugins/
 */

package net.sf.ij_plugins.color

import ij.IJ
import ij.process.ColorProcessor
import net.sf.ij_plugins.image.VectorImage

/** Color space conversions. */
package object converter {
  /** Convert between RGB and CIE L*a*b* color image representation.
    *
    * @param cp RGB image to be converted
    * @return CIE L*a*b* image represented by { @link VectorProcessor}.
    */
  def rgbToLab(cp: ColorProcessor): VectorImage = {
    val width = cp.getWidth
    val height = cp.getHeight
    val size = width * height
    val src = new VectorImage(cp)
    val dest = new VectorImage(width, height, 3)

    val progressStep: Int = Math.max(size / 10, 1)
    val converter = new ColorConverter(
      refWhite = ReferenceWhite.D65,
      rgbSpace = RGBWorkingSpace.sRGB,
      rgbScale = 255
    )
    for (i <- 0 until size) {
      if (i % progressStep == 0) {
        IJ.showProgress(i, size)
      }
      val rgb = src.getDouble(i)
      val xyz = converter.rgbToXYZ(rgb(0), rgb(1), rgb(2))
      val lab = converter.toLab(xyz).toArray
      dest.set(i, lab)
    }
    IJ.showProgress(size, size)
    dest
  }
}