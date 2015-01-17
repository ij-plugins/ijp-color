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

package net.sf.ij_plugins.color.calibration

import ij.{ImageStack, ImagePlus}
import java.awt.Rectangle
import net.sf.ij_plugins.color.DeltaE
import net.sf.ij_plugins.color.calibration.chart.{ReferenceColorSpace, ColorChart}
import net.sf.ij_plugins.color.calibration.regression.MappingMethod
import net.sf.ij_plugins.color.converter.ColorTriple.Lab

/**
 * @author Jarek Sacha 
 */
object LOOCrossValidation {


  /** Compute leave-one-out error for each of the chips in the chart.
    *
    * The error for a given chip is computed by excluding that chip from a chart.
    * Color corrector is computed using all but that selected chip, then a delta between value of that chip and
    * color computed for that chip by the corrector is determined (square root of sum of squares of
    * color components differences).
    *
    * @return deltas when each of the chips, in turn, is excluded.
    */
  def crossValidation(chart: ColorChart,
                      referenceColorSpace: ReferenceColorSpace,
                      mappingMethod: MappingMethod.Value,
                      image: ImagePlus): IndexedSeq[Double] = {

    val n = chart.referenceChips.size
    val expectedColors = chart.referenceColor(referenceColorSpace)

    for (i <- 0 until n) yield {
      // Disable i-th chip when computing calibration coefficients
      val enabled = Array.fill[Boolean](n) {true}
      enabled(i) = false
      val leaveOneOutChart = chart.copyWithEnableChips(enabled)

      // Compute color mapping coefficients
      val colorCalibrator = new ColorCalibrator(leaveOneOutChart, referenceColorSpace, mappingMethod)

      //      val fit = try {
      //        colorCalibrator.computeCalibrationMapping(chipMargin, image)
      //      } catch {
      //        case t: Throwable => {
      //          throw new Color
      //          // FIXME: query colorCalibrator if parameters (mapping method) can be used for calibration.
      //          //        showError("Error while computing color calibration.", t.getMessage, t)
      //          //        return
      //        }
      //      }
      val fit = colorCalibrator.computeCalibrationMapping(image)
      val corrector = new Corrector(fit.mapping)

      // Measure correction quality on the disabled chip
      // Computation of the mean is be done in L*a*b*
      val testChip = chart.alignedChips(i)
      val poly = toPolygonROI(testChip.outline)
      //      val bounds = poly.getBounds

      val (correctedFPs, cropPoly) = corrector.map(image, poly)
      val labFPs = referenceColorSpace.toLab(correctedFPs)
      val actualTestColor = for (fp <- labFPs) yield {
        fp.setRoi(cropPoly)
        fp.getStatistics.mean
      }
      val actualTestColorLab = Lab(actualTestColor(0), actualTestColor(1), actualTestColor(2))
      val expectedColor = expectedColors(i)

      // Delta needs to be computed in a manner independent of the reference color space so different reference
      // color spaces can be compared to each other. We will use CIE L*a*b* as common comparison space.
      DeltaE.e76(referenceColorSpace.toLab(expectedColor), actualTestColorLab)
    }
  }

  def delta(a: Array[Double], b: Array[Double]): Double = {
    require(a.length == b.length, "Length of input arrays must match")
    val sunOfSquares = (a zip b).foldLeft(0d)((s, v) => {val v2 = v._1 - v._2; s + (v2 * v2)})
    math.sqrt(sunOfSquares)
  }

  def crop(imp: ImagePlus, roi: Rectangle): ImagePlus = {
    val dstStack = new ImageStack(roi.width, roi.height)
    val srcStack = imp.getStack
    for (i <- 1 to srcStack.getSize) {
      val ip = srcStack.getProcessor(i).duplicate()
      ip.setRoi(roi)
      dstStack.addSlice(ip.crop())
    }

    new ImagePlus("", dstStack)
  }
}
