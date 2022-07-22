/*
 * Image/J Plugins
 * Copyright (C) 2002-2022 Jarek Sacha
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

package ij_plugins.color.calibration

import ij.ImagePlus
import ij_plugins.color.DeltaE
import ij_plugins.color.calibration.chart.{ColorChart, ReferenceColorSpace}
import ij_plugins.color.calibration.regression.MappingMethod
import ij_plugins.color.converter.ColorTriple.Lab
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics

/**
 * @author
 *   Jarek Sacha
 */
object LOOCrossValidation {

  case class Deltas(deltaE: Double, deltaL: Double, deltaA: Double, deltaB: Double)

  case class CrossValidationData(
    referenceColorSpace: ReferenceColorSpace,
    method: MappingMethod,
    statsDeltaE: DescriptiveStatistics,
    statsDeltaL: DescriptiveStatistics,
    statsDeltaA: DescriptiveStatistics,
    statsDeltaB: DescriptiveStatistics
  )

  /**
   * Compute leave-one-out error for each of the chips in the chart.
   *
   * The error for a given chip is computed by excluding that chip from a chart. Color corrector is computed using all
   * but that selected chip, then a delta between value of that chip and color computed for that chip by the corrector
   * is determined (square root of sum of squares of color components differences).
   *
   * @return
   *   deltas when each of the chips, in turn, is excluded. Index of the delta is the same as of the excluded (test)
   *   chip.
   */
  def crossValidation(
    chart: ColorChart,
    referenceColorSpace: ReferenceColorSpace,
    mappingMethod: MappingMethod,
    image: ImagePlus
  ): IndexedSeq[Deltas] = {
    val observed: IndexedSeq[IndexedSeq[Double]] = chart.averageChipColor(image)

    crossValidation(chart, referenceColorSpace, mappingMethod, observed)
  }

  /**
   * Compute leave-one-out error for each of the chips in the chart.
   *
   * The error for a given chip is computed by excluding that chip from a chart. Color corrector is computed using all
   * but that selected chip, then a delta between value of that chip and color computed for that chip by the corrector
   * is determined (square root of sum of squares of color components differences).
   *
   * @return
   *   deltas when each of the chips, in turn, is excluded. Each element in the sequence is a tuple: (deltaE, deltaL,
   *   deltaA, deltaB)
   */
  def crossValidation(
                       chart: ColorChart,
                       referenceColorSpace: ReferenceColorSpace,
                       mappingMethod: MappingMethod,
                       observedSamples: IndexedSeq[IndexedSeq[Double]]
                     ): IndexedSeq[Deltas] = {

    require(chart.referenceChipsEnabled.length == observedSamples.length)

    val n = chart.referenceChips.size

    // A hack to match indices in a full chart with indices in observations - inset null observations
    val observedSamples2: IndexedSeq[IndexedSeq[Double]] = {
      val a2 = new Array[IndexedSeq[Double]](n)
      var ii = 0
      for (i <- 0 until n) {
        if (chart.enabled(i)) {
          a2(i) = observedSamples(ii)
          ii += 1
        } else {
          // Yeh, nulls are ugly, but we should not get into them if algorithm is fine, if we do we should crash any way...
          a2(i) = null
        }
      }
      a2.toIndexedSeq
    }

    val excludeInvalidRGBRef = false
    val clipReferenceRGB     = false

    val expectedColors = chart.referenceColor(referenceColorSpace)
    val enabledChips = expectedColors.zipWithIndex.map { case (c, i) =>
      if (excludeInvalidRGBRef && referenceColorSpace == ReferenceColorSpace.sRGB)
        c.forall(_ >= 0)
      else
        chart.enabled(i)
    }

    for (i <- 0 until n if enabledChips(i)) yield {
      // Disable i-th chip when computing calibration coefficients
      val enabled = enabledChips.updated(i, false)
      val leaveOneOutChart = chart.copyWithEnabled(enabled)

      // Compute color mapping coefficients
      val colorCalibrator = new ColorCalibrator(leaveOneOutChart, referenceColorSpace, mappingMethod, clipReferenceRGB)

      // Separate training and testing samples
      val observedTrain = observedSamples2.zipWithIndex.filter(v => enabled(v._2)).map(_._1)
      val observedTest  = observedSamples2(i)

      val fit       = colorCalibrator.computeCalibrationMapping(observedTrain)
      val corrector = fit.corrector

      // Measure correction quality on the disabled chip
      // Computation of the mean is be done in L*a*b*

      val correctedTest         = corrector.map(observedTest)
      val correctedTestLab      = referenceColorSpace.toLab(correctedTest, chart.refWhite)
      val expectedColorLab: Lab = referenceColorSpace.toLab(expectedColors(i), chart.refWhite)

      val deltaL = math.abs(expectedColorLab.l - correctedTestLab.l)
      val deltaA = math.abs(expectedColorLab.a - correctedTestLab.a)
      val deltaB = math.abs(expectedColorLab.b - correctedTestLab.b)

      // Delta needs to be computed in a manner independent of the reference color space so different reference
      // color spaces can be compared to each other. We will use CIE L*a*b* as common comparison space.
      val deltaE = DeltaE.e76(expectedColorLab, correctedTestLab)

      Deltas(deltaE, deltaL, deltaA, deltaB)
    }
  }

