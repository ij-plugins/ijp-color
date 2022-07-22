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

import org.scalafx.extras.generic_dialog.GenericDialogFX
import scalafx.stage.Window

/**
  * GenericDialogFX that integrates the LastDirectoryHandler with ImageJ directory handling
  *
  * @param title        dialog title
  * @param header       dialog header
  * @param parentWindow optional parent window
  */
class GenericDialogFXIJ(title: String,
                        header: String,
                        parentWindow: Option[Window] = None)
  extends GenericDialogFX(title, header, parentWindow, LastDirectoryHandlerIJ)
