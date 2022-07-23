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

import ij_plugins.color.ui.fx.ColorFXUI
import org.jfree.chart.axis.NumberAxis
import org.jfree.chart.fx.ChartViewer
import org.jfree.chart.labels.{ItemLabelAnchor, ItemLabelPosition, StandardCategoryItemLabelGenerator}
import org.jfree.chart.plot.{CategoryPlot, PlotOrientation}
import org.jfree.chart.renderer.category.{BarRenderer, StatisticalBarRenderer}
import org.jfree.chart.ui.TextAnchor
import org.jfree.chart.{ChartFactory, ChartUtils}
import org.jfree.data.category.DefaultCategoryDataset
import org.jfree.data.statistics.DefaultStatisticalCategoryDataset
import scalafx.Includes.*
import scalafx.scene.paint.Color

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

  case class ValueEntry(row: String, column: String, value: Double)

  case class ValueErrorEntry(row: String, column: String, value: Double, error: Double)

  def createBarPlot(
    title: String,
    data: Seq[ValueEntry],
    categoryAxisLabel: String = "Type",
    valueAxisLabel: String = "Value",
    barColors: Seq[Color] = Seq.empty[Color]
  ): Unit = {

    val dataset = new DefaultCategoryDataset()
    for (d <- data) {
      dataset.addValue(d.value, d.row, d.column)
    }

    val chart = ChartFactory.createBarChart(
      null,              // chart title
      categoryAxisLabel, // domain axis label
      valueAxisLabel,    // range axis label
      dataset,
      PlotOrientation.HORIZONTAL,
      true, // include legend
      true, // tooltips?
      false // URLs?
    )

    val plot = chart.getPlot.asInstanceOf[CategoryPlot]

    // ensure the current theme is applied to the renderer just added
    ChartUtils.applyCurrentTheme(chart)

    // customise the renderer...
    val renderer = new BarRenderer()
    renderer.setDrawBarOutline(false)
    renderer.setIncludeBaseInRange(false)
    renderer.setShadowVisible(false)

    renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator())
    renderer.setDefaultItemLabelsVisible(true)
    renderer.setDefaultItemLabelPaint(java.awt.Color.WHITE)
    renderer.setDefaultPositiveItemLabelPosition(new ItemLabelPosition(
      ItemLabelAnchor.INSIDE6,
      TextAnchor.BOTTOM_CENTER
    ))

    for ((c, i) <- barColors.zipWithIndex) {
      renderer.setSeriesPaint(i, new java.awt.Color(c.red.toFloat, c.green.toFloat, c.blue.toFloat))
    }

    plot.setRenderer(renderer)
    ColorFXUI.showInNewWindow(new ChartViewer(chart), title)
  }

  def createBarErrorPlot(
    title: String,
    data: Seq[ValueErrorEntry],
    categoryAxisLabel: String = "Type",
    valueAxisLabel: String = "Value"
  ): Unit = {

    val dataset: DefaultStatisticalCategoryDataset = new DefaultStatisticalCategoryDataset
    for (d <- data) {
      dataset.add(d.value, d.error, d.row, d.column)
    }

    val chart = ChartFactory.createLineChart(
      null,              // chart title
      categoryAxisLabel, // domain axis label
      valueAxisLabel,    // range axis label
      dataset,
      PlotOrientation.HORIZONTAL,
      true, // include legend
      true, // tooltips?
      false // URLs?
    )

    val plot = chart.getPlot.asInstanceOf[CategoryPlot]

    // customise the range axis...
    val rangeAxis = plot.getRangeAxis.asInstanceOf[org.jfree.chart.axis.NumberAxis]
    rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits)
    rangeAxis.setAutoRangeIncludesZero(false)

    // ensure the current theme is applied to the renderer just added
    ChartUtils.applyCurrentTheme(chart)

    // customise the renderer...
    val renderer = new StatisticalBarRenderer()
    renderer.setDrawBarOutline(false)
    renderer.setErrorIndicatorPaint(java.awt.Color.GRAY)
    renderer.setIncludeBaseInRange(false)
    plot.setRenderer(renderer)

    renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator())
    renderer.setDefaultItemLabelsVisible(true)
    renderer.setDefaultItemLabelPaint(java.awt.Color.WHITE)
    renderer.setDefaultPositiveItemLabelPosition(new ItemLabelPosition(
      ItemLabelAnchor.INSIDE6,
      TextAnchor.BOTTOM_CENTER
    ))

    ColorFXUI.showInNewWindow(new ChartViewer(chart), title)
  }

}
