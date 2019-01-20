/*
 * Image/J Plugins
 * Copyright (C) 2002-2015 Jarek Sacha
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


import java.io.IOException
import java.net.URL

import ij.ImagePlus

import scala.reflect.runtime.universe.typeOf
import scalafx.stage.Window
import scalafxml.core.{DependenciesByType, FXMLView}

/**
 * @author Jarek Sacha 
 */
class ColorCalibratorMVVM(val image: ImagePlus, private var _parentWindow: Window) {

  // Load main view
  private val resource = resourceURL("ColorCalibratorView.fxml")

  val model = new ColorCalibratorModel(image, _parentWindow)

  val view = FXMLView(resource, new DependenciesByType(Map(typeOf[ColorCalibratorModel] -> model)))

  def parentWindow: Window = _parentWindow
  def parentWindow_=(newParent: Window): Unit = {
    _parentWindow = newParent
  }


  private def resourceURL(resourcePath: String): URL = {
    val resource = getClass.getResource(resourcePath)
    if (resource == null) {
      throw new IOException("Cannot load resource: '" + resourcePath + "'")
    }
    resource
  }
}
