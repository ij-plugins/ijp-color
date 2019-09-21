/*
 * Image/J Plugins
 * Copyright (C) 2002-2019 Jarek Sacha
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

package net.sf.ij_plugins.color.converter

import net.sf.ij_plugins.color.converter.ColorTriple.{Lab, RGB, XYZ}
import net.sf.ij_plugins.color.converter.RGBWorkingSpace.AdobeRGB1998
import org.scalatest.FlatSpec
import org.scalatest.Matchers._

/**
  * @author Jarek Sacha
  * @since 10/16/12 8:14 PM
  */
class ColorConverterTest extends FlatSpec {
  /**
    * From http://www.brucelindbloom.com/downloads/ColorCheckerSpreadsheets.zip, assumes D65 and 2 degree observer
    */
  private val labColorChecker: Array[Lab] = Array(
    Lab(37.257, 12.752, 14.852),
    Lab(65.959, 13.538, 17.198),
    Lab(50.585, -1.579, -21.292),
    Lab(43.188, -16.045, 21.953),
    Lab(55.662, 11.215, -25.042),
    Lab(71.232, -31.825, 1.475),
    Lab(60.536, 31.374, 58.339),
    Lab(40.523, 15.504, -42.489),
    Lab(50.358, 45.394, 14.489),
    Lab(30.572, 23.491, -22.344),
    Lab(71.98, -26.832, 58.562),
    Lab(71.789, 15.034, 67.043),
    Lab(29.594, 26.884, -52.691),
    Lab(55.523, -41.03, 34.925),
    Lab(40.366, 53.386, 26.102),
    Lab(81.7, -1.245, 79.398),
    Lab(50.394, 49.685, -15.704),
    Lab(95.37, -0.644, 2.583),
    Lab(80.984, -0.03, 0.27),
    Lab(66.246, -0.101, 0.056),
    Lab(51.244, -0.046, 0.662),
    Lab(35.379, -0.118, -0.144),
    Lab(20.522, 0.352, -0.201)
  )
  /**
    * Converted to XYZ from using http://www.brucelindbloom.com/iPhone/ColorConv.html
    * D65, no adaptation, x100.
    */
  private val xyzColorChecker: Array[XYZ] = Array(
    XYZ(0.108176, 0.096773, 0.062064),
    XYZ(0.375276, 0.352709, 0.260194),
    XYZ(0.176810, 0.189128, 0.343071)
  )
  //  /**
  //    * Converted sRGB from using http://www.brucelindbloom.com/iPhone/ColorConv.html
  //    * D65, no adaptation, x255.
  //    */
  //  private val srgbColorChecker: Array[RGB] = Array(
  //    RGB(114.8130, 79.5373, 64.3926),
  //    RGB(194.7544, 150.8414, 130.2082),
  //    RGB(93.7033, 122.8897, 156.3199)
  //  )

  "Color Converter" should "convert RGB to L*a*b*" in {
    val converter = new ColorConverter(ReferenceWhite.D50, AdobeRGB1998, None, rgbScale = 1, xyzScale = 1)

    val rgb = RGB(178 / 255d, 217 / 255d, 18 / 255d)
    val lab = converter.toLab(rgb)
    lab.l should be(80.4570 +- 0.0001)
    lab.a should be(-45.3046 +- 0.0001)
    lab.b should be(80.6919 +- 0.0001)
  }

  it should "convert L*a*b* to RGB" in {
    val converter = new ColorConverter(ReferenceWhite.D50, AdobeRGB1998, None, rgbScale = 255, xyzScale = 1)

    val lab = Lab(80.4570, -45.3046, 80.6919)
    val rgb = converter.toRGB(lab)
    rgb.r should be(178.0 +- 0.0001)
    rgb.g should be(217.0 +- 0.0001)
    rgb.b should be(18.0 +- 0.0001)
  }


