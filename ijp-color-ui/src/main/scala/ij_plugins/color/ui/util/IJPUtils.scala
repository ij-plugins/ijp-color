/*
 * Image/J Plugins
 * Copyright (C) 2002-2023 Jarek Sacha
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

import ij.IJ
import ij_plugins.color.BuildInfo
import scalafx.geometry.Insets
import scalafx.scene.Node
import scalafx.scene.control.Label
import scalafx.scene.image.ImageView
import scalafx.scene.layout.GridPane

import java.awt.*
import java.io.IOException
import java.net.URISyntaxException
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.event.HyperlinkEvent.EventType.ACTIVATED
import javax.swing.event.{HyperlinkEvent, HyperlinkListener}
import javax.swing.text.html.HTMLDocument
import scala.util.control.NonFatal

/**
 * Internal utilities.
 */
object IJPUtils {

  private val LogoIconPath = "/ij_plugins/color/ui/IJP-48-rounded.png"

  /**
    * Load icon as a resource for given class without throwing exceptions.
    *
    * @param aClass
    * Class requesting resource.
    * @param path
    * Icon file path.
    * @return
    * Icon or null if loading failed.
    */
  private def loadIcon(aClass: Class[?], path: String): ImageIcon = {
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
    * Create pane for displaying a message that may contain HTML formatting, including links.
    *
    * @param message
    * the message.
    * @param title
    * used in error dialogs.
    * @return
    * component containg the message.
    */
  private def createHTMLMessageComponent(message: String, title: String): JComponent = {
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
            case ex @ (_: IOException | _: URISyntaxException) =>
              IJ.error(title, "Error following a link.\n" + ex.getMessage)
          }
        }
      }
    })
    pane.setText(message)
    pane
  }

  /**
    * Create simple info panel for a plugin dialog. Intended to be displayed at the top.
    *
    * @param title
    * title displayed in bold font larger than default.
    * @param message
    * message that can contain HTML formatting.
    * @return
    * a panel containing the message with a title and a default icon.
    */
  def createHeaderAWT(title: String, message: String): Panel = {
    // TODO: use icon with rounded corners
    val titlePanel = new JPanel(new BorderLayout(10, 10))
    val logo = IJPUtils.loadIcon(this.getClass, LogoIconPath)
    if (logo != null) {
      val logoLabel = new JLabel(logo, SwingConstants.CENTER)
      logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT)
      titlePanel.add(logoLabel, BorderLayout.WEST)
    }

    val centerTitlePanel = new JPanel(new BorderLayout(7, 0))

    val titleLabel = new JLabel(title)
    val titleFont = titleLabel.getFont
    titleLabel.setFont(titleFont.deriveFont(Font.BOLD, titleFont.getSize.toFloat * 1.777f))
    centerTitlePanel.add(titleLabel, BorderLayout.CENTER)

    val versionLabel = new JLabel(BuildInfo.version)
    val versionFont = versionLabel.getFont
    versionLabel.setFont(versionFont.deriveFont(Font.PLAIN, versionFont.getSize.toFloat * 0.750f))
    centerTitlePanel.add(versionLabel, BorderLayout.SOUTH)

    titlePanel.add(centerTitlePanel, BorderLayout.CENTER)

    val rootPanel = new Panel(new BorderLayout(7, 10))
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

  def createHeaderFX(title: String, message: String): Node = {
    // Create header with logo, title, and a brief description
    val headerGP = new GridPane {
      vgap = 3
      hgap = 3
    }

    val ijpLogoView = new ImageView(LogoIconPath)
    headerGP.add(ijpLogoView, 0, 0, 1, 2)

    val pluginTitleLabel = new Label {
      text = title
      id = "ijp-header-title"
      padding = Insets(0, 0, 0, 7)
    }
    headerGP.add(pluginTitleLabel, 1, 0)

    val pluginVersionLabel = new Label {
      text = BuildInfo.version
      id = "ijp-version-string"
      padding = Insets(0, 0, 0, 7)
    }
    headerGP.add(pluginVersionLabel, 1, 1)

    val descriptionLabel = new Label {
      text = message
      id = "ijp-header-message"
      wrapText = true
    }
    headerGP.add(descriptionLabel, 0, 2, GridPane.Remaining, 1)

    headerGP
  }
}
