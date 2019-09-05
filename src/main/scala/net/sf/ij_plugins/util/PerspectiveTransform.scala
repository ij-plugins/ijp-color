/*
 * Image/J Plugins
 * Copyright (C) 2002-2019 Jarek Sacha
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

package net.sf.ij_plugins.util

import java.awt.geom.{AffineTransform, NoninvertibleTransformException, Point2D}

/** Factory methods for creating projective transforms. */
object PerspectiveTransform {

  /** Create Identity transform. */
  def identity() = new PerspectiveTransform()

  /** Create translation transformation.
    *
    * The matrix representing this transform is:
    * {{{
    * [   1    0    tx  ]
    * [   0    1    ty  ]
    * [   0    0    1   ]
    * }}}
    * @param tx The distance by which coordinates are translated in the X axis direction
    * @param ty The distance by which coordinates are translated in the Y axis direction
    */
  def translation(tx: Double, ty: Double) = new PerspectiveTransform(
    m00 = 1.0, m01 = 0.0, m02 = tx,
    m10 = 0.0, m11 = 1.0, m12 = ty,
    m20 = 0.0, m21 = 0.0, m22 = 1.0
  )

  /** Create rotation transformation.
    *
    * The matrix representing this transform is:
    * {{{
    * [   cos(theta)    -sin(theta)    0   ]
    * [   sin(theta)     cos(theta)    0   ]
    * [       0              0         1   ]
    * }}}
    * Rotating with a positive angle theta rotates points on the positive X axis toward the positive Y axis.
    * @param theta The angle of rotation in radians.
    */
  def rotation(theta: Double): PerspectiveTransform = {
    val m00 = math.cos(theta)
    val m01 = -math.sin(theta)
    new PerspectiveTransform(
      m00 = m00, m01 = m01, m02 = 0.0,
      m10 = -m01, m11 = m00, m12 = 0.0,
      m20 = 0.0, m21 = 0.0, m22 = 1.0
    )
  }

  /** Create rotation transformation about a specified point (x, y).
    *
    * Rotating with a positive angle theta rotates points on the positive X axis toward the positive Y axis.
    *
    * @param theta The angle of rotation in radians.
    * @param x The X coordinate of the origin of the rotation
    * @param y The Y coordinate of the origin of the rotation
    */
  def rotation(theta: Double, x: Double, y: Double): PerspectiveTransform = {
    val cos = math.cos(theta)
    val sin = math.sin(theta)
    val oneMinusCos = 1.0 - cos
    val m02 = x * oneMinusCos + y * sin
    val m12 = y * oneMinusCos - x * sin
    new PerspectiveTransform(
      m00 = cos, m01 = -sin, m02 = m02,
      m10 = sin, m11 = cos, m12 = m12,
      m20 = 0.0, m21 = 0.0, m22 = 1.0
    )
  }

  /** Scale transformation with scale factors sx and sy.
    *
    * The matrix representing this transform becomes:
    * {{{
    * [   sx   0    0   ]
    * [   0    sy   0   ]
    * [   0    0    1   ]
    * }}}
    *
    * @param sx The X axis scale factor.
    * @param sy The Y axis scale factor.
    */
  def scaling(sx: Double, sy: Double) = new PerspectiveTransform(
    m00 = sx, m01 = 0.0, m02 = 0.0,
    m10 = 0.0, m11 = sy, m12 = 0.0,
    m20 = 0.0, m21 = 0.0, m22 = 1.0
  )

  /** Sets this transform to a shearing transformation with shear factors sx and sy.
    *
    * The matrix representing this transform becomes:
    * {{{
    * [   1  shx    0   ]
    * [ shy    1    0   ]
    * [   0    0    1   ]
    * }}}
    *
    * @param shx The factor by which coordinates are shifted towards the positive X axis direction according to their Y
    *            coordinate.
    * @param shy The factor by which coordinates are shifted towards the positive Y axis direction according to their X
    *            coordinate.
    */
  def shearing(shx: Double, shy: Double) = new PerspectiveTransform(
    m00 = 1.0, m01 = shx, m02 = 0.0,
    m10 = shy, m11 = 1.0, m12 = 0.0,
    m20 = 0.0, m21 = 0.0, m22 = 1.0
  )


