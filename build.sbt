import xerial.sbt.Sonatype.GitHubHosting

import java.net.URL
import scala.xml.transform.{RewriteRule, RuleTransformer}
import scala.xml.{Node => XmlNode, NodeSeq => XmlNodeSeq, _}

// @formatter:off

name := "ijp-color-project"

val Scala2_12 = "2.12.15"
val Scala2_13 = "2.13.8"
val Scala3_0  = "3.0.2"

val _version       = "0.11.4.1-SNAPSHOT"
val _scalaVersions = Seq(Scala2_13, Scala2_12, Scala3_0)
//val _scalaVersion  = _scalaVersions.head
val _scalaVersion  = Scala3_0

ThisBuild / version             := _version
ThisBuild / scalaVersion        := _scalaVersion
ThisBuild / organization        := "net.sf.ij-plugins"
ThisBuild / sonatypeProfileName := "net.sf.ij-plugins"
ThisBuild / homepage            := Some(new URL("https://github.com/ij-plugins/ijp-color"))
ThisBuild / startYear           := Some(2002)
ThisBuild / licenses            := Seq(("LGPL-2.1", new URL("http://opensource.org/licenses/LGPL-2.1")))


publishArtifact     := false
publish / skip      := true

def isScala2(scalaVersion: String): Boolean =
      CrossVersion.partialVersion(scalaVersion) match {
    case Some((2, _)) => true
    case _            => false
  }

// Helper to determine Scala version-dependent settings
def isScala2_12(scalaVersion: String): Boolean =
  CrossVersion.partialVersion(scalaVersion) match {
    case Some((2, 12)) => true
    case _             => false
  }

def isScala2_13(scalaVersion: String): Boolean =
  CrossVersion.partialVersion(scalaVersion) match {
    case Some((2, 13)) => true
    case _             => false
  }

def isScala3(scalaVersion: String): Boolean =
  CrossVersion.partialVersion(scalaVersion) match {
    case Some((3, _)) => true
    case _            => false
  }


// Add src/main/scala-2.13+ for Scala 2.13 and newer
//   and src/main/scala-2.12- for Scala versions older than 2.13
def versionSubDir(scalaVersion: String): String =
  CrossVersion.partialVersion(scalaVersion) match {
    case Some((2, n)) if n < 13  => "scala-2.12-"
    case Some((2, n)) if n >= 13 => "scala-2.13+"
    case Some((3, _))            => "scala-3"
    case _ => throw new Exception(s"Unsupported Scala version $scalaVersion")
  }

def versionSubDir2v3(scalaVersion: String): String =
  CrossVersion.partialVersion(scalaVersion) match {
    case Some((2, _)) => "scala-2"
    case Some((3, _)) => "scala-3"
    case _            => throw new Exception(s"Unsupported Scala version $scalaVersion")
  }


// Determine OS version of JavaFX binaries
lazy val osName = System.getProperty("os.name") match {
  case n if n.startsWith("Linux")   => "linux"
  case n if n.startsWith("Mac")     => "mac"
  case n if n.startsWith("Windows") => "win"
  case _ => throw new Exception("Unknown platform!")
}
lazy val javaFXModules = Seq("base", "controls", "fxml", "graphics", "media", "swing", "web")
lazy val javaFXVersion = "17.0.1"

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
  resolvers ++= Seq(
    Resolver.sonatypeRepo("snapshots"),
    Resolver.mavenLocal
  ),
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
  sonatypeProfileName := "net.sf.ij-plugins",
  sonatypeProjectHosting := Some(GitHubHosting("ij-plugins", "ijp-color", "jpsacha@gmail.com")),
  publishTo := sonatypePublishToBundle.value,
  developers := List(
    Developer(id="jpsacha", name="Jarek Sacha", email="jpsacha@gmail.com", url=url("https://github.com/jpsacha"))
  )
)


// The core ijp-color module
lazy val ijp_color = (project in file("ijp-color"))
  .settings(
    name        := "ijp-color",
    description := "IJP Color Core",
    commonSettings,
    // Use different directories for code that is not source compatible between Scala versions
    Compile / unmanagedSourceDirectories += (Compile / sourceDirectory).value / versionSubDir2v3(scalaVersion.value),
    Test / unmanagedSourceDirectories += (Test / sourceDirectory).value / versionSubDir2v3(scalaVersion.value),
    //
    libraryDependencies ++= Seq(
      "net.imagej"              % "ij"                      % "1.53j",
      "org.apache.commons"      % "commons-math3"           % "3.6.1",
      "org.scala-lang.modules" %% "scala-collection-compat" % "2.6.0",
      // Test
      "org.scalatest"          %% "scalatest"               % "3.2.10" % "test"
    ),
    libraryDependencies ++= (
      if (isScala2_13(scalaVersion.value) || isScala3(scalaVersion.value)) {
        Seq("org.scala-lang.modules" %% "scala-parallel-collections" % "1.0.4")
      }
      else {
        Seq.empty[ModuleID]
      }
    ),
    libraryDependencies ++= (
      if(isScala2(scalaVersion.value)) {
        Seq("com.beachape" %% "enumeratum" % "1.7.0")
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
    // Use different directories for code that is not source compatible between Scala versions
    Compile / unmanagedSourceDirectories += (Compile / sourceDirectory).value / versionSubDir(scalaVersion.value),
    Test / unmanagedSourceDirectories += (Test / sourceDirectory).value / versionSubDir(scalaVersion.value),
    // Enable macro annotation processing for ScalaFXML
    scalacOptions += (if(isScala2_13(scalaVersion.value)) "-Ymacro-annotations" else ""),
    libraryDependencies ++= (
      if (isScala2_12(scalaVersion.value)) {
        Seq(compilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full))
      } else {
        Seq.empty[ModuleID]
      }),
    // JavaFX dependencies marked as "provided"
    libraryDependencies ++= javaFXModules.map( m =>
      "org.openjfx" % s"javafx-$m" % javaFXVersion % "provided" classifier osName
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
      "org.scalafx"        %% "scalafx"             % "17.0.1-R26",
      "org.scalafx"        %% "scalafx-extras"      % "0.5.0",
      // Test
      "org.scalatest"      %% "scalatest"           % "3.2.10"  % "test"
    )
  )
  .dependsOn(ijp_color)

// The 'experimental' is not a part of distribution.
// It is intended for ImageJ with plugins and fast local experimentation with new features.
lazy val experimental = (project in file("experimental"))
  .settings(
    name := "experimental",
    commonSettings,
    // Add JavaFX dependencies
    libraryDependencies ++= javaFXModules.map( m =>
      "org.openjfx" % s"javafx-$m" % javaFXVersion classifier osName
    ),
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