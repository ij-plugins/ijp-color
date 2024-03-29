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

import ij.gui.{PolygonRoi, Roi}
import ij.io.RoiEncoder
import ij.plugin.frame.RoiManager
import ij.process.{ByteProcessor, ColorProcessor, FloatProcessor, ImageProcessor}
import ij.{IJ, ImageJ}

import java.awt.geom.Point2D
import java.io.{BufferedOutputStream, DataOutputStream, File, FileOutputStream}
import java.util.zip.{ZipEntry, ZipOutputStream}
import scala.util.Using

/** Helper methods for working with ImageJ. */
object ImageJUtils {

  /**
   * Returns icon used by ImageJ main frame. Returns `null` if main frame is not instantiated or has no icon.
   *
   * @return
   *   ImageJ icon or `null`.
   */
  def imageJIconAsAWTImage: java.awt.Image = {
    val imageJ: ImageJ = IJ.getInstance
    if (imageJ != null) imageJ.getIconImage else null
  }

  /**
   * Splits ColorProcessor into ByteProcessors representing each of three bands (red, green, and blue).
   *
   * @param cp
   *   input color processor
   * @return
   *   ByteProcessor for each band.
   */
  def splitRGB(cp: ColorProcessor): Array[ByteProcessor] = {
    val width       = cp.getWidth
    val height      = cp.getHeight
    val redBP       = new ByteProcessor(width, height)
    val greenBP     = new ByteProcessor(width, height)
    val blueBP      = new ByteProcessor(width, height)
    val redPixels   = redBP.getPixels.asInstanceOf[Array[Byte]]
    val greenPixels = greenBP.getPixels.asInstanceOf[Array[Byte]]
    val bluePixels  = blueBP.getPixels.asInstanceOf[Array[Byte]]
    cp.getRGB(redPixels, greenPixels, bluePixels)
    Array[ByteProcessor](redBP, greenBP, blueBP)
  }

  /**
   * Merges RGB bands into a ColorProcessor.
   *
   * @param src
   *   ByteProcessor for red, green, and blue band.
   * @return
   *   merged bands
   * @see
   *   #splitRGB
   */
  def mergeRGB(src: Array[ByteProcessor]): ColorProcessor = {
    validateSameTypeAndDimensions(src, 3)

    val width  = src(0).getWidth
    val height = src(0).getHeight
    val dest   = new ColorProcessor(width, height)
    dest.setRGB(
      src(0).getPixels.asInstanceOf[Array[Byte]],
      src(1).getPixels.asInstanceOf[Array[Byte]],
      src(2).getPixels.asInstanceOf[Array[Byte]]
    )
    dest
  }

  /**
   * Merges RGB bands into a ColorProcessor.
   *
   * Floating point values are assumed in the range 0 to 255.
   *
   * @param src
   *   ByteProcessor for red, green, and blue band.
   * @return
   *   merged bands
   * @see
   *   #splitRGB
   */
  def mergeRGB(src: Array[FloatProcessor]): ColorProcessor = {
    validateSameTypeAndDimensions(src, 3)

    mergeRGB(Array.range(0, 3).map(src(_).convertToByte(false).asInstanceOf[ByteProcessor]))
  }

  def convertToFloat(src: ImageProcessor): Array[FloatProcessor] = {
    if (!src.isInstanceOf[ColorProcessor]) {
      Array[FloatProcessor](src.convertToFloat.asInstanceOf[FloatProcessor])
    } else {
      val srcBps: Array[ByteProcessor]   = splitRGB(src.asInstanceOf[ColorProcessor])
      val destFps: Array[FloatProcessor] = new Array[FloatProcessor](3)
      for (i <- srcBps.indices) {
        destFps(i) = srcBps(i).convertToFloat.asInstanceOf[FloatProcessor]
        srcBps(i) = null
      }
      destFps
    }
  }

  def toRoi(outline: Seq[Point2D]): PolygonRoi = {
    val x = new Array[Float](outline.length)
    val y = new Array[Float](outline.length)

    for (i <- outline.indices) {
      val p = outline(i)
      x(i) = p.getX.asInstanceOf[Float]
      y(i) = p.getY.asInstanceOf[Float]
    }
    new PolygonRoi(x, y, outline.length, Roi.POLYGON)
  }

  /**
   * Measure color within ROI.
   *
   * @param tri
   * three bands of an image, may represent only color space.
   * @param outline
   * outline of the region of interest.
   * @return
   * average color in the ROI.
   * @see
   * #measureColorXY(ij.process.ImageProcessor[], ij.gui.Roi)
   */
  def measureColor[T <: ImageProcessor](tri: Array[T], outline: Array[Point2D]): Array[Double] = {
    measureColor(tri, toRoi(outline.toSeq))
  }

  def measureColor[T <: ImageProcessor](tri: IndexedSeq[T], outline: IndexedSeq[Point2D]): IndexedSeq[Double] =
    measureColor(tri, toRoi(outline))