  /** Creates a PerspectiveTransform that maps the unit square onto an arbitrary quadrilateral.
    *
    * {{{
    * (0, 0) -> (x0, y0)
    * (1, 0) -> (x1, y1)
    * (1, 1) -> (x2, y2)
    * (0, 1) -> (x3, y3)
    * }}}
    */
  final def squareToQuad(x0: Double, y0: Double,
                         x1: Double, y1: Double,
                         x2: Double, y2: Double,
                         x3: Double, y3: Double): PerspectiveTransform = {
    val dx3 = x0 - x1 + x2 - x3
    val dy3 = y0 - y1 + y2 - y3
    val m22 = 1.0F
    if ((dx3 == 0.0F) && (dy3 == 0.0F)) {
      val v00 = x1 - x0
      val m01 = x2 - x1
      val m02 = x0
      val m10 = y1 - y0
      val m11 = y2 - y1
      val m12 = y0
      val m20 = 0.0F
      val m21 = 0.0F
      new PerspectiveTransform(
        m00 = v00, m01 = m01, m02 = m02,
        m10 = m10, m11 = m11, m12 = m12,
        m20 = m20, m21 = m21, m22 = m22
      )
    }
    else {
      val dx1 = x1 - x2
      val dy1 = y1 - y2
      val dx2 = x3 - x2
      val dy2 = y3 - y2
      val d = 1d / (dx1 * dy2 - dx2 * dy1)
      val m20 = (dx3 * dy2 - dx2 * dy3) * d
      val m21 = (dx1 * dy3 - dx3 * dy1) * d
      val m00 = x1 - x0 + m20 * x1
      val m01 = x3 - x0 + m21 * x3
      val m02 = x0
      val m10 = y1 - y0 + m20 * y1
      val m11 = y3 - y0 + m21 * y3
      val m12 = y0
      new PerspectiveTransform(
        m00 = m00, m01 = m01, m02 = m02,
        m10 = m10, m11 = m11, m12 = m12,
        m20 = m20, m21 = m21, m22 = m22
      )
    }
  }

  /** Creates a PerspectiveTransform that maps an arbitrary quadrilateral onto the unit square.
    *
    * {{{
    * (x0, y0) -> (0, 0)
    * (x1, y1) -> (1, 0)
    * (x2, y2) -> (1, 1)
    * (x3, y3) -> (0, 1)
    * }}}
    */
  def quadToSquare(x0: Double, y0: Double,
                   x1: Double, y1: Double,
                   x2: Double, y2: Double,
                   x3: Double, y3: Double): PerspectiveTransform = {
    squareToQuad(x0, y0, x1, y1, x2, y2, x3, y3).makeAdjoint()
  }

  /** Creates a PerspectiveTransform that maps an arbitrary quadrilateral onto another arbitrary quadrilateral.
    *
    * {{{
    * (x0, y0) -> (x0p, y0p)
    * (x1, y1) -> (x1p, y1p)
    * (x2, y2) -> (x2p, y2p)
    * (x3, y3) -> (x3p, y3p)
    * }}}
    */
  def quadToQuad(x0: Double, y0: Double,
                 x1: Double, y1: Double,
                 x2: Double, y2: Double,
                 x3: Double, y3: Double,
                 x0p: Double, y0p: Double,
                 x1p: Double, y1p: Double,
                 x2p: Double, y2p: Double,
                 x3p: Double, y3p: Double): PerspectiveTransform = {
    val tx1 = quadToSquare(x0, y0, x1, y1, x2, y2, x3, y3)
    val tx2 = squareToQuad(x0p, y0p, x1p, y1p, x2p, y2p, x3p, y3p)
    tx1.concatenate(tx2)
  }

  /** Creates a PerspectiveTransform that maps an arbitrary quadrilateral 1 onto another arbitrary quadrilateral 2.
    *
    * {{{
    * (x0, y0) -> (x0p, y0p)
    * (x1, y1) -> (x1p, y1p)
    * (x2, y2) -> (x2p, y2p)
    * (x3, y3) -> (x3p, y3p)
    * }}}
    */
  def quadToQuad(points1: Array[scalafx.geometry.Point2D],
                 points2: Array[scalafx.geometry.Point2D]): PerspectiveTransform = {
    quadToQuad(
      points1(0).x, points1(0).y,
      points1(1).x, points1(1).y,
      points1(2).x, points1(2).y,
      points1(3).x, points1(3).y,
      points2(0).x, points2(0).y,
      points2(1).x, points2(1).y,
      points2(2).x, points2(2).y,
      points2(3).x, points2(3).y
    )
  }


