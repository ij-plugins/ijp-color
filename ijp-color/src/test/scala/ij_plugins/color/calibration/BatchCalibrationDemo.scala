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

import ij.gui.Roi
import ij.io.RoiDecoder
import ij.{IJ, ImagePlus}
import ij_plugins.color.calibration.CalibrationUtils.convertToSRGB
import ij_plugins.color.calibration.chart.{ColorCharts, ReferenceColorSpace}
import ij_plugins.color.calibration.regression.MappingMethod
import ij_plugins.color.util.ImagePlusType

import java.io.File

/**
  * Example of batch color calibration.
  */
object BatchCalibrationDemo {

  val baseDir: File = new File("../test/data/batch_correction")
  val srcDir: File = new File(baseDir, "src")
  val dstDir: File = new File(baseDir, "dst")
  val chartImageFile = new File(srcDir, "im_1.jpg")
  val chartRoiFile = new File(srcDir, "im_1_chart.roi")

  def main(args: Array[String]): Unit = {

    println("Load chart image and ROI")
    val chartImage = IJ.openImage(chartImageFile.getCanonicalPath)

    val chartRoi = new RoiDecoder(chartRoiFile.getCanonicalPath).getRoi

    println("Create calibration recipe.")
    val recipe = createCalibrationRecipe(chartImage, chartRoi)

    val imageFiles = srcDir.listFiles((_, name) => name.endsWith(".jpg"))

    for (file <- imageFiles) {
      println(s"Correcting ${file.getName}")
      correct(recipe, file, dstDir)
    }

    println("Done.")
  }

  def createCalibrationRecipe(chartImage: ImagePlus, chartRoi: Roi): CorrectionRecipe = {
    // Create a ColorGauge default calibration chart and align it to ROI from our the our chart image
    val chart = ColorCharts.ImageScienceColorGaugeMatte.copyAlignedTo(chartRoi)

    val refColorSpace: ReferenceColorSpace = ReferenceColorSpace.sRGB
    val mappingMethod: MappingMethod = MappingMethod.LinearCrossBand
    val clipReferenceRGB = false
    val colorCalibrator = new ColorCalibrator(chart, refColorSpace, mappingMethod, clipReferenceRGB)
    val calibrationFit = colorCalibrator.computeCalibrationMapping(chartImage)
    val imageType = ImagePlusType.withValue(chartImage.getType)

    CorrectionRecipe(calibrationFit.corrector, chart.colorConverter, refColorSpace, imageType)
  }

  def correct(recipe: CorrectionRecipe, srcImageFile: File, dstDir: File): Unit = {
    val imp = IJ.openImage(srcImageFile.getCanonicalPath)

    val correctedBands = recipe.corrector.map(imp)

    val img = convertToSRGB(correctedBands, recipe.referenceColorSpace, recipe.colorConverter)

    val dstFile = new File(dstDir, srcImageFile.getName)
    dstFile.getParentFile.mkdirs()
    IJ.save(img, dstFile.getCanonicalPath)
  }
}