  /**
   * Measure color within ROI.
   *
   * @param tri
   * three bands of an image, may represent only color space.
   * @param roi
   * region of interest.
   * @return
   * average color in the ROI.
   * @see
   * #measureColorXY(ij.process.ImageProcessor[], ij.gui.Roi)
   */
  def measureColor[T <: ImageProcessor](tri: Array[T], roi: Roi): Array[Double] = {
    val color: Array[Double] = new Array[Double](tri.length)
    for (i <- tri.indices) {
      tri(i).setRoi(roi)
      color(i) = tri(i).getStatistics.mean
    }
    color
  }

  def measureColor[T <: ImageProcessor](tri: IndexedSeq[T], roi: Roi): IndexedSeq[Double] = {
    tri.map { ip =>
      ip.setRoi(roi)
      ip.getStatistics.mean
    }
  }

  /**
   * @param src
   * images to validate
   * @param length
   * expected number of images
   * @tparam T
   * image processor type
   * @throws java.lang.IllegalArgumentException
   * if the images in the array are not of the same dimension.
   */
  @inline
  def validateSameDimensions[T <: ImageProcessor](src: Array[T], length: Int): Unit = {
    require(src != null, "Input cannot be null.")
    require(src.length == length, "Input array has to have " + length + " elements.")
    require(!src.contains(null.asInstanceOf[T]), "Input array cannot have null entries.")
    val width  = src(0).getWidth
    val height = src(0).getHeight
    require(
      src.forall(width == _.getWidth),
      "All input images have to have the same width: " + src.map(_.getWidth).mkString(",")
    )
    require(
      src.forall(height == _.getHeight),
      "All input images have to have the same height: " + src.map(_.getHeight).mkString(",")
    )
  }

  /**
   * @param src
   * images to validate
   * @param length
   * expected number of images
   * @tparam T
   * image processor type
   * @throws java.lang.IllegalArgumentException
   * if the images in the array are not of the same dimension.
   */
  @inline
  def validateSameDimensions[T <: ImageProcessor](src: IndexedSeq[T], length: Int): Unit = {
    require(src != null, "Input cannot be null.")
    require(src.length == length, "Input array has to have " + length + " elements.")
    require(!src.contains(null.asInstanceOf[T]), "Input array cannot have null entries.")
    val width  = src(0).getWidth
    val height = src(0).getHeight
    require(
      src.forall(width == _.getWidth),
      "All input images have to have the same width: " + src.map(_.getWidth).mkString(",")
    )
    require(
      src.forall(height == _.getHeight),
      "All input images have to have the same height: " + src.map(_.getHeight).mkString(",")
    )
  }

  /**
   * @param src
   *   images to validate
   * @param length
   *   expected number of images
   * @tparam T
   *   image processor type
   * @throws java.lang.IllegalArgumentException
   *   if the images in the array are not of the same dimension.
   */
  @inline
  def validateSameTypeAndDimensions[T <: ImageProcessor](src: Array[T], length: Int): Unit = {
    validateSameDimensions(src, length)
    if (length > 1) {
      val t = src(0).getClass
      require(src.tail.forall(_.getClass == t), "All input images must be of the same type.")
    }
  }

  /**
   * @param src
   * images to validate
   * @param length
   * expected number of images
   * @tparam T
   * image processor type
   * @throws java.lang.IllegalArgumentException
   * if the images in the array are not of the same dimension.
   */
  @inline
  def validateSameTypeAndDimensions[T <: ImageProcessor](src: IndexedSeq[T], length: Int): Unit = {
    validateSameDimensions(src, length)
    if (length > 1) {
      val t = src(0).getClass
      require(src.tail.forall(_.getClass == t), "All input images must be of the same type.")
    }
  }

  /**
   * Get the singleton instance of ImageJ `RoiManager`
   *
   * @return
   *   RoiManager instance
   */
  def roiManagerInstance: RoiManager = { // Workaround for ImageJ bug.
    // RoiManger is a singleton in function, but it has constructors.
    // If a second instance of RoiManager is created it should not be used.
    // Make sure that RoiManager is created.
    new RoiManager
    // Get reference of primary instance, which may or may not be one created above.
    RoiManager.getInstance
  }

  /**
   * Add result ROIs to ROI Manager, replacing current content. If ROI Manager is not visible it will be opened.
   *
   * @param rois
   *   ROI's to be added.
   * @param clearContent
   *   if `true` ROI Manager content will be cleared before new rois will be added
   */
  def addToROIManager(rois: IterableOnce[Roi], clearContent: Boolean = false): Unit = {
    val roiManager = roiManagerInstance
    if (clearContent) roiManager.runCommand("Reset")
    rois.iterator.foreach(roiManager.addRoi)
  }

  /** Save ROI to a file */
  def saveROI(roi: Roi, file: File): Unit = {
    val encoder = new RoiEncoder(file.getPath)
    encoder.write(roi)
  }

  /** Save collection of ROIs to a file as a set in a format that ImageJ can read back - ZIP of individual ROI files. */
  def saveRoiZip(rois: Seq[(String, Roi)], file: File): Unit = {
    Using.resource(new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) { zos =>
      Using.resource(new DataOutputStream(new BufferedOutputStream(zos))) { out =>
        val encoder = new RoiEncoder(out)
        for (case (_name, roi) <- rois) {
          val name: String = if (_name.endsWith(".roi")) _name else _name + ".roi"
          zos.putNextEntry(new ZipEntry(name))
          encoder.write(roi)
          out.flush()
        }
      }
    }
  }
}
