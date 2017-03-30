/*
 * Image/J Plugins
 * Copyright (C) 2002-2017 Jarek Sacha
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

package net.sf.ij_plugins

import org.scalafx.extras._

/**
  * JavaFX/ScalaFX utilities.
  */
package object fx {

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
