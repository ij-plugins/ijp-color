/*
 * Image/J Plugins
 * Copyright (C) 2002-2013 Jarek Sacha
 * Author's email: jsacha at users dot sourceforge dot net
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
 * Latest release available at http://sourceforge.net/projects/ij-plugins/
 */

package net.sf.ij_plugins.color.calibration.ui

import javafx.scene.{layout => jfxsl}
import jfxtras.labs.scene.control.ListSpinner
import net.sf.ij_plugins.color.calibration.MappingMethod
import net.sf.ij_plugins.color.calibration.chart.{ReferenceColorSpace, GridColorChart, ColorCharts}
import scalafx.Includes._
import scalafx.collections.ObservableBuffer
import scalafx.event.ActionEvent
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.Label._
import scalafx.scene.control._
import scalafx.scene.layout.{Priority, HBox, GridPane}
import scalafx.scene.text.{FontWeight, Font}

class ColorCalibratorView(model: ColorCalibratorModel) extends jfxsl.StackPane {

  //private lazy val _viewNode: Parent = initView()
  // TODO: add button to suggest best mapping method.

  createContent()


  def createContent() {

    val gp = new GridPane {
      padding = Insets(10)
      vgap = 10
      hgap = 10
    }

    var row = 0


    //
    // Image title
    //
    val imageTitleLabel = new Label() {
      id = "ijp-image-title"
      hgrow = Priority.ALWAYS
      maxWidth = Double.MaxValue
      text <== model.imageTitle
      alignment = Pos.CENTER
      alignmentInParent = Pos.CENTER
    }
    gp.add(imageTitleLabel, 0, row, GridPane.REMAINING, 1)
    row += 1

    //
    // Reference chart
    //
    gp.add(separator("Reference Chart"), 0, row, GridPane.REMAINING, 1)
    row += 1

    val chartTypeChoiceBox = new ChoiceBox[GridColorChart] {
      items = ObservableBuffer(ColorCharts.values)
      value <==> model.chart
    }
    val renderReferenceChartSplitButton = new SplitMenuButton {
      text = "Render"
      onAction = (ae: ActionEvent) => model.onRenderReferenceChart()
      items = List(
        new MenuItem("Reference Colors") {
          onAction = (ae: ActionEvent) => model.onShowReferenceColors()
        }
      )
    }

    gp.addRow(row,
      new Label("Type") {
        id = "ijp-label"
        alignmentInParent = Pos.CENTER_RIGHT
      },
      chartTypeChoiceBox,
      renderReferenceChartSplitButton
    )
    row += 1

    val marginsListSpinner = new ListSpinner[Int](0, 49)
    marginsListSpinner.valueProperty() = model.chipMarginPercent()
    marginsListSpinner.valueProperty <==> model.chipMarginPercent
    marginsListSpinner.arrowDirectionProperty().set(ListSpinner.ArrowDirection.VERTICAL)
    gp.addRow(row,
      new Label("Chip margin %") {
        id = "ijp-label"
        alignmentInParent = Pos.CENTER_RIGHT
      },
      marginsListSpinner
    )
    row += 1


    //
    // Actual chart
    //
    gp.add(separator("Actual Chart"), 0, row, GridPane.REMAINING, 1)
    row += 1
    val importROIButton = new Button {
      id = "ijp-button"
      text = "Load location from ROI"
      onAction = (ae: ActionEvent) => {model.onLoadLocationFromROI()}
      alignmentInParent = Pos.CENTER
    }
    gp.add(importROIButton, 0, row, GridPane.REMAINING, 1)
    row += 1

    gp.add(separator("Calibration"), 0, row, GridPane.REMAINING, 1)
    row += 1

    val referenceColorSpaceChoiceBox = new ChoiceBox[ReferenceColorSpace] {
      items = ObservableBuffer(ReferenceColorSpace.values)
      value <==> model.referenceColorSpace
    }
    gp.addRow(row,
      new Label("Reference") {
        id = "ijp-label"
        alignmentInParent = Pos.CENTER_RIGHT
        tooltip = Tooltip("Reference color space")
      },
      referenceColorSpaceChoiceBox
    )
    row += 1

    val mappingMethodChoiceBox = new ChoiceBox[MappingMethod.Value] {
      items = ObservableBuffer(MappingMethod.values.toSeq)
      value <==> model.mappingMethod
    }
    gp.addRow(row, new
            Label("Mapping method") {
      id = "ijp-label"
      alignmentInParent = Pos.CENTER_RIGHT
    },
      mappingMethodChoiceBox
    )
    row += 1

    val calibrateButton = new Button {
      text = "Calibrate"
      id = "ijp-button"
      margin = Insets(10)
      onAction = (ae: ActionEvent) => {model.onCalibrate()}
      disable <== !model.chipValuesObserved
    }
    gp.add(calibrateButton, 0, row, GridPane.REMAINING, 1)
    calibrateButton.alignmentInParent = Pos.CENTER
    calibrateButton.font = {
      val f = calibrateButton.font()
      Font.font(f.family, FontWeight.BOLD, f.size)
    }
    row += 1

    calibrateButton.prefWidth <== importROIButton.width

    getChildren.addAll(gp)
  }

  private def separator(labelText: String): HBox = {
    new HBox {
      val label = new Label(labelText) {
        id = "ijp-separator"
      }
      content = List(
        label,
        new Separator {
          id = "ijp-separator"
          margin = Insets(10, 0, 5, 10)
          hgrow = Priority.ALWAYS
          alignment = Pos.TOP_CENTER
        }
      )
    }
  }
}
