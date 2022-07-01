/*
 * Image/J Plugins
 * Copyright (C) 2002-2022 Jarek Sacha
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

package ij_plugins.color.ui.util

import ij.gui.GenericDialog
import ij.measure.ResultsTable
import ij.plugin.PlugIn
import ij.process.ColorProcessor
import ij.{CompositeImage, IJ, ImagePlus}
import ij_plugins.color.util.WhiteBalance
import ij_plugins.color.util.WhiteBalance.AveragingMode

import scala.util.control.NonFatal

object WhiteBalancePlugIn {
  private var averagingMode        = AveragingMode.Median
  private var showCorrectionFactor = false
}

class WhiteBalancePlugIn extends PlugIn {
  import WhiteBalancePlugIn.*

  private val TITLE = "White Balance"
  private val ABOUT =
    """Performs White Balance of an RGB image.<br>
      |Requires selection of a area with an expected neutral color,<br>
      |preferably light gray. Avoid areas with saturated (white) values.
      |""".stripMargin

  override def run(arg: String): Unit = {

    // Get inpout image
    val imp = Option(IJ.getImage) match {
      case Some(v) =>
        v
      case None =>
        IJ.error(TITLE, "No open images")
        return
    }

    // Check image type
    val error: Option[String] = imp.getType match {
      case ImagePlus.COLOR_RGB => None
      case ImagePlus.GRAY8 | ImagePlus.GRAY16 | ImagePlus.GRAY32 =>
        if (imp.getStackSize == 3)
          None
        else
          Option(s"Expecting image with 3 bands, got ${imp.getStackSize}")
    }

    if (error.isDefined) {
      IJ.error(TITLE, error.get)
      return
    }

    val roi = Option(imp.getRoi) match {
      case Some(v) => v
      case None =>
        IJ.error(TITLE, "ROI required")
        return
    }

    // Ask to select options
    if (!showOptionsDialog()) {
      return
    }

    try {
      val (dstImp, redMult, blueMult) = WhiteBalance.whiteBalance(imp, roi, averagingMode)
      dstImp.setTitle(s"${imp.getTitle}+white balance")
      dstImp.show()

      if (showCorrectionFactor) {
        val rt = Option(ResultsTable.getResultsTable(TITLE)).getOrElse(new ResultsTable())
        rt.incrementCounter()
        rt.addValue("Label", imp.getShortTitle)
        rt.addValue("Red multiplier", redMult)
        rt.addValue("Blue multiplier", blueMult)
        rt.show(TITLE)
      }
    } catch {
      case NonFatal(ex) =>
        ex.printStackTrace()
        IJ.error(TITLE, ex.getMessage)
    }

  }

  def showOptionsDialog(): Boolean = {
    val gd = new GenericDialog(TITLE, IJ.getInstance)
    gd.addPanel(IJPUtils.createInfoPanel(TITLE, ABOUT))
    gd.addChoice("Averaging method", AveragingMode.values.map(_.name), averagingMode.name)
    gd.addCheckbox("Show correction factor", showCorrectionFactor)

    gd.addHelp("https://github.com/ij-plugins/ijp-color/wiki/White-Balance")

    gd.showDialog()

    if (gd.wasOKed()) {
      averagingMode = AveragingMode.values(gd.getNextChoiceIndex)
      showCorrectionFactor = gd.getNextBoolean
      true
    } else
      false
  }

}
