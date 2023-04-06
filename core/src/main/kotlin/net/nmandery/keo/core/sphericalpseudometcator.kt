package net.nmandery.keo.core

import org.locationtech.jts.geom.Coordinate
import kotlin.math.atan
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.tan

/**
 * spherical pseudo mercator projection
 *
 * https://wiki.openstreetmap.org/wiki/Mercator
 */
object SphericalPseudoMercator {

    /**
     * Returns the Spherical Mercator (x, y) in meters
     */
    fun fromLonLat(lonLatCoordinate: Coordinate) = Coordinate(
        RADIUS_EARTH_METERS * Math.toRadians(lonLatCoordinate.x),
        RADIUS_EARTH_METERS * ln(tan((Math.PI / 4) + (.5 * Math.toRadians(lonLatCoordinate.y)))),
        0.0
    )

    fun toLonLat(smCoordinate: Coordinate) = Coordinate(
        Math.toDegrees(smCoordinate.x / RADIUS_EARTH_METERS),
        Math.toDegrees(atan(exp(smCoordinate.y / RADIUS_EARTH_METERS)) * 2 - (Math.PI / 2))
    )

}