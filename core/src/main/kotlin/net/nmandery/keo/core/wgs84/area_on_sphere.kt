package net.nmandery.keo.core.wgs84

import net.nmandery.keo.core.geometries
import net.nmandery.keo.core.interiorRings
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.geom.GeometryCollection
import org.locationtech.jts.geom.LinearRing
import org.locationtech.jts.geom.Polygon
import kotlin.math.absoluteValue
import kotlin.math.pow
import kotlin.math.sin

/**
 * calculate the approximate area of the given geometry in square meters.
 *
 * The geometry must be in WGS84 projection
 *
 * Roughly taken from https://gis.stackexchange.com/questions/711/how-can-i-measure-area-from-geographic-coordinates
 * Full paper at: https://www.semanticscholar.org/paper/Some-algorithms-for-polygons-on-a-sphere.-Chamberlain-Duquette/79668c0fe32788176758a2285dd674fa8e7b8fa8
 */
fun Geometry.areaOnSphereApprox(sphereRadius: Double = RADIUS_EARTH_METERS): Double =
    when (this) {
        is LinearRing -> coordinates
            .toList()
            .windowed(2)
            .filter { it.size == 2 }
            .sumByDouble { coords ->
                Math.toRadians(coords[1].x - coords[0].x) * (2.0 + sin(Math.toRadians(coords[0].y)) + sin(
                    Math.toRadians(
                        coords[1].y
                    )
                ))
            }.absoluteValue * sphereRadius.pow(2) / 2.0
        is Polygon -> exteriorRing.areaOnSphereApprox(sphereRadius) - interiorRings().sumByDouble {
            it.areaOnSphereApprox(sphereRadius)
        }
        is GeometryCollection -> geometries().sumByDouble { it.areaOnSphereApprox(sphereRadius) }
        else -> 0.0
    }
