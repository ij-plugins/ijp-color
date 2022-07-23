v.0.12.0
========

New features:

* [LiveChartROI] allow to change width of the line in the drawn overlay [#66]
* [Chart Tool] allow for quick save of generated ROIs [#67]
* Add White Balance plugin [#68]

Bug fixes:

* [Chart Tool] Properly refresh overlay when next image is loaded [#65]

API Changes:

* Enums, like ImagePlusType, should have the same methods in Scala 2 and Scala 3 versions [#63]
* The base chart frame should allow arbitrary chip polygons [#64]
* Drop Scala 2.12 support [#70]
* API: prefer use of immutable IndexedSeq instead of Array [#72]
* `GridChartFrameUtils` object renamed to `ChartFrameUtils`
* `LiveChartROI` requires type parameter (chart on which it operates, for instance, `LiveChartROI`
* Use `new LiveChartROI[..](...)` instead of `new LiveChartROI.apply(...)`

There are some known issues with ImageJ launchers, if you encounter errors when starting plugins consider installation
hints in [Troubleshooting Wiki]. Please post your experience in the [Troubleshooting Discussions].


[Troubleshooting Wiki]: https://github.com/ij-plugins/ijp-color/wiki/ImageJ-Launcher-Troubleshooting

[Troubleshooting Discussions]: https://github.com/ij-plugins/ijp-color/issues/71

[#63]: https://github.com/ij-plugins/ijp-color/issues/63

[#64]: https://github.com/ij-plugins/ijp-color/issues/64

[#65]: https://github.com/ij-plugins/ijp-color/issues/65

[#66]: https://github.com/ij-plugins/ijp-color/issues/66

[#67]: https://github.com/ij-plugins/ijp-color/issues/67

[#68]: https://github.com/ij-plugins/ijp-color/issues/68

[#70]: https://github.com/ij-plugins/ijp-color/issues/70

[#72]: https://github.com/ij-plugins/ijp-color/issues/72