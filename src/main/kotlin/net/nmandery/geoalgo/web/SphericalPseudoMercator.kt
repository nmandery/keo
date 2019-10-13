package net.nmandery.geoalgo.web

import org.locationtech.jts.geom.Coordinate
import kotlin.math.*

// https://wiki.openstreetmap.org/wiki/Mercator

private val EARTH_RADIUS = 6378137.0 // in meters on the equator

/**
 * Returns the Spherical Mercator (x, y) in meters
 */
fun lonLatToSphericalPseudoMercator(lon: Double, lat: Double) = Coordinate(
    EARTH_RADIUS * Math.toRadians(lon),
    EARTH_RADIUS * ln(tan((Math.PI / 4) + (.5 * Math.toRadians(lat)))),
    0.0
)

/**
 * Returns the Spherical Mercator (x, y) in meters
 */
fun lonLatToSphericalPseudoMercator(c: Coordinate) = lonLatToSphericalPseudoMercator(c.x, c.y)


fun sphericalPseudoMercatorToLonLat(x: Double, y: Double) = Coordinate(
    Math.toDegrees(x / EARTH_RADIUS),
    Math.toDegrees(atan(exp(y / EARTH_RADIUS)) * 2 - (Math.PI/2))
)

fun sphericalPseudoMercatorToLonLat(c: Coordinate) = sphericalPseudoMercatorToLonLat(c.x, c.y)