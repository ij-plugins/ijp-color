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

package net.sf.ij_plugins.color

import net.sf.ij_plugins.color.converter.ColorTriple

import scala.math._

/** Formulas for computation of color differences. */
object DeltaE {

  private case class DeltaE94(kL: Double, k1: Double, k2: Double) {
    private val kC = 1
    private val kH = 1
    private val sL = 1

    def apply(referenceLab: Array[Double], sampleLab: Array[Double]): Double = {
      validateLab(referenceLab)
      validateLab(sampleLab)

      val l1 = referenceLab(0)
      val a1 = referenceLab(1)
      val b1 = referenceLab(2)
      val l2 = sampleLab(0)
      val a2 = sampleLab(1)
      val b2 = sampleLab(2)

      val c1 = sqrt(a1 * a1 + b1 * b1)
      val c2 = sqrt(a2 * a2 + b2 * b2)
      val sC = 1.0 + k1 * c1
      val sH = 1.0 + k2 * c1

      val dL = l2 - l1
      val da = a2 - a1
      val db = b2 - b1
      val dc = c1 - c2

      val dh2 = da * da + db * db - dc * dc

      sqrt((dL * dL) / (sL * sL * kL * kL) +
        (dc * dc) / (kC * kC * sC * sC) +
        dh2 / (kH * kH * sH * sH)
      )
    }
  }

  private val DeltaE94GraphicArts = DeltaE94(kL = 1, k1 = 0.045, k2 = 0.015)
  private val DeltaE94Textiles = DeltaE94(kL = 2, k1 = 0.048, k2 = 0.014)

  /** Compute color deference between color using CIE Delta E 1976 formula
    * (equivalent to Euclidean distance in L*a*b* space).
    * See http://www.brucelindbloom.com/index.html?Eqn_DeltaE_CIE94.html
    *
    * @param lab1 CIE L*a*b* reference
    * @param lab2 CIE L*a*b* sample
    * @return delta E
    */
  def e76(lab1: Array[Double], lab2: Array[Double]): Double = {
    validateLab(lab1)
    validateLab(lab2)

    var sum: Double = 0
    for (i <- 0 until 3) {
      val d = lab1(i) - lab2(i)
      sum += d * d
    }
    sqrt(sum)
  }

  /** Compute color deference between color using CIE Delta E 1976 formula
    * (equivalent to Euclidean distance in L*a*b* space).
    * See http://www.brucelindbloom.com/index.html?Eqn_DeltaE_CIE94.html
    *
    * @param lab1 CIE L*a*b* reference
    * @param lab2 CIE L*a*b* sample
    * @return delta E
    */
  def e76(lab1: ColorTriple.Lab, lab2: ColorTriple.Lab): Double = e76(lab1.toArray, lab2.toArray)

  /** Compute color deference between color using CIE Delta E 1994 formula, assuming graphic arts constants.
    * See [[http://www.brucelindbloom.com/index.html?Eqn_DeltaE_CIE94.html]]
    *
    * @param referenceLab CIE L*a*b* reference
    * @param sampleLab    CIE L*a*b* sample
    * @return delta E
    * @see #e94Textiles
    */
  def e94GraphicArts(referenceLab: Array[Double], sampleLab: Array[Double]): Double = {
    DeltaE94GraphicArts(referenceLab, sampleLab)
  }

  /** Compute color deference between color using CIE Delta E 1994 formula, assuming graphic arts constants.
    * See [[http://www.brucelindbloom.com/index.html?Eqn_DeltaE_CIE94.html]]
    *
    * @param referenceLab CIE L*a*b* reference
    * @param sampleLab    CIE L*a*b* sample
    * @return delta E
    * @see #e94Textiles
    */
  def e94GraphicArts(referenceLab: ColorTriple.Lab, sampleLab: ColorTriple.Lab): Double =
    e94GraphicArts(referenceLab.toArray, sampleLab.toArray)

  /** Compute color deference between color using CIE Delta E 1994 formula, assuming textile constants.
    * See [[http://www.brucelindbloom.com/index.html?Eqn_DeltaE_CIE94.html]]
    *
    * @param referenceLab CIE L*a*b* reference
    * @param sampleLab    CIE L*a*b* sample
    * @return delta E
    * @see #e94GraphicArts
    */
  def e94Textiles(referenceLab: Array[Double], sampleLab: Array[Double]): Double = {
    DeltaE94Textiles(referenceLab, sampleLab)
  }

