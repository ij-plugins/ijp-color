
// Where is the source code hosted

import xerial.sbt.Sonatype._

sonatypeProjectHosting := Some(GitHubHosting("ij-plugins", "ijp-color", "jpsacha@gmail.com"))

//
// Customize Java style publishing
//
// Enables publishing to maven repo
publishMavenStyle := true

publishTo := sonatypePublishTo.value

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
