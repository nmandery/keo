package com.github.nmandery.keo.geojson

import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.JsonNode
import org.locationtech.jts.geom.*


internal fun readCoordinate(array: JsonNode, context: DeserializationContext): Coordinate =
    when (array.size()) {
        2 -> Coordinate(
            array.get(0).asDouble(),
            array.get(1).asDouble()
        )
        3 ->
            Coordinate(
                array.get(0).asDouble(),
                array.get(1).asDouble(),
                array.get(2).asDouble()
            )
        else -> throw JsonMappingException.from(
            context,
            "Coordinates must have 2 or 3 elements. Found ${array.size()} elements"
        )
    }


internal fun readCoordinates(array: JsonNode, context: DeserializationContext) =
    (0 until array.size())
        .map { array[it] }
        .mapNotNull { readCoordinate(it, context) }
        .toTypedArray()


abstract class GeometryParser<out T : Geometry>(protected val gf: GeometryFactory) {
    abstract fun parse(node: JsonNode, context: DeserializationContext): T
}

class PointParser(gf: GeometryFactory) : GeometryParser<Point>(gf) {
    override fun parse(node: JsonNode, context: DeserializationContext): Point =
        gf.createPoint(
            readCoordinate(
                node.get(Identifier.Coordinates.ident),
                context
            )
        )
}

class LineStringParser(gf: GeometryFactory) : GeometryParser<LineString>(gf) {
    override fun parse(node: JsonNode, context: DeserializationContext): LineString =
        gf.createLineString(
            readCoordinates(
                node.get(Identifier.Coordinates.ident),
                context
            )
        )
}

private fun readLinearRing(
    gf: GeometryFactory,
    coordinates: JsonNode,
    context: DeserializationContext
): LinearRing {
    if (!coordinates.isArray) {
        throw JsonMappingException.from(context, "expected coordinates array for linear ring")
    }
    return gf.createLinearRing(readCoordinates(coordinates, context))
}

private fun readPolygonFromRings(
    gf: GeometryFactory,
    arrayOfRings: JsonNode,
    context: DeserializationContext
): Polygon {
    if (!arrayOfRings.isArray) {
        throw JsonMappingException.from(context, "expected coordinates array for linear rings of polygon")
    }
    val shell = readLinearRing(gf, arrayOfRings.first(), context)
    val holes = (1 until arrayOfRings.size())
        .map { arrayOfRings[it] }
        .mapNotNull { readLinearRing(gf, it, context) }
        .toTypedArray()
    return gf.createPolygon(shell, holes)
}

class PolygonParser(gf: GeometryFactory) : GeometryParser<Polygon>(gf) {
    override fun parse(node: JsonNode, context: DeserializationContext) =
        readPolygonFromRings(
            gf,
            node.get(Identifier.Coordinates.ident),
            context
        )
}

class MultiPointParser(gf: GeometryFactory) : GeometryParser<MultiPoint>(gf) {
    override fun parse(node: JsonNode, context: DeserializationContext): MultiPoint =
        gf.createMultiPointFromCoords(
            readCoordinates(
                node.get(Identifier.Coordinates.ident),
                context
            )
        )
}

class MultiLineStringParser(gf: GeometryFactory) : GeometryParser<MultiLineString>(gf) {
    override fun parse(node: JsonNode, context: DeserializationContext): MultiLineString {
        val array = node.get(Identifier.Coordinates.ident)
        return gf.createMultiLineString(
            (0 until array.size())
                .mapNotNull { array[it] }
                .map { gf.createLineString(readCoordinates(it, context)) }
                .toTypedArray())
    }
}

class MultiPolygonParser(gf: GeometryFactory) : GeometryParser<MultiPolygon>(gf) {
    override fun parse(node: JsonNode, context: DeserializationContext): MultiPolygon {
        val arrayOfPolygons = node.get(Identifier.Coordinates.ident)
        return gf.createMultiPolygon(
            (0 until arrayOfPolygons.size())
                .map { arrayOfPolygons[it] }
                .mapNotNull { readPolygonFromRings(gf, it, context) }
                .toTypedArray()
        )
    }
}


class GeometryCollectionParser(gf: GeometryFactory) : GeometryParser<GeometryCollection>(gf) {

    private val genericGeometriesParser = GenericGeometryParser(gf)

    override fun parse(node: JsonNode, context: DeserializationContext): GeometryCollection {
        val arrayOfGeoms = node.get(Identifier.Geometries.ident)
        return gf.createGeometryCollection(
            (0 until arrayOfGeoms.size())
                .map { arrayOfGeoms[it] }
                .mapNotNull { genericGeometriesParser.parse(it, context) }
                .toTypedArray()
        )
    }
}

class GenericGeometryParser(gf: GeometryFactory) : GeometryParser<Geometry>(gf) {

    override fun parse(node: JsonNode, context: DeserializationContext): Geometry {
        val parser = when (val typeName = node.get(Identifier.Type.ident).asText()) {
            GeometryType.Point.ident -> PointParser(
                gf
            )
            GeometryType.LineString.ident -> LineStringParser(
                gf
            )
            GeometryType.Polygon.ident -> PolygonParser(
                gf
            )
            GeometryType.MultiPoint.ident -> MultiPointParser(
                gf
            )
            GeometryType.MultiLineString.ident -> MultiLineStringParser(
                gf
            )
            GeometryType.MultiPolygon.ident -> MultiPolygonParser(
                gf
            )
            GeometryType.GeometryCollection.ident -> GeometryCollectionParser(
                gf
            )
            else -> throw JsonMappingException.from(context, "Unsupported geometry type: $typeName")
        } as GeometryParser<*>
        return parser.parse(node, context)
    }
}

internal fun readEnvelope(node: JsonNode, context: DeserializationContext): Envelope {
    if (node.isObject) {
        return Envelope(
            node.get(EnvelopeKey.maxx.key).asDouble(),
            node.get(EnvelopeKey.minx.key).asDouble(),
            node.get(EnvelopeKey.maxy.key).asDouble(),
            node.get(EnvelopeKey.miny.key).asDouble()
        )
    } else if (node.isArray) {
        if (node.size() != 4) {
            throw JsonMappingException.from(
                context,
                "Envelope arrays must have 4 elements"
            )
        }
        return Envelope(
            node.get(0).asDouble(),
            node.get(2).asDouble(),
            node.get(1).asDouble(),
            node.get(3).asDouble()
        )
    } else {
        throw JsonMappingException.from(
            context,
            "Unsupported value type for an Envelope type"
        )
    }
}