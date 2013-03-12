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

import net.sf.ij_plugins.color.converter.ColorTriple.Lab


/** Enumeration of some common color charts. */
object ColorCharts {

  /** GretagMacbeth ColorChecker with values measured by Robin D. Myers, average of two charts manufactured 2002-10.
    *
    * Illuminant D65.
    * [[http://www.rmimaging.com/spectral_library/Reflective/Charts-Calibration/ColorChecker_2002-10_averaged.ss3.zip]]
    */
  val GretagMacbethColorChecker = new GridColorChart(
    "GretagMacbeth ColorChecker", 6, 4,
    List(
      ("Dark skin", Lab(40.59, 14.68, 16.83)),
      ("Light skin", Lab(68.22, 22.21, 20.33)),
      ("Blue sky", Lab(49.31, -9.20, -23.05)),
      ("Foliage", Lab(43.77, -10.79, 21.11)),
      ("Blue flower", Lab(55.43, 5.70, -24.63)),
      ("Bluish green", Lab(69.08, -32.29, -4.97)),
      ("Orange", Lab(66.58, 35.42, 62.91)),
      ("Purplish blue", Lab(39.04, -0.66, -45.20)),
      ("Moderate red", Lab(55.99, 47.72, 24.62)),
      ("Purple", Lab(32.00, 16.88, -17.93)),
      ("Yellow green", Lab(72.59, -16.94, 52.81)),
      ("Orange yellow", Lab(75.48, 21.52, 72.46)),
      ("Blue", Lab(27.43, 0.79, -52.09)),
      ("Green", Lab(53.91, -31.90, 25.47)),
      ("Red", Lab(47.42, 56.34, 36.45)),
      ("Yellow", Lab(84.79, 9.91, 79.58)),
      ("Magenta", Lab(55.99, 47.25, -6.84)),
      ("Cyan", Lab(48.66, -31.92, -34.03)),
      ("White", Lab(96.46, 0.38, 3.09)),
      ("Neutral 8", Lab(81.67, -0.58, 0.13)),
      ("Neutral 6.5", Lab(66.47, -0.55, 0.01)),
      ("Neutral 5", Lab(50.58, -1.29, -0.32)),
      ("Neutral 3.5", Lab(36.19, -0.71, -0.30)),
      ("Black", Lab(20.68, -0.03, -0.50))
    )
  )

  /** X-Rite Passport ColorChecker chart, based on the values from "ColorChecker Passport Technical Review"
    *
    * Illuminant D65.
    * [[http://www.rmimaging.com/information/ColorChecker_Passport_Technical_Report.pdf]]
    */
  val XRitePassportColorChecker = new GridColorChart("X-Rite Passport", 6, 4,
    List(
      ("Dark Skin", Lab(38.96, 12.13, 13.84)),
      ("Light Skin", Lab(65.50, 15.59, 16.81)),
      ("Blue Sky", Lab(50.69, -2.09, -21.75)),
      ("Foliage", Lab(43.92, -13.33, 22.19)),
      ("Blue Flower", Lab(56.01, 10.88, -24.39)),
      ("Bluish Green", Lab(71.84, -32.97, 1.91)),
      ("Orange", Lab(61.81, 32.91, 55.95)),
      ("Purplish Blue", Lab(41.33, 17.83, -46.95)),
      ("Moderate Red", Lab(50.35, 47.10, 15.00)),
      ("Purple", Lab(30.48, 24.51, -21.61)),
      ("Yellow Green", Lab(73.21, -26.94, 59.01)),
      ("Orange Yellow", Lab(71.32, 16.60, 67.24)),
      ("Blue", Lab(31.16, 22.38, -50.04)),
      ("Green", Lab(56.90, -41.24, 35.11)),
      ("Red", Lab(41.88, 48.07, 26.22)),
      ("Yellow", Lab(82.45, -1.08, 81.57)),
      ("Magenta", Lab(52.05, 49.70, -16.34)),
      ("Cyan", Lab(52.40, -24.88, -25.64)),
      ("White", Lab(97.94, -0.96, 2.26)),
      ("Neutral 8", Lab(82.33, -0.60, 0.26)),
      ("Neutral 6.5", Lab(67.43, -0.71, 0.24)),
      ("Neutral 5", Lab(51.31, -0.10, 0.20)),
      ("Neutral 3.5", Lab(36.20, -0.59, -0.73)),
      ("Black", Lab(20.44, 0.12, -0.54))
    )
  )

  /** All pre-defined color charts */
  val values = List(GretagMacbethColorChecker, XRitePassportColorChecker)
}
