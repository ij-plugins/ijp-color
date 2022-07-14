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

package ij_plugins.color.ui.fx

import ij.io.OpenDialog
import org.scalafx.extras.generic_dialog.LastDirectoryHandler

import java.io.File

/**
  * Integrates last directory handling with ImageJ
  */
object LastDirectoryHandlerIJ extends LastDirectoryHandler {

  def lastDirectory: java.io.File = {
    Option(OpenDialog.getDefaultDirectory) match {
      case Some(dir) =>
        val f = new File(dir)
        if (f.exists() & f.isFile) f.getParentFile else f
      case None => new File(".")
    }
  }

  def lastDirectory_=(newDir: java.io.File): Unit = {
    Option(newDir).foreach { f =>
      val dir = if (f.exists() & f.isFile) f.getParentFile else f
      OpenDialog.setLastDirectory(dir.getAbsolutePath)
      OpenDialog.setDefaultDirectory(dir.getAbsolutePath)
    }
  }
}
