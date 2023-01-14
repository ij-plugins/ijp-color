package ij_plugins.color

import org.scalatest.flatspec.AnyFlatSpec

class BuildInfoSpec extends AnyFlatSpec {

  "BuildInfo" should "have 'version'" in {
    assert(BuildInfo.version.nonEmpty)
  }

}
