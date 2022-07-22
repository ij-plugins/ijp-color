/*
 * Image/J Plugins
 * Copyright (C) 2002-2022 Jarek Sacha
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

import ij_plugins.color.calibration.regression.MappingMethod.*
import org.apache.commons.math3.linear.MatrixUtils

/**
 * Factory creating mapping between a reference and observed values using various polynomial mappings and linear
 * regression.
 */
object MappingFactory {

  def createCubicPolynomialTriple(standard: IndexedSeq[IndexedSeq[Double]],
                                  observed: IndexedSeq[IndexedSeq[Double]],
                                  method: MappingMethod
                                 ): CubicPolynomialTriple = {
    validateStandardAndObserved(standard, observed)
    val standardM = MatrixUtils.createRealMatrix(toArrayArray(standard))
    val observedM = MatrixUtils.createRealMatrix(toArrayArray(observed))
    val (redBandCoefficients, greenBandCoefficients, blueBandCoefficients) = method match {
      case LinearNoIntercept => (
        createLinearNoIntercept(standardM.getColumn(0), observedM.getColumn(0), 0),
        createLinearNoIntercept(standardM.getColumn(1), observedM.getColumn(1), 1),
        createLinearNoIntercept(standardM.getColumn(2), observedM.getColumn(2), 2)
      )
      case LinearNoInterceptCrossBand => (
        createLinearNoInterceptXBand(standardM.getColumn(0), observed),
        createLinearNoInterceptXBand(standardM.getColumn(1), observed),
        createLinearNoInterceptXBand(standardM.getColumn(2), observed)
      )
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
    }
    CubicPolynomialTriple(redBandCoefficients, greenBandCoefficients, blueBandCoefficients)
  }

  private[regression] def createLinearNoIntercept(
    standard: Array[Double],
    observed: Array[Double],
    band: Int
  ): CubicPolynomial = {
    validateStandardAndObserved(standard, observed)
    val regressionResult = createLinearNoIntercept(standard, observed)
    band match {
      case 0 => CubicPolynomial(
          intercept = 0,
          a = regressionResult.beta(0),
          regressionResult = Some(regressionResult)
        )
      case 1 => CubicPolynomial(
          intercept = 0,
          b = regressionResult.beta(0),
          regressionResult = Some(regressionResult)
        )
      case 2 => CubicPolynomial(
          intercept = 0,
          c = regressionResult.beta(0),
          regressionResult = Some(regressionResult)
        )
      case _ =>
        throw new IllegalArgumentException("Unknown band '" + band + "'")
    }
  }

  private[regression] def createLinearNoIntercept(
    standard: Array[Double],
    observed: Array[Double]
  ): Regression.Result = {
    validateStandardAndObserved(standard, observed)
    require(observed.length >= 1, "Linear No-intercept fit needs at least 1 observation, got " + observed.length)

    val data = observed.map(v => Array(v).toIndexedSeq).toIndexedSeq

    Regression.regression(standard, data, noIntercept = true)
  }

  private[regression] def createLinearNoInterceptXBand(standard: Array[Double],
                                                       observation: IndexedSeq[IndexedSeq[Double]]
                                                      ): CubicPolynomial = {
    validateStandardAndObserved2(standard, observation)
    require(observation.length >= 4, "Linear cross-band fit needs at least 4 observations, got " + observation.length)
    val rr = Regression.regression(standard, observation, noIntercept = true)

    require(rr != null)
    require(rr.beta != null)
    require(rr.beta.length == 3, "Size of beta must be equal 3, got " + rr.beta.length)

    val alpha14 = new Array[Double](14)
    rr.beta.copyToArray(alpha14, 1)

    new CubicPolynomial(alpha14, Option(rr))
  }

