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

package ij_plugins.color.ui

import ij.IJ
import ij_plugins.color.util.AWTtoFXImageConverter.toBufferImage
import javafx.embed.swing as jfxes
import org.scalafx.extras.*
import scalafx.Includes.*
import scalafx.geometry.Point2D
import scalafx.scene.image.Image

/**
 * JavaFX/ScalaFX utilities.
 */
package object fx {

  /**
   * Convert AWT Point2D to ScalaFX Point2D
   */
  def toFX(point: java.awt.geom.Point2D): Point2D = new Point2D(point.getX, point.getY)

  def toAWT(point: Point2D): java.awt.geom.Point2D = new java.awt.geom.Point2D.Double(point.x, point.y)

  /**
   * Returns icon used by ImageJ main frame. Returns `null` if main frame is not instantiated or has no icon.
   *
   * @return
   *   ImageJ icon or `null`.
   */
  def imageJIconAsFXImage: Option[Image] = {
    Option(IJ.getInstance).flatMap { i =>
      Option(i.getIconImage).map { icon =>
        toFXImage(icon)
      }
    }
  }

  /**
   * Convert AWT image to JavaFX Image.
   *
   * @param image
   *   AWT image.
   */
  def toFXImage(image: java.awt.Image): Image = {
    val bi = toBufferImage(image)
    jfxes.SwingFXUtils.toFXImage(bi, null)
  }

  /**
   * Ensure that JavaFX thread is initialized and unhandled exceptions are caught.
   */
  def initializeFX(): Unit = {

    initFX()

    onFXAndWait {
      // Start is called on the FX Application Thread,
      // so Thread.currentThread() is the FX application thread:
      Thread.currentThread().setUncaughtExceptionHandler(
        new Thread.UncaughtExceptionHandler {
          override def uncaughtException(t: Thread, e: Throwable): Unit = {
            println("FX handler caught exception: " + e.getMessage)
            e.printStackTrace()
            onFX {
              showException("ImageJ FX Exception Handler", "Unexpected error.", e)
            }
          }
        }
      )
    }
  }
}
