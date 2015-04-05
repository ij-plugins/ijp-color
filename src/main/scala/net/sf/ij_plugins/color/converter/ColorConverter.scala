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

package net.sf.ij_plugins.color.converter

import net.sf.ij_plugins.color.converter.ColorTriple.{Lab, RGB, XYZ}

import scala.math.pow

/** Color conversion constants */
object ColorConverter {

  val kE = 216.0 / 24389.0
  val kK = 24389.0 / 27.0
  val kKE = 8.0
}

/** Color converter based on information given at [[http://www.brucelindbloom.com brucelindbloom.com]].
  *
  * Conversion between CIE XYZ and sRGB is defined in the IEC 619602-1 standard
  * [[http://www.colour.org/tc8-05/Docs/colorspace/61966-2-1.pdf]],
  * though it uses approximated coefficients (compared to Lindbloom).
  *
  * Conversion between CIE XYZ and reference white points are defined in NIST E308 standard.
  *
  * @param refWhite reference white for CIE XYZ color space.
  * @param rgbSpace RGB working space (may have different reference white).
  * @param chromaticAdaptation chromatic adaptation used for conversions from/to RGB.
  * @param rgbScale scale used for RGB values, for instance, if scale is 255 the range of RGB values will be between
  *                 0 and 255.
  * @param xyzScale scale used for CIE XYZ values, for instance, if scale is 100 the range of CIE XYZ values will
  *                 be between 0 and 100.
  */