  /** Compute color deference between color using CIE Delta E 1994 formula, assuming textile constants.
    * See [[http://www.brucelindbloom.com/index.html?Eqn_DeltaE_CIE94.html]]
    *
    * @param referenceLab CIE L*a*b* reference
    * @param sampleLab    CIE L*a*b* sample
    * @return delta E
    * @see #e94GraphicArts
    */
  def e94Textiles(referenceLab: ColorTriple.Lab, sampleLab: ColorTriple.Lab): Double =
    e94Textiles(referenceLab.toArray, sampleLab.toArray)

  /** Compute color deference between color using Delta CMC(l:c) formula,
    * see [[http://www.brucelindbloom.com/Eqn_DeltaE_CMC.html]]
    *
    * @param referenceLab CIE L*a*b* reference
    * @param sampleLab    CIE L*a*b* sample
    * @param l            lightness weight, typically equal 1 (for imperceptibility) or 2 (for acceptability).
    * @param c            chroma weight, typically equal 1.
    * @see #cmcAcceptability
    * @see #cmcPerceptibility
    */
  def cmc(referenceLab: Array[Double], sampleLab: Array[Double], l: Double, c: Double): Double = {
    validateLab(referenceLab)
    validateLab(sampleLab)

    val l1 = referenceLab(0)
    val a1 = referenceLab(1)
    val b1 = referenceLab(2)
    val l2 = sampleLab(0)
    val a2 = sampleLab(1)
    val b2 = sampleLab(2)

    val c1 = sqrt(a1 * a1 + b1 * b1)
    val c2 = sqrt(a2 * a2 + b2 * b2)

    val h1 = {
      val hh = toDegrees(atan2(b1, a1))
      if (hh < 0) hh + 360 else if (hh > 360) hh - 360 else hh
    }

    val c14 = c1 * c1 * c1 * c1
    val f = sqrt(c14 / (c14 + 1900))
    val t = if (164 <= h1 && h1 <= 345)
      .56 + abs(.2 * cos(toRadians(h1 + 168)))
    else
      .36 + abs(.4 * cos(toRadians(h1 + 35)))

    val sL = if (l1 < 16) 0.511 else 0.040975 * l1 / (1 + 0.01765 * l1)
    val sC = 0.0638 * c1 / (1 + 0.0131 * c1) + 0.638
    val sH = sC * (f * t + 1 - f)

    val dL = l2 - l1
    val da = a2 - a1
    val db = b2 - b1
    val dc = c1 - c2

    val dh2 = da * da + db * db - dc * dc

    sqrt((dL * dL) / (l * l * sL * sL) +
      (dc * dc) / (c * c * sC * sC) +
      dh2 / (sH * sH)
    )
  }

  /** Compute color deference between color using Delta CMC(l:c) formula,
    * see [[http://www.brucelindbloom.com/Eqn_DeltaE_CMC.html]]
    *
    * @param referenceLab CIE L*a*b* reference
    * @param sampleLab    CIE L*a*b* sample
    * @param l            lightness weight, typically equal 1 (for imperceptibility) or 2 (for acceptability).
    * @param c            chroma weight, typically equal 1.
    * @see #cmcAcceptability
    * @see #cmcPerceptibility
    */
  def cmc(referenceLab: ColorTriple.Lab, sampleLab: ColorTriple.Lab, l: Double, c: Double): Double =
    cmc(referenceLab.toArray, sampleLab.toArray, l, c)


  /** Compute color deference between color using Delta CMC(2:1) formula "for acceptability",
    * see [[http://www.brucelindbloom.com/Eqn_DeltaE_CMC.html]]
    *
    * @param referenceLab CIE L*a*b* reference
    * @param sampleLab    CIE L*a*b* sample
    * @see #cmc
    * @see #cmcPerceptibility
    */
  def cmcAcceptability(referenceLab: Array[Double], sampleLab: Array[Double]): Double =
    cmc(referenceLab, sampleLab, 2, 1)

  /** Compute color deference between color using Delta CMC(1:c) formula "for imperceptibility",
    * see [[http://www.brucelindbloom.com/Eqn_DeltaE_CMC.html]]
    *
    * @param referenceLab CIE L*a*b* reference
    * @param sampleLab    CIE L*a*b* sample
    * @see #cmcAcceptability
    * @see #cmc
    */
  def cmcPerceptibility(referenceLab: Array[Double], sampleLab: Array[Double]): Double =
    cmc(referenceLab, sampleLab, 1, 1)

  private def validateLab(lab: Array[Double]): Unit = {
    require(lab.length == 3)
  }
}
