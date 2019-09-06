
// Where is the source code hosted

import xerial.sbt.Sonatype._

sonatypeProjectHosting := Some(GitHubHosting("ij-plugins", "ijp-color", "jpsacha@gmail.com"))

//
// Customize Java style publishing
//
// Enables publishing to maven repo
publishMavenStyle := true

publishTo := sonatypePublishTo.value

