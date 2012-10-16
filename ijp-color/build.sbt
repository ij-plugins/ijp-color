name := "ijp-color"

organization := "ij-plugins.sf.net"

version := "1.0.0"

scalaVersion := "2.9.2"

// Main sources
scalaSource in Compile <<= baseDirectory(_ / "src")

// Main resources
resourceDirectory in Compile <<= baseDirectory(_ / "src")

// Test sources
scalaSource in Test <<= baseDirectory(_ / "test/src")

// Extra dependent libraries, in addition to those in 'lib' subdirectory
libraryDependencies ++= Seq(
    "org.scala-lang" % "scala-compiler" % "2.9.2",
    "org.scala-lang" % "scala-swing" % "2.9.2"
)

// Test dependencies
libraryDependencies ++= Seq(
    "com.novocode" % "junit-interface" % "0.9" % "test->default",
    "junit" % "junit" % "4.10" % "test"
)


//
// Optimize loops using ScalaCL compiler plugin
//
resolvers += "NativeLibs4Java Repository" at "http://nativelibs4java.sourceforge.net/maven/"

autoCompilerPlugins := true

addCompilerPlugin("com.nativelibs4java" % "scalacl-compiler-plugin" % "0.2")


// Fork a new JVM for 'run' and 'test:run'
fork := true

// Add a JVM option to use when forking a JVM for 'run'
javaOptions += "-Xmx1G"