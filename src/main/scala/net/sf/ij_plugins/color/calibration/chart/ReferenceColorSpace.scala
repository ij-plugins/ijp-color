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

import net.sf.ij_plugins.color.converter.{ColorConverter, ColorTriple}
import ij.process.FloatProcessor

/** Color spaces used for creation of reference color values. */
sealed case class ReferenceColorSpace(name: String) {
  override def toString = name

  def bands: Array[String] = toString.toArray.takeRight(3).map(_.toString)

  /** Convert color value from the current color space to CIE L*a*b* */
  def toLab(c1: Double, c2: Double, c3: Double): ColorTriple.Lab = {
    this match {
      case ReferenceColorSpace.XYZ => new ColorConverter().xyzToLab(c1, c2, c3)
      case ReferenceColorSpace.sRGB => new ColorConverter().toLab(ColorTriple.RGB(c1, c2, c3))
    }
  }

  /** Convert color value from the current color space to CIE L*a*b* */
  def toLab(c: Array[Double]): ColorTriple.Lab = {
    require(c.length == 3)
    toLab(c(0), c(1), c(2))
  }

  /** Convert color value from the current color space to CIE L*a*b* */
  def toLab(fps: Array[FloatProcessor]): Array[FloatProcessor] = {
    require(fps.length == 3)
    val w = fps(0).getWidth
    val h = fps(0).getHeight
    val n = w * h
    val lFP = new FloatProcessor(w, h)
    val aFP = new FloatProcessor(w, h)
    val bFP = new FloatProcessor(w, h)
    val pixels0 = fps(0).getPixels.asInstanceOf[Array[Float]]
    val pixels1 = fps(1).getPixels.asInstanceOf[Array[Float]]
    val pixels2 = fps(2).getPixels.asInstanceOf[Array[Float]]
    val lPixels = lFP.getPixels.asInstanceOf[Array[Float]]
    val aPixels = aFP.getPixels.asInstanceOf[Array[Float]]
    val bPixels = bFP.getPixels.asInstanceOf[Array[Float]]
    for (i <- 0 until n) {
      val lab = toLab(pixels0(i), pixels1(i), pixels2(i))
      lPixels(i) = lab.l.toFloat
      aPixels(i) = lab.a.toFloat
      bPixels(i) = lab.b.toFloat
    }
    Array(lFP, aFP, bFP)
  }
}

/** Enumeration of supported reference color spacesa. */
object ReferenceColorSpace {
  /** CIE XYZ color space */
  val XYZ = ReferenceColorSpace("XYZ")
  /** sRGB color space */
  val sRGB = ReferenceColorSpace("sRGB")

  /** All refined reference color spaces. */
  val values = List(XYZ, sRGB)
}