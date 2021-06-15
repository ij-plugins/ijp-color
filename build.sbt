import xerial.sbt.Sonatype.GitHubHosting

import java.net.URL
import scala.xml.transform.{RewriteRule, RuleTransformer}
import scala.xml.{Node => XmlNode, NodeSeq => XmlNodeSeq, _}

// @formatter:off

name := "ijp-color-project"

val _version       = "0.10.1.1-SNAPSHOT"
val _scalaVersions = Seq("2.13.6", "2.12.14")
val _scalaVersion  = _scalaVersions.head

version             := _version
scalaVersion        := _scalaVersion
publishArtifact     := false
skip in publish     := true
sonatypeProfileName := "net.sf.ij-plugins"

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
lazy val javaFXVersion = "16"

val commonSettings = Seq(
  version      := _version,
  organization := "net.sf.ij-plugins",
  homepage     := Some(new URL("https://github.com/ij-plugins/ijp-color")),
  startYear    := Some(2002),
  licenses     := Seq(("LGPL-2.1", new URL("http://opensource.org/licenses/LGPL-2.1"))),
  //
  crossScalaVersions := _scalaVersions,
  scalaVersion       := _scalaVersion,
  //
  scalacOptions ++= Seq(
    "-encoding", "UTF-8",
    "-unchecked",
    "-deprecation",
    "-Xlint",
    "-feature",
    "-explaintypes", 
  ),
  scalacOptions in(Compile, doc) ++= Opts.doc.title("IJP Color API"),
  scalacOptions in(Compile, doc) ++= Opts.doc.version(_version),
  scalacOptions in(Compile, doc) ++= Seq(
    "-doc-footer", s"IJP Color API v.${_version}",
    "-doc-root-content", baseDirectory.value + "/src/main/scala/root-doc.creole"
  ),
  scalacOptions in(Compile, doc) ++= (
    Option(System.getenv("GRAPHVIZ_DOT_PATH")) match {
      case Some(path) => Seq("-diagrams", "-diagrams-dot-path", path, "-diagrams-debug")
      case None => Seq.empty[String]
    }),
  javacOptions  ++= Seq("-deprecation", "-Xlint"),
  //
  resolvers += Resolver.sonatypeRepo("snapshots"),
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
    libraryDependencies ++= Seq(
      "com.beachape"           %% "enumeratum"              % "1.6.1",
      "net.imagej"              % "ij"                      % "1.53j",
      "org.apache.commons"      % "commons-math3"           % "3.6.1",
      "org.scala-lang.modules" %% "scala-collection-compat" % "2.4.4",
      // Test
      "org.scalatest" %% "scalatest" % "3.2.9"  % "test"
    ),
    libraryDependencies ++= (
      if (isScala2_13plus(scalaVersion.value)) {
        Seq("org.scala-lang.modules" %% "scala-parallel-collections" % "1.0.3")
      } else {
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
      "org.scalafx"        %% "scalafx"             % "16.0.0-R24",
      "org.scalafx"        %% "scalafx-extras"      % "0.3.6",
      "org.scalafx"        %% "scalafxml-core-sfx8" % "0.5",
      // Test
      "org.scalatest"      %% "scalatest"           % "3.2.9"  % "test"
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
    skip in publish := true,
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