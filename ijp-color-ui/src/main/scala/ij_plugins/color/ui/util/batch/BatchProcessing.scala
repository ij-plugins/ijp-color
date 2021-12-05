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

package ij_plugins.color.ui.util.batch

import ij.IJ
import org.scalafx.extras.ShowMessage
import scalafx.stage.Window

import java.util.concurrent.atomic.{AtomicBoolean, AtomicInteger}
//import scala.collection.parallel.CollectionConverters._
import scala.jdk.CollectionConverters.*
import scala.util.control.NonFatal

object BatchProcessing {

  /** Individual batch processing item */
  trait BatchItem[T] {

    /** Name of the item, for instance, title opf the image that is processed. */
    def name: String

    /** Perform processing */
    def run(): T
  }
}

/** Helper for running a set of tasks in a batch mode */
class BatchProcessing(override val parentWindow: Option[Window]) extends ShowMessage {

  import BatchProcessing.*

  /**
   * Helper method for implementing ImageJ plugins that do batch processing
   *
   * @param title
   *   title used for messages and dialogs
   * @param itemTasks
   *   items to process
   * @tparam T
   *   type of items return value
   */
  def processItems[T](title: String, itemTasks: Seq[BatchItem[T]]): Unit = {
    IJ.getInstance().getProgressBar.setBatchMode(true)
    IJ.resetEscape()
    IJ.showProgress(0, 100)
    IJ.showStatus(title + " - initializing ...")

    //    Utils.initializeFX(implicitExit = false)
    //    val progressStatus = onFXAndWait {new ProgressStatusDialog("Sample process dialog")}

    try {

      val numberOfItems   = itemTasks.size
      val progressCount   = new AtomicInteger(0)
      val successfulCount = new AtomicInteger(0)
      val abort           = new AtomicBoolean(false)
      val errors          = new java.util.concurrent.ConcurrentHashMap[String, Throwable]()
      val abortingMessage = title + " - processing aborted by user. Waiting to complete..."

      val runningItems = new AtomicInteger(0)

      IJ.showStatus(s"Processed 0 of $numberOfItems. Press ESC to abort.")
      //      onFX {
      //        progressStatus.show()
      //        progressStatus.progress.value = -0.01
      //        progressStatus.statusText.value = s"Processed 0 of $numberOfItems..."
      //      }

      //      // Use number of threads setup in ImageJ preferences
      //      val items: ParSeq[BatchItem[T]] = {
      //        val pc           = itemTasks.par
      //        val forkJoinPool = new java.util.concurrent.ForkJoinPool(Prefs.getThreads)
      //        pc.tasksupport = new ForkJoinTaskSupport(forkJoinPool)
      //        pc
      //      }

      ParHelper.par(itemTasks)
        .foreach { item =>
          ijDebug(s"Running items: ${runningItems.incrementAndGet()}")

          //        if (enableParallelProcessing)
          Thread.currentThread().setPriority(Thread.MIN_PRIORITY)

          if (!abort.get()) {
            try {
              item.run()
              successfulCount.incrementAndGet()
            } catch {
              case NonFatal(t) =>
                errors.put(item.name, t)
            }
            val c = progressCount.incrementAndGet()
            IJ.showProgress(c, numberOfItems)
            IJ.showStatus(if (abort.get) abortingMessage else s"Processed $c of $numberOfItems. Press ESC to abort.")
            //          onFX {
            //            progressStatus.progress.value = c / numberOfItems.toDouble
            //            progressStatus.statusText.value = if (abort.get) abortingMessage else s"Processed $c of $numberOfItems..."
            //          }
          }

          abort.synchronized {
            if (!abort.get) {
              //            abort.set(IJ.escapePressed || progressStatus.abortFlag.value)
              abort.set(IJ.escapePressed)
              if (abort.get) {
                //              onFX {
                //                progressStatus.abortFlag.value = true
                //                progressStatus.statusText.value =
                //                  "Batch processing aborted by user. Waiting for running tasks to complete..."
                //              }
                IJ.beep()
                IJ.showStatus(abortingMessage)
                showWarning(title, "Batch processing aborted by user", "")
              }
            }
          }

          ijDebug(s"Running items (end loop): ${runningItems.decrementAndGet()}")
        }
      if (abort.get()) {
        IJ.showStatus(title + " - batch processing aborted by user.")
      } else {
        IJ.showStatus(title + " - competed.")
      }

      //      onFX {progressStatus.close()}

      // Show summary of processing, list errors if any happened
      showInformation(
        title,
        "" +
          "Processing completed:\n" +
          s"  $numberOfItems items scheduled\n" +
          s"  ${progressCount.get} items processed\n" +
          s"  ${numberOfItems - progressCount.get} items aborted\n" +
          s"  ${successfulCount.get()} successful\n" +
          s"  ${errors.size()} failed",
        ""
      )
      errors.asScala.keys.toSeq.sorted.foreach(k => IJ.log(k + ": " + errors.get(k).getMessage))
    } finally {
      //      onFX {progressStatus.close()}
    }

  }

  private def ijDebug(message: String): Unit = {
    if (IJ.debugMode) IJ.log(message)
  }
}
