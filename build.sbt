import xerial.sbt.Sonatype.GitHubHosting

import java.net.URL

// @formatter:off

name := "ijp-color-project"

val Scala2 = "2.13.10"
val Scala3 = "3.3.0-RC3"

val _version       = "0.12.1.1-SNAPSHOT"
val _scalaVersions = Seq(Scala2, Scala3)
//val _scalaVersion  = _scalaVersions.head
val _scalaVersion  = Scala3

ThisBuild / version             := _version
ThisBuild / scalaVersion        := _scalaVersion
ThisBuild / versionScheme       := Some("early-semver")
ThisBuild / organization        := "net.sf.ij-plugins"
ThisBuild / sonatypeProfileName := "net.sf.ij-plugins"
ThisBuild / homepage            := Some(new URL("https://github.com/ij-plugins/ijp-color"))
ThisBuild / startYear           := Some(2002)
ThisBuild / licenses            := Seq(("LGPL-2.1", new URL("https://opensource.org/licenses/LGPL-2.1")))
ThisBuild / developers          := List(
  Developer(id="jpsacha", name="Jarek Sacha", email="jpsacha@gmail.com", url=url("https://github.com/jpsacha"))
)

publishArtifact     := false
publish / skip      := true

def isScala2(scalaVersion: String): Boolean =
      CrossVersion.partialVersion(scalaVersion) match {
    case Some((2, _)) => true
    case _            => false
  }

def isScala3(scalaVersion: String): Boolean =
  CrossVersion.partialVersion(scalaVersion) match {
    case Some((3, _)) => true
    case _            => false
  }


val commonSettings = Seq(
  //
  crossScalaVersions := _scalaVersions,
  scalaVersion       := _scalaVersion,
  //
  scalacOptions ++= Seq(
    "-encoding", "UTF-8",
    "-unchecked",
    "-deprecation",
    "-feature",
    // Java 8 for compatibility with ImageJ/FIJI
    "-release",
    "8"
  ) ++ (
    if(isScala2(scalaVersion.value))
      Seq(
        "-explaintypes",
        "-Xsource:3",
        "-Xlint",
        "-Xcheckinit",
        "-Xlint:missing-interpolator",
        "-Ywarn-dead-code",
        "-Ywarn-unused:-patvars,_",
      )
    else
      Seq(
        "-explain",
        "-explain-types"
      )
  ),
  Compile / doc / scalacOptions ++= Opts.doc.title("IJP Color API"),
  Compile / doc / scalacOptions ++= Opts.doc.version(_version),
  Compile / doc / scalacOptions ++= Seq(
    "-doc-footer", s"IJP Color API v.${_version}",
    "-doc-root-content", baseDirectory.value + "/src/main/scala/root-doc.creole"
  ),
  Compile / doc / scalacOptions ++= (
    Option(System.getenv("GRAPHVIZ_DOT_PATH")) match {
      case Some(path) => Seq("-diagrams", "-diagrams-dot-path", path, "-diagrams-debug")
      case None => Seq.empty[String]
    }),
  javacOptions  ++= Seq("-deprecation", "-Xlint"),
  //
  resolvers  += Resolver.mavenLocal,
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
  publishMavenStyle := true,
  sonatypeProjectHosting := Some(GitHubHosting("ij-plugins", "ijp-color", "jpsacha@gmail.com")),
  publishTo := sonatypePublishToBundle.value,
)


// The core ijp-color module
lazy val ijp_color = (project in file("ijp-color"))
  .settings(
    name        := "ijp-color",
    description := "IJP Color Core",
    commonSettings,
    //
    libraryDependencies ++= Seq(
      "net.imagej"              % "ij"                      % "1.54d",
      "org.apache.commons"      % "commons-math3"           % "3.6.1",
      // Test
      "org.scalatest"          %% "scalatest"               % "3.2.15" % "test"
    ),
    libraryDependencies += "org.scala-lang.modules" %% "scala-parallel-collections" % "1.0.4",
    libraryDependencies ++= (
      if(isScala2(scalaVersion.value)) {
        Seq("com.beachape" %% "enumeratum" % "1.7.2")
      }
      else {
        Seq.empty[ModuleID]
      }
    ),
  )

// The ijp-color UI and ImageJ plugins module
lazy val ijp_color_ui = (project in file("ijp-color-ui"))
  .settings(
    name        := "ijp-color-ui",
    description := "IJP Color UI and ImageJ plugins",
    commonSettings,
    // Enable macro annotation processing for ScalaFXML
    scalacOptions += (if(isScala2(scalaVersion.value)) "-Ymacro-annotations" else ""),
    // Other dependencies
    libraryDependencies ++= Seq(
      "org.jfree"           % "jfreechart-fx"       % "1.0.1",
      "org.jfree"           % "fxgraphics2d"        % "1.8",
      "org.scalafx"        %% "scalafx"             % "20.0.0-R31",
      "org.scalafx"        %% "scalafx-extras"      % "0.8.0",
      // Test
      "org.scalatest"      %% "scalatest"           % "3.2.15"  % "test"
    ),
    // Customize `sbt-imagej` plugin
    ijRuntimeSubDir         := "sandbox",
    ijPluginsSubDir         := "ij-plugins",
    ijCleanBeforePrepareRun := true,
    cleanFiles += ijPluginsDir.value,
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
    cleanFiles += ijPluginsDir.value,
  )
  .dependsOn(ijp_color_ui)

lazy val manifestSetting = packageOptions += {
  Package.ManifestAttributes(
    "Created-By" -> "Simple Build Tool",
    "Built-By"  -> Option(System.getenv("JAR_BUILT_BY")).getOrElse(System.getProperty("user.name")),
    "Build-Jdk" -> System.getProperty("java.version"),
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