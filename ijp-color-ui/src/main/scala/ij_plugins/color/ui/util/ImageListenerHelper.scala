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

package ij_plugins.color.ui.util

import ij.{ImageListener, ImagePlus}

/**
  * Helps managing use of [[ij.ImageListener]]
  *
  * Use of the helper is responsible for calling `setupImageListener` and `removeImageListener`
  */
trait ImageListenerHelper {

  private var imageListener: Option[ImageListener] = None

  /** Image of interest, when changed `handleImageUpdated` or `handleImageClosed` will be called. */
  protected var image: Option[ImagePlus] = None


  /**
    * Called when the image is updated
    */
  protected def handleImageUpdated(): Unit


  /**
    * Called when the image is closed
    */
  protected def handleImageClosed(): Unit


  protected def setupImageListener(): Unit = {

    if (imageListener.nonEmpty) {
      throw new IllegalStateException("ImageListener already created")
    }

    imageListener = Some(
      new ImageListener {
        def imageUpdated(imp: ImagePlus): Unit = {
          if (image.contains(imp)) {
            handleImageUpdated()
          }
        }

        def imageClosed(imp: ImagePlus): Unit = {
          if (image.contains(imp)) {
            handleImageClosed()
          }
        }

        def imageOpened(imp: ImagePlus): Unit = {}
      }
    )

    ImagePlus.addImageListener(imageListener.get)
  }


  protected def removeImageListener(): Unit = {
    imageListener match {
      case Some(il) => ImagePlus.removeImageListener(il)
      case _ =>
    }
  }
}