  private final val PERSPECTIVE_DIVIDE_EPSILON: Double = 1.0e-10
}


/** A 2D perspective (or projective) transform.
  *
  * A perspective transformation is capable of mapping an arbitrary quadrilateral into another arbitrary quadrilateral,
  * while preserving the straightness of lines.  Unlike an affine transformation, the parallelism of lines in the
  * source is not necessarily preserved in the output.
  *
  * Such a coordinate transformation can be represented by a 3x3 matrix which transforms homogeneous source coordinates
  * `(x, y, 1)` into destination coordinates `(x', y', w)`.
  * To convert back into non-homogeneous coordinates (X, Y), `x'` and `y'` are divided by `w`.
  *
  * {{{
  * [ x']   [  m00  m01  m02  ] [ x ]   [ m00x + m01y + m02 ]
  * [ y'] = [  m10  m11  m12  ] [ y ] = [ m10x + m11y + m12 ]
  * [ w ]   [  m20  m21  m22  ] [ 1 ]   [ m20x + m21y + m22 ]
  *
  * x' = (m00x + m01y + m02)
  * y' = (m10x + m11y + m12)
  *
  * w  = (m20x + m21y + m22)
  *
  * X = x' / w
  * Y = y' / w
  * }}}
  *
  * This implementation was inspired by Java Advanced Imaging.
  */