  it should "convert RGB to XYZ" in {
    val converter = new ColorConverter(ReferenceWhite.D50, AdobeRGB1998, None, rgbScale = 1, xyzScale = 1)

    val rgb = RGB(178 / 255d, 217 / 255d, 18 / 255d)
    val xyz = converter.rgbToXYZ(rgb.r, rgb.g, rgb.b)
    xyz.x should be(0.392179 +- 0.00001)
    xyz.y should be(0.574946 +- 0.00001)
    xyz.z should be(0.064729 +- 0.00001)
  }

  it should "convert (RGB) to XYZ" in {
    val converter = new ColorConverter(ReferenceWhite.D50, AdobeRGB1998, None, rgbScale = 1, xyzScale = 1)

    val rgb = RGB(178 / 255d, 217 / 255d, 18 / 255d)
    val xyz = converter.toXYZ(rgb)
    xyz.x should be(0.392179 +- 0.00001)
    xyz.y should be(0.574946 +- 0.00001)
    xyz.z should be(0.064729 +- 0.00001)
  }


  it should "convert RGB to XYZ with scaled XYZ" in {
    val xyzScale = 100
    val converter = new ColorConverter(ReferenceWhite.D50, AdobeRGB1998, None, rgbScale = 1, xyzScale = xyzScale)

    val rgb = RGB(178 / 255d, 217 / 255d, 18 / 255d)
    val xyz = converter.rgbToXYZ(rgb.r, rgb.g, rgb.b)
    xyz.x should be(0.392179 * xyzScale +- 0.00001 * xyzScale)
    xyz.y should be(0.574946 * xyzScale +- 0.00001 * xyzScale)
    xyz.z should be(0.064729 * xyzScale +- 0.00001 * xyzScale)
  }


  it should "convert RGB to XYZ with scaled RGB" in {
    val converter = new ColorConverter(ReferenceWhite.D50, AdobeRGB1998, None, rgbScale = 255, xyzScale = 1)

    val rgb = RGB(178, 217, 18)
    val xyz = converter.rgbToXYZ(rgb.r, rgb.g, rgb.b)
    xyz.x should be(0.392179 +- 0.00001)
    xyz.y should be(0.574946 +- 0.00001)
    xyz.z should be(0.064729 +- 0.00001)
  }


  it should "convert AdobeRGB1998 to XYZ/D50 with chromatic adaptation" in {
    val converter = new ColorConverter(
      ReferenceWhite.D50, AdobeRGB1998, Some(ChromaticAdaptation.Bradford),
      rgbScale = 255, xyzScale = 1
    )

    val rgb = RGB(178, 217, 18)
    val xyz = converter.rgbToXYZ(rgb.r, rgb.g, rgb.b)
    xyz.x should be(0.420844 +- 0.00001)
    xyz.y should be(0.579957 +- 0.00001)
    xyz.z should be(0.053712 +- 0.00001)
  }

  it should "convert XYZ to L*a*b*" in {
    val converter = new ColorConverter(ReferenceWhite.D65, RGBWorkingSpace.sRGB, None, xyzScale = 1)

    for (i <- xyzColorChecker.indices) {
      val xyz = xyzColorChecker(i)
      val expectedLab = labColorChecker(i)
      val actualLab = converter.toLab(xyz)
      actualLab.l should be(expectedLab.l +- 0.001)
      actualLab.a should be(expectedLab.a +- 0.001)
      actualLab.b should be(expectedLab.b +- 0.001)
    }
  }

  it should "convert XYZ to L*a*b* (Purple 1)" in {
    val converter = new ColorConverter(
      refWhite = ReferenceWhite.D65,
      rgbSpace = RGBWorkingSpace.sRGB,
      chromaticAdaptation = None,
      xyzScale = 100,
      rgbScale = 255)

    val xyz = XYZ(9.31, 7.11, 15.33)
    // Expected values computed using http://www.brucelindbloom.com/index.html?ColorCalculator.html
    val expectedLab = Lab(32.0560, 23.3458, -21.1905)
    val actualLab = converter.toLab(xyz)
    actualLab.l should be(expectedLab.l +- 0.0001)
    actualLab.a should be(expectedLab.a +- 0.0001)
    actualLab.b should be(expectedLab.b +- 0.0001)
  }

}
