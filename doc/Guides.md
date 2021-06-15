Guides
======


Color conversion
----------------


Simple `RGB` to `L*a*b*` conversion

```scala
import ij_plugins.color.converter.ColorConverter
import ij_plugins.color.converter.ColorTriple.RGB

val rgb = RGB(178, 217, 18)

val lab = new ColorConverter().toLab(rgb)

println(s"Input : $rgb")
println(s"Output: $lab")

```