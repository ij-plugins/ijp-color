v.0.11.4
========

Provide modifications needed to build binaries with Scala 3 [#62]. 
Scala 2 code depends on some libraries that are no longer available in Scala 3:

* Enumeratum is replaced by Scala 3 `enum`s
* ScalaFXML is not supported due to lack of macro annotations in Scala 3

In Scala 3 code enumerations are rewritten with `enum`s with some extra methods added (like `withName`) to make use similar to Enumeratum. FXML use is rewritten, in Scala 3 code, to use JavaFX `@fxml` annotations directly. Scala 2 use of FXML is not changed.

There are no changes to end user functionality of the plugins, so plugins are not republished.

[#62]: https://github.com/ij-plugins/ijp-color/issues/62