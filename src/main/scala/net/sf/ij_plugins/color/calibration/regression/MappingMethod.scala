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

/** Enumeration of polynomial function mapping methods used by `MappingFactory`. */
object MappingMethod extends Enumeration {
  type MappingMethod = Value

  /** First order polynomial, within single color band - single input. */
  val Linear             = Value("Linear")
  /** First order polynomial within with three input for each color band. */
  val LinearCrossBand    = Value("Linear Cross-band")
  /** Second order polynomial, within single color band - single input. */
  val Quadratic          = Value("Quadratic")
  /** Second order polynomial within with three input for each color band. */
  val QuadraticCrossBand = Value("Quadratic Cross-band")
  /** Third order polynomial, within single color band - single input. */
  val Cubic              = Value("Cubic")
  /** Third order polynomial within with three input for each color band. */
  val CubicCrossBand     = Value("Cubic Cross-band")
}
