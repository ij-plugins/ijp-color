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

package net.sf.ij_plugins.color.ui.util

import ij.gui.Roi

/**
  * Helps using LiveChartROI. Handles adding and removing ROI Listener
  */
trait LiveChartROIHelper {
  private var _liveChartROIOption: Option[LiveChartROI] = None

  protected def liveChartROIOption: Option[LiveChartROI] = _liveChartROIOption

  final protected def setupROIListener(liveChartROI: LiveChartROI): Unit = {

    if (liveChartROIOption.nonEmpty) {
      throw new IllegalStateException("RoiListener already created")
    }

    _liveChartROIOption = Some(liveChartROI)

    liveChartROIOption.foreach(Roi.addRoiListener)
  }

  final protected def removeROIListener(): Unit = {
    liveChartROIOption.foreach(Roi.removeRoiListener)
  }
}
