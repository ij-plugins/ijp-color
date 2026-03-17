name := "ijp-color-project"

//
// Environment variables used by the build:
// GRAPHVIZ_DOT_PATH - Full path to Graphviz dot utility. If not defined, Scaladoc will be built without diagrams.
// JAR_BUILT_BY      - Name to be added to Jar metadata field "Built-By" (defaults to System.getProperty("user.name")
//

ThisBuild / version              := "0.12.3.1-SNAPSHOT"
ThisBuild / scalaVersion         := "3.3.7"
ThisBuild / organization         := "net.sf.ij-plugins"
ThisBuild / organizationName     := "IJ-Plugins"
ThisBuild / organizationHomepage := Some(url("https://github.com/ij-plugins"))
ThisBuild / homepage             := Some(url("https://github.com/ij-plugins/ijp-color"))
ThisBuild / startYear            := Some(2002)
ThisBuild / licenses             := Seq("LGPL-2.1" -> url("https://opensource.org/licenses/LGPL-2.1"))
ThisBuild / scmInfo              := Option(
  ScmInfo(
    url("https://github.com/ij-plugins/ijp-color"),
    "scm:https://github.com/ij-plugins/ijp-color.git"
  )
)
ThisBuild / versionScheme := Some("early-semver")

// Resolvers
// Add snapshots to the root project to enable compilation with Scala SNAPSHOT compiler,
// e.g., 2.11.0-SNAPSHOT
ThisBuild / resolvers += Resolver.sonatypeCentralSnapshots
ThisBuild / resolvers += Resolver.mavenLocal

publishArtifact := false
publish / skip  := true

// Set the Java version target for compatibility for the current FIJI distribution
// We do not want to be over the FIJI Java version.
lazy val javaTargetVersion = "21"

lazy val libCommonsMath              = "org.apache.commons"      % "commons-math3"              % "3.6.1"
lazy val libFXGraphics2D             = "org.jfree"               % "fxgraphics2d"               % "1.8"
lazy val libImageJ                   = "net.imagej"              % "ij"                         % "1.54p"
lazy val libJFreeChartFX             = "org.jfree"               % "jfreechart-fx"              % "1.0.1"
lazy val libScalaTest                = "org.scalatest"          %% "scalatest"                  % "3.2.19"
lazy val libScalaFX                  = "org.scalafx"            %% "scalafx"                    % "25.0.2-R37"
lazy val libScalaFXExtras            = "org.scalafx"            %% "scalafx-extras"             % "0.12.0"
lazy val libScalaParallelCollections = "org.scala-lang.modules" %% "scala-parallel-collections" % "1.2.0"

val commonSettings = Seq(
  scalacOptions ++= Seq(
    "-encoding",
    "UTF-8",
    "-unchecked",
    "-deprecation",
    "-feature",
    "-explain",
    "-explain-types",
    "-rewrite",
    "-source:3.3-migration",
//    "-Wvalue-discard",
    "-Wunused:all",
    "-release",
    javaTargetVersion
  ),
  Compile / doc / scalacOptions ++= Opts.doc.title("IJP Color API"),
  Compile / doc / scalacOptions ++= Opts.doc.version(version.value),
  Compile / doc / scalacOptions ++= Seq(
    "-doc-footer",
    s"IJP Color API v.${version.value}",
    "-doc-root-content",
    baseDirectory.value + "/src/main/scala/root-doc.creole"
  ),
  Compile / doc / scalacOptions ++= (
    Option(System.getenv("GRAPHVIZ_DOT_PATH")) match {
      case Some(path) => Seq("-diagrams", "-diagrams-dot-path", path, "-diagrams-debug")
      case None       => Seq.empty[String]
    }
  ),
  Compile / compile / javacOptions ++= Seq("-deprecation", "-Xlint", "--release", javaTargetVersion),
  //
  exportJars := true,
  //
  autoCompilerPlugins := true,
  // Fork a new JVM for 'run' and 'test:run'
  fork := true,
  // Add a JVM option to use when forking a JVM for 'run'
  javaOptions += "-Xmx1G",
  // Instruct `clean` to delete created plugins subdirectory created by `ijRun`/`ijPrepareRun`.
  cleanFiles += ijPluginsDir.value,
  //
  manifestSetting
)

