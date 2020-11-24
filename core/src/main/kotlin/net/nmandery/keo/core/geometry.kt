package net.nmandery.keo.core

import org.locationtech.jts.geom.*
import org.locationtech.jts.geom.util.GeometryCombiner

/**
 * Combines Geometries to produce a GeometryCollection of the most appropriate type.
 **/
fun Collection<Geometry>.collectGeometries(): Geometry = GeometryCombiner(this).combine()


inline fun <reified T : Geometry> GeometryCollection.extract(): List<T> =
    (0 until this.numGeometries)
        .mapNotNull { idx ->
            when (val geom = this.getGeometryN(idx)) {
                is T -> geom
                // TODO: destruct multi* geometries
                else -> null
            }
        }

fun GeometryCollection.polygons(): List<Polygon> = this.extract()

fun GeometryCollection.points(): List<Point> = this.extract()

fun GeometryCollection.linearrings(): List<LinearRing> = this.extract()

fun GeometryCollection.linestrings(): List<LineString> = this.extract()

fun GeometryCollection.collections(): List<GeometryCollection> = this.extract()

fun GeometryCollection.geometries(): List<Geometry> = this.extract()

fun Polygon.interiorRings(): List<LinearRing> =
    (0 until this.numInteriorRing)
        .mapNotNull { idx -> this.getInteriorRingN(idx) }

fun Envelope.toPolygon(geometryFactory: GeometryFactory = GeometryFactory()): Polygon = geometryFactory.createPolygon(
    arrayOf(
        Coordinate(minX, minY),
        Coordinate(minX, maxY),
        Coordinate(maxX, maxY),
        Coordinate(maxX, minY),
        Coordinate(minX, minY) // must be a closed ring
    )
)

/**
 * lazily slice the geometry by teh given sequence of geometries. Returns
 * only non-empty intersections.
 */
inline fun <reified T: Geometry> T.sliceBy(polygons: Sequence<Polygon>): Sequence<Geometry> =
    polygons.flatMap { other ->
        val isec = this.intersection(other)
        if (isec.isEmpty) {
            emptySequence()
        } else {
            when (isec) {
                is T -> sequenceOf(isec)
                is GeometryCollection -> isec.extract<T>().asSequence()
                else -> emptySequence()
            }
        }
    }
