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

package ij_plugins.color.calibration.regression

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.*

class MappingFactoryTest extends AnyFlatSpec {

  behavior of "MappingFactoryTest"

  it should "createLinearNoIntercept" in {
    val observed = Array(1, 1.5, 2, 3, 4, 5, 7, 10)
    val standard =
      Array(2.205656769, 4.753046388, 5.84672507, 10.1354754, 10.65357822, 14.62303566, 20.3311704, 25.41447332)

    val r = MappingFactory.createLinearNoIntercept(standard, observed)

    r.beta.length should be(1)
    r.beta(0) should be(2.7327 +- 0.0001)
    r.rSquared should be(0.9931 +- 0.0001)
  }

  it should "createLinearNoIntercept (for a band)" in {
    val observed = Array(1, 1.5, 2, 3, 4, 5, 7, 10)
    val standard =
      Array(2.205656769, 4.753046388, 5.84672507, 10.1354754, 10.65357822, 14.62303566, 20.3311704, 25.41447332)

    val cp = MappingFactory.createLinearNoIntercept(standard, observed, 0)

    cp.intercept should be(0.0)
    cp.a should be(2.7327 +- 0.0001)
    cp.b should be(0.0)
    cp.c should be(0.0)
    cp.aa should be(0.0)
    cp.ab should be(0.0)
    cp.bb should be(0.0)
    cp.bc should be(0.0)
    cp.cc should be(0.0)
    cp.aaa should be(0.0)
    cp.abc should be(0.0)
    cp.bbb should be(0.0)
    cp.ccc should be(0.0)

    cp.regressionResult.isDefined should be(true)
    cp.regressionResult.get.rSquared should be(0.9931 +- 0.0001)
  }

  it should "createLinearNoInterceptXBand (for a band)" in {

    // Values based on "IMG_0903_025p-crop.tif"
    // Chip margin: 16
    // Band: red (0)
    // Chips with negative standard values excluded (21 of 24 chips used)

    val standard = Array(126.91979030350699, 216.20834154788753, 65.59369249416655, 98.31654126463174,
      118.2264657620243, 79.14341258243843, 242.05769847504052, 217.36207592151652, 88.1513891672125,
      177.94611517624423, 252.25703548744028, 83.31632229886839, 204.2661490485881, 265.5540885367791,
      204.39823118845072, 248.2796227241613, 201.91675592164282, 160.56106457681673, 117.76955679703373,
      83.66445997737526, 49.37241240933231)
    val observed = Array(
      Array(50.0181598062954, 20.692493946731236, 6.2941888619854724),
      Array(123.94199243379572, 60.84741488020177, 43.90668348045397),
      Array(52.774739583333336, 48.147135416666664, 55.24609375),
      Array(49.07827260458839, 45.76923076923077, 3.4116059379217276),
      Array(80.11018131101812, 56.83682008368201, 76.83124128312413),
      Array(70.71551724137932, 104.76867816091954, 77.96120689655173),
      Array(129.9434832756632, 48.10957324106113, 6.1280276816609),
      Array(122.0730198019802, 31.084158415841586, 27.407178217821784),
      Array(34.896153846153844, 13.28974358974359, 25.8),
      Array(111.19521912350598, 106.04780876494024, 0.0),
      Array(159.51440329218107, 89.1673525377229, 0.0),
      Array(42.25541619156214, 64.54161915621437, 4.850627137970354),
      Array(101.95985832349469, 20.65289256198347, 14.334120425029516),
      Array(166.38536585365853, 109.20487804878049, 0.0),
      Array(118.84702907711757, 41.25031605562579, 65.6826801517067),
      Array(160.03979057591624, 124.83769633507853, 102.07748691099476),
      Array(130.071661237785, 99.51031487513572, 81.14332247557003),
      Array(101.88004484304933, 76.65807174887892, 62.26569506726457),
      Array(71.38675958188153, 50.54239256678281, 37.92566782810685),
      Array(42.70169082125604, 29.83816425120773, 21.544685990338163),
      Array(15.848180677540778, 5.183186951066499, 2.986198243412798)
    )

    val expectedResult = Array(0.0, 1.9724784162678572, -0.426914583530878, -0.1296547005685931, 0.0, 0.0, 0.0, 0.0,
      0.0, 0.0, 0.0, 0.0, 0.0, 0.0)

    val cp      = MappingFactory.createLinearNoInterceptXBand(standard, observed)
    val cpArray = cp.toArray

    cpArray should be(expectedResult)

    cp.regressionResult.isDefined should be(true)
    cp.regressionResult.get.rSquared should be(0.991 +- 0.001)
  }

  it should "createLinear" in {
    val observed = Array(1, 1.5, 2, 3, 4, 5, 7, 10)
    val standard =
      Array(2.205656769, 4.753046388, 5.84672507, 10.1354754, 10.65357822, 14.62303566, 20.3311704, 25.41447332)

    val r = MappingFactory.createLinear(standard, observed)

    r.beta.length should be(2)
    r.beta(0) should be(0.9443 +- 0.0001)
    r.beta(1) should be(2.5794 +- 0.0001)
    r.rSquared should be(0.9812 +- 0.0001)
  }

}
