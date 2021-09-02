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

package ij_plugins.color.ui.calibration

import ij.gui.GenericDialog
import ij.measure.ResultsTable
import ij.plugin.BrowserLauncher
import ij.{ImagePlus, Prefs}
import ij_plugins.color.calibration.chart.{ColorChartType, ColorCharts, GridColorChart, ReferenceColorSpace}
import ij_plugins.color.calibration.regression.MappingMethod
import ij_plugins.color.calibration.{CorrectionRecipe, renderReferenceChart}
import ij_plugins.color.converter.ReferenceWhite
import ij_plugins.color.ui.calibration.tasks.{ApplyToCurrentImageTask, CalibrateTask, SuggestCalibrationOptionsTask}
import ij_plugins.color.ui.util.LiveChartROI
import javafx.beans.property.ReadOnlyBooleanProperty
import org.scalafx.extras.mvcfx.ModelFX
import org.scalafx.extras.{BusyWorker, ShowMessage, onFX}
import scalafx.beans.property._
import scalafx.stage.Window

import java.io.File
import java.util.concurrent.Future

object ColorCalibratorUIModel {
  val HelpURL = "https://github.com/ij-plugins/ijp-color/wiki/Color-Calibrator"
  val Title = "IJP Color Calibrator"

  object Config {

    private val ReferencePrefix = classOf[Config].getName

    def loadFromIJPref(): Option[Config] = {
      // We will use `null` to indicate missing values from Java API

      {
        val colorChartTypeName = Option(Prefs.get(ReferencePrefix + ".colorChartType", null.asInstanceOf[String]))
        val colorChartType = ColorChartType.withNameOption(colorChartTypeName.get)
        println(colorChartType)
      }

      for {
        referenceColorSpaceName <-
          Option(Prefs.get(ReferencePrefix + ".referenceColorSpace", null.asInstanceOf[String]))
        referenceColorSpace <- ReferenceColorSpace.withNameOption(referenceColorSpaceName)

        mappingMethodName <- Option(Prefs.get(ReferencePrefix + ".mappingMethod", null.asInstanceOf[String]))
        mappingMethod <- MappingMethod.withNameOption(mappingMethodName)

        colorChartTypeName <- Option(Prefs.get(ReferencePrefix + ".colorChartType", null.asInstanceOf[String]))
        colorChartType <- ColorChartType.withNameOption(colorChartTypeName)

        chipMargin <- Option(Prefs.get(ReferencePrefix + ".chipMargin", null.asInstanceOf[Double]))
        showExtraInfo <- Option(Prefs.get(ReferencePrefix + ".showExtraInfo", null.asInstanceOf[Boolean]))
      } yield Config(
        referenceColorSpace,
        mappingMethod,
        colorChartType,
        chipMargin,
        showExtraInfo
      )
    }
  }

  /**
    * @param referenceColorSpace
    * @param mappingMethod
    * @param chartName name of a predefined chart from ij_plugins.color.calibration.chart.ColorCharts
    * @param chipMargin
    * @param showExtraInfo
    */
  case class Config(
                     referenceColorSpace: ReferenceColorSpace,
                     mappingMethod: MappingMethod,
                     colorChartType: ColorChartType,
                     chipMargin: Double,
                     showExtraInfo: Boolean
                   ) {

    import Config._

    def saveToIJPref(): Unit = {
      Prefs.set(ReferencePrefix + ".referenceColorSpace", referenceColorSpace.entryName)
      Prefs.set(ReferencePrefix + ".mappingMethod", mappingMethod.entryName)
      Prefs.set(ReferencePrefix + ".colorChartType", colorChartType.entryName)
      Prefs.set(ReferencePrefix + ".chipMargin", s"$chipMargin")
      Prefs.set(ReferencePrefix + ".showExtraInfo", s"$showExtraInfo")
    }

  }
}

/**
 * Model for color calibrator UI.
 */
class ColorCalibratorUIModel(val image: ImagePlus, parentWindow: Window) extends ModelFX with ShowMessage {

  import ColorCalibratorUIModel._

  require(parentWindow != null, "Argument `parentStage` cannot be null.")

  val imageTitle = new StringProperty(this, "imageTitle", image.getTitle)
  val referenceColorSpace =
    new ObjectProperty[ReferenceColorSpace](this, "referenceColorSpace", ReferenceColorSpace.sRGB)

  // Parameters defining chart, beside ROI that will be handled by `liveChartROI`
  val referenceChartType = new ObjectProperty[ColorChartType](this, "chart", ColorChartType.XRitePassportColorChecker)
  referenceChartType.onChange { (_, _, _) =>
    recreateReferenceChart()
  }

  // This is a derived value that needs to be updated when UI selections change: `referenceChartType` and `chipMarginPercent`
  private val referenceChartOptionWrapper = new ReadOnlyObjectWrapper[Option[GridColorChart]](this, "chart", None)
  val referenceChartOption: ReadOnlyObjectProperty[Option[GridColorChart]] =
    referenceChartOptionWrapper.readOnlyProperty

  private val referenceChartDefinedWrapper = new ReadOnlyBooleanWrapper()
  val referenceChartDefined: ReadOnlyBooleanProperty = referenceChartDefinedWrapper.readOnlyProperty

  private val referenceChartEditEnabledWrapper = new ReadOnlyBooleanWrapper()
  val referenceChartEditEnabled: ReadOnlyBooleanProperty = referenceChartEditEnabledWrapper.readOnlyProperty

  private var customChartOption: Option[GridColorChart] = None

  val chartInfoText = new StringProperty("???")

