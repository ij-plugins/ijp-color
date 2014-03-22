// sbt-imagej configuration keys

import ImageJKeys._

name := "ijp-color"

organization := "ij-plugins.sf.net"

version := "0.5.0-SNAPSHOT"

crossScalaVersions := Seq("2.10.3")

scalaVersion := crossScalaVersions.value.head

// append -deprecation to the options passed to the Scala compiler
scalacOptions ++= Seq("-unchecked", "-deprecation", "-optimize", "-Xlint")

// Point to location of a snapshot repository for ScalaFX
resolvers ++= Seq(
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
  "ImageJ Releases" at "http://maven.imagej.net/content/repositories/releases/"
)

libraryDependencies ++= Seq(
  "net.imagej"         % "ij"            % "1.47v",
  "org.apache.commons" % "commons-math3" % "3.2",
  "org.controlsfx"     % "controlsfx"    % "8.0.5",
  "org.jfxtras"        % "jfxtras-labs"  % "8.0-r1",
  "org.scalafx"       %% "scalafx"       % "8.0.0-R4"
)

// Test dependencies
libraryDependencies += "org.scalatest" %% "scalatest" % "2.1.0" % "test"

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
shellPrompt in ThisBuild := { state => "sbt:" + Project.extract(state).currentRef.project + "> "}

// sbt-imagej plugin
ijSettings

ijRuntimeSubDir := "sandbox"

ijPluginsSubDir := "ij-plugins"

ijExclusions += """nativelibs4java\S*"""

cleanFiles += ijPluginsDir.value
