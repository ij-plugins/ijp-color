ijp-color
=========

Operations on color spaces and color images. Can be used as a stand-alone library or a plugin for [ImageJ](http://rsb.info.nih.gov/ij)

This is a part of [IJ Plugins Project](http://ij-plugins.sourceforge.net/).

[![Build Status](https://travis-ci.org/ij-plugins/ijp-color.svg?branch=develop)](https://travis-ci.org/ij-plugins/ijp-color)


ImageJ Plugins
--------------

* __IJP Color Calculator__ - tool for converting individual color values between different color spaces.
* __IJP Color Calibrator__ - color calibrates images using a color chart embedded in the image.


Test Images
-----------

You can test the calibrator plugin using images in [test/data](test/data).


Stand-alone Library
-------------------

`ijp-color` was designed to be easily used as a stand-alone library. Examples of use are provided in the [test](src/main/test/scala/net/ij/ij_plugins/color) directory.


Development Setup
-----------------

The minimum requirement to build an run the plugin in development environment is [Java 8](java.oracle.com) and [SBT](http://www.scala-sbt.org/). SBT will download all needed dependencies.


### Command line

You can build the `ij-color` plugins and run it within ImageJ using SBT task `ijRun`:
 
```
sbt ijRun
```


### IntelliJ

You will need to install Scala plugin then import the project from `build.sbt` file. To run the `ij-color` plugins in ImageJ, setup "Run Configuration" for "SBT Task" and add task `ijRun`.  


### Eclipse

Eclipse project can be generated using SBT task `eclipse`:

```
sbt eclipse
```
