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

package ij_plugins.color.calibration.chart

import enumeratum.{Enum, EnumEntry}
import ij.process.FloatProcessor
import ij_plugins.color.converter.{ColorConverter, ColorTriple, ReferenceWhite}

import scala.collection.immutable

/**
 * Color spaces used for creation of reference color values.
 *
 * Note that for proper correction to L*a*b* you need to keep track of the reference white (illuminant) of the reference
 * color values.
 */
sealed abstract class ReferenceColorSpace(override val entryName: String, bands: Array[String]) extends EnumEntry {

  val name: String = entryName

  override def toString: String = entryName

  private val _bands: Array[String] = bands.clone()

  def bandsNames: Array[String] = _bands.clone()

  /**
   * Convert color value from the current color space to CIE L*a*b*
   *
   * @param refWhite
   *   reference white of the reference color chart.
   */
  def toLab(c1: Double, c2: Double, c3: Double, refWhite: ReferenceWhite): ColorTriple.Lab = {
    this match {
      case ReferenceColorSpace.XYZ  => new ColorConverter(refWhite = refWhite).xyzToLab(c1, c2, c3)
      case ReferenceColorSpace.sRGB => new ColorConverter(refWhite = refWhite).toLab(ColorTriple.RGB(c1, c2, c3))
    }
  }

  /**
   * Convert color value from the current color space to CIE L*a*b*
   *
   * @param refWhite
   *   reference white of the reference color chart.
   */
  def toLab(c: Array[Double], refWhite: ReferenceWhite): ColorTriple.Lab = {
    require(c.length == 3)
    toLab(c(0), c(1), c(2), refWhite)
  }

  /**
   * Convert color value from the current color space to CIE L*a*b*
   *
   * @param refWhite
   *   reference white of the reference color chart.
   */
  def toLab(fps: Array[FloatProcessor], refWhite: ReferenceWhite): Array[FloatProcessor] = {
    require(fps.length == 3)
    val w       = fps(0).getWidth
    val h       = fps(0).getHeight
    val n       = w * h
    val lFP     = new FloatProcessor(w, h)
    val aFP     = new FloatProcessor(w, h)
    val bFP     = new FloatProcessor(w, h)
    val pixels0 = fps(0).getPixels.asInstanceOf[Array[Float]]
    val pixels1 = fps(1).getPixels.asInstanceOf[Array[Float]]
    val pixels2 = fps(2).getPixels.asInstanceOf[Array[Float]]
    val lPixels = lFP.getPixels.asInstanceOf[Array[Float]]
    val aPixels = aFP.getPixels.asInstanceOf[Array[Float]]
    val bPixels = bFP.getPixels.asInstanceOf[Array[Float]]
    for (i <- 0 until n) {
      val lab = toLab(pixels0(i), pixels1(i), pixels2(i), refWhite)
      lPixels(i) = lab.l.toFloat
      aPixels(i) = lab.a.toFloat
      bPixels(i) = lab.b.toFloat
    }
    Array(lFP, aFP, bFP)
  }
}

/** Enumeration of supported reference color spaces. */
case object ReferenceColorSpace extends Enum[ReferenceColorSpace] {

  /** CIE XYZ color space */
  case object XYZ extends ReferenceColorSpace("XYZ", Array("X", "Y", "Z"))

  /** sRGB color space */
  case object sRGB extends ReferenceColorSpace("sRGB", Array("Red", "Green", "Blue"))

  /** All refined reference color spaces. */
  val values: immutable.IndexedSeq[ReferenceColorSpace] = findValues
}
