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

  /** Compute color deference between color using CIE Delta E 200 formula.
    * See [[http://www.brucelindbloom.com/Eqn_DeltaE_CIE2000.html]]
    *
    * @param lab1 CIE L*a*b* sample 1
    * @param lab2 CIE L*a*b* sample 2
    * @return delta E 2000
    */
  def e2000(lab1: Array[Double], lab2: Array[Double]): Double = {
    validateLab(lab1)
    validateLab(lab2)
    e2000(
      ColorTriple.Lab(lab1(0), lab1(1), lab1(2)),
      ColorTriple.Lab(lab2(0), lab2(1), lab2(2)))
  }

  /** Compute color deference between color using CIE Delta E 200 formula.
    * See [[http://www.brucelindbloom.com/Eqn_DeltaE_CIE2000.html]]
    *
    * @param lab1 CIE L*a*b* sample 1
    * @param lab2 CIE L*a*b* sample 2
    * @return delta E 2000
    */
  def e2000(lab1: ColorTriple.Lab, lab2: ColorTriple.Lab,
            kL: Double = 1.0, kC: Double = 1.0, kH: Double = 1.0): Double = {

    val lBarPrime = 0.5 * (lab1.l + lab2.l)
    val c1 = Math.sqrt(lab1.a * lab1.a + lab1.b * lab1.b);
    val c2 = Math.sqrt(lab2.a * lab2.a + lab2.b * lab2.b)
    val cBar = 0.5 * (c1 + c2)
    val cBar7 = cBar * cBar * cBar * cBar * cBar * cBar * cBar;
    val g = 0.5 * (1.0 - Math.sqrt(cBar7 / (cBar7 + 6103515625.0)));
    /* 6103515625 = 25^7 */
    val a1Prime = lab1.a * (1.0 + g)
    val a2Prime = lab2.a * (1.0 + g)
    val c1Prime = Math.sqrt(a1Prime * a1Prime + lab1.b * lab1.b);
    val c2Prime = Math.sqrt(a2Prime * a2Prime + lab2.b * lab2.b);
    val cBarPrime = 0.5 * (c1Prime + c2Prime)
    var h1Prime = (Math.atan2(lab1.b, a1Prime) * 180.0) / Math.PI
    if (h1Prime < 0.0)
      h1Prime += 360.0;
    var h2Prime = (Math.atan2(lab2.b, a2Prime) * 180.0) / Math.PI
    if (h2Prime < 0.0)
      h2Prime += 360.0
    val hBarPrime =
      if (Math.abs(h1Prime - h2Prime) > 180.0)
        0.5 * (h1Prime + h2Prime + 360.0)
      else
        0.5 * (h1Prime + h2Prime)
    val t = 1.0 -
      0.17 * Math.cos(Math.PI * (hBarPrime - 30.0) / 180.0) +
      0.24 * Math.cos(Math.PI * (2.0 * hBarPrime) / 180.0) +
      0.32 * Math.cos(Math.PI * (3.0 * hBarPrime + 6.0) / 180.0) -
      0.20 * Math.cos(Math.PI * (4.0 * hBarPrime - 63.0) / 180.0);
    val dhPrime =
      if (Math.abs(h2Prime - h1Prime) <= 180.0)
        h2Prime - h1Prime
      else if (h2Prime <= h1Prime)
        h2Prime - h1Prime + 360.0
      else
        h2Prime - h1Prime - 360.0;
    val dLPrime = lab2.l - lab1.l
    val dCPrime = c2Prime - c1Prime
    val dHPrime = 2.0 * Math.sqrt(c1Prime * c2Prime) * Math.sin(Math.PI * (0.5 * dhPrime) / 180.0);
    val sL = 1.0 + ((0.015 * (lBarPrime - 50.0) * (lBarPrime - 50.0)) / Math.sqrt(20.0 + (lBarPrime - 50.0) * (lBarPrime - 50.0)))
    val sC = 1.0 + 0.045 * cBarPrime
    val sH = 1.0 + 0.015 * cBarPrime * t
    val dTheta = 30.0 * Math.exp(-((hBarPrime - 275.0) / 25.0) * ((hBarPrime - 275.0) / 25.0));
    val cBarPrime7 = cBarPrime * cBarPrime * cBarPrime * cBarPrime * cBarPrime * cBarPrime * cBarPrime;
    val rC = Math.sqrt(cBarPrime7 / (cBarPrime7 + 6103515625.0))
    val rT = -2.0 * rC * Math.sin(Math.PI * (2.0 * dTheta) / 180.0);
    Math.sqrt(
      (dLPrime / (kL * sL)) * (dLPrime / (kL * sL)) +
        (dCPrime / (kC * sC)) * (dCPrime / (kC * sC)) +
        (dHPrime / (kH * sH)) * (dHPrime / (kH * sH)) +
        (dCPrime / (kC * sC)) * (dHPrime / (kH * sH)) * rT)
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
