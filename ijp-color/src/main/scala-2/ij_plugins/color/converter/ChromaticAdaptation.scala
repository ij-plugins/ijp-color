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

package ij_plugins.color.converter

import enumeratum.{Enum, EnumEntry}

import scala.collection.immutable

/**
 * Represents chromatic adaptation when converting between color spaces using difference reference whites.
 *
 * @param entryName
 *   adaptation name
 * @param ma
 *   adaptation matrix
 */
sealed abstract class ChromaticAdaptation(override val entryName: String, val ma: Matrix3x3) extends EnumEntry {

  /** Inverse of the adaptation matrix */
  val maI: Matrix3x3 = ma.inverse

  def name: String              = entryName
  override def toString: String = entryName
}

/**
 * Concrete coefficients for chromatic adaptation transforms.
 *
 * See details on [[http://www.brucelindbloom.com/index.html?Eqn_ChromAdapt.html Chromatic Adaptation]] page.
 */
case object ChromaticAdaptation extends Enum[ChromaticAdaptation] {

  /** Bradford */
  case object Bradford
      extends ChromaticAdaptation(
        "Bradford",
        new Matrix3x3(
          m00 = 0.8951,
          m01 = -0.7502,
          m02 = 0.0389,
          m10 = 0.2664,
          m11 = 1.7135,
          m12 = -0.0685,
          m20 = -0.1614,
          m21 = 0.0367,
          m22 = 1.0296
        )
      )

  /** von Kries */
  case object VonKries
      extends ChromaticAdaptation(
        "von Kries",
        new Matrix3x3(
          m00 = 0.40024,
          m01 = -0.22630,
          m02 = 0.00000,
          m10 = 0.70760,
          m11 = 1.16532,
          m12 = 0.00000,
          m20 = -0.08081,
          m21 = 0.04570,
          m22 = 0.91822
        )
      )

  /** XYZ Scaling */
  case object XYZScaling
      extends ChromaticAdaptation(
        "XYZ Scaling",
        new Matrix3x3(
          m00 = 1,
          m01 = 0,
          m02 = 0,
          m10 = 0,
          m11 = 1,
          m12 = 0,
          m20 = 0,
          m21 = 0,
          m22 = 1
        )
      )

  /** List of all predefined chromatic adaptations. */
  val values: immutable.IndexedSeq[ChromaticAdaptation] = findValues
}
