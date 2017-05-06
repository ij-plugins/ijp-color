// @formatter:off

name         := "ijp-color"
organization := "net.sf.ij-plugins"
version      := "0.5.0-SNAPSHOT"

crossScalaVersions := Seq("2.11.11", "2.12.2")
scalaVersion       := crossScalaVersions.value.head

// append -deprecation to the options passed to the Scala compiler
scalacOptions ++= Seq(
  "-target:jvm-1.8",
  "-encoding", "UTF-8",
  "-unchecked",
  "-deprecation",
  "-optimize",
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

libraryDependencies ++= Seq(
  "net.imagej"          % "ij"             % "1.51f",
  "org.apache.commons"  % "commons-math3"  % "3.6.1",
  "org.scalafx"        %% "scalafx"        % "8.0.102-R11",
  "org.scalafx"        %% "scalafx-extras" % "0.1.0",
  "org.scalatest"      %% "scalatest"      % "3.0.3"  % "test"

)

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