// The core ijp-color module
lazy val ijp_color = (project in file("ijp-color"))
  .settings(
    name        := "ijp-color",
    description := "IJP Color Core",
    commonSettings,
    //
    libraryDependencies ++= Seq(
      libImageJ,
      libCommonsMath,
      // Test
      libScalaTest % "test"
    ),
    libraryDependencies += libScalaParallelCollections
  )

// The ijp-color UI and ImageJ plugins module
lazy val ijp_color_ui = (project in file("ijp-color-ui"))
  .settings(
    name        := "ijp-color-ui",
    description :=
      """
        |<html>
        |  IJP Color UI and ImageJ plugins. Operations on color spaces and color images.
        |  <ul>
        |    <li>Color Calibrator - Color calibrates images using a color chart.</li>
        |    <li>Color Calculator - Tool for converting individual color values between different color spaces.</li>
        |    <li>Color Chart ROI Tool - Converts color chart ROI to individual chip ROIs. Measures the color of each chip.</li>
        |    <li>White Balance - Performs White Balance of an RGB image.</li>
        |  </ul>
        |</html>
        """.stripMargin,
    commonSettings,
    // Other dependencies
    libraryDependencies ++= Seq(
      libJFreeChartFX,
      libFXGraphics2D,
      libScalaFX,
      libScalaFXExtras,
      // Test
      libScalaTest % "test"
    ),
    scalacOptions ++= Seq(
      // To deal with classes implementing `ControllerFX` and using @FXML annotations to passing variables from FXML declarations
      // or "-Wunused:-privates"
      "-Wconf:msg=unset private var:s"
    ),
    // Customize `sbt-imagej` plugin
    ijRuntimeSubDir         := "sandbox",
    ijPluginsSubDir         := "ij-plugins",
    ijCleanBeforePrepareRun := true,
    cleanFiles += ijPluginsDir.value
  )
  .dependsOn(ijp_color)

// The 'experimental' is not a part of distribution.
// It is intended for ImageJ with plugins and fast local experimentation with new features.
lazy val experimental = (project in file("experimental"))
  .settings(
    name := "experimental",
    commonSettings,
    // Do not publish this artifact
    publishArtifact := false,
    publish / skip  := true,
    // Customize `sbt-imagej` plugin
    ijRuntimeSubDir         := "sandbox",
    ijPluginsSubDir         := "ij-plugins",
    ijCleanBeforePrepareRun := true,
    cleanFiles += ijPluginsDir.value
  )
  .dependsOn(ijp_color_ui)

lazy val manifestSetting = packageOptions += {
  Package.ManifestAttributes(
    "Created-By"               -> "Simple Build Tool",
    "Built-By"                 -> Option(System.getenv("JAR_BUILT_BY")).getOrElse(System.getProperty("user.name")),
    "Build-Jdk"                -> System.getProperty("java.version"),
    "Specification-Title"      -> name.value,
    "Specification-Version"    -> version.value,
    "Specification-Vendor"     -> organization.value,
    "Implementation-Title"     -> name.value,
    "Implementation-Version"   -> version.value,
    "Implementation-Vendor-Id" -> organization.value,
    "Implementation-Vendor"    -> organization.value
  )
}

//
// Customize Java style publishing
//
// Enables publishing to maven repo
ThisBuild / publishMavenStyle      := true
ThisBuild / Test / publishArtifact := false
ThisBuild / publishTo              := {
  val centralSnapshots = "https://central.sonatype.com/repository/maven-snapshots/"
  if (isSnapshot.value) Some("central-snapshots" at centralSnapshots)
  else localStaging.value
}
ThisBuild / developers := List(
  Developer(
    id = "jpsacha",
    name = "Jarek Sacha",
    email = "jpsacha@gmail.com",
    url = url("https://github.com/jpsacha")
  )
)

// Enable and customize `sbt-imagej` plugin
enablePlugins(SbtImageJ)
ijRuntimeSubDir         := "sandbox"
ijPluginsSubDir         := "ij-plugins"
ijCleanBeforePrepareRun := true
// Instruct `clean` to delete created plugins subdirectory created by `ijRun`/`ijPrepareRun`.
cleanFiles += ijPluginsDir.value

addCommandAlias("ijRun", "experimental/ijRun")
