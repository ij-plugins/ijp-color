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

package ij_plugins.color.image

import ij.plugin.Duplicator
import ij.process.{ColorProcessor, FloatProcessor, ImageConverter, StackConverter}
import ij.{ImagePlus, ImageStack}

/** Represents image with multiple values per pixel. */
class VectorImage(private val src: ImagePlus) {

  val stack: ImageStack = convertToFloatStack(src)
  val pixels: Array[Array[Float]] = for (i <- (1 to stack.getSize).toArray) yield
    stack.getProcessor(i).getPixels.asInstanceOf[Array[Float]]

  def this(cp: ColorProcessor) = {
    this(new ImagePlus("", cp))
  }

  def this(width: Int, height: Int, nbValues: Int) = {
    this(
      new ImagePlus("", {
        val s = new ImageStack(width, height)
        for (i <- 1 to nbValues) {
          s.addSlice(i.toString, new FloatProcessor(width, height))
        }
        s
      })
    )
  }

  def get(index: Int): Array[Float] = {
    val r = new Array[Float](pixels.length)
    for (i <- pixels.indices) {
      r(i) = pixels(i)(index)
    }
    r
  }

  def getDouble(index: Int): Array[Double] = {
    val r = new Array[Double](pixels.length)
    for (i <- pixels.indices) {
      r(i) = pixels(i)(index)
    }
    r
  }

  def set(index: Int, value: Array[Float]): Unit = {
    require(pixels.length == value.length)
    for (i <- pixels.indices) {
      pixels(i)(index) = value(i)
    }
  }

  def set(index: Int, value: Array[Double]): Unit = {
    require(pixels.length == value.length)
    for (i <- pixels.indices) {
      pixels(i)(index) = value(i).toFloat
    }
  }


  /** Convert, in place, image internal representation to stack of floating point slices */
  private def convertToFloatStack(src: ImagePlus): ImageStack = {
    require(src != null)

    val imp = new Duplicator().run(src)
    val doScaling = ImageConverter.getDoScaling
    try {
      ImageConverter.setDoScaling(false)
      // Convert color image to a stack of single value images
      if (imp.getType == ImagePlus.COLOR_RGB) {
        if (imp.getStackSize > 1) throw new RuntimeException("Unsupported image type: stack of COLOR_RGB")
        new ImageConverter(imp).convertToRGBStack()
      }
      // Convert pixel representation to Float
      if (imp.getStackSize > 1)
        new StackConverter(imp).convertToGray32()
      else
        new ImageConverter(imp).convertToGray32()

      imp.getStack
    }
    finally {
      ImageConverter.setDoScaling(doScaling)
    }
  }
}


