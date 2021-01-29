Guides
======


Color conversion
----------------


Simple `RGB` to `L*a*b*` conversion

```scala
import net.sf.ij_plugins.color.converter.ColorConverter
import net.sf.ij_plugins.color.converter.ColorTriple.RGB

val lab = new Converter().toLab(RGB(178, 217, 18))
```