final class PerspectiveTransform(val m00: Double = 1, val m01: Double = 0, val m02: Double = 0,
                                 val m10: Double = 0, val m11: Double = 1, val m12: Double = 0,
                                 val m20: Double = 0, val m21: Double = 0, val m22: Double = 1) {

  import PerspectiveTransform._

  /** Constructs a new PerspectiveTransform from a two-dimensional array of doubles.
    * @throws NullPointerException if matrix is null
    * @throws ArrayIndexOutOfBoundsException if matrix is too small
    */
  def this(matrix: Array[Array[Double]]) {
    this(
      matrix(0)(0), matrix(0)(1), matrix(0)(2),
      matrix(1)(0), matrix(1)(1), matrix(1)(2),
      matrix(2)(0), matrix(2)(1), matrix(2)(2)
    )
  }

  /** Constructs a new PerspectiveTransform with the same effect as an existing AffineTransform.
    * @throws NullPointerException if transform is null
    */
  def this(transform: AffineTransform) {
    this(
      transform.getScaleX, transform.getShearX, transform.getTranslateX,
      transform.getShearY, transform.getScaleY, transform.getTranslateY,
      0.0, 0.0, 1.0
    )
  }

  /** Sets this transform to a given PerspectiveTransform.
    * @throws NullPointerException if tx is null
    */
  def this(tx: PerspectiveTransform) {
    this(
      tx.m00, tx.m01, tx.m02,
      tx.m10, tx.m11, tx.m12,
      tx.m20, tx.m21, tx.m22
    )
  }

  /** Creates new transform with the matrix replaced with its adjoint. */
  private def makeAdjoint(): PerspectiveTransform = {

    val m00p = m11 * m22 - m12 * m21
    val m01p = m12 * m20 - m10 * m22
    val m02p = m10 * m21 - m11 * m20
    val m10p = m02 * m21 - m01 * m22
    val m11p = m00 * m22 - m02 * m20
    val m12p = m01 * m20 - m00 * m21
    val m20p = m01 * m12 - m02 * m11
    val m21p = m02 * m10 - m00 * m12
    val m22p = m00 * m11 - m01 * m10
    new PerspectiveTransform(
      m00 = m00p, m01 = m10p, m02 = m20p,
      m10 = m01p, m11 = m11p, m12 = m21p,
      m20 = m02p, m21 = m12p, m22 = m22p
    )
  }

  /** Creates new transform that scales the matrix elements so m22 is equal to 1.0. m22 must not be equal to 0. */
  private def normalize(): PerspectiveTransform = {
    val scale = 1.0 / m22
    new PerspectiveTransform(
      m00 = m00 * scale, m01 = m01 * scale, m02 = m02 * scale,
      m10 = m10 * scale, m11 = m11 * scale, m12 = m12 * scale,
      m20 = m20 * scale, m21 = m21 * scale, m22 = 1.0
    )
  }

  /** Returns the determinant of the matrix representation of the transform. */
  def getDeterminant: Double = {
    (m00 * ((m11 * m22) - (m12 * m21))) - (m01 * ((m10 * m22) - (m12 * m20))) + (m02 * ((m10 * m21) - (m11 * m20)))
  }

  /** Retrieves the 9 specifiable values in the 3x3 affine transformation matrix into a 2-dimensional array of double
    * precision values.
    *
    * The values are stored into the 2-dimensional array using the row index as the first subscript and the column
    * index as the second.
    *
    * @param matrix The 2-dimensional double array to store the
    *               returned values.  The array is assumed to be at least 3x3.
    * @throws ArrayIndexOutOfBoundsException if matrix is too small
    * @throws IllegalArgumentException if matrix is null
    */
  def toMatrix(matrix: Array[Array[Double]] = Array.ofDim[Double](3, 3)): Array[Array[Double]] = {
    require(matrix != null, "The input argument 'matrix' may not be null.")
    matrix(0)(0) = m00
    matrix(0)(1) = m01
    matrix(0)(2) = m02
    matrix(1)(0) = m10
    matrix(1)(1) = m11
    matrix(1)(2) = m12
    matrix(2)(0) = m20
    matrix(2)(1) = m21
    matrix(2)(2) = m22
    matrix
  }

  /** Creates new transform that concatenates this transform with a translation transformation.
    *
    * This is equivalent to calling concatenate(T), where T is an
    * PerspectiveTransform represented by the following matrix:
    * {{{
    * [   1    0    tx  ]
    * [   0    1    ty  ]
    * [   0    0    1   ]
    * }}}
    */
  def translate(tx: Double, ty: Double): PerspectiveTransform = concatenate(translation(tx, ty))

  /** Creates new transform that concatenates this transform with a rotation transformation.
    *
    * This is equivalent to calling concatenate(R), where R is an
    * PerspectiveTransform represented by the following matrix:
    * {{{
    * [   cos(theta)    -sin(theta)    0   ]
    * [   sin(theta)     cos(theta)    0   ]
    * [       0              0         1   ]
    * }}}
    * Rotating with a positive angle theta rotates points on the positive X axis toward the positive Y axis.
    *
    * @param theta The angle of rotation in radians.
    */
  def rotate(theta: Double): PerspectiveTransform = concatenate(rotation(theta))

  /** Creates new transform that concatenates this transform with a translated rotation transformation.
    *
    * This is equivalent to the following sequence of calls:
    * {{{
    * translate(x, y);
    * rotate(theta);
    * translate(-x, -y);
    * }}}
    * Rotating with a positive angle theta rotates points on the positive X axis toward the positive Y axis.
    *
    * @param theta The angle of rotation in radians.
    * @param x The X coordinate of the origin of the rotation
    * @param y The Y coordinate of the origin of the rotation
    */
  def rotate(theta: Double, x: Double, y: Double): PerspectiveTransform = concatenate(rotation(theta, x, y))

  /** Creates new transform that concatenates this transform with a scaling transformation.
    *
    * This is equivalent to calling concatenate(S), where S is an PerspectiveTransform represented by the following matrix:
    * {{{
    * [   sx   0    0   ]
    * [   0    sy   0   ]
    * [   0    0    1   ]
    * }}}
    *
    * @param sx The X axis scale factor.
    * @param sy The Y axis scale factor.
    */
  def scale(sx: Double, sy: Double): PerspectiveTransform = concatenate(scaling(sx, sy))

  /** Creates new transform that cConcatenates this transform with a shearing transformation.
    *
    * This is equivalent to calling concatenate(SH), where SH is an
    * PerspectiveTransform represented by the following matrix:
    * {{{
    * [   1   shx   0   ]
    * [  shy   1    0   ]
    * [   0    0    1   ]
    * }}}
    *
    * @param shx The factor by which coordinates are shifted towards the positive X axis direction according to their Y
    *            coordinate.
    * @param shy The factor by which coordinates are shifted towards the positive Y axis direction according to their X
    *            coordinate.
    */
  def shear(shx: Double, shy: Double): PerspectiveTransform = concatenate(shearing(shx, shy))


  /** Creates new transform that post-concatenates a given AffineTransform to this transform.
    * @throws IllegalArgumentException if tx is null
    */
  def concatenate(tx: AffineTransform): PerspectiveTransform = {
    require(tx != null, "The input argument 'tx' may not be null.")
    val tx_m00 = tx.getScaleX
    val tx_m01 = tx.getShearX
    val tx_m02 = tx.getTranslateX
    val tx_m10 = tx.getShearY
    val tx_m11 = tx.getScaleY
    val tx_m12 = tx.getTranslateY
    val m00p = m00 * tx_m00 + m10 * tx_m01 + m20 * tx_m02
    val m01p = m01 * tx_m00 + m11 * tx_m01 + m21 * tx_m02
    val m02p = m02 * tx_m00 + m12 * tx_m01 + m22 * tx_m02
    val m10p = m00 * tx_m10 + m10 * tx_m11 + m20 * tx_m12
    val m11p = m01 * tx_m10 + m11 * tx_m11 + m21 * tx_m12
    val m12p = m02 * tx_m10 + m12 * tx_m11 + m22 * tx_m12
    val m20p = m20
    val m21p = m21
    val m22p = m22
    new PerspectiveTransform(
      m00 = m00p, m10 = m10p, m20 = m20p,
      m01 = m01p, m11 = m11p, m21 = m21p,
      m02 = m02p, m12 = m12p, m22 = m22p
    )
  }

  /** Creates new transform that post-concatenates a given PerspectiveTransform to this transform.
    * @throws IllegalArgumentException if tx is null
    */
  def concatenate(tx: PerspectiveTransform): PerspectiveTransform = {
    require(tx != null, "The input argument 'tx' may not be null.")
    val m00p = m00 * tx.m00 + m10 * tx.m01 + m20 * tx.m02
    val m10p = m00 * tx.m10 + m10 * tx.m11 + m20 * tx.m12
    val m20p = m00 * tx.m20 + m10 * tx.m21 + m20 * tx.m22
    val m01p = m01 * tx.m00 + m11 * tx.m01 + m21 * tx.m02
    val m11p = m01 * tx.m10 + m11 * tx.m11 + m21 * tx.m12
    val m21p = m01 * tx.m20 + m11 * tx.m21 + m21 * tx.m22
    val m02p = m02 * tx.m00 + m12 * tx.m01 + m22 * tx.m02
    val m12p = m02 * tx.m10 + m12 * tx.m11 + m22 * tx.m12
    val m22p = m02 * tx.m20 + m12 * tx.m21 + m22 * tx.m22
    new PerspectiveTransform(
      m00 = m00p, m10 = m10p, m20 = m20p,
      m01 = m01p, m11 = m11p, m21 = m21p,
      m02 = m02p, m12 = m12p, m22 = m22p
    )
  }

  /** Creates new transform that pre-concatenates a given AffineTransform to this transform.
    * @throws IllegalArgumentException if tx is null
    */
  def preConcatenate(tx: AffineTransform): PerspectiveTransform = {
    require(tx != null, "The input argument 'tx' may not be null.")
    val tx_m00 = tx.getScaleX
    val tx_m01 = tx.getShearX
    val tx_m02 = tx.getTranslateX
    val tx_m10 = tx.getShearY
    val tx_m11 = tx.getScaleY
    val tx_m12 = tx.getTranslateY
    val m00p = tx_m00 * m00 + tx_m10 * m01
    val m01p = tx_m01 * m00 + tx_m11 * m01
    val m02p = tx_m02 * m00 + tx_m12 * m01 + m02
    val m10p = tx_m00 * m10 + tx_m10 * m11
    val m11p = tx_m01 * m10 + tx_m11 * m11
    val m12p = tx_m02 * m10 + tx_m12 * m11 + m12
    val m20p = tx_m00 * m20 + tx_m10 * m21
    val m21p = tx_m01 * m20 + tx_m11 * m21
    val m22p = tx_m02 * m20 + tx_m12 * m21 + m22
    new PerspectiveTransform(
      m00 = m00p, m10 = m10p, m20 = m20p,
      m01 = m01p, m11 = m11p, m21 = m21p,
      m02 = m02p, m12 = m12p, m22 = m22p
    )
  }

  /** Creates new transform that pre-concatenates a given PerspectiveTransform to this transform.
    * @throws IllegalArgumentException if tx is null
    */
  def preConcatenate(tx: PerspectiveTransform): PerspectiveTransform = {
    require(tx != null, "The input argument 'tx' may not be null.")
    val m00p = tx.m00 * m00 + tx.m10 * m01 + tx.m20 * m02
    val m10p = tx.m00 * m10 + tx.m10 * m11 + tx.m20 * m12
    val m20p = tx.m00 * m20 + tx.m10 * m21 + tx.m20 * m22
    val m01p = tx.m01 * m00 + tx.m11 * m01 + tx.m21 * m02
    val m11p = tx.m01 * m10 + tx.m11 * m11 + tx.m21 * m12
    val m21p = tx.m01 * m20 + tx.m11 * m21 + tx.m21 * m22
    val m02p = tx.m02 * m00 + tx.m12 * m01 + tx.m22 * m02
    val m12p = tx.m02 * m10 + tx.m12 * m11 + tx.m22 * m12
    val m22p = tx.m02 * m20 + tx.m12 * m21 + tx.m22 * m22
    new PerspectiveTransform(
      m00 = m00p, m10 = m10p, m20 = m20p,
      m01 = m01p, m11 = m11p, m21 = m21p,
      m02 = m02p, m12 = m12p, m22 = m22p
    )
  }

  /** Returns a new PerspectiveTransform that is the inverse of the current transform.
    * @throws NoninvertibleTransformException if transform cannot be inverted
    */
  def createInverse: PerspectiveTransform = {
    val tx = makeAdjoint()
    if (math.abs(tx.m22) < PERSPECTIVE_DIVIDE_EPSILON) {
      throw new NoninvertibleTransformException("PerspectiveTransform.createInverse")
    }
    tx.normalize()
    tx
  }

  /** * Returns a new PerspectiveTransform that is the adjoint, of the current transform.
    *
    * The adjoint is defined as the matrix of co-factors, which in turn are the determinants
    * of the sub-matrices defined by removing the row and column
    * of each element from the original matrix in turn.
    *
    * The adjoint is a scalar multiple of the inverse matrix.
    * Because points to be transformed are converted into homogeneous
    * coordinates, where scalar factors are irrelevant, the adjoint
    * may be used in place of the true inverse. Since it is unnecessary
    * to normalize the adjoint, it is both faster to compute and more
    * numerically stable than the true inverse.
    */
  def createAdjoint: PerspectiveTransform = {
    val tx = clone.asInstanceOf[PerspectiveTransform]
    tx.makeAdjoint()
  }

  /** Transforms the specified ptSrc and stores the result in ptDst.
    *
    * If ptDst is null, a new Point2D object will be allocated before
    * storing. In either case, ptDst containing the transformed point
    * is returned for convenience.
    * Note that ptSrc and ptDst can the same. In this case, the input
    * point will be overwritten with the transformed point.
    *
    * @param ptSrc The array containing the source point objects.
    * @param ptDst The array where the transform point objects are returned.
    * @throws IllegalArgumentException if ptSrc is null
    */
  def transform(ptSrc: Point2D, ptDst: Point2D = null): Point2D = {
    require(ptSrc != null, "The input argument 'ptSrc' may not be null.")
    val dest = if (ptDst != null) ptDst
    else if (ptSrc.isInstanceOf[Point2D.Double]) new Point2D.Double else new Point2D.Float
    val x = ptSrc.getX
    val y = ptSrc.getY
    val w = m20 * x + m21 * y + m22
    dest.setLocation((m00 * x + m01 * y + m02) / w, (m10 * x + m11 * y + m12) / w)
    dest
  }

  /** Transforms the specified ptSrc and stores the result in ptDst.
    *
    * If ptDst is null, a new Point2D object will be allocated before
    * storing. In either case, ptDst containing the transformed point
    * is returned for convenience.
    * Note that ptSrc and ptDst can the same. In this case, the input
    * point will be overwritten with the transformed point.
    *
    * @param ptSrc The array containing the source point objects.
    * @throws IllegalArgumentException if ptSrc is null
    */
  def transform(ptSrc: scalafx.geometry.Point2D): scalafx.geometry.Point2D = {
    require(ptSrc != null, "The input argument 'ptSrc' may not be null.")
    val x = ptSrc.getX
    val y = ptSrc.getY
    val w = m20 * x + m21 * y + m22
    new scalafx.geometry.Point2D((m00 * x + m01 * y + m02) / w, (m10 * x + m11 * y + m12) / w)
  }

  /** Transforms the specified ptSrc and stores the result in ptDst.
    *
    * If ptDst is null, a new Point2D object will be allocated before
    * storing. In either case, ptDst containing the transformed point
    * is returned for convenience.
    * Note that ptSrc and ptDst can the same. In this case, the input
    * point will be overwritten with the transformed point.
    *
    * @param points The array containing the source point objects.
    * @throws IllegalArgumentException if ptSrc is null
    */
  def transform(points: Seq[scalafx.geometry.Point2D]): Seq[scalafx.geometry.Point2D] = {
    require(points != null, "The input argument 'points' may not be null.")
    points.map(transform)
  }


  /** Inverse transforms the specified ptSrc and stores the result in ptDst.
    * If ptDst is null, a new Point2D object will be allocated before
    * storing. In either case, ptDst containing the transformed point
    * is returned for convenience.
    * Note that ptSrc and ptDst can the same. In this case, the input
    * point will be overwritten with the transformed point.
    * @param ptSrc The point to be inverse transformed.
    * @param ptDst The resulting transformed point.
    * @throws NoninvertibleTransformException  if the matrix cannot be
    *                                          inverted.
    * @throws IllegalArgumentException if ptSrc is null
    */
  def inverseTransform(ptSrc: Point2D, ptDst: Point2D = null): Point2D = {
    require(ptSrc != null, "The input argument 'ptSrc' may not be null.")
    val dst = if (ptDst != null) ptDst else if (ptSrc.isInstanceOf[Point2D.Double]) new Point2D.Double else new Point2D.Float
    val x = ptSrc.getX
    val y = ptSrc.getY
    val tmp_x = (m11 * m22 - m12 * m21) * x + (m02 * m21 - m01 * m22) * y + (m01 * m12 - m02 * m11)
    val tmp_y = (m12 * m20 - m10 * m22) * x + (m00 * m22 - m02 * m20) * y + (m02 * m10 - m00 * m12)
    val w = (m10 * m21 - m11 * m20) * x + (m01 * m20 - m00 * m21) * y + (m00 * m11 - m01 * m10)
    var wabs = w
    if (w < 0) {
      wabs = -w
    }
    if (wabs < PERSPECTIVE_DIVIDE_EPSILON) {
      throw new NoninvertibleTransformException("Divide by zero error.")
    }
    dst.setLocation(tmp_x / w, tmp_y / w)
    dst
  }

  override def toString: String = {
    "Perspective transform matrix\n" +
      m00 + "\t" + m01 + "\t" + m02 + "\n" +
      m10 + "\t" + m11 + "\t" + m12 + "\n" +
      m20 + "\t" + m21 + "\t" + m22 + "\n"
  }

  /** Returns `true` if this is an identity transform, `false` otherwise. */
  def isIdentity: Boolean = {
    m01 == 0.0 && m02 == 0.0 && m10 == 0.0 && m12 == 0.0 && m20 == 0.0 && m21 == 0.0 && m22 != 0.0 &&
      m00 / m22 == 1.0 && m11 / m22 == 1.0
  }

  override def equals(obj: Any): Boolean = {
    if (!obj.isInstanceOf[PerspectiveTransform]) {
      return false
    }
    val a = obj.asInstanceOf[PerspectiveTransform]

    (m00 == a.m00) && (m10 == a.m10) && (m20 == a.m20) &&
      (m01 == a.m01) && (m11 == a.m11) && (m21 == a.m21) &&
      (m02 == a.m02) && (m12 == a.m12) && (m22 == a.m22)
  }

  override def hashCode(): Int = (m00 + m01 + m02 + m10 + m11 + m12 + m20 + m21 + m22).hashCode()
}
