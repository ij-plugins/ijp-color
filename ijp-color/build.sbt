// sbt-imagej configuration keys

import ImageJKeys._

name := "ijp-color"

organization := "ij-plugins.sf.net"

version := "0.5.0-SNAPSHOT"

crossScalaVersions := Seq("2.10.3", "2.9.3")

scalaVersion := crossScalaVersions.value.head

unmanagedJars in Compile += Attributed.blank(file(scala.util.Properties.javaHome) / "lib" / "jfxrt.jar")

// append -deprecation to the options passed to the Scala compiler
scalacOptions ++= Seq("-unchecked", "-deprecation", "-optimize", "-Xlint")

// Point to location of a snapshot repository for ScalaFX
resolvers ++= Seq(
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
  "ImageJ Releases" at "http://maven.imagej.net/content/repositories/releases/"
)

libraryDependencies ++= Seq(
  "org.scalafx" %% "scalafx" % "1.0.0-M7",
  "net.imagej" % "ij" % "1.47v",
  "org.apache.commons" % "commons-math3" % "3.2",
  "org.jfxtras" % "jfxtras-labs" % "2.2-r5"
)

// Test dependencies
libraryDependencies += "org.scalatest" %% "scalatest" % "1.9.2" % "test"

//
// Optimize loops using ScalaCL compiler plugin
//
resolvers += "NativeLibs4Java Repository" at "http://nativelibs4java.sourceforge.net/maven/"

autoCompilerPlugins := true

//addCompilerPlugin("com.nativelibs4java" % "scalacl-compiler-plugin" % "0.2")

libraryDependencies ++= {
  if (scalaVersion.value.startsWith("2.9."))
    Seq(compilerPlugin("com.nativelibs4java" % "scalacl-compiler-plugin" % "0.2"))
  else
    Seq.empty
}

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
