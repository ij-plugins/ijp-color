import java.net.URL

// @formatter:off

name         := "ijp-color"
organization := "net.sf.ij-plugins"
version      := "0.7.0.1-SNAPSHOT"

homepage     := Some(new URL("https://github.com/ij-plugins/ijp-color"))
startYear    := Some(2002)
licenses     := Seq(("LGPL-2.1", new URL("http://opensource.org/licenses/LGPL-2.1")))

scalaVersion := "2.12.9"

// append -deprecation to the options passed to the Scala compiler
scalacOptions ++= Seq(
  "-target:jvm-1.8",
  "-encoding", "UTF-8",
  "-unchecked",
  "-deprecation",
//  "-optimize",
  "-Xlint",
  "-Xfuture",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
//  "-Ywarn-numeric-widen",
//  "-Ywarn-value-discard",
  "-Ywarn-unused",
  "-Ywarn-unused-import")

// Point to location of a snapshot repository for ScalaFX
resolvers ++= Seq(
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
  "ImageJ Releases" at "http://maven.imagej.net/content/repositories/releases/"
)

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)

// Determine OS version of JavaFX binaries
lazy val osName = System.getProperty("os.name") match {
  case n if n.startsWith("Linux")   => "linux"
  case n if n.startsWith("Mac")     => "mac"
  case n if n.startsWith("Windows") => "win"
  case _ => throw new Exception("Unknown platform!")
}

lazy val javaFXModules = Seq("base", "controls", "fxml", "graphics", "media", "swing", "web")
libraryDependencies ++= javaFXModules.map( m =>
  "org.openjfx" % s"javafx-$m" % "12.0.2" classifier osName
)

libraryDependencies ++= Seq(
  "net.imagej"          % "ij"                  % "1.52j",
  "org.apache.commons"  % "commons-math3"       % "3.6.1",
  "org.jfree"           % "jfreechart-fx"       % "1.0.1",
  "org.jfree"           % "fxgraphics2d"        % "1.8",
  "org.scalafx"        %% "scalafx"             % "12.0.1-R17",
  "org.scalafx"        %% "scalafx-extras"      % "0.3.0",
  "org.scalafx"        %% "scalafxml-core-sfx8" % "0.4",
  "org.scalatest"      %% "scalatest"           % "3.0.8"  % "test"
)

autoCompilerPlugins := true

// Fork a new JVM for 'run' and 'test:run'
fork := true

// Fork a new JVM for 'test:run', but not 'run'
fork in Test := true

// Only use a single thread for building
parallelExecution := false

// Execute tests in the current project serially
parallelExecution in Test := false

// Add a JVM option to use when forking a JVM for 'run'
javaOptions += "-Xmx1G"

// Set the prompt (for this build) to include the project id.
shellPrompt in ThisBuild := { state => "sbt:" + Project.extract(state).currentRef.project + "> " }

// Enable and customize `sbt-imagej` plugin
enablePlugins(SbtImageJ)
ijRuntimeSubDir         := "sandbox"
ijPluginsSubDir         := "ij-plugins"
ijCleanBeforePrepareRun := true
// Instruct `clean` to delete created plugins subdirectory created by `ijRun`/`ijPrepareRun`.
cleanFiles += ijPluginsDir.value
