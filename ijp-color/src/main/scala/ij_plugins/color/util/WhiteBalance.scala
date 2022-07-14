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

package ij_plugins.color.util

import ij.gui.Roi
import ij.process.{ByteProcessor, ColorProcessor, ImageProcessor, ImageStatistics}
import ij.{CompositeImage, IJ, ImagePlus, ImageStack}
import ij_plugins.color.util.ImageJUtils.{mergeRGB, splitRGB}

/** Methods for performing white balancing of RGB color images. */
object WhiteBalance {

  /**
    * White balance an image using provided ROI. Median color value within ROI is used.
    *
    * @param cp  image to correct
    * @param roi area to use for computing white balance
    * @return white-balanced image
    */
  def whiteBalance(cp: ColorProcessor, roi: Roi): (ColorProcessor, Double, Double) =
    whiteBalance(cp = cp, roi = roi, averagingMode = AveragingMode.Median)

  /**
    * White balance an image using provided ROI.
    *
    * @param cp            input colr images with an ROI over a neutral color area.
    * @param averagingMode what method is used to find an average channel value within the ROI.
    * @return white-balanced image.
    */
  def whiteBalance(cp: ColorProcessor,
                   roi: Roi,
                   averagingMode: AveragingMode
                  ): (ColorProcessor, Double, Double) = {
    require(cp != null, "Argument 'cp' cannot be null")

    val imp = toRGBStackImp(cp)
    val (dstImp, redMult, blueMult) = whiteBalanceRGBStack(imp, roi, averagingMode)

    val rgb = slices(dstImp).map(_.asInstanceOf[ByteProcessor]).toArray

    val dstCP = mergeRGB(rgb)

    (dstCP, redMult, blueMult)
  }

  /**
    * White balance an image using provided ROI. Median color value within ROI is used.
    *
    * @param imp image to correct
    * @param roi area to use for computing white balance
    * @return white-balanced image
    */
  def whiteBalance(imp: ImagePlus, roi: Roi): (ImagePlus, Double, Double) = {
    whiteBalance(imp, roi, AveragingMode.Median)
  }

  /**
    * White balance an image using provided ROI.
    *
    * @param imp           input colr images with an ROI over a neutral color area.
    * @param averagingMode what method is used to find an average channel value within the ROI.
    * @return white-balanced image.
    */
  def whiteBalance(imp: ImagePlus, roi: Roi, averagingMode: AveragingMode): (ImagePlus, Double, Double) = {
    val compositeModeOpt = imp match {
      case c: CompositeImage => Some(c.getMode)
      case _ => None
    }

    val (dstImp1, redMult, blueMult) =
      imp.getType match {
        case ImagePlus.COLOR_RGB =>
          val cp = imp.getProcessor.asInstanceOf[ColorProcessor]
          val (dst, rm, bm) = WhiteBalance.whiteBalance(cp, roi, averagingMode)
          (new ImagePlus("", dst), rm, bm)
        case ImagePlus.GRAY8 | ImagePlus.GRAY16 | ImagePlus.GRAY32 =>
          WhiteBalance.whiteBalanceRGBStack(imp, roi, averagingMode)
      }

    val dstImp2 = compositeModeOpt match {
      case Some(mode) => new CompositeImage(dstImp1, mode)
      case None => dstImp1
    }

    (dstImp2, redMult, blueMult)
  }

  /**
    * White balance an image using provided ROI. 
    * Assume that the source image is a stack with 3 gray level bands corresponding to Red, Green, and Blue. 
    *
    * @param imp           input colr images with an ROI over a neutral color area.
    * @param averagingMode what method is used to find an average channel value within the ROI.
    * @return white-balanced image
    */
  def whiteBalanceRGBStack(imp: ImagePlus, roi: Roi, averagingMode: AveragingMode): (ImagePlus, Double, Double) = {
    require(imp.getStackSize == 3)
    require(imp.getType == ImagePlus.GRAY8 || imp.getType == ImagePlus.GRAY16 || imp.getType == ImagePlus.GRAY32)

    val stats: Seq[ImageStatistics] = measureROI(imp: ImagePlus, roi: Roi)

    val v = averagingMode match {
      case AveragingMode.Mean => stats.map(_.mean)
      case AveragingMode.Median => stats.map(_.median)
    }

    val r = v(0)
    val g = v(1)
    val b = v(2)

    if (r == 0 || g == 0 || b == 0)
      throw new IllegalStateException(s"Mean value of gray channel is 0, cannot white balance rgb=($r, $g, $b)")

    val redMult = g / r
    val blueMult = g / b

    if (IJ.debugMode) {
      IJ.log(s"White Balance")
      IJ.log(s"    Area: ${stats.head.area}")
      IJ.log(s"       R: $r")
      IJ.log(s"       G: $g")
      IJ.log(s"       B: $b")
      IJ.log(s"  mult R: $redMult")
      IJ.log(s"  mult B: $blueMult")
    }

    val dstImp = whiteBalance(imp, redMult, blueMult)

    (dstImp, redMult, blueMult)
  }

  /**
    * White balance an image using provided multiplier for read and blue channel. Green chanel is not changed.
    *
    * @param imp      source image
    * @param redMult  red channel multiplier
    * @param blueMult blue channel multiplier 
    * @return white-balanced image
    */
  def whiteBalance(imp: ImagePlus, redMult: Double, blueMult: Double): ImagePlus = {
    assert(imp.getStackSize == 3)

    val srcStack = imp.getStack

    val ipR = srcStack.getProcessor(1).duplicate()
    ipR.multiply(redMult)

    val ipG = srcStack.getProcessor(2).duplicate()

    val ipB = srcStack.getProcessor(3).duplicate()
    ipB.multiply(blueMult)

    val dstStack = new ImageStack(imp.getWidth, imp.getHeight)
    dstStack.addSlice(srcStack.getSliceLabel(1), ipR)
    dstStack.addSlice(srcStack.getSliceLabel(2), ipG)
    dstStack.addSlice(srcStack.getSliceLabel(3), ipB)

    new ImagePlus("", dstStack)
  }

  def measureROI(imp: ImagePlus, roi: Roi): Seq[ImageStatistics] = {
    val stack = imp.getStack
    for (i <- 1 to stack.size()) yield {
      val ip = stack.getProcessor(i)
      ip.setRoi(roi)
      ip.getStatistics
    }
  }

  def toRGBStackImp(cp: ColorProcessor): ImagePlus = {
    val rgb = splitRGB(cp)
    val labels = Array("Red", "Green", "Blue")
    val stack = new ImageStack(cp.getWidth, cp.getHeight)
    (0 until 3).foreach { i =>
      stack.addSlice(labels(i), rgb(i))
    }
    new ImagePlus("", stack)
  }

  def slices(imp: ImagePlus): Seq[ImageProcessor] = {
    val stack = imp.getStack
    (1 to stack.getSize).map(stack.getProcessor)
  }
}
