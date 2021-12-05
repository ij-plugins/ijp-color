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

package ij_plugins.color.ui.calibration.tasks

import ij.{IJ, ImagePlus}
import ij_plugins.color.calibration.CalibrationUtils.{convertToLab, convertToSRGB}
import ij_plugins.color.calibration.CorrectionRecipe
import ij_plugins.color.ui.calibration.tasks.ColorCorrectionBatchItem.{Config, nameWithoutExtension, openImage, saveImage}
import ij_plugins.color.ui.util.IJPrefs
import ij_plugins.color.ui.util.batch.BatchProcessing

import java.io.{File, IOException}

object ColorCorrectionBatchItem {
  private val ReferencePrefix = classOf[ColorCorrectionBatchItem].getName

  object Config {
    def loadFromIJPrefOption(): Option[Config] = {
      for {
        enableSaveSRGB <- IJPrefs.getBooleanOption(ReferencePrefix + ".enableSaveSRGB")
        enableSaveLab  <- IJPrefs.getBooleanOption(ReferencePrefix + ".enableSaveLab")
      } yield Config(
        enableSaveSRGB = enableSaveSRGB,
        enableSaveLab = enableSaveLab
      )
    }

    def loadFromIJPref(): Config = loadFromIJPrefOption().getOrElse(Config())
  }

  case class Config(
    enableSaveSRGB: Boolean = true,
    enableSaveLab: Boolean = false
  ) {

    def saveToIJPref(): Unit = {
      IJPrefs.set(ReferencePrefix + ".enableSaveSRGB", enableSaveSRGB)
      IJPrefs.set(ReferencePrefix + ".enableSaveLab", enableSaveLab)
    }
  }

  /** List files in the `inputDir`, select ones that match the `inputExt`-ension, for create a batch item */
  def createBatchItems(
    recipe: CorrectionRecipe,
    itemConfig: ColorCorrectionBatchItem.Config,
    inputDir: File,
    inputExt: String,
    outputDir: File
  ): Seq[ColorCorrectionBatchItem] = {
    if (!inputDir.exists()) throw new IOException(s"Input directory must exist: ${inputDir.getAbsolutePath}")

    val files = inputDir.listFiles().filter(_.getName.endsWith(inputExt))

    files.map(file => new ColorCorrectionBatchItem(recipe, file, dstDir = outputDir, config = itemConfig))
  }

  def nameWithoutExtension(file: File): String = {
    val fullName = file.getName
    val index    = fullName.lastIndexOf('.')
    if (index > -1) {
      fullName.substring(0, index)
    } else {
      fullName
    }
  }

  def openImage(file: File): ImagePlus = {
    val path = file.getCanonicalPath

    if (!file.exists())
      throw new IOException(s"File does not exist: $path")

    Option(IJ.openImage(path)) match {
      case Some(img) => img
      case None      => throw new IOException(s"Cannot open file as image: $path")
    }
  }

  def saveImage(imp: ImagePlus, dstDir: File, name: String, imageFileType: String): Unit = {
    val dstFile = new File(dstDir, name + "." + imageFileType)
    this.synchronized {
      dstFile.getParentFile.mkdirs()
    }
    IJ.save(imp, dstFile.getCanonicalPath)
  }
}

/**
 * Color correction of a single image using the `recipe`. Loads image from `srcFile` and saves result to `dstDir`.
 */
class ColorCorrectionBatchItem(recipe: CorrectionRecipe, srcFile: File, dstDir: File, config: Config)
    extends BatchProcessing.BatchItem[String] {

  private val sRGBExt = "png"
  private val LabExt  = "tif"

  override val name: String = nameWithoutExtension(srcFile)

  override def run(): String = {
    val srcImp = openImage(srcFile)
    val name   = nameWithoutExtension(srcFile)

    val correctedBands = recipe.corrector.map(srcImp)

    if (config.enableSaveSRGB) {
      val imgDst = convertToSRGB(correctedBands, recipe.referenceColorSpace, recipe.colorConverter)
      saveImage(imgDst, new File(dstDir, "sRGB"), name, sRGBExt)
    }

    if (config.enableSaveLab) {
      val imgDst = convertToLab(correctedBands, recipe.referenceColorSpace, recipe.colorConverter.refWhite)
      saveImage(imgDst, new File(dstDir, s"Lab-${recipe.colorConverter.refWhite}"), name, LabExt)
    }

    nameWithoutExtension(srcFile)
  }
}
