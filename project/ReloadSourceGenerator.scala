/*
 * Image/J Plugins
 * Copyright (C) 2002-2023 Jarek Sacha
 * Author's email: jpsacha at gmail dot com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Latest release available at https://github.com/ij-plugins/ijp-color/
 */

import sbt.plugins.JvmPlugin
import sbt.{Def, *}

/**
  * A workaround for Build-Info source generation in IntelliJ. It is not happening automatically in IntelliJ.
  * This is causing the generation of BuildInfo on project reload.
  *
  * Sometimes IDEA will add multiple instances of the same "[generated]" directory.
  * It will have to be manually removed under "Project Structure".
  *
  * [[https://youtrack.jetbrains.com/issue/SCL-18993/IntelliJ-still-does-not-support-sbt-buildinfo-plugin]]
  *
  * See also [[https://youtrack.jetbrains.com/issue/SCL-19660/sbt-buildinfo-support]]
  */
object ReloadSourceGenerator extends AutoPlugin {

  override def requires: Plugins = JvmPlugin

  override def trigger: PluginTrigger = allRequirements

  private val generateSources = taskKey[Seq[File]]("run all sourceGenerators in current project")
  private val generateAllSources = taskKey[Unit]("run all sourceGenerators in ALL project")

  override def projectSettings: Seq[Def.Setting[_]] = Seq(
    generateSources := Def.taskDyn {
      val gens: Seq[Task[Seq[File]]] = (Compile / Keys.sourceGenerators).value
      Def.task {
        joinTasks(gens).join.value.flatten
      }
    }.value
  )

  override def buildSettings: Seq[Def.Setting[_]] = Seq(
    generateAllSources := generateSources.all(ScopeFilter(inAnyProject)).value
  )

  override def globalSettings: Seq[Def.Setting[_]] = Seq(
    Keys.onLoad := ((s: State) => {
      generateAllSources.key.toString :: s
    }) compose (Keys.onLoad in Global).value
  )

}
