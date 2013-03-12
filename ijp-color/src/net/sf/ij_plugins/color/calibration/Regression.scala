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

import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression

/** Helper methods for computing linear regression. */
object Regression {

  /** Result returned by regression methods. */
  case class Result(var beta: Array[Double],
                    var rSquared: Double,
                    var adjustedRSquared: Double,
                    var regressandVariance: Double,
                    var regressionStandardError: Double)

  /** Compute linear fit coefficient
    * {{{
    *   s = A*o.
    * }}}
    *
    * @param standard    array of expected output values.
    * @param observation array of input values
    * @return linear fit coefficients
    * @throws ColorException if problem is ill defined
    * @see #regression(double[], double[][], boolean)
    */
  def regression(standard: Array[Double], observation: Array[Array[Double]]): Regression.Result = {
    regression(standard, observation, noIntercept = true)
  }

  /** Compute linear fit coefficient `s = A*o` if `noIntercept` is true or `s = A*o + b` if `noIntercept` is `false`.
    *
    * @param standard    array of expected output values.
    * @param observation array of input values
    * @param noIntercept true means the model is to be estimated without an intercept term
    * @return linear fit coefficients
    */
  def regression(standard: Array[Double], observation: Array[Array[Double]], noIntercept: Boolean
                    ): Regression.Result = {
    require(observation != null)
    require(standard != null)
    require(observation.length == standard.length)

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
   * @throws ColorMatchException if regression fails.
   */
  def createLinear(standard: Array[Double], observation: Array[Array[Double]]): Regression.Result = {
    require(observation != null)
    require(standard != null)
    require(observation.length == standard.length)
    require(observation.length >= observation(0).length + 1)

    regression(standard, observation, noIntercept = false)
  }
}