  val chipMarginPercent = new ObjectProperty[Integer](this, "chipMargin", 20)
  chipMarginPercent.onChange { (_, _, _) =>
    recreateReferenceChart()
  }

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

  // Initialize reference chart
  recreateReferenceChart()

  def recreateReferenceChart(): Unit = {
    val chartOpt =
      (
        if (referenceChartType.value != ColorChartType.Custom) {
          ColorCharts.withColorChartType(referenceChartType.value)
        } else {
          customChartOption
        }
        ).map(c => c.copyWithNewChipMargin(chipMarginPercent.value / 100d))

    referenceChartOptionWrapper.value = chartOpt

    referenceChartDefinedWrapper.value = referenceChartOptionWrapper.value.isDefined

    referenceChartEditEnabledWrapper.value = referenceChartType.value == ColorChartType.Custom

    chartInfoText.value =
      referenceChartOptionWrapper.value
        .map(c => s"${c.nbColumns} x ${c.nbRows}, ${c.refWhite}")
        .getOrElse("Chart not defined")
  }

  private def currentChart: GridColorChart = {
    liveChartROI.locatedChart.value match {
      case Some(c) => c match {
        case gcc: GridColorChart =>
          gcc
        case x =>
          throw new IllegalStateException(
            s"Internal error. Unexpected class type. Expecting ${classOf[GridColorChart]}, got ${x.getClass}"
          )
      }
      case None => throw new IllegalStateException(s"Internal error. Option is empty.")

    }
  }

  private val busyWorker: BusyWorker = new BusyWorker("Color Calibrator", parentWindow)

  def selectReferenceChartType(newValue: ColorChartType): Unit = {
    referenceChartType.value = newValue
  }

  def onRenderReferenceChart(): Unit = busyWorker.doTask("onRenderReferenceChart") { () =>
    referenceChartOption.value match {
      case Some(referenceChart) =>
        renderReferenceChart(referenceChart).show()
      case None =>
        showError(
          Title,
          "Internal error: Reference Chart not defined to render.",
          "Report the error in project issue tracker at https://github.com/ij-plugins/ijp-color/issues"
        )
    }
  }

  def onShowReferenceColors(): Unit = busyWorker.doTask("onShowReferenceColors") { () =>
    referenceChartOption.value match {
      case Some(referenceChart) =>
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
      case None =>
        showError(
          Title,
          "Internal error: Reference Chart not defined to show color reference.",
          "Report the error in project issue tracker at https://github.com/ij-plugins/ijp-color/issues"
        )
    }
  }

  def onEditChart(): Unit = {

    val _nbRows = customChartOption.map(_.nbRows).getOrElse(5)
    val _nbColumns = customChartOption.map(_.nbColumns).getOrElse(6)
    val _refWhite = customChartOption.map(_.refWhite).getOrElse(ReferenceWhite.D50)
    val _defaultPath = ""

    val gd = new GenericDialog("Edit Custom Reference Chart") {
      addMessage("Chart Layout")
      addNumericField("Rows", _nbRows, 0, 3, "")
      addNumericField("Columns", _nbColumns, 0, 3, "")
      addChoice("Reference White", ReferenceWhite.values.map(_.toString).toArray, _refWhite.toString)
      addFileField("Reference values file", _defaultPath)
    }

    gd.showDialog()

    if (gd.wasOKed()) {
      val nbRows = {
        val v = gd.getNextNumber
        math.max(1, math.round(v).toInt)
      }

      val nbCols = {
        val v = gd.getNextNumber
        math.max(1, math.round(v).toInt)
      }

      val refWhite: ReferenceWhite = {
        val v = gd.getNextChoice
        ReferenceWhite.withName(v)
      }

      val filePath = gd.getNextString
      val file = new File(filePath)

      val chips = ColorCharts.loadReferenceValues(file)

      val chart = new GridColorChart(
        s"Custom - ${file.getName}",
        nbColumns = nbCols,
        nbRows = nbRows,
        chips = chips,
        chipMargin = 0.2,
        refWhite = refWhite
      )

      customChartOption = Option(chart)
      recreateReferenceChart()
    } else {
      //
    }

  }

  def onSuggestCalibrationOptions(): Unit = busyWorker.doTask("onSuggestCalibrationOptions") {
    new SuggestCalibrationOptionsTask(currentChart, image, Option(parentWindow))
  }

  def onCalibrate(): Unit = busyWorker.doTask("onCalibrate") {
    new CalibrateTask(referenceColorSpace, mappingMethod, image, currentChart, showExtraInfo, Option(parentWindow)) {
      override def onFinish(result: Future[Option[CorrectionRecipe]], successful: Boolean): Unit = {
        if (successful) onFX {
          correctionRecipe.value = result.get()
        }
      }
    }
  }

  def onApplyToCurrentImage(): Unit = busyWorker.doTask("onApplyToCurrentImage") {
    new ApplyToCurrentImageTask(correctionRecipe, showExtraInfo, Option(parentWindow))
  }

  def onHelp(): Unit = busyWorker.doTask("onHelp") { () =>
    BrowserLauncher.openURL(HelpURL)
  }

  def toConfig: Config = {
    Config(
      referenceColorSpace.value,
      mappingMethod.value,
      referenceChartType.value,
      chipMarginPercent.value / 100d,
      showExtraInfo.value
    )
  }

  def fromConfig(config: Config): Unit = {
    onFX {
      referenceColorSpace.value = config.referenceColorSpace
      mappingMethod.value = config.mappingMethod
      showExtraInfo.value = config.showExtraInfo
      referenceChartType.value = config.colorChartType
      chipMarginPercent.value = math.round(config.chipMargin * 100).toInt
    }
  }
}
