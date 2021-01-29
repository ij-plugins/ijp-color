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

package net.sf.ij_plugins.color.calibration

import net.sf.ij_plugins.color.calibration.regression.{CubicPolynomial, CubicPolynomialTriple}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._


/**
  * @author Jarek Sacha
  */
class CorrectorSpec extends AnyFlatSpec {

  "Corrector" should "map correctly" in {
    val poly0 = new CubicPolynomial(intercept = 2.94261364, a = 0.00152897, b = 0.00100197, c = 0.00017698)
    val poly1 = new CubicPolynomial(intercept = 3.03222422, a = 0.00082806, b = 0.00206088, c = -0.00017526)
    val poly2 = new CubicPolynomial(intercept = 3.41117364, a = 0.00024909, b = -0.00010136, c = 0.00270941)

    val corrector = CubicPolynomialTriple(poly0, poly1, poly2)

    val observed = Array(
      Array(3851.8935, 1820.0959, 1081.1987),
      Array(16410.105, 10655.9209, 7690.6463),
      Array(3472.7686, 8135.1251, 12503.3714),
      Array(2684.0717, 4054.299, 1411.8277),
      Array(7477.836, 8336.0778, 15226.6103),
      Array(5201.1832, 19058.8477, 16467.0218),
      Array(20038.0401, 4672.8133, 78.9282),
      Array(2221.6156, 4260.4349, 14121.6244),
      Array(15785.1385, 2236.8796, 2232.0834),
      Array(2970.861, 1290.8259, 4063.3003),
      Array(8753.7481, 17177.9334, 2280.2256),
      Array(19219.8492, 9679.0086, 266.2074),
      Array(414.4198, 2596.39, 9125.5971),
      Array(1107.6495, 10670.6027, 3125.244),
      Array(11850.0087, 241.4328, 334.8191),
      Array(23971.9866, 19444.3318, 353.3156),
      Array(14612.4164, 3170.6016, 8516.6393),
      Array(856.1605, 9154.4555, 13771.144),
      Array(29347.4846, 35452.8842, 34019.2315),
      Array(17745.2141, 22728.3415, 22223.1236),
      Array(10271.6726, 13308.6545, 13039.6492),
      Array(4647.7389, 5868.4717, 5809.7155),
      Array(1399.1883, 1857.1424, 1683.8465),
      Array(376.3982, 466.0531, 423.6355)
    )

    val expected = Array(
      Array(10.8471, 9.7833, 7.1156),
      Array(40.0712, 37.2335, 27.2557),
      Array(18.6163, 20.4821, 37.3284),
      Array(11.3586, 13.3628, 7.494),
      Array(25.4232, 23.7354, 45.684),
      Array(32.9057, 43.7311, 47.3908),
      Array(38.2762, 29.2412, 8.1426),
      Array(13.1074, 11.1771, 41.794),
      Array(29.714, 20.322, 13.1639),
      Array(9.4975, 7.4404, 15.0295),
      Array(33.9422, 45.2829, 10.0286),
      Array(42.0744, 38.848, 7.9388),
      Array(7.7928, 7.1269, 27.9762),
      Array(15.8809, 25.3925, 11.0731),
      Array(21.3621, 13.2836, 7.2455),
      Array(59.1403, 62.893, 8.3687),
      Array(29.9687, 20.1738, 29.8046),
      Array(15.8613, 20.1939, 40.0082),
      Array(89.3574, 94.4356, 99.2998),
      Array(56.7806, 60.6719, 65.7391),
      Array(34.2903, 36.68, 39.9505),
      Array(16.9571, 17.9568, 19.7149),
      Array(7.2407, 7.7231, 8.1337),
      Array(4.0601, 4.2301, 4.6055)
    )

    val corrected = corrector.map(observed)
     for (i <- 0 until expected.length) {
      for (j <- 0 until 3)
        corrected(i)(j) should be(expected(i)(j) +- 0.001)
    }
  }
}
