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
 * Latest release available at http://sourceforge.net/projects/ij-plugins/
 */

package net.sf.ij_plugins.util

import java.awt.Color

import net.sf.ij_plugins.color.ColorFXUI
import org.jfree.chart.axis.NumberAxis
import org.jfree.chart.fx.ChartViewer
import org.jfree.chart.labels.{ItemLabelAnchor, ItemLabelPosition, StandardCategoryItemLabelGenerator}
import org.jfree.chart.plot.{CategoryPlot, PlotOrientation}
import org.jfree.chart.renderer.category.StatisticalBarRenderer
import org.jfree.chart.ui.TextAnchor
import org.jfree.chart.{ChartFactory, ChartUtils}
import org.jfree.data.statistics.DefaultStatisticalCategoryDataset
import scalafx.Includes._

object PlotUtils {

  //  def createBarPlotFX(title: String, values: Seq[Double], categories: Seq[String]): Unit = {
  //
  //    val yAxis = CategoryAxis(ObservableBuffer(categories))
  //    val xAxis = NumberAxis("Mean Delta E (smaller is better)")
  //
  //    def xyData(xs: Seq[Double]) = ObservableBuffer(
  //      xs zip categories map (xy => XYChart.Data[Number, String](xy._1, xy._2))
  //    )
  //
  //    val series1 = XYChart.Series("Deltas", xyData(values))
  //
  //    val chart = new BarChart[Number, String](xAxis, yAxis) {
  //      data = series1
  //      legendVisible = false
  //    }
  //
  //    ColorFXUI.showInNewWindow(chart, title)
  //  }

  //  def createBarPlot(title: String,
  //                    data: Seq[(String, Double)],
  //                    categoryAxisLabel: String = "Type",
  //                    valueAxisLabel: String = "Value"
  //                   ): Unit = {
  //
  //
  //    val dataset: DefaultStatisticalCategoryDataset = new DefaultStatisticalCategoryDataset
  //    for (d <- data) {
  //      dataset.add(d._2, 1, "", d._1)
  //    }
  //
  //    val chart = ChartFactory.createBarChart(title, // chart title
  //      categoryAxisLabel, // domain axis label
  //      valueAxisLabel, // range axis label
  //      dataset,
  //      PlotOrientation.HORIZONTAL,
  //      true, // include legend
  //      true, // tooltips?
  //      false // URLs?
  //    )
  //
  //    ColorFXUI.showInNewWindow(new ChartViewer(chart), title)
  //  }

  def createBarErrorPlot(title: String,
                         data: Seq[(String, Double, Double)],
                         categoryAxisLabel: String = "Type",
                         valueAxisLabel: String = "Value"
                        ): Unit = {

    val dataset: DefaultStatisticalCategoryDataset = new DefaultStatisticalCategoryDataset
    for (d <- data) {
      dataset.add(d._2, d._3, "", d._1)
    }

    val chart = ChartFactory.createLineChart(null, // chart title
      categoryAxisLabel, // domain axis label
      valueAxisLabel, // range axis label
      dataset,
      PlotOrientation.HORIZONTAL,
      false, // include legend
      true, // tooltips?
      false // URLs?
    )

    val plot = chart.getPlot.asInstanceOf[CategoryPlot]

    // customise the range axis...
    val rangeAxis = plot.getRangeAxis.asInstanceOf[org.jfree.chart.axis.NumberAxis]
    rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits)
    rangeAxis.setAutoRangeIncludesZero(false)

    // customise the renderer...
    val renderer = new StatisticalBarRenderer()
    renderer.setDrawBarOutline(false)
    renderer.setErrorIndicatorPaint(Color.GRAY)
    renderer.setIncludeBaseInRange(false)
    plot.setRenderer(renderer)

    // ensure the current theme is applied to the renderer just added
    ChartUtils.applyCurrentTheme(chart)

    renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator())
    renderer.setDefaultItemLabelsVisible(true)
    renderer.setDefaultItemLabelPaint(Color.WHITE)
    renderer.setDefaultPositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.INSIDE6, TextAnchor.BOTTOM_CENTER))

    ColorFXUI.showInNewWindow(new ChartViewer(chart), title)
  }


}
