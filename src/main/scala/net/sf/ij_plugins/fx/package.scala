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

import java.io.{PrintWriter, StringWriter}
import java.util.concurrent
import javafx.embed.swing.JFXPanel

import scalafx.Includes._
import scalafx.application.Platform
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.{Alert, Label, TextArea}
import scalafx.scene.layout.{GridPane, Priority}
import scalafx.stage.Window

/**
  * JavaFX/ScalaFX utilities.
  */
package object fx {

  /**
    * Ensure that JavaFX thread is initialized and unhandled exceptions are caught.
    */
  def initializeFX(): Unit = {

    // Make sure that FX thread is not shutdown automatically,
    // auto shutdown may cause calling `Platform.runLater` to hang, possible bug in JVM 8.0_11
    Platform.implicitExit = false

    // Create JFXPanel to force initialization of JavaFX.
    new JFXPanel()

    onFXAndWait {
      // Start is called on the FX Application Thread,
      // so Thread.currentThread() is the FX application thread:
      Thread.currentThread().setUncaughtExceptionHandler(
        new Thread.UncaughtExceptionHandler {
          override def uncaughtException(t: Thread, e: Throwable): Unit = {
            println("FX handler caught exception: " + e.getMessage)
            e.printStackTrace()
            onFX {
              showException("ImageJ FX Exception Handler", "Unexpected error.", e, null)
            }
          }
        }
      )
    }
  }

  /**
    * Run operation `op` on FX application thread. Return without waiting for the operation to complete.
    *
    * @param op operation to be performed.
    */
  def onFX[R](op: => R): Unit = {
    if (Platform.isFxApplicationThread) {
      op
    } else {
      Platform.runLater {
        op
      }
    }
  }

  /**
    * Run operation `op` off FX application thread and wait for completion.
    * If the current thread is not the FX application, the operation will be run on it (no new thread will ne created).
    *
    * @param op operation to be performed.
    */
  def offFXAndWait[R](op: => R): R = {
    if (!Platform.isFxApplicationThread) {
      op
    } else {
      val callable = new concurrent.Callable[R] {
        override def call(): R = op
      }
      val future = new concurrent.FutureTask(callable)
      val th = new Thread(future)
      th.setDaemon(true)
      th.start()
      future.get()
    }
  }


  /**
    * Run operation `op` on FX application thread and wait for completion.
    * If the current thread is the FX application, the operation will be run on it.
    *
    * @param op operation to be performed.
    */
  def onFXAndWait[R](op: => R): R = {
    if (Platform.isFxApplicationThread) {
      op
    } else {
      val callable = new concurrent.Callable[R] {
        override def call(): R = op
      }
      val future = new concurrent.FutureTask(callable)
      Platform.runLater(future)
      future.get()
    }
  }

  def showException(title: String, message: String, t: Throwable, owner: Window): Unit = {
    t.printStackTrace()

    // Rename to avoid name clashes
    val dialogTitle = title

    // Create expandable Exception.
    val exceptionText = {
      val sw = new StringWriter()
      val pw = new PrintWriter(sw)
      t.printStackTrace(pw)
      sw.toString
    }
    val label = new Label("The exception stack trace was:")
    val textArea = new TextArea {
      text = exceptionText
      editable = false
      wrapText = true
      maxWidth = Double.MaxValue
      maxHeight = Double.MaxValue
      vgrow = Priority.Always
      hgrow = Priority.Always
    }
    val expContent = new GridPane {
      maxWidth = Double.MaxValue
      add(label, 0, 0)
      add(textArea, 0, 1)
    }

    onFXAndWait {
      new Alert(AlertType.Error) {
        initOwner(owner)
        title = dialogTitle
        headerText = message
        contentText = Option(t.getMessage).getOrElse("")
        // Set expandable Exception into the dialog pane.
        dialogPane().expandableContent = expContent
      }.showAndWait()
    }
  }

}
