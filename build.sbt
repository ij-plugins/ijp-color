import java.net.URL

// @formatter:off

name         := "ijp-color"
organization := "net.sf.ij-plugins"
version      := "0.5.1-SNAPSHOT"

homepage     := Some(new URL("https://ij-plugins.sf.net"))
startYear    := Some(2002)
licenses     := Seq(("LGPL-2.1", new URL("http://opensource.org/licenses/LGPL-2.1")))

scalaVersion       := "2.12.8"

// append -deprecation to the options passed to the Scala compiler
scalacOptions ++= Seq(
//  "-target:jvm-1.8",
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
  "org.openjfx" % s"javafx-$m" % "11" classifier osName
)

libraryDependencies ++= Seq(
  "net.imagej"          % "ij"                  % "1.51f",
  "org.apache.commons"  % "commons-math3"       % "3.6.1",
  "org.scalafx"        %% "scalafx"             % "11-R16",
  "org.scalafx"        %% "scalafx-extras"      % "0.2.0",
  "org.scalafx"        %% "scalafxml-core-sfx8" % "0.4",
  "org.scalatest"      %% "scalatest"           % "3.0.5"  % "test"
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
//
// Customize Java style publishing
//
// Enables publishing to maven repo
publishMavenStyle := true

publishTo := version {
  version: String =>
    if (version.contains("-SNAPSHOT"))
      Some("Sonatype Nexus Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots")
    else
      Some("Sonatype Nexus Releases" at "https://oss.sonatype.org/service/local/staging/deploy/maven2")
}.value


pomExtra :=
    <scm>
      <url>https://github.com/ij-plugins/ijp-color</url>
      <connection>scm:https://github.com/ij-plugins/ijp-color.git</connection>
    </scm>
    <developers>
      <developer>
        <id>jpsacha</id>
        <name>Jarek Sacha</name>
        <url>https://github.com/jpsacha</url>
      </developer>
    </developers>
