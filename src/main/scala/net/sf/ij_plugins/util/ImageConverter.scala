/*
 * Image/J Plugins
 * Copyright (C) 2002-2019 Jarek Sacha
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

package net.sf.ij_plugins.util

import java.awt.image.BufferedImage
import javafx.embed.{swing => jfxes}

import scalafx.Includes._
import scalafx.scene.image.Image

/** Tools for converting images between AWT and JavaFX. */
object ImageConverter {

  /** Convert AWT image to BufferedImage.
    * @param image AWT image.
    */
  def toBufferImage(image: java.awt.Image): BufferedImage = toBufferImage(image, BufferedImage.TYPE_INT_ARGB)

  /** Convert AWT image to BufferedImage.
    * @param image  image to convert
    * @param imageType `BufferedImage` type
    * @see [[java.awt.image.BufferedImage]]
    */
  def toBufferImage(image: java.awt.Image, imageType: Int): BufferedImage = {
    val bi = new BufferedImage(image.getWidth(null), image.getHeight(null), imageType)
    val g = bi.createGraphics
    g.drawImage(image, null, null)
    bi
  }

  /** Convert AWT image to JavaFX Image.
    * @param image AWT image.
    */
  def toFXImage(image: java.awt.Image): Image = {
    val bi = toBufferImage(image)
    jfxes.SwingFXUtils.toFXImage(bi, null)
  }
}
