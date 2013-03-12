/*
 * Image/J Plugins
 * Copyright (C) 2002-2013 Jarek Sacha
 * Author's email: jsacha at users dot sourceforge dot net
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

package net.sf.ij_plugins.color.calibration

import net.sf.ij_plugins.color.calibration.MappingMethod._
import org.apache.commons.math3.linear.MatrixUtils

/** Factory creating mapping between a reference and observed values using various
  * polynomial mappings and linear regression.
  */
object MappingFactory {
  def createCubicPolynomialTriple(standard: Array[Array[Double]],
                                  observed: Array[Array[Double]],
                                  method: MappingMethod): CubicPolynomialTriple = {
    validateStandardAndObserved(standard, observed)
    val standardM = MatrixUtils.createRealMatrix(standard)
    val observedM = MatrixUtils.createRealMatrix(observed)
    val (redBandCoefficients, greenBandCoefficients, blueBandCoefficients) = method match {
      case Linear => (
          createLinear(standardM.getColumn(0), observedM.getColumn(0), 0),
          createLinear(standardM.getColumn(1), observedM.getColumn(1), 1),
          createLinear(standardM.getColumn(2), observedM.getColumn(2), 2)
          )
      case LinearCrossBand => (
          createLinearXBand(standardM.getColumn(0), observed),
          createLinearXBand(standardM.getColumn(1), observed),
          createLinearXBand(standardM.getColumn(2), observed)
          )
      case Quadratic => (
          createQuadratic(standardM.getColumn(0), observedM.getColumn(0), 0),
          createQuadratic(standardM.getColumn(1), observedM.getColumn(1), 1),
          createQuadratic(standardM.getColumn(2), observedM.getColumn(2), 2)
          )
      case QuadraticCrossBand => (
          createQuadraticXBand(standardM.getColumn(0), observed),
          createQuadraticXBand(standardM.getColumn(1), observed),
          createQuadraticXBand(standardM.getColumn(2), observed)
          )
      case Cubic => (
          createCubic(standardM.getColumn(0), observedM.getColumn(0), 0),
          createCubic(standardM.getColumn(1), observedM.getColumn(1), 1),
          createCubic(standardM.getColumn(2), observedM.getColumn(2), 2)
          )
      case CubicCrossBand => (
          createCubicXBand(standardM.getColumn(0), observed),
          createCubicXBand(standardM.getColumn(1), observed),
          createCubicXBand(standardM.getColumn(2), observed)
          )
      case _ =>
        throw new IllegalArgumentException("Invalid Mapping method '" + method + "'")
    }
    new CubicPolynomialTriple(redBandCoefficients, greenBandCoefficients, blueBandCoefficients)
  }

  def createLinear(standard: Array[Double], observed: Array[Double], band: Int): CubicPolynomial = {
    validateStandardAndObserved(standard, observed)
    val regressionResult = createLinear(standard, observed)
    val intercept = regressionResult.beta(0)
    band match {
      case 0 => new CubicPolynomial(
        intercept = intercept,
        a = regressionResult.beta(1),
        regressionResult = Some(regressionResult)
      )
      case 1 => new CubicPolynomial(
        intercept = intercept,
        b = regressionResult.beta(1),
        regressionResult = Some(regressionResult)
      )
      case 2 => new CubicPolynomial(
        intercept = intercept,
        c = regressionResult.beta(1),
        regressionResult = Some(regressionResult)
      )
      case _ =>
        throw new IllegalArgumentException("Unknown band '" + band + "'")
    }
  }

  def createLinear(standard: Array[Double], observed: Array[Double]): Regression.Result = {
    validateStandardAndObserved(standard, observed)
    require(observed.length >= 2, "Linear fit needs at least 2 observations, got " + observed.length)

    val data = Array.ofDim[Double](observed.length, 1)
    for (i <- 0 until observed.length) {
      data(i)(0) = observed(i)
    }
    Regression.regression(standard, data, noIntercept = false)
  }

  private def createLinearXBand(standard: Array[Double],
                                observation: Array[Array[Double]]): CubicPolynomial = {
    validateStandardAndObserved(standard, observation)
    require(observation.length >= 4, "Linear cross-band fit needs at least 4 observations, got " + observation.length)
    val rr = Regression.regression(standard, observation, noIntercept = false)
    toCubicPolynomial(rr)
  }

  private def createQuadraticXBand(standard: Array[Double], observation: Array[Array[Double]]): CubicPolynomial = {
    validateStandardAndObserved(standard, observation)
    require(observation.length >= 10, "Quadratic cross-band fit needs at least 10 observations, got " + observation.length)
    val data = Array.ofDim[Double](observation.length, 9)

    for (i <- 0 until observation.length) {
      val o: Array[Double] = observation(i)
      assert(o.length == 3)
      val a = o(0)
      val b = o(1)
      val c = o(2)
      data(i)(0) = a
      data(i)(1) = b
      data(i)(2) = c
      data(i)(3) = a * a
      data(i)(4) = a * b
      data(i)(5) = a * c
      data(i)(6) = b * b
      data(i)(7) = b * c
      data(i)(8) = c * c
    }
    val rr = Regression.regression(standard, data, noIntercept = false)
    toCubicPolynomial(rr)
  }

