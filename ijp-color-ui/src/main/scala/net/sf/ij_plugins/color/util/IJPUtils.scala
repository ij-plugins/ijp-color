/*
 * Image/J Plugins
 * Copyright (C) 2002-2020 Jarek Sacha
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

package net.sf.ij_plugins.color.util

import java.awt._
import java.io.IOException
import java.net.URISyntaxException

import ij.IJ
import javax.swing._
import javax.swing.border.EmptyBorder
import javax.swing.event.HyperlinkEvent.EventType.ACTIVATED
import javax.swing.event.{HyperlinkEvent, HyperlinkListener}
import javax.swing.text.html.HTMLDocument
import scalafx.geometry.Insets
import scalafx.scene.Node
import scalafx.scene.control.Label
import scalafx.scene.image.ImageView
import scalafx.scene.layout.GridPane

import scala.util.control.NonFatal

/**
  * Internal utilities.
  */
object IJPUtils {

  /**
    * Load icon as a resource for given class without throwing exceptions.
    *
    * @param aClass Class requesting resource.
    * @param path   Icon file path.
    * @return Icon or null if loading failed.
    */
  def loadIcon(aClass: Class[_], path: String): ImageIcon = {
    try {
      val url = aClass.getResource(path)
      if (url == null) {
        IJ.log("Unable to find resource '" + path + "' for class '" + aClass.getName + "'.")
        return null
      }
      return new ImageIcon(url)
    } catch {
      case NonFatal(t) =>
        IJ.log("Error loading icon from resource '" + path + "' for class '" + aClass.getName + "'. \n" + t.getMessage)
    }
    null
  }

  /**
    * Create pane for displaying a message that may contain HTLM formatting, including links.
    *
    * @param message the message.
    * @param  title  used in error dialogs.
    * @return component containg the message.
    */
  def createHTMLMessageComponent(message: String, title: String): JComponent = {
    val pane = new JEditorPane()
    pane.setContentType("text/html")
    pane.setEditable(false)
    pane.setOpaque(false)
    pane.setBorder(null)
    val htmlDocument = pane.getDocument.asInstanceOf[HTMLDocument]
    val font = UIManager.getFont("Label.font")
    val bodyRule = "body { font-family: " + font.getFamily + "; " + "font-size: " + font.getSize + "pt; }"
    htmlDocument.getStyleSheet.addRule(bodyRule)
    pane.addHyperlinkListener(new HyperlinkListener() {
      def hyperlinkUpdate(e: HyperlinkEvent): Unit = {
        if (e.getEventType == ACTIVATED) {
          try {
            Desktop.getDesktop.browse(e.getURL.toURI)
          } catch {
            case ex@(_: IOException | _: URISyntaxException) =>
              IJ.error(title, "Error following a link.\n" + ex.getMessage)
          }
        }
      }
    })
    pane.setText(message)
    pane
  }

  /**
    * Creeate simple info panel for a plugin dialog. Intended to be displayed at the top.
    *
    * @param title   title displayed in bold font larger than default.
    * @param message message that can contain HTML formatting.
    * @return a panel containing the message with a title and a default icon.
    */
  def createInfoPanel(title: String, message: String): Panel = {
    // TODO: use icon with rounded corners
    val rootPanel = new Panel(new BorderLayout(7, 7))
    val titlePanel = new Panel(new BorderLayout(7, 7))
    val logo = IJPUtils.loadIcon(this.getClass, "/net/sf/ij_plugins/color/IJP-48.png")
    if (logo != null) {
      val logoLabel = new JLabel(logo, SwingConstants.CENTER)
      logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT)
      titlePanel.add(logoLabel, BorderLayout.WEST)
    }
    val titleLabel = new JLabel(title)
    val font = titleLabel.getFont
    titleLabel.setFont(font.deriveFont(Font.BOLD, font.getSize.toFloat * 2))
    titlePanel.add(titleLabel, BorderLayout.CENTER)

    rootPanel.add(titlePanel, BorderLayout.NORTH)

    val messageComponent = IJPUtils.createHTMLMessageComponent(message, title)
    rootPanel.add(messageComponent, BorderLayout.CENTER)

    // Add some spacing at the bottom
    val separatorPanel = new JPanel(new BorderLayout())
    separatorPanel.setBorder(new EmptyBorder(7, 0, 7, 0))
    separatorPanel.add(new JSeparator(), BorderLayout.SOUTH)
    rootPanel.add(separatorPanel, BorderLayout.SOUTH)

    rootPanel
  }


  def createHeaderNode(title: String, message: String): Node = {
    // Create header with logo, title, and a brief description
    val headerGP = new GridPane {
      vgap = 3
      hgap = 3
    }

    val ijpLogoView = new ImageView("/net/sf/ij_plugins/color/IJP-48.png")
    headerGP.add(ijpLogoView, 0, 0)

    val pluginTitleLabel = new Label {
      text = title
      id = "ijp-header-title"
      padding = Insets(0, 0, 0, 7)
    }
    headerGP.add(pluginTitleLabel, 1, 0)

    val descriptionLabel = new Label {
      text = message
      id = "ijp-header-message"
      wrapText = true
    }
    headerGP.add(descriptionLabel, 0, 1, GridPane.Remaining, 1)

    headerGP
  }

}
