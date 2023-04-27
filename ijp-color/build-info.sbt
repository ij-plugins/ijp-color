//
// Generate build and version information
//
enablePlugins(BuildInfoPlugin)

buildInfoUsePackageAsPath := true
buildInfoKeys             := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion)
buildInfoPackage          := "ij_plugins.color"
buildInfoObject           := "BuildInfo"
buildInfoKeys ++= Seq[BuildInfoKey](
  BuildInfoKey.action("buildTime") {
    System.currentTimeMillis
  } // re-computed each time at compile
)
