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

package ij_plugins.color.calibration

import ij.IJ
import ij_plugins.color.calibration.chart.{ColorCharts, ReferenceColorSpace}
import ij_plugins.color.calibration.regression.MappingMethod
import ij_plugins.color.util.PerspectiveTransform
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._

import java.io.File

/**
  * @author Jarek Sacha
  */
class ColorCalibratorSpec extends AnyFlatSpec {

  "ColorCalibrator" should "perform color calibration in XYZ" in {

    // Chip values extracted from image "test/data/Passport-linear.tif"
    val observed = Array(
      Array(3851.893490574798, 1820.0958925310304, 1081.198749497013),
      Array(16410.104982096556, 10655.920854426473, 7690.646283491789),
      Array(3472.768551673046, 8135.125138906038, 12503.371373009013),
      Array(2684.0717372515123, 4054.299049265341, 1411.8277256451413),
      Array(7477.835998271392, 8336.07775651315, 15226.610291394),
      Array(5201.1832443052745, 19058.847700620125, 16467.021796524838),
      Array(20038.040115145326, 4672.813291237193, 78.92818893738199),
      Array(2221.6156006914434, 4260.434930238301, 14121.624367205828),
      Array(15785.138535621682, 2236.879583899247, 2232.0833744906777),
      Array(2970.8610322262007, 1290.8258735646375, 4063.300253117669),
      Array(8753.748147919496, 17177.93335596987, 2280.2255525373503),
      Array(19219.849174188003, 9679.008565113281, 266.20740467857803),
      Array(414.4198337846671, 2596.390045755906, 9125.597099013292),
      Array(1107.6494937646623, 10670.602697863933, 3125.2439807383626),
      Array(11850.008704778367, 241.4327694777133, 334.81911347079887),
      Array(23971.986603284357, 19444.33176935424, 353.3155945178417),
      Array(14612.416440301271, 3170.6015866156317, 8516.639276453883),
      Array(856.160496101185, 9154.455516669737, 13771.1440412599),
      Array(29347.484639088616, 35452.884178416905, 34019.23145640738),
      Array(17745.21410220695, 22728.341535890057, 22223.123595505618),
      Array(10271.672644770959, 13308.654494382023, 13039.64915421657),
      Array(4647.7389183849855, 5868.471663168292, 5809.715520434622),
      Array(1399.1883257192246, 1857.1423632547228, 1683.8465242622547),
      Array(376.3981937853972, 466.053053727231, 423.635542629726)

    )

    // Parameters
    val chart = ColorCharts.XRitePassportColorChecker.copyWithNewChipMargin(0.25)
    val colorSpace = ReferenceColorSpace.XYZ
    val method = MappingMethod.LinearCrossBand

    val expectedXYZDeltas = Array(
      1.240, 3.884, 3.085, 0.512, 1.109, 2.427,
      1.906, 1.420, 2.287, 1.491, 1.783, 5.575,
      3.046, 1.190, 2.417, 2.491, 2.795, 0.992,
      0.527, 0.940, 1.070, 2.671, 2.808, 1.951

    )

    // Create color calibration
    val colorCalibrator = new ColorCalibrator(chart, colorSpace, method, clipReferenceRGB = true)
    val fit = colorCalibrator.computeCalibrationMapping(observed)

    //    fit.observed.foreach(a => println(a.mkString("Array(", ",", ")\n")))

    // Check deltas, this is a consistency check, deltas may be lower if the fit algorithm is improved
    val deltas = fit.correctedDeltas
    for (i <- expectedXYZDeltas.indices) {
      deltas(i) should be(expectedXYZDeltas(i) +- 0.1)
    }
  }


  "ColorCalibrator" should "perform color calibration of an image in XYZ" in {

    // Parameters
    val chart = ColorCharts.XRitePassportColorChecker.copyWithNewChipMargin(0.25)
    val colorSpace = ReferenceColorSpace.XYZ
    val method = MappingMethod.LinearCrossBand
    val testImage = new File("../test/data/Passport-linear-25.tif")
    assert(testImage.exists(), "File must exists: " + testImage.getCanonicalPath)

    val chartLocationROI = Array(
      point2D(25, 18), point2D(25 + 546, 18),
      point2D(25 + 546, 18 + 362), point2D(25, 18 + 362)
    )
    val newChart = chart.copyWith(PerspectiveTransform.quadToQuad(
      chart.referenceOutline.toArray, chartLocationROI))

    val expectedXYZDeltas = Array(
      1.240, 3.884, 3.085, 0.512, 1.109, 2.427,
      1.906, 1.420, 2.287, 1.491, 1.783, 5.575,
      3.046, 1.190, 2.417, 2.491, 2.795, 0.992,
      0.527, 0.940, 1.070, 2.671, 2.808, 1.951

    )

    // Load test image
    val imp = IJ.openImage(testImage.getCanonicalPath)
    imp should not equal null

    // Create color calibration
    val clipReferenceRGB = false
    val colorCalibrator = new ColorCalibrator(newChart, colorSpace, method, clipReferenceRGB)
    val fit = colorCalibrator.computeCalibrationMapping(imp)

    // Check deltas, this is a consistency check, deltas may be lower if the fit algorithm is improved
    val deltas = fit.correctedDeltas
    for (i <- expectedXYZDeltas.indices) {
      deltas(i) should be(expectedXYZDeltas(i) +- 0.1)
    }
  }

  "ColorCalibrator" should "be fast :) - benchmark test" in {

    // Parameters
    val chart = ColorCharts.XRitePassportColorChecker.copyWithNewChipMargin(0.25)
    val colorSpace = ReferenceColorSpace.XYZ
    val method = MappingMethod.LinearCrossBand
    val testImage = "../test/data/Passport-linear-25.tif"

    val chartLocationROI = Array(
      point2D(25, 18), point2D(25 + 546, 18),
      point2D(25 + 546, 18 + 362), point2D(25, 18 + 362)
    )
    val newChart = chart.copyWith(PerspectiveTransform.quadToQuad(
      chart.referenceOutline.toArray, chartLocationROI))

    val expectedXYZDeltas = Array(
      1.240, 3.884, 3.085, 0.512, 1.109, 2.427,
      1.906, 1.420, 2.287, 1.491, 1.783, 5.575,
      3.046, 1.190, 2.417, 2.491, 2.795, 0.992,
      0.527, 0.940, 1.070, 2.671, 2.808, 1.951

    )

    // Load test image
    val imp = IJ.openImage(testImage)
    imp should not equal null

    // Create color calibration
    val clipReferenceRGB = false
    val colorCalibrator = new ColorCalibrator(newChart, colorSpace, method, clipReferenceRGB)
    val fit = colorCalibrator.computeCalibrationMapping(imp)

    // Check deltas, this is a consistency check, deltas may be lower if the fit algorithm is improved
    val deltas = fit.correctedDeltas
    for (i <- expectedXYZDeltas.indices) {
      deltas(i) should be(expectedXYZDeltas(i) +- 0.1)
    }

    var bestTime = Long.MaxValue
    val corrector = fit.corrector
    for (_ <- 1 to 10) {
      val startTime: Long = System.currentTimeMillis
      val correctedImp = corrector.map(imp)
      val time: Long = System.currentTimeMillis - startTime
      println("Correction time: " + time + " ms")
      assert(correctedImp != null)
      bestTime = math.min(bestTime, time)
    }
    println("Correction best time: " + bestTime + " ms")

  }

}
