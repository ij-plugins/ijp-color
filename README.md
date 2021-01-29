ijp-color
=========

Operations on color spaces and color images. Can be used as a stand-alone library or a plugin for [ImageJ](http://rsb.info.nih.gov/ij)

This is a part of [IJ Plugins Project](http://ij-plugins.sourceforge.net/).

[![Actions Status](https://github.com/ij-plugins/ijp-color/workflows/Scala%20CI/badge.svg)](https://github.com/ij-plugins/ijp-color/actions) 
[![Build Status](https://travis-ci.org/ij-plugins/ijp-color.svg?branch=develop)](https://travis-ci.org/ij-plugins/ijp-color) 
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/net.sf.ij-plugins/ijp-color_2.13/badge.svg)](https://maven-badges.herokuapp.com/maven-central/net.sf.ij-plugins/ijp-color_2.13) 
[![Scaladoc](http://javadoc-badge.appspot.com/net.sf.ij-plugins/ijp-color_2.13.svg?label=scaladoc)](http://javadoc-badge.appspot.com/net.sf.ij-plugins/ijp-color_2.13)


ImageJ Plugins
--------------


### IJP Color Calibrator

Color calibrates images using a color chart. Supported charts:

* GretagMacbeth ColorChecker
* X-Rite Passport
* Image Science Associates ColorGauge

Supports 8, 16, 32 bit per channel color images, including raw.

![Image Calibrator](assets/https://github.com/ij-plugins/ijp-color/wiki/assets/Color_Calibrator_0.6_01.png)

More details in [project Wiki]


### IJP Color Calculator

Tool for converting individual color values between different color spaces. Inspired by Bruce Lindbloom [CIE Color Calculator](http://www.brucelindbloom.com/index.html?ColorCalculator.html)

![Image Calibrator](https://github.com/ij-plugins/ijp-color/wiki/assets/Color_Converter_0.6_01.png)


### IJP Color Chart ROI Tool

Converts color chart ROI to individual chip ROIs. Measures color of each chip.

![Chart Tool](https://github.com/ij-plugins/ijp-color/wiki/assets/Chart_Tool_0.9_01.png)


Test Images
-----------

You can test the calibrator plugin using images in [test/data](test/data).


Stand-alone Library
-------------------

`ijp-color` was designed to be easily used as a stand-alone library. 
Examples of use are provided in the [ijp-color/test](ijp-color/src/main/test/scala/net/ij/ij_plugins/color) directory.


Development Setup
-----------------

The minimum requirement to build an run the plugin in development environment is [Java](java.oracle.com) and [SBT](http://www.scala-sbt.org/). 
SBT will download all needed dependencies. Key libraries: 
[ImageJ](https://imagej.nih.gov/ij/), [Scala](https://www.scala-lang.org/), and [ScalaFX](http://www.scalafx.org/).

### Command line

You can build the `ij-color` plugins and run it within ImageJ using SBT task `ijRun` from the `experimental` module:
 
```
sbt experimental/ijRun
```


### IntelliJ

You will need to install Scala plugin then import the project from `build.sbt` file. To run the `ij-color` plugins in ImageJ, setup "Run Configuration" for "SBT Task" and add task `experimental/ijRun`.  

[project Wiki]: https://github.com/ij-plugins/ijp-color/wiki