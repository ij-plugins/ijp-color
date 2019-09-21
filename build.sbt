import java.net.URL

import xerial.sbt.Sonatype._

import scala.xml.transform.{RewriteRule, RuleTransformer}
import scala.xml.{Node => XmlNode, NodeSeq => XmlNodeSeq, _}

// @formatter:off

name         := "ijp-color-project"

lazy val _version       = "0.7.1.1-SNAPSHOT"
lazy val _scalaVersions = Seq("2.13.1", "2.12.10")
lazy val _scalaVersion  = _scalaVersions.head

scalaVersion := _scalaVersion
publishArtifact := false
skip in publish := true

// Helper to determine Scala version-dependent settings
def isScala2_13plus(scalaVersion: String): Boolean =
  CrossVersion.partialVersion(scalaVersion) match {
    case Some((2, n)) if n >= 13 => true
    case _ => false
  }

// Determine OS version of JavaFX binaries
lazy val osName = System.getProperty("os.name") match {
  case n if n.startsWith("Linux")   => "linux"
  case n if n.startsWith("Mac")     => "mac"
  case n if n.startsWith("Windows") => "win"
  case _ => throw new Exception("Unknown platform!")
}
lazy val javaFXModules = Seq("base", "controls", "fxml", "graphics", "media", "swing", "web")

val commonSettings = Seq(
  organization := "net.sf.ij-plugins",
  homepage     := Some(new URL("https://github.com/ij-plugins/ijp-color")),
  startYear    := Some(2002),
  licenses     := Seq(("LGPL-2.1", new URL("http://opensource.org/licenses/LGPL-2.1"))),
  //
  crossScalaVersions := _scalaVersions,
  scalaVersion := _scalaVersion,
  //
  scalacOptions ++= Seq(
    "-encoding", "UTF-8",
    "-unchecked",
    "-deprecation",
    "-Xlint",
    "-feature"
  ),
  // Point to location of a snapshot repository
  resolvers += Resolver.sonatypeRepo("snapshots"),
  //
  autoCompilerPlugins := true,
  // Fork a new JVM for 'run' and 'test:run'
  fork := true,
  // Fork a new JVM for 'test:run', but not 'run'
  fork in Test := true,
  // Only use a single thread for building
  parallelExecution := false,
  // Execute tests in the current project serially
  parallelExecution in Test := false,
  // Add a JVM option to use when forking a JVM for 'run'
  javaOptions += "-Xmx1G",
  // Setup publishing
  publishMavenStyle := true,
  sonatypeProjectHosting := Some(GitHubHosting("ij-plugins", "ijp-color", "jpsacha@gmail.com")),
  publishTo := sonatypePublishTo.value
)


// The core ijp-color module
lazy val ijp_color = (project in file("ijp-color")).settings(
  commonSettings,
  libraryDependencies ++= Seq(
    "net.imagej"          % "ij"            % "1.52j",
    "org.apache.commons"  % "commons-math3" % "3.6.1",
    "org.scalatest"      %% "scalatest"     % "3.0.8"  % "test"
  ),
  libraryDependencies ++= (
    if (isScala2_13plus(scalaVersion.value)) {
      Seq("org.scala-lang.modules" %% "scala-parallel-collections" % "0.2.0")
    } else {
      Seq.empty[ModuleID]
    }
  ),
)

// The ijp-color UI and ImageJ plugins module
lazy val ijp_color_ui = (project in file("ijp-color-ui"))
  .settings(
    commonSettings,
    // Enable macro annotation processing for ScalaFXML
    scalacOptions += (if(isScala2_13plus(scalaVersion.value)) "-Ymacro-annotations" else ""),
    libraryDependencies ++= (
      if (isScala2_13plus(scalaVersion.value)) {
        Seq.empty[ModuleID]
      } else {
        Seq(compilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full))
      }
    ),
    // JavaFX dependencies marked as "provided"
    libraryDependencies ++= javaFXModules.map( m =>
      "org.openjfx" % s"javafx-$m" % "12.0.2" % "provided" classifier osName
    ),
    // Use `pomPostProcess` to remove dependencies marked as "provided" from publishing in POM
    // This is to avoid dependency on wrong OS version JavaFX libraries
    // See also [https://stackoverflow.com/questions/27835740/sbt-exclude-certain-dependency-only-during-publish]
    pomPostProcess := { node: XmlNode =>
      new RuleTransformer(new RewriteRule {
        override def transform(node: XmlNode): XmlNodeSeq = node match {
          case e: Elem if e.label == "dependency" && e.child.exists(c => c.label == "scope" && c.text == "provided") =>
            val organization = e.child.filter(_.label == "groupId").flatMap(_.text).mkString
            val artifact = e.child.filter(_.label == "artifactId").flatMap(_.text).mkString
            val version = e.child.filter(_.label == "version").flatMap(_.text).mkString
            Comment(s"provided dependency $organization#$artifact;$version has been omitted")
          case _ => node
        }
      }).transform(node).head
    },
    // Other dependencies
    libraryDependencies ++= Seq(
      "org.jfree"           % "jfreechart-fx"       % "1.0.1",
      "org.jfree"           % "fxgraphics2d"        % "1.8",
      "org.scalafx"        %% "scalafx"             % "12.0.2-R18",
      "org.scalafx"        %% "scalafx-extras"      % "0.3.1",
      "org.scalafx"        %% "scalafxml-core-sfx8" % "0.5",
    )
  )
  .dependsOn(ijp_color)

// Set the prompt (for this build) to include the project id.
shellPrompt in ThisBuild := { state => "sbt:" + Project.extract(state).currentRef.project + "> " }

// Enable and customize `sbt-imagej` plugin
enablePlugins(SbtImageJ)
ijRuntimeSubDir         := "sandbox"
ijPluginsSubDir         := "ij-plugins"
ijCleanBeforePrepareRun := true
// Instruct `clean` to delete created plugins subdirectory created by `ijRun`/`ijPrepareRun`.
cleanFiles += ijPluginsDir.value