  private[regression] def createLinear(standard: Array[Double], observed: Array[Double], band: Int): CubicPolynomial = {
    validateStandardAndObserved(standard, observed)
    val regressionResult = createLinear(standard, observed)
    val intercept        = regressionResult.beta(0)
    band match {
      case 0 => CubicPolynomial(
          intercept = intercept,
          a = regressionResult.beta(1),
          regressionResult = Some(regressionResult)
        )
      case 1 => CubicPolynomial(
          intercept = intercept,
          b = regressionResult.beta(1),
          regressionResult = Some(regressionResult)
        )
      case 2 => CubicPolynomial(
          intercept = intercept,
          c = regressionResult.beta(1),
          regressionResult = Some(regressionResult)
        )
      case _ =>
        throw new IllegalArgumentException("Unknown band '" + band + "'")
    }
  }

  private[regression] def createLinear(standard: Array[Double], observed: Array[Double]): Regression.Result = {
    validateStandardAndObserved(standard, observed)
    require(observed.length >= 2, "Linear fit needs at least 2 observations, got " + observed.length)

    val data = observed.map(v => Array(v).toIndexedSeq).toIndexedSeq
    Regression.regression(standard, data, noIntercept = false)
  }

  private[regression] def createLinearXBand(standard: Array[Double],
                                            observation: IndexedSeq[IndexedSeq[Double]]
                                           ): CubicPolynomial = {
    validateStandardAndObserved2(standard, observation)
    require(observation.length >= 4, "Linear cross-band fit needs at least 4 observations, got " + observation.length)
    val rr = Regression.regression(standard, observation, noIntercept = false)
    toCubicPolynomial(rr)
  }

  private[regression] def createQuadratic(
                                           standard: Array[Double],
                                           observed: Array[Double],
                                           band: Int
                                         ): CubicPolynomial = {
    validateStandardAndObserved(standard, observed)
    val regressionResult = createQuadratic(standard, observed)
    val intercept = regressionResult.beta(0)
    band match {
      case 0 => CubicPolynomial(
        intercept = intercept,
        a = regressionResult.beta(1),
        aa = regressionResult.beta(2),
        regressionResult = Some(regressionResult)
      )
      case 1 => CubicPolynomial(
          intercept = intercept,
          b = regressionResult.beta(1),
          bb = regressionResult.beta(2),
          regressionResult = Some(regressionResult)
        )
      case 2 => CubicPolynomial(
          intercept = intercept,
          c = regressionResult.beta(1),
          cc = regressionResult.beta(2),
          regressionResult = Some(regressionResult)
        )
      case _ =>
        throw new IllegalArgumentException("Unknown band '" + band + "'")
    }
  }

  private[regression] def createQuadratic(standard: Array[Double], observed: Array[Double]): Regression.Result = {
    validateStandardAndObserved(standard, observed)
    require(observed.length >= 3, "Quadratic fit needs at least 3 observations, got " + observed.length)
    val data = observed.map { o =>
      Array(o, o * o).toIndexedSeq
    }.toIndexedSeq

    Regression.regression(standard, data, noIntercept = false)
  }

  private[regression] def createQuadraticXBand(standard: Array[Double],
                                               observation: IndexedSeq[IndexedSeq[Double]]
                                              ): CubicPolynomial = {
    validateStandardAndObserved2(standard, observation)
    require(
      observation.length >= 10,
      "Quadratic cross-band fit needs at least 10 observations, got " + observation.length
    )

    val data =
      observation.map { o =>
        assert(o.length == 3)
        val a = o(0)
        val b = o(1)
        val c = o(2)
        Array(
          a,
          b,
          c,
          a * a,
          a * b,
          a * c,
          b * b,
          b * c,
          c * c
        ).toIndexedSeq
      }

    val rr = Regression.regression(standard, data, noIntercept = false)
    toCubicPolynomial(rr)
  }

