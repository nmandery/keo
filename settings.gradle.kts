rootProject.name = "keo"

include("core", "geojson", "optimize")

// add a prefix to the jars
rootProject.children.forEach { it.name = "keo-${it.name}" }