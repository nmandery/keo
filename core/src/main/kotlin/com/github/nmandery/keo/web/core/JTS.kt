package com.github.nmandery.keo.web.core

import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.geom.util.GeometryCombiner

/**
 * Combines Geometrys to produce a GeometryCollection of the most appropriate type.
 **/
fun Collection<Geometry>.collectGeometries() = GeometryCombiner(this).combine()