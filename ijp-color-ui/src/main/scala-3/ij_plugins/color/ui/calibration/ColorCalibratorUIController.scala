/*
 * Image/J Plugins
 * Copyright (C) 2002-2023 Jarek Sacha
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

package ij_plugins.color.ui.calibration

import ij_plugins.color.calibration.chart.{ColorChartType, ReferenceColorSpace}
import ij_plugins.color.calibration.regression.MappingMethod
import ij_plugins.color.ui.util.{IJPUtils, ImageJUIColors}
import javafx.fxml as jfxf
import javafx.scene.{control as jfxsc, layout as jfxsl}
import org.scalafx.extras.mvcfx.ControllerFX
import scalafx.Includes.*
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Pos
import scalafx.scene.control.*
import scalafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory
import scalafx.scene.layout.GridPane

import java.util.concurrent.atomic.AtomicBoolean

/**
 * Binds ColorCalibrator FXML to UI model.
 */
class ColorCalibratorUIController(
  private val model: ColorCalibratorUIModel
) extends ControllerFX {

  @jfxf.FXML
  private var imageTitleLabel: jfxsc.Label = _
  @jfxf.FXML
  private var chartTypeChoiceBox: jfxsc.ChoiceBox[ColorChartType] = _
  @jfxf.FXML
  private var renderReferenceChartSplitButton: jfxsc.SplitMenuButton = _
  @jfxf.FXML
  private var chartInfoLabel: jfxsc.Label = _
  @jfxf.FXML
  private var editChartButton: jfxsc.Button = _
  @jfxf.FXML
  private var marginsSpinner: jfxsc.Spinner[java.lang.Integer] = _
  @jfxf.FXML
  private var chipOverlayColorChoiceBox: jfxsc.ChoiceBox[String] = _
  @jfxf.FXML
  private var overlayStrokeWidthSpinner: jfxsc.Spinner[java.lang.Integer] = _
  @jfxf.FXML
  private var enabledChipsChoiceBox: jfxsc.ChoiceBox[ChipsEnabledType] = _
  @jfxf.FXML
  private var selectChipsButton: jfxsc.Button = _
  @jfxf.FXML
  private var referenceColorSpaceChoiceBox: jfxsc.ChoiceBox[ReferenceColorSpace] = _
  @jfxf.FXML
  private var mappingMethodChoiceBox: jfxsc.ChoiceBox[MappingMethod] = _
  @jfxf.FXML
  private var suggestCalibrationOptionsButton: jfxsc.Button = _
  @jfxf.FXML
  private var selectOutputsButton: jfxsc.Button = _
  @jfxf.FXML
  private var calibrateButton: jfxsc.Button = _
  @jfxf.FXML
  private var applyToCurrentImageButton: jfxsc.Button = _
  @jfxf.FXML
  private var applyInBatchButton: jfxsc.Button = _
  @jfxf.FXML
  private var helpButton: jfxsc.Button = _
  @jfxf.FXML
  private var rootGridPane: jfxsl.GridPane = _

  @jfxf.FXML
  def initialize(): Unit = {

    // Dialog header
    val headerNode = IJPUtils.createHeaderFX(
      "Color Calibrator",
      "Performs color calibration of an image using a color chart."
    )
    rootGridPane.add(headerNode, 0, 0, GridPane.Remaining, 1)

    // Image title
    imageTitleLabel.text <== model.imageTitle

    // Reference chart
    chartTypeChoiceBox.items = ObservableBuffer.from(ColorChartType.values)

    val chartIsChanging = new AtomicBoolean(false)
    chartTypeChoiceBox.selectionModel().selectedItem.onChange { (_, _, newValue) =>
      chartIsChanging.synchronized {
        if (!chartIsChanging.getAndSet(true)) {
          model.selectReferenceChartType(newValue)
        }
        chartIsChanging.set(false)
      }
    }
    chartTypeChoiceBox.selectionModel().selectFirst()

    model.referenceChartType.onChange { (_, _, newValue) =>
      chartTypeChoiceBox.selectionModel().select(newValue)
    }

    editChartButton.onAction = _ => model.onEditChart()
    editChartButton.disable <== model.referenceChartType =!= ColorChartType.Custom

    renderReferenceChartSplitButton.onAction = _ => model.onRenderReferenceChart()
    renderReferenceChartSplitButton.items = List(
      new MenuItem("Reference Colors") {
        onAction = _ => model.onShowReferenceColors()
      }
    )

    renderReferenceChartSplitButton.disable <== !model.referenceChartDefined

    chartInfoLabel.text <== model.chartInfoText

    // Actual chart
    marginsSpinner.valueFactory = new IntegerSpinnerValueFactory(0, 49) {
      value = model.chipMarginPercent()
      value <==> model.chipMarginPercent
    }

    chipOverlayColorChoiceBox.items = ObservableBuffer.from(ImageJUIColors.listColorNames)
    chipOverlayColorChoiceBox.selectionModel.value.select(model.chipOverlayColorName.value)
    chipOverlayColorChoiceBox.selectionModel.value.selectedItem.onChange { (_, _, newValue) =>
      model.chipOverlayColorName.value = newValue
    }
    model.chipOverlayColorName.onChange { (_, _, newValue) =>
      chipOverlayColorChoiceBox.selectionModel.value.select(newValue)
    }

    overlayStrokeWidthSpinner.valueFactory = new IntegerSpinnerValueFactory(0, 100) {
      value = model.chipOverlayStrokeWidth()
      value <==> model.chipOverlayStrokeWidth
    }

    // Enabled chips type choice
    enabledChipsChoiceBox.items = ObservableBuffer.from(ChipsEnabledType.values)
    // Update model when UI changed
    val enabledChipsIsChanging = new AtomicBoolean(false)
    enabledChipsChoiceBox.selectionModel().selectedItem.onChange { (_, _, newValue) =>
      enabledChipsIsChanging.synchronized {
        if (!enabledChipsIsChanging.getAndSet(true)) {
          model.enabledChipsType.value = newValue
        }
        enabledChipsIsChanging.set(false)
      }
    }
    enabledChipsChoiceBox.selectionModel().selectFirst()
    // Update UI when enabledChipsType changed
    model.enabledChipsType.onChange { (_, _, newValue) =>
      enabledChipsChoiceBox.selectionModel().select(newValue)
    }

    selectChipsButton.onAction = _ => model.onSelectEnabledChips()
    selectChipsButton.disable <== (model.enabledChipsType =!= ChipsEnabledType.Custom) or !model.referenceChartDefined

    selectOutputsButton.onAction = _ => model.onSelectOutputs()

    // Calibration
    referenceColorSpaceChoiceBox.items = ObservableBuffer.from(ReferenceColorSpace.values)
    referenceColorSpaceChoiceBox.value <==> model.referenceColorSpace

    mappingMethodChoiceBox.items = ObservableBuffer.from(MappingMethod.values)
    mappingMethodChoiceBox.value <==> model.mappingMethod
    suggestCalibrationOptionsButton.onAction = _ => model.onSuggestCalibrationOptions()
    suggestCalibrationOptionsButton.disable <== !model.chipValuesObserved

    calibrateButton.onAction = _ => model.onCalibrate()
    calibrateButton.disable <== !model.chipValuesObserved
    calibrateButton.alignmentInParent = Pos.Center

    applyToCurrentImageButton.onAction = _ => model.onApplyToCurrentImage()
    applyToCurrentImageButton.disable <== !model.correctionRecipeAvailable

    applyInBatchButton.onAction = _ => model.onApplyInBatch()
    applyInBatchButton.disable <== !model.correctionRecipeAvailable

    helpButton.onAction = _ => model.onHelp()
  }
}
