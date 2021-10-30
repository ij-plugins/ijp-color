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

package ij_plugins.color

import ij_plugins.color.DeltaE._
import ij_plugins.color.converter.ColorTriple
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._

class DeltaESpec extends AnyFlatSpec {
  private final val LAB_1                 = Array(59.8, 13.0, 19.4)
  private final val LAB_2                 = Array(63.4, 13.2, 20.7)
  private final val DELTA_E_1976          = 3.832754d
  private final val DELTA_E_1994_TEXT_1_2 = 1.930490d
  private final val DELTA_E_1994_TEXT_2_1 = 1.925103d
  private final val DELTA_E_1994_ARTS_1_2 = 3.669222d
  private final val DELTA_E_1994_ARTS_2_1 = 3.666295d
  private final val DELTA_E_2000_111      = 3.182956d
  private final val DELTA_E_CMC_11_1_2    = 3.202334d
  private final val DELTA_E_CMC_11_2_1    = 3.110326d
  private final val DELTA_E_CMC_21_1_2    = 1.847990d
  private final val DELTA_E_CMC_21_2_1    = 1.790792d
  private final val TOLERANCE             = 0.000001d

  "E76" should " be equal Lindbloom" in {
    e76(LAB_1, LAB_2) should be(DELTA_E_1976 +- TOLERANCE)
    e76(LAB_2, LAB_1) should be(DELTA_E_1976 +- TOLERANCE)

    e76(ColorTriple.Lab(LAB_1), ColorTriple.Lab(LAB_2)) should be(DELTA_E_1976 +- TOLERANCE)
    e76(ColorTriple.Lab(LAB_2), ColorTriple.Lab(LAB_1)) should be(DELTA_E_1976 +- TOLERANCE)
  }

  "E94" should " be equal Lindbloom" in {
    e94GraphicArts(LAB_1, LAB_2) should be(DELTA_E_1994_ARTS_1_2 +- TOLERANCE)
    e94GraphicArts(LAB_2, LAB_1) should be(DELTA_E_1994_ARTS_2_1 +- TOLERANCE)
    e94GraphicArts(LAB_1, LAB_2) should be(DELTA_E_1994_ARTS_1_2 +- TOLERANCE)
    e94GraphicArts(LAB_2, LAB_1) should be(DELTA_E_1994_ARTS_2_1 +- TOLERANCE)

    e94Textiles(LAB_1, LAB_2) should be(DELTA_E_1994_TEXT_1_2 +- TOLERANCE)
    e94Textiles(LAB_2, LAB_1) should be(DELTA_E_1994_TEXT_2_1 +- TOLERANCE)
  }

  "cmc" should " be equal Lindbloom" in {
    cmc(LAB_1, LAB_2, 1, 1) should be(DELTA_E_CMC_11_1_2 +- TOLERANCE)
    cmc(LAB_2, LAB_1, 1, 1) should be(DELTA_E_CMC_11_2_1 +- TOLERANCE)

    cmcPerceptibility(LAB_1, LAB_2) should be(DELTA_E_CMC_11_1_2 +- TOLERANCE)
    cmcPerceptibility(LAB_2, LAB_1) should be(DELTA_E_CMC_11_2_1 +- TOLERANCE)

    cmc(LAB_1, LAB_2, 2, 1) should be(DELTA_E_CMC_21_1_2 +- TOLERANCE)
    cmc(LAB_2, LAB_1, 2, 1) should be(DELTA_E_CMC_21_2_1 +- TOLERANCE)

    cmcAcceptability(LAB_1, LAB_2) should be(DELTA_E_CMC_21_1_2 +- TOLERANCE)
    cmcAcceptability(LAB_2, LAB_1) should be(DELTA_E_CMC_21_2_1 +- TOLERANCE)
  }

  "E20000" should " be equal Lindbloom" in {
    e2000(ColorTriple.Lab(LAB_1), ColorTriple.Lab(LAB_2)) should be(DELTA_E_2000_111 +- TOLERANCE)
    e2000(ColorTriple.Lab(LAB_2), ColorTriple.Lab(LAB_1)) should be(DELTA_E_2000_111 +- TOLERANCE)

    e2000(LAB_1, LAB_2) should be(DELTA_E_2000_111 +- TOLERANCE)
    e2000(LAB_2, LAB_1) should be(DELTA_E_2000_111 +- TOLERANCE)
  }
}
