name := "ijp-color"

organization := "ij-plugins.sf.net"

version := "0.2.0-SNAPSHOT"

scalaVersion := "2.9.3"

// Main sources
scalaSource in Compile <<= baseDirectory(_ / "src")

// Main resources
resourceDirectory in Compile <<= baseDirectory(_ / "src")

// Test sources
scalaSource in Test <<= baseDirectory(_ / "test/src")

unmanagedJars in Compile += Attributed.blank(file(scala.util.Properties.javaHome) / "lib" / "jfxrt.jar")

// append -deprecation to the options passed to the Scala compiler
scalacOptions += "-deprecation"

// Point to location of a snapshot repositiry for ScalaFX
resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

resolvers += "ImageJ Releases" at "http://maven.imagej.net/content/repositories/releases/"

// ScalaFX dedpendency
libraryDependencies += "org.scalafx" %% "scalafx" % "1.0.0-M2-SNAPSHOT"

libraryDependencies += "net.imagej" % "ij" % "1.47h"

libraryDependencies += "org.apache.commons" % "commons-math3" % "3.1.1"

// Test dependencies
libraryDependencies += "org.scalatest" %% "scalatest" % "1.9.1" % "test"

// Add JavaFX 2 to the unmanaged classpath
// For Java 7 update 06+ the JFXRT JAR is part of the Java Runtime Environment
unmanagedJars in Compile += Attributed.blank(file(System.getenv("JAVA_HOME") + "/jre/lib/jfxrt.jar"))

//
// Optimize loops using ScalaCL compiler plugin
//
resolvers += "NativeLibs4Java Repository" at "http://nativelibs4java.sourceforge.net/maven/"

autoCompilerPlugins := true

addCompilerPlugin("com.nativelibs4java" % "scalacl-compiler-plugin" % "0.2")


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