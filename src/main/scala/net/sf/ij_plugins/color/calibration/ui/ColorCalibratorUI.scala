/*
 * Image/J Plugins
 * Copyright (C) 2002-2016 Jarek Sacha
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

import ij.ImagePlus
import org.scalafx.extras._

import scala.reflect.runtime.universe.{typeOf, _}
import scalafx.Includes._
import scalafx.scene.layout.Pane
import scalafx.stage.Window
import scalafxml.core.{DependenciesByType, FXMLView}

/**
  * @author Jarek Sacha 
  */
class ColorCalibratorUI(imp: ImagePlus, _parent: Window = null) {

  private val _model      = new ColorCalibratorMVVM(imp, _parent)
  private val _view: Pane = createView()

  def model: ColorCalibratorMVVM = _model
  def view: Pane = _view

  def parent: Window = _parent
  def parent_=(newParent: Window): Unit = {
    _model.parentWindow = newParent
  }


  private def createView(): Pane = {

    val view = onFXAndWait {createFXMLView(_model, typeOf[ColorCalibratorMVVM], "ColorCalibratorView.fxml")}
    //    _model.initialize()
    view
  }

  /**
    * Creates FXMLView using provided FXML file (`fxmlFilePath`).
    *
    * @param model        model for the created view.
    * @param fxmlFilePath location of FXML file, in relative the in relation to `model`
    * @return
    */
  private def createFXMLView(model: Object, modelType: Type, fxmlFilePath: String): Pane = {
    // Load main view
    val resource = getClass.getResource(fxmlFilePath)
    if (resource == null) {
      throw new IOException("Cannot load resource: '" + fxmlFilePath + "'")
    }

    val v = FXMLView(resource, new DependenciesByType(Map(modelType -> model)))
    v.asInstanceOf[javafx.scene.layout.Pane]
  }

}
