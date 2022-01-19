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

package ij_plugins.color.calibration.chart

import ij.ImagePlus
import ij.gui.Roi
import ij.process.{ColorProcessor, FloatPolygon, ImageProcessor, ImageStatistics}
import ij_plugins.color.util.PerspectiveTransform

import java.awt.geom.Point2D
import scala.collection.immutable.ListMap

object ChartFrameUtils {

  /**
   * Measure ROIs defined in the chart frame.
   *
   * Note that alignment transform is applied to chip outlines.
   *
   * @param imp input image
   * @param chart chart fefining locations of measurements
   * @return  ordered map of measurements: key are rois, values are measurement for each band.
   *          Order corresponds to the order of input `rois`.
   *          Values are ordered maps as well, where key is band name, and value is `ImageStatistics` for the roi.
   */
  def measureRois(imp: ImagePlus, chart: ChartFrame): ListMap[Roi, ListMap[String, ImageStatistics]] = {
    measureRois(imp, chart.alignedChipROIs)
  }

  /**
   * Measure ROIs in the source image. The images is assumed to consts of bands (slices). For `COLOR_RGB`
   * (`ColorProcessor`) images there can be only one slice - it is interpreted as 3 bands: "Red", "Green", and "Blue".
   * For images with "GRAY*" slices, each slice is interpreted as a "band".
   *
   * Measurements for each "ROI" are grouped together. Measurements are reported as `ImageStatistics`, so you have
   * flexibility extracting desired measurement, like mean, area, min, max, and so on.
   *
   * @param imp   input image
   * @param rois   rois to measure
   * @return ordered map of measurements: key are rois, values are measurement for each band.
   *         Order corresponds to the order of input `rois`.
   *         Values are ordered maps as well, where key is band name, and value is `ImageStatistics` for the roi.
   */
  def measureRois(imp: ImagePlus, rois: IndexedSeq[Roi]): ListMap[Roi, ListMap[String, ImageStatistics]] = {

    // So safety remember original ROI in the source image
    val impRoi = imp.getRoi

    // Extract image bands with their names
    val bandMap: ListMap[String, ImageProcessor] = imp.getType match {
      case ImagePlus.COLOR_RGB =>
        val cp = imp.getProcessor.asInstanceOf[ColorProcessor]
        ListMap(
          "Red"   -> cp.getChannel(1, null),
          "Green" -> cp.getChannel(2, null),
          "Blue"  -> cp.getChannel(3, null)
        )
      case _ =>
        val stack = imp.getStack
        val slices = for (i <- 1 to stack.getSize) yield {
          val ip    = stack.getProcessor(i)
          val label = Option(stack.getSliceLabel(i)).getOrElse(s"$i")
          (label, ip)
        }
        ListMap(slices*)
    }

    // Measure ROIs in each band
    val roiMeasurementMaps =
      for (roi <- rois) yield {
        val bandStats =
          for ((bandName, bandIP) <- bandMap) yield {
            bandIP.setRoi(roi)
            val stats = bandIP.getStatistics
            (bandName, stats)
          }
        (roi, bandStats)
      }

    // Restore ROI
    imp.setRoi(impRoi)

    ListMap(roiMeasurementMaps*)
  }

  /**
   * Compute an a perspective transform, such that when applied to the outline of the reference chart will produce outline of the `chartROI`
   * @param chartROI target ROI (polygon with 4 vertices)
   * @param refChartFrame source ROI
   * @return perspective transform
   */
  def computeAlignmentTransform(chartROI: Roi, refChartFrame: ChartFrame): PerspectiveTransform = {
    require(chartROI != null, "'chartROI' cannot be null.")
    require(
      chartROI.getType == Roi.POLYGON,
      s"'chartROI's type must be a 'Roi.POLYGON', got ${chartROI.getTypeAsString}."
    )
    require(chartROI.getPolygon.npoints == 4, s"'chartROI's must have 4 vertices, got ${chartROI.getPolygon.npoints}.")
    require(refChartFrame != null)

    val points = toOutline(chartROI.getFloatPolygon)

    // Create alignment transform
    PerspectiveTransform.quadToQuad(
      refChartFrame.referenceOutline.toArray,
      points
    )
  }

  /**
   * Convert polygon to an array of points from its vertices
   * @param poly source polygon
   * @return an array of vertices
   */
  def toOutline(poly: FloatPolygon): Array[Point2D] = {
    val r = new Array[Point2D](poly.npoints)
    for (i <- r.indices) {
      r(i) = new Point2D.Float(poly.xpoints(i), poly.ypoints(i))
    }
    r
  }
}
