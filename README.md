# {k}otlin g{eo} - geographic extensions for [Kotlin](https://kotlinlang.org) 

This is a collection of various geo-related extensions for Kotlin. The extensions mostly
integrate existing libraries - like [JTS](https://projects.eclipse.org/projects/locationtech.jts) 
into the the Kotlin ecosystem but sometimes also provide their own functionality. It is currently only 
a small collection which will hopefully grow in the future.

The project is split into subpackages to reduce the number of dependencies when only a subset
of the functionality is required.

Packages are available via github.

## Available libraries

### core

package name: `keo-core`

The core package provides functionality shared between multiple subprojects. 

### geojson

package name: `keo-geojson`

[GeoJSON](https://geojson.org/) support for the [jackson](https://github.com/FasterXML/jackson) JSON library. 
This package allows serializing and deserializing the JTS geometry types as well as JTS Coordinates and Envelopes.
Some support for Features and FeatureCollections is also included.

Usage:

```kotlin
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import net.nmandery.keo.geojson.registerJTSGeoJSON

// ...

val om = jacksonObjectMapper()
om.registerJTSGeoJSON()

// Now jacksons object mapper supports the JTS types
```

### optimize

package name: `keo-optimize`

Geometry-optimization algorithms:

* reorder coordinates to build the shortest-possible path.

### Documentation

Sadly there currently is not to much documentation - please refer to
the included unittests - they should show most of the implemented features.


## License

Apache 2.0