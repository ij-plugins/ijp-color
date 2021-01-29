v.0.9.0
=======

### Changes in UI

* Added "Color Chart Tool" plugin to extract and measure chips ROIs within a chart. Works with color images and stacks #29

* Each plugin now has a "Help" button that points to corresponding WIKI page

### Changes in API

* Keep all code within `net.sf.ij_plugins.color` to avoid naming conflicts #28
  
* Package names changed to avoid conflicts, code in module `ijp-color-ui` moved to package `net.sf.ij_plugins.color.ui`

* Various code cleanups

* ReferenceColorSpace - add method to create from text name, like "sRGB" #10
  
* `MappingMethod` use "sealed abstract" pattern instead of Enumeration #23
  
* Add computation of DeltaE 2000 #24
  
* Add ability to use other correction schemes, beside polynomial approximation #26
  
* Correct order of arguments in GridColorChart constructor #30
