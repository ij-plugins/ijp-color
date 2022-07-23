ijp-color
=========

Operations on color spaces and color images. Can be used as a stand-alone library or a plugin
for [ImageJ](http://rsb.info.nih.gov/ij)

[![Actions Status](https://github.com/ij-plugins/ijp-color/workflows/Scala%20CI/badge.svg)](https://github.com/ij-plugins/ijp-color/actions)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/net.sf.ij-plugins/ijp-color_2.13/badge.svg)](https://maven-badges.herokuapp.com/maven-central/net.sf.ij-plugins/ijp-color_2.13)
[![Scaladoc](https://javadoc.io/badge2/net.sf.ij-plugins/ijp-color_2.13/scaladoc.svg)](https://javadoc.io/doc/net.sf.ij-plugins/ijp-color_2.13)


<!-- TOC -->

* [ImageJ Plugins](#imagej-plugins)
    * [IJP Color Calibrator](#ijp-color-calibrator)
    * [IJP Color Calculator](#ijp-color-calculator)
    * [IJP Color Chart ROI Tool](#ijp-color-chart-roi-tool)
    * [IJP White Balance](#ijp-white-balance)
* [Test Images](#test-images)
* [Installing Plugins in ImageJ](#installing-plugins-in-imagej)
    * [ImageJ](#imagej)
        * [Option 1](#option-1)
        * [Option 2](#option-2)
    * [ImageJ2/FIJI](#imagej2fiji)
    * [Troubleshooting](#troubleshooting)
* [Stand-alone Library](#stand-alone-library)
* [Development Setup](#development-setup)
    * [Command line](#command-line)
    * [IntelliJ](#intellij)

<!-- TOC -->

ImageJ Plugins
--------------

### IJP Color Calibrator

Color calibrates images using a color chart. Supported charts:

* GretagMacbeth ColorChecker
* X-Rite Passport
* Image Science Associates ColorGauge
* Custom charts - provide your own layout and CIE L\*a\*b\* color values

Supports 8, 16, 32 bit per channel color images, including raw.

![Image Calibrator](https://github.com/ij-plugins/ijp-color/wiki/assets/Color_Calibrator_quick_usage.png)

More details in [project Wiki] under [Color Calibrator]

### IJP Color Calculator

Tool for converting individual color values between different color spaces. Inspired by Bruce
Lindbloom [CIE Color Calculator](http://www.brucelindbloom.com/index.html?ColorCalculator.html)

![Image Calibrator](https://github.com/ij-plugins/ijp-color/wiki/assets/Color_Converter_0.6_01.png)

More details in [project Wiki] under [Color Calculator]

### IJP Color Chart ROI Tool

Converts color chart ROI to individual chip ROIs. Measures color of each chip.

![Chart Tool](https://github.com/ij-plugins/ijp-color/wiki/assets/Chart_Tool_0.9_01.png)

More details in [project Wiki] under [Color Chart ROI Tool]

### IJP White Balance

Performs White Balance of an RGB image.

![Chart Tool](https://github.com/ij-plugins/ijp-color/wiki/assets/White_Balance_0.12_01.png)

More details in [project Wiki] under [White Balance]

Test Images
-----------

You can test the calibrator plugin using images in [test/data](test/data).

Installing Plugins in ImageJ
----------------------------

### ImageJ

#### Option 1

Prebuild binaries are published with each [Release](https://github.com/ij-plugins/ijp-color/releases).

1. Look for in the asset section for an "ijp-color_plugins_*_win.zip" file,
2. Download and unzip into ImageJ's `plugins` directory. It should create subdirectory "ij-plugins".
3. Restart ImageJ

#### Option 2

IJP Color is also a part of the ij-plugins-bundle. You can download from
its [Release](https://github.com/ij-plugins/ij-plugins-bundle/releases) page.

### ImageJ2/FIJI

IJP Color is a part of the ij-plugins-bundle that is also distributed for FIJI/ImageJ2
as [IJ-Plugins Update Site](https://sites.imagej.net/IJ-Plugins/): "https://sites.imagej.net/IJ-Plugins/"

### Troubleshooting

Depending on your ImageJ installation there may be issues properly loading the ijp-color plugin components,
see [Troubleshooting Wiki] for some hints. Please post your experience or ask questions in
the [Troubleshooting Discussions].


Stand-alone Library
-------------------

`ijp-color` was designed to be easily used as a stand-alone library. Examples of use are provided in
the [ijp-color/test](ijp-color/src/main/test/scala/net/ij/ij_plugins/color) directory.


Development Setup
-----------------

The minimum requirement to build an run the plugin in development environment is [Java](java.oracle.com)
and [SBT](http://www.scala-sbt.org/). SBT will download all needed dependencies. Key libraries:
[ImageJ](https://imagej.nih.gov/ij/), [Scala](https://www.scala-lang.org/), and [ScalaFX](http://www.scalafx.org/).

### Command line

You can build the `ij-color` plugins and run it within ImageJ using SBT task `ijRun` from the `experimental` module:

```
sbt ijRun
```

### IntelliJ

You will need to install Scala plugin then import the project from `build.sbt` file. To run the `ij-color` plugins in
ImageJ, setup "Run Configuration" for "SBT Task" and add task `experimental/ijRun`.

[project Wiki]: https://github.com/ij-plugins/ijp-color/wiki

[Discussions]: https://github.com/ij-plugins/ijp-color/discussions

[Color Calculator]: https://github.com/ij-plugins/ijp-color/wiki/Color-Calculator.md

[Color Calibrator]: https://github.com/ij-plugins/ijp-color/wiki/Color-Calibrator.md

[Color Chart ROI Tool]: https://github.com/ij-plugins/ijp-color/wiki/Color-Chart-ROI-Tool.md

[White Balance]: https://github.com/ij-plugins/ijp-color/wiki/White-Balance.md

[Troubleshooting Wiki]: https://github.com/ij-plugins/ijp-color/wiki/ImageJ-Launcher-Troubleshooting

[Troubleshooting Discussions]: https://github.com/ij-plugins/ijp-color/issues/71