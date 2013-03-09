/*
 * Image/J Plugins
 * Copyright (C) 2002-2012 Jarek Sacha
 * Author's email: jsacha at users dot sourceforge dot net
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
 * Latest release available at http://sourceforge.net/projects/ij-plugins/
 */

import sbt._
import Keys._

/**
 * Customization for the SBT.
 */
object MyBuild extends Build {

    val prepareRun = TaskKey[Seq[File]]("prepareRun", "Prepare plugins directory to run with ImageJ")


    /**
     * Copy dependencies to "./"sandbox/plugin/ij-plugins".
     * Excludes ImageJ and nativelibs4java jars.
     */
    def prepareRunFiles(jar: java.io.File, dependencies: Seq[Attributed[File]]): Seq[java.io.File] = {
        println("Preparing ImageJ plugin directories")
        val files = jar +: (for (f <- dependencies) yield f.data)
        val pluginsDir = file(".") / "sandbox" / "plugins" / "ij-plugins"
        pluginsDir.mkdirs()
        for (f <- files; if (!f.isDirectory); if(!f.getPath.contains("nativelibs4java")); if(!f.getPath.contains("ij-1.46r.jar"))) {
            println("Copying: " + f)
            IO.copyFile(f, pluginsDir / f.getName)
        }
        files
    }


    val project = Project(
        "ijp-color",
        file("."),
        settings = Defaults.defaultSettings ++ Seq(
            prepareRun <<= (packageBin in Compile, dependencyClasspath in Compile) map prepareRunFiles
        )
    )
}
