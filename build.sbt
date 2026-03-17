name := "ijp-color-project"

val Scala2 = "2.13.14"
val Scala3 = "3.3.7"

val _version       = "0.12.2.1-SNAPSHOT"
val _scalaVersions = Seq(Scala2, Scala3)
//val _scalaVersion  = _scalaVersions.head
val _scalaVersion = Scala3

ThisBuild / version       := _version
ThisBuild / scalaVersion  := _scalaVersion
ThisBuild / versionScheme := Some("early-semver")
ThisBuild / organization  := "net.sf.ij-plugins"
ThisBuild / homepage      := Some(new URI("https://github.com/ij-plugins/ijp-color").toURL)
ThisBuild / startYear     := Some(2002)
ThisBuild / licenses      := Seq(("LGPL-2.1", new URI("https://opensource.org/licenses/LGPL-2.1").toURL))
ThisBuild / developers := List(
  Developer(id = "jpsacha", name = "Jarek Sacha", email = "jpsacha@gmail.com", url = url("https://github.com/jpsacha"))
)

publishArtifact := false
publish / skip  := true

val commonSettings = Seq(
  //
  crossScalaVersions := _scalaVersions,
  scalaVersion       := _scalaVersion,
  //
  scalacOptions ++= Seq(
    "-encoding",
    "UTF-8",
    "-unchecked",
    "-deprecation",
    "-feature",
    // Java 8 for compatibility with ImageJ/FIJI
    "-release",
    "8",
    "-explain",
    "-explain-types",
    "-rewrite",
    "-source:3.3-migration",
    "-Wvalue-discard",
    "-Wunused:all"
  ),
  Compile / doc / scalacOptions ++= Opts.doc.title("IJP Color API"),
  Compile / doc / scalacOptions ++= Opts.doc.version(_version),
  Compile / doc / scalacOptions ++= Seq(
    "-doc-footer",
    s"IJP Color API v.${_version}",
    "-doc-root-content",
    baseDirectory.value + "/src/main/scala/root-doc.creole"
  ),
  Compile / doc / scalacOptions ++= (
    Option(System.getenv("GRAPHVIZ_DOT_PATH")) match {
      case Some(path) => Seq("-diagrams", "-diagrams-dot-path", path, "-diagrams-debug")
      case None       => Seq.empty[String]
    }
  ),
  javacOptions ++= Seq("-deprecation", "-Xlint"),
  //
  resolvers += Resolver.mavenLocal,
  resolvers ++= Resolver.sonatypeOssRepos("snapshots"),
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
  manifestSetting,
  // Setup publishing
  publishMavenStyle := true
)

lazy val libImageJ        = "net.imagej"         % "ij"             % "1.54p"
lazy val libCommonsMath   = "org.apache.commons" % "commons-math3"  % "3.6.1"
lazy val libScalaTest     = "org.scalatest"     %% "scalatest"      % "3.2.19"
lazy val libJFreeChartFX  = "org.jfree"          % "jfreechart-fx"  % "1.0.1"
lazy val libFXGraphics2D  = "org.jfree"          % "fxgraphics2d"   % "1.8"
lazy val libScalaFX       = "org.scalafx"       %% "scalafx"        % "25.0.2-R37"
lazy val libScalaFXExtras = "org.scalafx"       %% "scalafx-extras" % "0.12.0"

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
    libraryDependencies += "org.scala-lang.modules" %% "scala-parallel-collections" % "1.2.0"
  )

// The ijp-color UI and ImageJ plugins module
lazy val ijp_color_ui = (project in file("ijp-color-ui"))
  .settings(
    name        := "ijp-color-ui",
    description := "IJP Color UI and ImageJ plugins",
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

// Instruct `clean` to delete created plugins subdirectory created by `ijRun`/`ijPrepareRun`.
enablePlugins(SbtImageJ)
cleanFiles += ijPluginsDir.value

addCommandAlias("ijRun", "experimental/ijRun")
