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

import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression

/** Helper methods for computing linear regression. */
object Regression {

  /** Result returned by regression methods. */
  case class Result(
    var beta: Array[Double],
    var rSquared: Double,
    var adjustedRSquared: Double,
    var regressandVariance: Double,
    var regressionStandardError: Double
  )

  /**
   * Compute linear fit coefficient
   * {{{
   *   s = A*o.
   * }}}
   *
   * @param standard    array of expected output values.
   * @param observation array of input values
   * @return linear fit coefficients
   * @see #regression(double[], double[][], boolean)
   */
  def regression(standard: Array[Double], observation: Array[Array[Double]]): Regression.Result = {
    regression(standard, observation, noIntercept = true)
  }

  /**
   * Compute linear fit coefficient `s = A*o` if `noIntercept` is true or `s = A*o + b` if `noIntercept` is `false`.
   *
   * @param standard    array of expected output values.
   * @param observation array of input values
   * @param noIntercept true means the model is to be estimated without an intercept term
   * @return linear fit coefficients
   */
  def regression(
    standard: Array[Double],
    observation: Array[Array[Double]],
    noIntercept: Boolean
  ): Regression.Result = {
    require(standard != null, "Argument `standard` cannot be null.")
    require(observation != null, "Argument `observation` cannot be null.")
    require(observation.length == standard.length)
    require(
      observation.length == standard.length,
      s"observation.length=${observation.length} must equal standard.length=${standard.length}."
    )
    require(
      observation.length > observation(0).length,
      s"observation.length=${observation.length} must be greater than observation(0).length={observation(0).length}."
    )

    val regression = new OLSMultipleLinearRegression()
    regression.setNoIntercept(noIntercept)
    regression.newSampleData(standard, observation)
    Regression.Result(
      beta = regression.estimateRegressionParameters,
      rSquared = regression.calculateRSquared,
      adjustedRSquared = regression.calculateAdjustedRSquared,
      regressandVariance = regression.estimateRegressandVariance,
      regressionStandardError = regression.estimateRegressionStandardError
    )
  }

  /**
   * Compute linear fit coefficients that map observations to a reference: `s = A*[o, 1]`.
   *
   * @param standard    reference values.
   * @param observation observed values.
   * @return linear fit coefficients.
   */
  def createLinear(standard: Array[Double], observation: Array[Array[Double]]): Regression.Result = {
    regression(standard, observation, noIntercept = false)
  }
}