  def createQuadratic(standard: Array[Double], observed: Array[Double], band: Int): CubicPolynomial = {
    validateStandardAndObserved(standard, observed)
    val regressionResult = createQuadratic(standard, observed)
    val intercept = regressionResult.beta(0)
    band match {
      case 0 => new CubicPolynomial(
        intercept = intercept,
        a = regressionResult.beta(1),
        aa = regressionResult.beta(2),
        regressionResult = Some(regressionResult)
      )
      case 1 => new CubicPolynomial(
        intercept = intercept,
        b = regressionResult.beta(1),
        bb = regressionResult.beta(2),
        regressionResult = Some(regressionResult)
      )
      case 2 => new CubicPolynomial(
        intercept = intercept,
        c = regressionResult.beta(1),
        cc = regressionResult.beta(2),
        regressionResult = Some(regressionResult)
      )
      case _ =>
        throw new IllegalArgumentException("Unknown band '" + band + "'")
    }
  }

  def createQuadratic(standard: Array[Double], observed: Array[Double]): Regression.Result = {
    validateStandardAndObserved(standard, observed)
    require(observed.length >= 3, "Quadratic fit needs at least 3 observations, got " + observed.length)
    val data: Array[Array[Double]] = Array.ofDim[Double](observed.length, 2)
    for (i <- 0 until observed.length) {
      val o: Double = observed(i)
      data(i)(0) = o
      data(i)(1) = o * o
    }
    Regression.regression(standard, data, noIntercept = false)
  }

  private def createCubicXBand(standard: Array[Double], observed: Array[Array[Double]]): CubicPolynomial = {
    validateStandardAndObserved(standard, observed)
    require(observed.length >= 14, "Cubic cross-band fit needs at least 14 observations, got " + observed.length)
    val data: Array[Array[Double]] = Array.ofDim[Double](observed.length, 13)

    for (i <- 0 until observed.length) {
      val o: Array[Double] = observed(i)
      assert(o.length == 3)
      val a = o(0)
      val b = o(1)
      val c = o(2)
      data(i)(0) = a
      data(i)(1) = b
      data(i)(2) = c
      data(i)(3) = a * a
      data(i)(4) = a * b
      data(i)(5) = a * c
      data(i)(6) = b * b
      data(i)(7) = b * c
      data(i)(8) = c * c
      data(i)(9) = a * a * a
      data(i)(10) = a * b * c
      data(i)(11) = b * b * b
      data(i)(12) = c * c * c
    }
    val rr = Regression.regression(standard, data, noIntercept = false)
    toCubicPolynomial(rr)
  }

  def createCubic(standard: Array[Double], observed: Array[Double], band: Int): CubicPolynomial = {
    validateStandardAndObserved(standard, observed)
    val regressionResult = createCubic(standard, observed)
    val intercept = regressionResult.beta(0)
    band match {
      case 0 => new CubicPolynomial(
        intercept = intercept,
        a = regressionResult.beta(1),
        aa = regressionResult.beta(2),
        aaa = regressionResult.beta(3),
        regressionResult = Some(regressionResult)
      )
      case 1 => new CubicPolynomial(
        intercept = intercept,
        b = regressionResult.beta(1),
        bb = regressionResult.beta(2),
        bbb = regressionResult.beta(3),
        regressionResult = Some(regressionResult)
      )
      case 2 => new CubicPolynomial(
        intercept = intercept,
        c = regressionResult.beta(1),
        cc = regressionResult.beta(2),
        ccc = regressionResult.beta(3),
        regressionResult = Some(regressionResult)
      )
      case _ =>
        throw new IllegalArgumentException("Unknown band '" + band + "'")
    }
  }

  private def createCubic(standard: Array[Double], observed: Array[Double]): Regression.Result = {
    validateStandardAndObserved(standard, observed)
    require(observed.length >= 4, "Cubic fit needs at least 4 observations, got " + observed.length)
    val data: Array[Array[Double]] = Array.ofDim[Double](observed.length, 3)

    for (i <- 0 until observed.length) {
      val o: Double = observed(i)
      data(i)(0) = o
      data(i)(1) = o * o
      data(i)(2) = o * o * o
    }
    Regression.regression(standard, data, noIntercept = false)
  }

  private def toCubicPolynomial(rr: Regression.Result): CubicPolynomial = {
    require(rr != null)
    require(rr.beta != null)
    require(rr.beta.length <= 14, "Size of alpha must be less or equal 14, got" + rr.beta.length)

    val alpha14 = new Array[Double](14)
    rr.beta.copyToArray(alpha14)

    new CubicPolynomial(alpha14, Option(rr))
  }

  private def validateStandardAndObserved(standard: Array[Double], observed: Array[Double]) {
    require(observed != null)
    require(standard != null)
    require(observed.length == standard.length, "Number of Standard and Observed values are not equal reference.length='" + standard.length + "' observed.length='" + observed.length + "'")
  }

  private def validateStandardAndObserved(standard: Array[Double], observation: Array[Array[Double]]) {
    require(standard != null)
    require(observation != null)
    require(observation.length == standard.length)
  }

  private def validateStandardAndObserved(standard: Array[Array[Double]], observed: Array[Array[Double]]) {
    require(observed != null)
    require(standard != null)
    require(observed.length == standard.length,
      "Number of Standard and Observed values are not equal reference.length='" + standard.length + "' observed.length='" + observed.length + "'")

    standard.foreach(s =>
      require(s.length == 3, "Number of Standard Color values should be equal to 3 (RED,GREEN and BLUE), got " + s.length)
    )
    observed.foreach(o =>
      require(o.length == 3, "Number of Observed Color values should be equal to 3 (RED,GREEN and BLUE), got " + o.length)
    )
  }
}
