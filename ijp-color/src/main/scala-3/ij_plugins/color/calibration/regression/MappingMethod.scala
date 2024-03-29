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

import ij_plugins.color.util.EnumCompanion.{WithName, WithNameCompanion}

/**
 * Polynomial function mapping methods used by `MappingFactory`.
 */
enum MappingMethod(val name: String) extends WithName:

  /** First order polynomial with no intercept term, within single color band - single input. */
  case LinearNoIntercept extends MappingMethod("Linear No-intercept")

  /** First order polynomial with no intercept term, within with three input for each color band. */
  case LinearNoInterceptCrossBand extends MappingMethod("Linear No-intercept Cross-band")

  /** First order polynomial, within single color band - single input. */
  case Linear extends MappingMethod("Linear")

  /** First order polynomial within with three input for each color band. */
  case LinearCrossBand extends MappingMethod("Linear Cross-band")

  /** Second order polynomial, within single color band - single input. */
  case Quadratic extends MappingMethod("Quadratic")

  /** Second order polynomial within with three input for each color band. */
  case QuadraticCrossBand extends MappingMethod("Quadratic Cross-band")

  /** Third order polynomial, within single color band - single input. */
  case Cubic extends MappingMethod("Cubic")

  /** Third order polynomial within with three input for each color band. */
  case CubicCrossBand extends MappingMethod("Cubic Cross-band")

object MappingMethod extends WithNameCompanion[MappingMethod]
