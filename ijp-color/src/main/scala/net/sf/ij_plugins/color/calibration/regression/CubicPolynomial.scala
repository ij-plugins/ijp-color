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

package net.sf.ij_plugins.color.calibration.regression

/** Cubic polynomial function of 3 variables, with some 3-order cross-terms dropped (aab, aac, abb, acc, bbc, bcc)
  * to reduce requirements on size of data needed to fit the polynomial.
  * {{{
  *   f(A,B,C) = intercept +
  *              a*A + b*B + c*C +
  *              aa*A*A + ab*A*B + ac*A*C + bb*B*B + bc*B*C + cc*C*C +
  *              aaa*A*A*A + bbb*B*B*B + ccc*C*C*C + abc*A*B*C
  * }}}
  */
case class CubicPolynomial(intercept: Double = .0,
                           a: Double = .0,
                           b: Double = .0,
                           c: Double = .0,
                           aa: Double = .0,
                           ab: Double = .0,
                           ac: Double = .0,
                           bb: Double = .0,
                           bc: Double = .0,
                           cc: Double = .0,
                           aaa: Double = .0,
                           abc: Double = .0,
                           bbb: Double = .0,
                           ccc: Double = .0,
                           regressionResult: Option[Regression.Result] = None) {

  /** Initialize coefficients from an `Array(intercept, a, b, c, aa, ab, ac, bb, bc, cc, aaa, abc, bbb, ccc)`
    *
    * @param coeff coefficient array.
    */
  def this(coeff: Array[Double], regressionResult: Option[Regression.Result] = None) {
    this(coeff(0), coeff(1), coeff(2), coeff(3), coeff(4), coeff(5), coeff(6),
      coeff(7), coeff(8), coeff(9), coeff(10), coeff(11), coeff(12), coeff(13), regressionResult
    )
  }

  /** Evaluate polynomial for the given argument.
    *
    * @param v array of length 3.
    */
  def evaluate(v: Array[Double]): Double = {
    assert(v.length == 3)
    val _a = v(0)
    val _b = v(1)
    val _c = v(2)
    val _aa = _a * _a
    val _bb = _b * _b
    val _cc = _c * _c
    intercept +
      a * _a + b * _b + c * _c +
      aa * _aa + ab * _a * _b + ac * _a * _c + bb * _bb + bc * _b * _c + cc * _cc +
      aaa * _aa * _a + abc * _a * _b * _c + bbb * _bb * _b + ccc * _cc * _c
  }

  /** Convert coefficient to an array, the order is the same as in the constructor. */
  def toArray: Array[Double] = {
    Array[Double](intercept, a, b, c, aa, ab, ac, bb, bc, cc, aaa, abc, bbb, ccc)
  }

  /** Map of the coefficient where values are coefficient names, same as in the constructor. */
  def toMap: scala.collection.mutable.LinkedHashMap[String, Double] =
    scala.collection.mutable.LinkedHashMap[String, Double](
      ("intercept" -> intercept),
      ("a" -> a),
      ("b" -> b),
      ("c" -> c),
      ("aa" -> aa),
      ("ab" -> ab),
      ("ac" -> ac),
      ("bb" -> bb),
      ("bc" -> bc),
      ("cc" -> cc),
      ("aaa" -> aaa),
      ("abc" -> abc),
      ("bbb" -> bbb),
      ("ccc" -> ccc)
    )
}
