v.0.11.0
=======

This is a major feature release with many enhancement to the Color Calibrator and the Chart Tool.

__Calibrator__:

* Support color correction with linear mapping without intercept (`y = a*x`) [#52]
* Add option to run batch correction directly from UI  [#50]
* Example of using the color calibrator from a script [#48]
* Add help button to the custom chart editor  [#49]
* Enable changing of chip ROI color  [#35]
* Allow for calibration with some chips disabled [#39]
* `ReferenceColorSpace` conversion to L\*a\*b\* must use correct illuminant (bug fix) [#46]
* Add options to select what information is displayed when calibration is created [#40]
* Allow use of custom charts - user provides reference L\*a\*b\* values [#38]
* Remember settings between plugin invocations [#37]

__ChartTool__:

* Add option to list chart vertices [#43]
* Remember plugin dialog selections between invocations - save in Prefs [#44]
* Use slice numbers when slice label is `null` (bug fix) [#47]

__API__:

* Add emulation of ImageJ's GenericDialog in JavaFX [#42]
* Several other API enhancements

[#35]: https://github.com/ij-plugins/ijp-color/issues/35

[#37]: https://github.com/ij-plugins/ijp-color/issues/37

[#38]: https://github.com/ij-plugins/ijp-color/issues/38

[#39]: https://github.com/ij-plugins/ijp-color/issues/39

[#40]: https://github.com/ij-plugins/ijp-color/issues/40

[#42]: https://github.com/ij-plugins/ijp-color/issues/42

[#43]: https://github.com/ij-plugins/ijp-color/issues/43

[#44]: https://github.com/ij-plugins/ijp-color/issues/44

[#46]: https://github.com/ij-plugins/ijp-color/issues/46

[#47]: https://github.com/ij-plugins/ijp-color/issues/47

[#48]: https://github.com/ij-plugins/ijp-color/issues/48

[#49]: https://github.com/ij-plugins/ijp-color/issues/49

[#50]: https://github.com/ij-plugins/ijp-color/issues/50

[#52]: https://github.com/ij-plugins/ijp-color/issues/52

