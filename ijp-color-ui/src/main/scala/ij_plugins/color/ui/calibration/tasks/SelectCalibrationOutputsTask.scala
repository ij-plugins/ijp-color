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

import ij_plugins.color.ui.calibration.tasks.CalibrateTask.OutputConfig
import ij_plugins.color.ui.fx.GenericDialogFXIJ
import org.scalafx.extras.BusyWorker.SimpleTask
import scalafx.scene.text.{Font, FontWeight}
import scalafx.stage.Window

class SelectCalibrationOutputsTask(src: OutputConfig, parentWindowOption: Option[Window])
    extends SimpleTask[Option[OutputConfig]] {

  override def call(): Option[OutputConfig] = {

    // Derive Bold font for headings
    val messageFont = {
      val df = Font.default
      Font.font(df.family, FontWeight.Bold, df.size)
    }

    val gd =
      new GenericDialogFXIJ(
        "Select Calibrate Outputs",
        "Choose outputs that will be generated by \"Calibrate\".",
        parentWindowOption
      ) {
        addMessage("Calibrated images", messageFont)
        addCheckbox("sRGB_image", src.imageInSRGB)
        addCheckbox("Reference_Color_Space_float_image", src.imageInReferenceColorSpace)
        addCheckbox("CIE_L*a*b*_image", src.imageInLab)

        addMessage("Diagnostic information", messageFont)
        addCheckbox("Scatter-plot_of_Fit", src.plotScatterFit)
        addCheckbox("Plot_Individual_Chip_Error", src.plotIndividualChipError)
        addCheckbox("Table_of_Regression_Results", src.tableRegressionResults)
        addCheckbox("Table_of_Expected_vs_Corrected", src.tableExpectedVsCorrected)
        addCheckbox("Table_of_Individual_Chip_Delta_in_CIE_L*a*b*", src.tableIndividualChipDeltaInLab)
        addCheckbox("Log_Delta_in_Reference_Color_Space", src.logDeltaInReferenceColorSpace)
      }

    gd.showDialog()

    if (gd.wasOKed) {
      val dst = OutputConfig(
        imageInSRGB = gd.nextBoolean(),
        imageInReferenceColorSpace = gd.nextBoolean(),
        imageInLab = gd.nextBoolean(),
        plotScatterFit = gd.nextBoolean(),
        plotIndividualChipError = gd.nextBoolean(),
        tableRegressionResults = gd.nextBoolean(),
        tableExpectedVsCorrected = gd.nextBoolean(),
        tableIndividualChipDeltaInLab = gd.nextBoolean(),
        logDeltaInReferenceColorSpace = gd.nextBoolean()
      )

      Option(dst)
    } else
      None
  }
}