  def crossValidationStats(
    chart: ColorChart,
    referenceColorSpace: ReferenceColorSpace,
    mappingMethod: MappingMethod,
    image: ImagePlus
  ): CrossValidationData = {

    val observedSamples: IndexedSeq[IndexedSeq[Double]] = chart.averageChipColor(image)

    crossValidationStats(chart, referenceColorSpace, mappingMethod, observedSamples)
  }

  def crossValidationStats(
                            chart: ColorChart,
                            referenceColorSpace: ReferenceColorSpace,
                            mappingMethod: MappingMethod,
                            observedSamples: IndexedSeq[IndexedSeq[Double]]
                          ): CrossValidationData = {
    val _statsDeltaE = new DescriptiveStatistics()
    val _statsDeltaL = new DescriptiveStatistics()
    val _statsDeltaA = new DescriptiveStatistics()
    val _statsDeltaB = new DescriptiveStatistics()
    val deltas       = LOOCrossValidation.crossValidation(chart, referenceColorSpace, mappingMethod, observedSamples)
    deltas.foreach { case Deltas(deltaE, deltaL, deltaA, deltaB) =>
      _statsDeltaE.addValue(deltaE)
      _statsDeltaL.addValue(deltaL)
      _statsDeltaA.addValue(deltaA)
      _statsDeltaB.addValue(deltaB)
    }
    CrossValidationData(
      referenceColorSpace = referenceColorSpace,
      method = mappingMethod,
      statsDeltaE = _statsDeltaE,
      statsDeltaL = _statsDeltaL,
      statsDeltaA = _statsDeltaA,
      statsDeltaB = _statsDeltaB
    )
  }

  def crossValidationStatsAll(
    chart: ColorChart,
    image: ImagePlus,
    referenceColorSpaces: Seq[ReferenceColorSpace],
    mappingMethods: Seq[MappingMethod]
  ): Seq[CrossValidationData] = {

    val observedSamples: IndexedSeq[IndexedSeq[Double]] = chart.averageChipColorEnabled(image)

    crossValidationStatsAll(chart, observedSamples, referenceColorSpaces, mappingMethods)
  }

  def crossValidationStatsAll(
                               chart: ColorChart,
                               observedSamples: IndexedSeq[IndexedSeq[Double]],
                               referenceColorSpaces: Seq[ReferenceColorSpace],
                               mappingMethods: Seq[MappingMethod]
                             ): Seq[CrossValidationData] = {


    val refSpaceMethods = for (
      rcs <- referenceColorSpaces;
      method <- mappingMethods
    ) yield (rcs, method)

    for (((rcs, _method), _) <- refSpaceMethods.zipWithIndex) yield {
      LOOCrossValidation.crossValidationStats(chart, rcs, _method, observedSamples)
    }
  }

  //  def delta(a: Array[Double], b: Array[Double]): Double = {
  //    require(a.length == b.length, "Length of input arrays must match")
  //    val sunOfSquares = (a zip b).foldLeft(0d)((s, v) => {
  //      val v2 = v._1 - v._2
  //      s + (v2 * v2)
  //    })
  //    math.sqrt(sunOfSquares)
  //  }
  //
  //  def crop(imp: ImagePlus, roi: Rectangle): ImagePlus = {
  //    val dstStack = new ImageStack(roi.width, roi.height)
  //    val srcStack = imp.getStack
  //    for (i <- 1 to srcStack.getSize) {
  //      val ip = srcStack.getProcessor(i).duplicate()
  //      ip.setRoi(roi)
  //      dstStack.addSlice(ip.crop())
  //    }
  //
  //    new ImagePlus("", dstStack)
  //  }
}
