/*
 * Image/J Plugins
 * Copyright (C) 2002-2023 Jarek Sacha
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

package ij_plugins.color.converter

import ij_plugins.color.converter.ReferenceWhite.{C, D50, D65, E}
import ij_plugins.color.util.EnumCompanion.{WithName, WithNameCompanion}

/**
 * Predefined RGB color working spaces.
 *
 * Each provides conversions between that RGB and CIE XYZ color space.
 *
 * The conversion from an RGB to CIE XYZ (and its inverse) are defined by: <ul> <li>red, green, and blue primaries,</li>
 * <li>gamma</li> <li>reference white (or white point)</li> </ul>
 *
 * Detailed information can be found on the
 * [[http://www.brucelindbloom.com/index.html?WorkingSpaceInfo.html Bruce Lindbloom's RGB Working Space Information]]
 * page.
 */
enum RGBWorkingSpace(
  val name: String,
  val xR: Double,
  val yR: Double,
  val xG: Double,
  val yG: Double,
  val xB: Double,
  val yB: Double,
  val refWhite: ReferenceWhite,
  val gamma: Double
) extends WithName {

  /** Adobe RGB (1998) */
  case AdobeRGB1998
      extends RGBWorkingSpace(
        "Adobe RGB (1998)",
        xR = 0.64,
        yR = 0.33,
        xG = 0.21,
        yG = 0.71,
        xB = 0.15,
        yB = 0.06,
        refWhite = D65,
        gamma = 2.2
      )

  /** AppleRGB */
  case AppleRGB
      extends RGBWorkingSpace(
        "Apple RGB",
        xR = 0.625,
        yR = 0.340,
        xG = 0.280,
        yG = 0.595,
        xB = 0.155,
        yB = 0.070,
        refWhite = D65,
        gamma = 1.8
      )

  /** Best RGB */
  case BestRGB
      extends RGBWorkingSpace(
        "Best RGB",
        xR = 0.7347,
        yR = 0.2653,
        xG = 0.2150,
        yG = 0.7750,
        xB = 0.1300,
        yB = 0.0350,
        refWhite = D50,
        gamma = 2.2
      )

  /** Beta RGB */
  case BetaRGB
      extends RGBWorkingSpace(
        "Beta RGB",
        xR = 0.6888,
        yR = 0.3112,
        xG = 0.1986,
        yG = 0.7551,
        xB = 0.1265,
        yB = 0.0352,
        refWhite = D50,
        gamma = 2.2
      )

  /** Bruce RGB */
  case BruceRGB
      extends RGBWorkingSpace(
        "Bruce RGB",
        xR = 0.64,
        yR = 0.33,
        xG = 0.28,
        yG = 0.65,
        xB = 0.15,
        yB = 0.06,
        refWhite = D65,
        gamma = 2.2
      )

  /** CIE RGB */
  case CIERGB
      extends RGBWorkingSpace(
        "CIE RGB",
        xR = 0.735,
        yR = 0.265,
        xG = 0.274,
        yG = 0.717,
        xB = 0.167,
        yB = 0.009,
        refWhite = E,
        gamma = 2.2
      )

  /** ColorMatch RGB */
  case ColorMatchRGB
      extends RGBWorkingSpace(
        "ColorMatch RGB",
        xR = 0.630,
        yR = 0.340,
        xG = 0.295,
        yG = 0.605,
        xB = 0.150,
        yB = 0.075,
        refWhite = D50,
        gamma = 1.8
      )

  /** Don RGB 4 */
  case DonRGB4
      extends RGBWorkingSpace(
        "Don RGB 4",
        xR = 0.696,
        yR = 0.300,
        xG = 0.215,
        yG = 0.765,
        xB = 0.130,
        yB = 0.035,
        refWhite = D50,
        gamma = 2.2
      )

  /** ECI RGB v2 */
  case ECIRGBv2
      extends RGBWorkingSpace(
        "ECI RGB v2",
        xR = 0.67,
        yR = 0.33,
        xG = 0.21,
        yG = 0.71,
        xB = 0.14,
        yB = 0.08,
        refWhite = D50,
        gamma = 0.0
      )

  /** Ekta Space PS5 */
  case EktaSpacePS5
      extends RGBWorkingSpace(
        "Ekta Space PS5",
        xR = 0.695,
        yR = 0.305,
        xG = 0.260,
        yG = 0.700,
        xB = 0.110,
        yB = 0.005,
        refWhite = D50,
        gamma = 2.2
      )

  /** NTSC RGB */
  case NTSCRGB
      extends RGBWorkingSpace(
        "NTSC RGB",
        xR = 0.67,
        yR = 0.33,
        xG = 0.21,
        yG = 0.71,
        xB = 0.14,
        yB = 0.08,
        refWhite = C,
        gamma = 2.2
      )

  /** PAL/SECAM RGB */
  case PALSECAMRGB
      extends RGBWorkingSpace(
        "PAL/SECAM RGB",
        xR = 0.64,
        yR = 0.33,
        xG = 0.29,
        yG = 0.60,
        xB = 0.15,
        yB = 0.06,
        refWhite = D65,
        gamma = 2.2
      )

  /** ProPhoto RGB */
  case ProPhotoRGB
      extends RGBWorkingSpace(
        "ProPhoto RGB",
        xR = 0.7347,
        yR = 0.2653,
        xG = 0.1596,
        yG = 0.8404,
        xB = 0.0366,
        yB = 0.0001,
        refWhite = D50,
        gamma = 1.8
      )

  /** SMPTE-C RGB */
  case SMPTE_CRGB
      extends RGBWorkingSpace(
        "SMPTE-C RGB",
        xR = 0.630,
        yR = 0.340,
        xG = 0.310,
        yG = 0.595,
        xB = 0.155,
        yB = 0.070,
        refWhite = D65,
        gamma = 2.2
      )

  /**
   * sRGB
   *
   * Note: The gamma of sRGB is not exactly 2.2, but rather, is a grafting together of two different functions,
   * that when viewed together, may be approximated by a simple 2.2 gamma curve.
   * See [[http://www.brucelindbloom.com/index.html?WorkingSpaceInfo.html Bruce Lindbloom's RGB Working Space Information]]
   */
  case sRGB
      extends RGBWorkingSpace(
        "sRGB",
        xR = 0.64,
        yR = 0.33,
        xG = 0.30,
        yG = 0.60,
        xB = 0.15,
        yB = 0.06,
        refWhite = D65,
        gamma = -2.2
      )

  /** Wide Gamut RGB */
  case WideGamutRGB
      extends RGBWorkingSpace(
        "Wide Gamut RGB",
        xR = 0.735,
        yR = 0.265,
        xG = 0.115,
        yG = 0.826,
        xB = 0.157,
        yB = 0.018,
        refWhite = D50,
        gamma = 2.2
      )

  private val m = new Matrix3x3(
    m00 = xR / yR,
    m01 = xG / yG,
    m02 = xB / yB,
    m10 = 1.0,
    m11 = 1.0,
    m12 = 1.0,
    m20 = (1.0 - xR - yR) / yR,
    m21 = (1.0 - xG - yG) / yG,
    m22 = (1.0 - xB - yB) / yB
  )
  private val mi = m.inverse

  private val sr = refWhite.x * mi.m00 + refWhite.y * mi.m01 + refWhite.z * mi.m02
  private val sg = refWhite.x * mi.m10 + refWhite.y * mi.m11 + refWhite.z * mi.m12
  private val sb = refWhite.x * mi.m20 + refWhite.y * mi.m21 + refWhite.z * mi.m22

  val rgb2xyz: Matrix3x3 = new Matrix3x3(
    m00 = sr * m.m00,
    m01 = sg * m.m01,
    m02 = sb * m.m02,
    m10 = sr * m.m10,
    m11 = sg * m.m11,
    m12 = sb * m.m12,
    m20 = sr * m.m20,
    m21 = sg * m.m21,
    m22 = sb * m.m22
  ).transpose

  val xyz2rgb: Matrix3x3 = rgb2xyz.inverse
}

object RGBWorkingSpace extends WithNameCompanion[RGBWorkingSpace]
