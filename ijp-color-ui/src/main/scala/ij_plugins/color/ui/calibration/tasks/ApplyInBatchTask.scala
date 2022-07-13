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

import ij.IJ
import ij_plugins.color.calibration.CorrectionRecipe
import ij_plugins.color.ui.fx.GenericDialogFXIJ
import ij_plugins.color.ui.util.IJPrefs
import ij_plugins.color.ui.util.batch.BatchProcessing
import org.scalafx.extras.BusyWorker.SimpleTask
import org.scalafx.extras.ShowMessage
import scalafx.beans.property.ObjectProperty
import scalafx.stage.Window

import java.io.File

object ApplyInBatchTask {

  private val ReferencePrefix = classOf[ApplyInBatchTask].getName

  object Config {
    def loadFromIJPrefOption(): Option[Config] = {
      for {
        itemConfig <- ColorCorrectionBatchItem.Config.loadFromIJPrefOption()
        inputDir   <- IJPrefs.getStringOption(ReferencePrefix + ".inputDir")
        inputExt   <- IJPrefs.getStringOption(ReferencePrefix + ".inputExt")
        outputDir  <- IJPrefs.getStringOption(ReferencePrefix + ".outputDir")
        //        enableParallelProcessing <- IJPrefs.getBooleanOption(ReferencePrefix + ".enableParallelProcessing")
      } yield Config(
        itemConfig = itemConfig,
        inputDir = new File(inputDir),
        inputExt = inputExt,
        outputDir = new File(outputDir)
        //        enableParallelProcessing = enableParallelProcessing
      )
    }

    def loadFromIJPref(): Config = loadFromIJPrefOption().getOrElse(defaultConfig)
  }

  def defaultConfig: Config = Config(
    itemConfig = ColorCorrectionBatchItem.Config.loadFromIJPref(),
    inputDir = new File("input"),
    inputExt = "tif",
    outputDir = new File("output")
    //    enableParallelProcessing = true
  )

  case class Config(
    itemConfig: ColorCorrectionBatchItem.Config,
    inputDir: File,
    inputExt: String,
    outputDir: File
    //                     enableParallelProcessing: Boolean
  ) {

    def saveToIJPref(): Unit = {
      itemConfig.saveToIJPref()
      IJPrefs.set(ReferencePrefix + ".inputDir", inputDir.getCanonicalPath)
      IJPrefs.set(ReferencePrefix + ".inputExt", inputExt)
      IJPrefs.set(ReferencePrefix + ".outputDir", outputDir.getCanonicalPath)
      //      IJPrefs.set(ReferencePrefix + ".enableParallelProcessing", enableParallelProcessing)
    }

  }
}

/**
 * Task performed by Calibrator's "Batch Apply ..." button
 *
 * @param correctionRecipe
 *   recipe to be applied
 * @param parentWindow
 *   parent window for dialogs
 */
class ApplyInBatchTask(
  correctionRecipe: ObjectProperty[Option[CorrectionRecipe]],
  val parentWindow: Option[Window]
) extends SimpleTask[Unit]
    with ShowMessage {

  import ApplyInBatchTask.Config

  private val Title = "Batch Apply Color Calibration"

  private var config: Config = Config.loadFromIJPref()

  def call(): Unit = {
    val errorTitle = s"Error when performing $Title"

    // Check that calibration recipe is computed
    val recipe = correctionRecipe() match {
      case Some(r) => r
      case None =>
        showError(errorTitle, "Correction parameters not available.", "")
        return
    }

    val gd =
      new GenericDialogFXIJ(
        Title,
        "" +
          "Color correction will be applied to images in the 'Input directory' with the 'Input extension'.\n" +
          "Corrected files will be saved in the 'Output directory'.",
        parentWindow
      ) {
        addDirectoryField("Input_directory", config.inputDir.getPath)
        addStringField("Input_extension", config.inputExt)
        addDirectoryField("Output_directory", config.outputDir.getPath)
        addCheckbox("Save sRGB", config.itemConfig.enableSaveSRGB)
        addCheckbox("Save_CIE_L*a*b*", config.itemConfig.enableSaveLab)
        //        addCheckbox("Run_in_parallel", config.enableParallelProcessing)
        addHelp("https://github.com/ij-plugins/ijp-color/wiki/Calibrator-Batch-Apply")
      }

    gd.showDialog()

    val configOpt = {
      if (gd.wasOKed) {
        val inputDir = new File(gd.nextString())
        val inputExt = gd.nextString()
        val outputDir = new File(gd.nextString())
        val itemConfig = ColorCorrectionBatchItem.Config(
          enableSaveSRGB = gd.nextBoolean(),
          //      enableSaveXYZ = gd.getNextBoolean(),
          enableSaveLab = gd.nextBoolean()
        )
        //        val enableParallelProcessing = gd.getNextBoolean()

        // Verify inputs
        if (!(itemConfig.enableSaveSRGB || itemConfig.enableSaveLab)) {
          showError(errorTitle, "At least one of the outputs needs to be selected: sRGB, CIE XYZ, or CIE L*a*b*")
          None
        } else if (!inputDir.exists()) {
          showError(errorTitle, s"Input directory does not exist: ${inputDir.getCanonicalPath}")
          None
        } else if (!inputDir.isDirectory) {
          showError(errorTitle, s"Input is not a directory: ${inputDir.getCanonicalPath}")
          None
        } else {
          Option(
            Config(
              itemConfig = itemConfig,
              inputDir = inputDir,
              inputExt = inputExt,
              outputDir = outputDir
              //              enableParallelProcessing = enableParallelProcessing
            )
          )
        }
      } else {
        None
      }
    }

    configOpt.foreach { c =>
      config = c
      config.saveToIJPref()

      // Prepare items for processing
      IJ.showProgress(0, 1)
      IJ.showStatus(Title + " - setting up processing ...")
      val itemTasks = ColorCorrectionBatchItem.createBatchItems(
        recipe,
        config.itemConfig,
        config.inputDir,
        config.inputExt,
        config.outputDir
      )

      showInformation(Title, s"Found ${itemTasks.length} input files.", "")

      if (itemTasks.nonEmpty) {
        // Run batch processing
        new BatchProcessing(parentWindow)
          .processItems(Title, itemTasks)
      }
    }
  }
}