final class ColorConverter(val refWhite: ReferenceWhite = ReferenceWhite.D65,
                           val rgbSpace: RGBWorkingSpace = RGBWorkingSpace.sRGB,
                           val chromaticAdaptation: Option[ChromaticAdaptation] = Some(ChromaticAdaptation.Bradford),
                           val rgbScale: Double = 255,
                           val xyzScale: Double = 100) {

  import ColorConverter._

  /** Convert color from CIE L*a*b* to CIE XYZ color space. */
  def labToXYZ(l: Double, a: Double, b: Double): XYZ = {
    val fy = (l + 16.0) / 116.0
    val fx = 0.002 * a + fy
    val fz = fy - 0.005 * b

    val fx3 = fx * fx * fx
    val fz3 = fz * fz * fz

    val xr = if (fx3 > kE) fx3 else (116.0 * fx - 16.0) / kK
    val yr = if (l > kKE) math.pow((l + 16.0) / 116.0, 3.0) else l / kK
    val zr = if (fz3 > kE) fz3 else (116.0 * fz - 16.0) / kK

    val x = xr * refWhite.x * xyzScale
    val y = yr * refWhite.y * xyzScale
    val z = zr * refWhite.z * xyzScale

    XYZ(x, y, z)
  }

  /** Convert RGB to CIE XYZ color space. */
  def rgbToXYZ(r: Double, g: Double, b: Double): XYZ = {
    val r1 = invCompand(r / rgbScale)
    val g1 = invCompand(g / rgbScale)
    val b1 = invCompand(b / rgbScale)

    val rgb2xyz = rgbSpace.rgb2xyz
    val xyz = XYZ(
      (r1 * rgb2xyz.m00 + g1 * rgb2xyz.m10 + b1 * rgb2xyz.m20) * xyzScale,
      (r1 * rgb2xyz.m01 + g1 * rgb2xyz.m11 + b1 * rgb2xyz.m21) * xyzScale,
      (r1 * rgb2xyz.m02 + g1 * rgb2xyz.m12 + b1 * rgb2xyz.m22) * xyzScale
    )

    if (chromaticAdaptation.isEmpty || rgbSpace.refWhite == refWhite) {
      xyz
    } else {
      // TODO pre-compute for given reference white to optimize performance
      // See http://www.brucelindbloom.com/index.html?Eqn_ChromAdapt.html
      val ma = chromaticAdaptation.get.ma
      val ad = refWhite.x * ma.m00 + refWhite.y * ma.m10 + refWhite.z * ma.m20
      val bd = refWhite.x * ma.m01 + refWhite.y * ma.m11 + refWhite.z * ma.m21
      val cd = refWhite.x * ma.m02 + refWhite.y * ma.m12 + refWhite.z * ma.m22

      val refWhiteRGB = rgbSpace.refWhite
      val as = refWhiteRGB.x * ma.m00 + refWhiteRGB.y * ma.m10 + refWhiteRGB.z * ma.m20
      val bs = refWhiteRGB.x * ma.m01 + refWhiteRGB.y * ma.m11 + refWhiteRGB.z * ma.m21
      val cs = refWhiteRGB.x * ma.m02 + refWhiteRGB.y * ma.m12 + refWhiteRGB.z * ma.m22

      val x1 = xyz.x
      val y1 = xyz.y
      val z1 = xyz.z

      var x2 = x1 * ma.m00 + y1 * ma.m10 + z1 * ma.m20
      var y2 = x1 * ma.m01 + y1 * ma.m11 + z1 * ma.m21
      var z2 = x1 * ma.m02 + y1 * ma.m12 + z1 * ma.m22

      x2 *= (ad / as)
      y2 *= (bd / bs)
      z2 *= (cd / cs)

      val maI = chromaticAdaptation.get.maI
      val x3 = x2 * maI.m00 + y2 * maI.m10 + z2 * maI.m20
      val y3 = x2 * maI.m01 + y2 * maI.m11 + z2 * maI.m21
      val z3 = x2 * maI.m02 + y2 * maI.m12 + z2 * maI.m22
      XYZ(x3, y3, z3)
    }
  }


  /** Convert color from RGB to CIE L*a*b* color space. */
  @inline
  def toLab(rgb: RGB): Lab = toLab(toXYZ(rgb))


  /** Convert CIE XYZ to CIE L*a*b* color space. */
  @inline
  def toLab(xyz: XYZ): Lab = xyzToLab(xyz.x, xyz.y, xyz.z)


  /** Convert color from CIE L*a*b* to RGB color space. */
  @inline
  def toRGB(lab: Lab): RGB = toRGB(toXYZ(lab))


  /** Convert CIE XYZ to an RGB color space. */
  @inline
  def toRGB(xyz: XYZ): RGB = xyzToRGB(xyz.x, xyz.y, xyz.z)


  /** Convert color from CIE L*a*b* to CIE XYZ color space. */
  @inline
  def toXYZ(lab: Lab): XYZ = labToXYZ(lab.l, lab.a, lab.b)


  /** Convert RGB to CIE XYZ color space. */
  @inline
  def toXYZ(rgb: RGB): XYZ = rgbToXYZ(rgb.r, rgb.g, rgb.b)


  /** Convert CIE XYZ to CIE L*a*b* color space. */
  def xyzToLab(x: Double, y: Double, z: Double): Lab = {
    val xr = x / refWhite.x / xyzScale
    val yr = y / refWhite.y / xyzScale
    val zr = z / refWhite.z / xyzScale

    val fx = if (xr > kE) pow(xr, 1.0 / 3.0) else (kK * xr + 16.0) / 116.0
    val fy = if (yr > kE) pow(yr, 1.0 / 3.0) else (kK * yr + 16.0) / 116.0
    val fz = if (zr > kE) pow(zr, 1.0 / 3.0) else (kK * zr + 16.0) / 116.0

    val l = 116.0 * fy - 16.0
    val a = 500.0 * (fx - fy)
    val b = 200.0 * (fy - fz)

    Lab(l, a, b)
  }


  /** Convert CIE XYZ to an RGB color space. */
  def xyzToRGB(x: Double, y: Double, z: Double): RGB = {

    val x1 = x / xyzScale
    val y1 = y / xyzScale
    val z1 = z / xyzScale

    val (x2, y2, z2) = if (chromaticAdaptation.isDefined) {
      val ma = chromaticAdaptation.get.ma
      val maI = chromaticAdaptation.get.maI

      val As = refWhite.x * ma.m00 + refWhite.y * ma.m10 + refWhite.z * ma.m20
      val Bs = refWhite.x * ma.m01 + refWhite.y * ma.m11 + refWhite.z * ma.m21
      val Cs = refWhite.x * ma.m02 + refWhite.y * ma.m12 + refWhite.z * ma.m22

      val refWhiteRGB = rgbSpace.refWhite
      val Ad = refWhiteRGB.x * ma.m00 + refWhiteRGB.y * ma.m10 + refWhiteRGB.z * ma.m20
      val Bd = refWhiteRGB.x * ma.m01 + refWhiteRGB.y * ma.m11 + refWhiteRGB.z * ma.m21
      val Cd = refWhiteRGB.x * ma.m02 + refWhiteRGB.y * ma.m12 + refWhiteRGB.z * ma.m22

      var x2 = x1 * ma.m00 + y1 * ma.m10 + z1 * ma.m20
      var y2 = x1 * ma.m01 + y1 * ma.m11 + z1 * ma.m21
      var z2 = x1 * ma.m02 + y1 * ma.m12 + z1 * ma.m22

      x2 *= (Ad / As)
      y2 *= (Bd / Bs)
      z2 *= (Cd / Cs)

      val x3 = x2 * maI.m00 + y2 * maI.m10 + z2 * maI.m20
      val y3 = x2 * maI.m01 + y2 * maI.m11 + z2 * maI.m21
      val z3 = x2 * maI.m02 + y2 * maI.m12 + z2 * maI.m22
      (x3, y3, z3)
    } else {
      (x1, y1, z1)
    }

    val xyz2rgb = rgbSpace.xyz2rgb
    val r = compand(x2 * xyz2rgb.m00 + y2 * xyz2rgb.m10 + z2 * xyz2rgb.m20) * rgbScale
    val g = compand(x2 * xyz2rgb.m01 + y2 * xyz2rgb.m11 + z2 * xyz2rgb.m21) * rgbScale
    val b = compand(x2 * xyz2rgb.m02 + y2 * xyz2rgb.m12 + z2 * xyz2rgb.m22) * rgbScale

    RGB(r, g, b)
  }

  private def compand(linear: Double): Double = {
    rgbSpace match {
      case RGBWorkingSpace.sRGB =>
        assert(rgbSpace.gamma < 0)
        val (l, sign) = if (linear < 0.0) (-linear, -1.0) else (linear, 1.0)
        val c = if (l <= 0.0031308) l * 12.92 else 1.055 * math.pow(l, 1.0 / 2.4) - 0.055
        c * sign
      case RGBWorkingSpace.ECIRGBv2 =>
        assert(rgbSpace.gamma == 0)
        val (l, sign) = if (linear < 0.0) (-linear, -1.0) else (linear, 1.0)
        val c = if (l <= (216.0 / 24389.0)) l * 24389.0 / 2700.0 else 1.16 * math.pow(l, 1.0 / 3.0) - 0.16
        c * sign
      case _ =>
        assert(rgbSpace.gamma > 0)
        if (linear >= 0.0) math.pow(linear, 1.0 / rgbSpace.gamma) else -math.pow(-linear, 1.0 / rgbSpace.gamma)
    }
  }

  private def invCompand(companded: Double): Double =
    if (rgbSpace.gamma > 0.0) {
      if (companded >= 0.0) pow(companded, rgbSpace.gamma) else -pow(-companded, rgbSpace.gamma)
    }
    else if (rgbSpace.gamma < 0.0) {
      /* sRGB */
      val (c, sign) = if (companded < 0.0) {
        (-companded, -1.0d)
      } else {
        (companded, 1.0d)
      }
      sign * (if (c <= 0.04045) c / 12.92 else pow((c + 0.055) / 1.055, 2.4))
    } else {
      /* L* */
      val (c, sign) = if (companded < 0.0) {
        (-companded, -1)
      } else {
        (companded, 1)
      }
      sign * (if (c <= 0.08) {
        2700.0 * companded / 24389.0
      } else {
        (((1000000.0 * c + 480000.0) * c + 76800.0) * c + 4096.0) / 1560896.0
      })
    }
}

