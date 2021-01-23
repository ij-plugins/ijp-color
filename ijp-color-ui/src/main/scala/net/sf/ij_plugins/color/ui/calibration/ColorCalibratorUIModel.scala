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

package net.sf.ij_plugins.color.ui.calibration

import ij.ImagePlus
import ij.measure.ResultsTable
import ij.plugin.BrowserLauncher
import javafx.beans.property.ReadOnlyBooleanProperty
import net.sf.ij_plugins.color.calibration.chart.{GridColorChart, ReferenceColorSpace}
import net.sf.ij_plugins.color.calibration.regression.MappingMethod
import net.sf.ij_plugins.color.calibration.{CorrectionRecipe, renderReferenceChart}
import net.sf.ij_plugins.color.ui.calibration.tasks.{ApplyToCurrentImageTask, CalibrateTask, SuggestCalibrationOptionsTask}
import net.sf.ij_plugins.color.ui.util.LiveChartROI
import org.scalafx.extras.mvcfx.ModelFX
import org.scalafx.extras.{BusyWorker, ShowMessage}
import scalafx.beans.property._
import scalafx.stage.Window

object ColorCalibratorUIModel {
  val HelpURL = "https://github.com/ij-plugins/ijp-color"
}

/**
  * Model for color calibrator UI.
  */
class ColorCalibratorUIModel(val image: ImagePlus, parentWindow: Window) extends ModelFX with ShowMessage {

  import ColorCalibratorUIModel._

  require(parentWindow != null, "Argument `parentStage` cannot be null.")

  val imageTitle = new StringProperty(this, "imageTitle", image.getTitle)
  val referenceColorSpace = new ObjectProperty[ReferenceColorSpace](this, "referenceColorSpace", ReferenceColorSpace.sRGB)
  val referenceChartOption = new ObjectProperty[Option[GridColorChart]](this, "chart", None)
  val chipMarginPercent = new ObjectProperty[Integer](this, "chipMargin", 20)
  val mappingMethod = new ObjectProperty[MappingMethod](this, "mappingMethod", MappingMethod.LinearCrossBand)
  val clipReferenceRGB = new BooleanProperty(this, "clipReferenceRGB", true)
  val showExtraInfo = new BooleanProperty(this, "showExtraInfo", false)
  val correctionRecipe = new ObjectProperty[Option[CorrectionRecipe]](this, "correctionRecipe", None)


  val liveChartROI: LiveChartROI = LiveChartROI(image, referenceChartOption)

  val chipValuesObserved: ReadOnlyBooleanProperty = {
    val p = new ReadOnlyBooleanWrapper(this, "chipValuesObserved", false)
    // Chip values are observed when `locatedChart` is available
    p <== liveChartROI.locatedChart =!= None
    p.getReadOnlyProperty
  }

  // True when correction parameters are available and can be applied to another image
  val correctionRecipeAvailable: ReadOnlyBooleanProperty = {
    val p = new ReadOnlyBooleanWrapper(this, "correctionRecipeAvailable", false)
    p <== correctionRecipe =!= None
    p.getReadOnlyProperty
  }

  private def currentChart: GridColorChart = {
    liveChartROI.locatedChart.value match {
      case Some(c) => c match {
        case gcc: GridColorChart =>
          gcc
        case x =>
          throw new IllegalStateException(
            s"Internal error. Unexpected class type. Expecting ${classOf[GridColorChart]}, got ${x.getClass}")
      }
      case None => throw new IllegalStateException(s"Internal error. Option is empty.")

    }
  }

  private val busyWorker: BusyWorker = new BusyWorker("Color Calibrator", parentWindow)

  chipMarginPercent.onChange { (_, _, _) =>
    updateChipMarginPercent()
  }

  private def updateChipMarginPercent(): Unit = {
    referenceChart = referenceChart.copyWithNewChipMargin(chipMarginPercent() / 100d)
  }

  def selectReferenceChart(newValue: GridColorChart): Unit = {
    referenceChart = newValue
    updateChipMarginPercent()
  }

  private def referenceChart = referenceChartOption().get

  private def referenceChart_=(newValue: GridColorChart): Unit = {
    referenceChartOption() = Option(newValue)
  }


  def onRenderReferenceChart(): Unit = busyWorker.doTask("onRenderReferenceChart") { () =>
    renderReferenceChart(referenceChart).show()
  }

  def onShowReferenceColors(): Unit = busyWorker.doTask("onShowReferenceColors") { () =>
    val rt = new ResultsTable()
    val chips = referenceChart.referenceChips
    for (i <- chips.indices) {
      rt.incrementCounter()
      rt.setLabel(chips(i).name, i)
      val lab = chips(i).color
      val xyz = referenceChart.colorConverter.toXYZ(lab)
      val rgb = referenceChart.colorConverter.toRGB(xyz)
      rt.setValue("CIE L*", i, lab.l)
      rt.setValue("CIE a*", i, lab.a)
      rt.setValue("CIE b*", i, lab.b)
      rt.setValue("CIE X", i, xyz.x)
      rt.setValue("CIE Y", i, xyz.y)
      rt.setValue("CIE Z", i, xyz.z)
      rt.setValue("sRGB R", i, rgb.r)
      rt.setValue("sRGB G", i, rgb.g)
      rt.setValue("sRGB B", i, rgb.b)
    }
    rt.show(referenceChart.name + " / " + referenceChart.refWhite)
  }

  def onSuggestCalibrationOptions(): Unit = busyWorker.doTask("onSuggestCalibrationOptions") {
    new SuggestCalibrationOptionsTask(currentChart, image, Option(parentWindow))
  }

  def onCalibrate(): Unit = busyWorker.doTask("onCalibrate") {
    new CalibrateTask(correctionRecipe, referenceColorSpace, mappingMethod, image, currentChart, showExtraInfo, Option(parentWindow))
  }

  def onApplyToCurrentImage(): Unit = busyWorker.doTask("onApplyToCurrentImage") {
    new ApplyToCurrentImageTask(correctionRecipe, showExtraInfo, Option(parentWindow))
  }

  def onHelp(): Unit = busyWorker.doTask("onHelp") { () =>
    BrowserLauncher.openURL(HelpURL)
  }
}
