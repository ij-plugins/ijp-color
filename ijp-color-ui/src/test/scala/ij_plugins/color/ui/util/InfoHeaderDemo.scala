package ij_plugins.color.ui.util

import ij_plugins.color.ui.fx.ColorFXUI
import scalafx.Includes.*
import scalafx.application.JFXApp3
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.layout.BorderPane

object InfoHeaderDemo extends JFXApp3 {
  override def start(): Unit = {
    stage = new JFXApp3.PrimaryStage {
      title = "InfoHeader Demo"
      scene = new Scene {
        content = new BorderPane {
          center = IJPUtils.createHeaderNode(
            title = "My Plugin Name",
            message = "Fancy, but brief, description what this plugin does"
          )
          margin = Insets(14)
          padding = Insets(14)
        }
        stylesheets ++= ColorFXUI.stylesheets
      }
    }
  }
}
