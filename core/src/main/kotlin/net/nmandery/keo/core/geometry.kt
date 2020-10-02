package net.nmandery.keo.core

import org.locationtech.jts.geom.*
import org.locationtech.jts.geom.util.GeometryCombiner

/**
 * Combines Geometries to produce a GeometryCollection of the most appropriate type.
 **/
fun Collection<Geometry>.collectGeometries() = GeometryCombiner(this).combine()


private inline fun <reified T : Geometry> GeometryCollection.extract(): List<T> =
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
