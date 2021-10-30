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

package ij_plugins.color.calibration

import ij.gui.{PolygonRoi, Roi}
import ij.process.{ColorProcessor, FloatProcessor}
import ij.{CompositeImage, ImagePlus, ImageStack}
import ij_plugins.color.calibration.chart.{ColorChip, GridColorChart, ReferenceColorSpace}
import ij_plugins.color.converter.{ColorConverter, ReferenceWhite}
import ij_plugins.color.util.Utils.clipUInt8
import ij_plugins.color.util.{ImageJUtils, PerspectiveTransform}

import java.awt.geom.{Path2D, Point2D}
import java.awt.{Color, Polygon, Shape}
import scala.collection.parallel.immutable.ParRange

object CalibrationUtils {

  def point2D(x: Double, y: Double): Point2D = new Point2D.Double(x, y)

  def toPolygonROI(outline: Seq[Point2D]): PolygonRoi = {
    val x = outline.map { p => p.getX.toFloat }.toArray
    val y = outline.map { p => p.getY.toFloat }.toArray
    new PolygonRoi(x, y, outline.size, Roi.POLYGON)
  }

  def toShape(chips: Seq[ColorChip]): Shape = {
    val shape = new Path2D.Double
    for (chip <- chips) shape.append(outlineToShape(chip.outline.toArray), false)
    shape
  }

  def outlineToShape(outline: Array[java.awt.geom.Point2D]): Shape = {
    val path: Path2D = new Path2D.Double
    path.moveTo(outline.head.getX, outline.head.getY)
    outline.tail.foreach(point => path.lineTo(point.getX, point.getY))
    path.closePath()
    path
  }

  def renderReferenceChart(referenceChart: GridColorChart): ImagePlus = {
    val scale = 80
    val margin = referenceChart.chipMargin * scale

    val chart = referenceChart.copyWith(new PerspectiveTransform())
    val maxX = chart.referenceOutline.map(_.getX).max * scale
    val maxY = chart.referenceOutline.map(_.getY).max * scale
    val width: Int = (maxX + 2 * margin).toInt
    val height: Int = (maxY + 2 * margin).toInt
    val cp = new ColorProcessor(width, height)
    cp.setColor(Color.BLACK)
    cp.fill()
    val converter = chart.colorConverter
    for (chip <- chart.referenceChips) {
      // ROI
      val outline = chip.outline
      val xPoints = outline.map(p => (margin + p.getX * scale).toInt).toArray
      val yPoints = outline.map(p => (margin + p.getY * scale).toInt).toArray
      val roi = new PolygonRoi(new Polygon(xPoints, yPoints, xPoints.length), Roi.POLYGON)
      cp.setRoi(roi)
      // Color
      val color = {
        val rgb = converter.toRGB(converter.toXYZ(chip.color))
        new Color(clipUInt8(rgb.r), clipUInt8(rgb.g), clipUInt8(rgb.b))
      }
      cp.setColor(color)
      cp.fill()
    }

    val imp = new ImagePlus(chart.name, cp)
    imp.setRoi(
      new PolygonRoi(
        new Polygon(
          Array(margin.toInt, (maxX + margin).toInt, (maxX + margin).toInt, margin.toInt),
          Array(margin.toInt, margin.toInt, (maxY + margin).toInt, (maxY + margin).toInt),
          4
        ),
        Roi.POLYGON
      )
    )
    imp
  }

  /**
    * Convert floating point color bands to an RGB image (24-bit ColorProcessor)
    *
    * @param bands      color bands
    * @param colorSpace color space of the `bands`
    * @param converter  color converter, used if color space is XYZ
    * @return RGB color image
    */
  def convertToSRGB(
                     bands: Array[FloatProcessor],
                     colorSpace: ReferenceColorSpace,
                     converter: ColorConverter
                   ): ImagePlus = {
    colorSpace match {
      case ReferenceColorSpace.sRGB => new ImagePlus("", ImageJUtils.mergeRGB(bands))
      case ReferenceColorSpace.XYZ =>
        // Convert XYZ to sRGB
        val cp = new ColorProcessor(bands(0).getWidth, bands(0).getHeight)
        val n = bands(0).getWidth * bands(0).getHeight
        for (i <- new ParRange(0 until n)) {
          val rgb = converter.xyzToRGB(bands(0).getf(i), bands(1).getf(i), bands(2).getf(i))
          val r = clipUInt8(rgb.r)
          val g = clipUInt8(rgb.g)
          val b = clipUInt8(rgb.b)
          val color = ((r & 0xff) << 16) | ((g & 0xff) << 8) | ((b & 0xff) << 0)
          cp.set(i, color)
        }
        new ImagePlus("sRGB", cp)
      case _ => throw new IllegalArgumentException("Unsupported reference color space '" + colorSpace + "'.")
    }
  }

  /**
    * Convert floating point color bands to an CIE L*a*b* image (floating point bands)
    *
    * @param bands      input color bands
    * @param colorSpace color space of the input `bands`
    * @param refWhite   assumed reference while
    * @return CIE L*a*b* color image
    */
  def convertToLab(
                    bands: Array[FloatProcessor],
                    colorSpace: ReferenceColorSpace,
                    refWhite: ReferenceWhite
                  ): ImagePlus = {
    val labFPs = colorSpace.toLab(bands, refWhite)
    val stack = new ImageStack(labFPs(0).getWidth, labFPs(0).getHeight)
    stack.addSlice("L*", labFPs(0))
    stack.addSlice("a*", labFPs(1))
    stack.addSlice("b*", labFPs(2))
    new CompositeImage(new ImagePlus("L*a*b*", stack), CompositeImage.GRAYSCALE)
  }

}
