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

import net.sf.ij_plugins.color.converter._
import net.sf.ij_plugins.color.converter.ui.ColorConverterModel.Update
import scalafx.beans.property.ObjectProperty

object ColorConverterModel {

  case class Update(lab: ColorTriple, xyz: ColorTriple, rgb: ColorTriple)

}

class ColorConverterModel {

  val referenceWhite      = new ObjectProperty(this, "referenceWhite", ReferenceWhite.D65)
  val rgbWorkingSpace     = new ObjectProperty(this, "rgbWorkingSpace", RGBWorkingSpace.sRGB)
  val gamma               = new ObjectProperty(this, "gamma", Gamma.sRGB)
  val chromaticAdaptation = new ObjectProperty[Option[ChromaticAdaptation]](
    this, "chromaticAdaptation", Some(ChromaticAdaptation.Bradford))

  /** Create new converter with current settings. */
  private def converter = new ColorConverter(referenceWhite(), rgbWorkingSpace(), chromaticAdaptation())

  def updateFromLab(lab: ColorTriple): Update = {
    // XYZ -> L*a*b*
    val xyz = converter.lab2XYZ(lab(0), lab(1), lab(2))
    // XYZ -> RGB
    val rgb = converter.xyz2RGB(xyz.x, xyz.y, xyz.z)
    Update(lab, xyz, rgb)
  }

  def updateFromRGB(rgb: ColorTriple): Update = {
    // RGB -> XYZ
    val xyz = converter.rgb2XYZ(rgb(0), rgb(1), rgb(2))
    // XYZ -> L*a*b*
    val lab = converter.xyz2Lab(xyz.x, xyz.y, xyz.z)
    Update(lab, xyz, rgb)
  }

  def updateFromXYZ(xyz: ColorTriple): Update = {
    // XYZ -> L*a*b*
    val lab = converter.xyz2Lab(xyz(0), xyz(1), xyz(2))
    // XYZ -> RGB
    val rgb = converter.xyz2RGB(xyz(0), xyz(1), xyz(2))
    Update(lab, xyz, rgb)
  }
}
