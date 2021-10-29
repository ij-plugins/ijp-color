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
import org.scalatest.matchers.should.Matchers._

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