  private[regression] def createCubic(standard: Array[Double], observed: Array[Double], band: Int): CubicPolynomial = {
    validateStandardAndObserved(standard, observed)
    val regressionResult = createCubic(standard, observed.toIndexedSeq)
    val intercept = regressionResult.beta(0)
    band match {
      case 0 => CubicPolynomial(
        intercept = intercept,
        a = regressionResult.beta(1),
        aa = regressionResult.beta(2),
        aaa = regressionResult.beta(3),
        regressionResult = Some(regressionResult)
      )
      case 1 => CubicPolynomial(
        intercept = intercept,
        b = regressionResult.beta(1),
        bb = regressionResult.beta(2),
        bbb = regressionResult.beta(3),
        regressionResult = Some(regressionResult)
      )
      case 2 => CubicPolynomial(
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

  private[regression] def createCubic(standard: Array[Double], observed: IndexedSeq[Double]): Regression.Result = {
    validateStandardAndObserved(standard, observed)
    require(observed.length >= 4, "Cubic fit needs at least 4 observations, got " + observed.length)

    val data =
      observed.map { o =>
        IndexedSeq(
          o,
          o * o,
          o * o * o
        )
      }

    Regression.regression(standard, data, noIntercept = false)
  }

  private[regression] def createCubicXBand(standard: Array[Double],
                                           observed: IndexedSeq[IndexedSeq[Double]]): CubicPolynomial = {
    validateStandardAndObserved2(standard, observed)
    require(observed.length >= 14, "Cubic cross-band fit needs at least 14 observations, got " + observed.length)

    val data =
      observed.map { o =>
        assert(o.length == 3)
        val a = o(0)
        val b = o(1)
        val c = o(2)
        Array(
          a,
          b,
          c,
          a * a,
          a * b,
          a * c,
          b * b,
          b * c,
          c * c,
          a * a * a,
          a * b * c,
          b * b * b,
          c * c * c,
        ).toIndexedSeq
      }

    val rr = Regression.regression(standard, data, noIntercept = false)
    toCubicPolynomial(rr)
  }

  private[regression] def toCubicPolynomial(rr: Regression.Result): CubicPolynomial = {
    require(rr != null)
    require(rr.beta != null)
    require(rr.beta.length <= 14, "Size of beta must be less or equal 14, got" + rr.beta.length)

    val alpha14 = new Array[Double](14)
    rr.beta.copyToArray(alpha14)

    new CubicPolynomial(alpha14, Option(rr))
  }

  private[regression] def validateStandardAndObserved(standard: Array[Double], observed: Array[Double]): Unit = {
    require(observed != null)
    require(standard != null)
    require(
      observed.length == standard.length,
      "Number of Standard and Observed values are not equal reference.length='" + standard.length + "' observed.length='" + observed.length + "'"
    )
  }

  private[regression] def validateStandardAndObserved(standard: Array[Double], observed: IndexedSeq[Double]): Unit = {
    require(observed != null)
    require(standard != null)
    require(
      observed.length == standard.length,
      "Number of Standard and Observed values are not equal reference.length='" + standard.length + "' observed.length='" + observed.length + "'"
    )
  }


  private[regression] def validateStandardAndObserved2(standard: Array[Double],
                                                       observation: IndexedSeq[IndexedSeq[Double]]): Unit = {
    require(standard != null)
    require(observation != null)
    require(observation.length == standard.length)
  }

  private[regression] def validateStandardAndObserved(standard: IndexedSeq[IndexedSeq[Double]],
                                                      observed: IndexedSeq[IndexedSeq[Double]]): Unit = {
    require(observed != null)
    require(standard != null)
    require(
      observed.length == standard.length,
      "Number of Standard and Observed values are not equal reference.length='" + standard.length + "' observed.length='" + observed.length + "'"
    )

    standard.foreach(s =>
      require(
        s.length == 3,
        "Number of Standard Color values should be equal to 3 (RED,GREEN and BLUE), got " + s.length
      )
    )
    observed.foreach(o =>
      require(
        o.length == 3,
        "Number of Observed Color values should be equal to 3 (RED,GREEN and BLUE), got " + o.length
      )
    )
  }
}
