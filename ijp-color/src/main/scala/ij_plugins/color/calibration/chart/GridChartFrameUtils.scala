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
import ij.process.{ColorProcessor, ImageProcessor, ImageStatistics}

import scala.collection.immutable.ListMap

object GridChartFrameUtils {

  def measureRois(imp: ImagePlus, chart: GridChartFrame): ListMap[Roi, ListMap[String, ImageStatistics]] = {
    measureRois(imp, chart.alignedChipROIs)
  }

  /**
    * Measure ROIs in the source image. The images is assumed to consts of bands (slices).
    * For `COLOR_RGB` (`ColorProcessor`) images there can be only one slice - it is interpreted as 3 bands:
    * "Red", "Green", and "Blue".
    * For images with "GRAY*" slices, each slice is interpreted as a "band".
    *
    * Measurements for each "ROI" are grouped together.
    * Measurements are reported as `ImageStatistics`, so you have flexibility extracting desired measurement, like
    * mean, area, min, max, and so on.
    *
    * @param imp  input image
    * @param rois rois to measure
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
          "Red" -> cp.getChannel(1, null),
          "Green" -> cp.getChannel(2, null),
          "Blue" -> cp.getChannel(3, null),
        )
      case _ =>
        val stack = imp.getStack
        val slices = for (i <- 1 to stack.getSize) yield {
          val ip = stack.getProcessor(i)
          val label = stack.getSliceLabel(i)
          (label, ip)
        }
        ListMap(slices: _*)
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

    ListMap(roiMeasurementMaps: _*)
  }
}
