/*
 * Image/J Plugins
 * Copyright (C) 2002-2019 Jarek Sacha
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

package net.sf.ij_plugins.color.calibration.ui

import net.sf.ij_plugins.color.calibration.chart.{ColorCharts, GridColorChart, ReferenceColorSpace}
import net.sf.ij_plugins.color.calibration.regression.MappingMethod
import net.sf.ij_plugins.util.IJPUtils
import org.scalafx.extras.mvcfx.ControllerFX
import scalafx.Includes._
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Pos
import scalafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory
import scalafx.scene.control._
import scalafx.scene.layout.GridPane
import scalafxml.core.macros.sfxml

/**
  * Binds ColorCalibrator FXML to UI model.
  */
@sfxml
class ColorCalibratorUIController(private val imageTitleLabel: Label,
                                  private val chartTypeChoiceBox: ChoiceBox[GridColorChart],
                                  private val renderReferenceChartSplitButton: SplitMenuButton,
                                  private val marginsSpinner: Spinner[java.lang.Integer],
                                  private val referenceColorSpaceChoiceBox: ChoiceBox[ReferenceColorSpace],
                                  private val enableExtraInfoCB: CheckBox,
                                  private val mappingMethodChoiceBox: ChoiceBox[MappingMethod.Value],
                                  private val suggestCalibrationOptionsButton: Button,
                                  private val calibrateButton: Button,
                                  private val applyToCurrentImageButton: Button,
                                  private val helpButton: Button,
                                  private val rootGridPane: GridPane,
                                  private val model: ColorCalibratorUIModel) extends ControllerFX {

  // Dialog header
  private val headerNode = IJPUtils.createHeaderNode(
    "Color Calibrator",
    "Performs color calibration of an image using a color chart.")
  rootGridPane.add(headerNode, 0, 0, GridPane.Remaining, 1)

  // Image title
  imageTitleLabel.text <== model.imageTitle

  // Reference chart
  chartTypeChoiceBox.items = ObservableBuffer(ColorCharts.values)
  chartTypeChoiceBox.value <==> model.referenceChart
  renderReferenceChartSplitButton.onAction = _ => model.onRenderReferenceChart()
  renderReferenceChartSplitButton.items = List(
    new MenuItem("Reference Colors") {
      onAction = _ => model.onShowReferenceColors()
    }
  )

  // Actual chart
  marginsSpinner.valueFactory = new IntegerSpinnerValueFactory(0, 49) {
    value = model.chipMarginPercent()
    value <==> model.chipMarginPercent
  }
  //  importROIButton.onAction = _ => model.onLoadLocationFromROI()

  // Calibration
  referenceColorSpaceChoiceBox.items = ObservableBuffer(ReferenceColorSpace.values)
  referenceColorSpaceChoiceBox.value <==> model.referenceColorSpace
  model.showExtraInfo <==> enableExtraInfoCB.selected

  mappingMethodChoiceBox.items = ObservableBuffer(MappingMethod.values.toSeq)
  mappingMethodChoiceBox.value <==> model.mappingMethod
  suggestCalibrationOptionsButton.onAction = _ => model.onSuggestCalibrationOptions()
  suggestCalibrationOptionsButton.disable <== !model.chipValuesObserved

  calibrateButton.onAction = _ => model.onCalibrate()
  calibrateButton.disable <== !model.chipValuesObserved
  calibrateButton.alignmentInParent = Pos.Center

  applyToCurrentImageButton.onAction = _ => model.onApplyToCurrentImage()
  applyToCurrentImageButton.disable <== !model.correctionRecipeAvailable

  helpButton.onAction = _ => model.onHelp()
}
