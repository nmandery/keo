package net.nmandery.keo.io.geojson

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import org.locationtech.jts.geom.*

private fun serializeCoordinate(c: Coordinate, gen: JsonGenerator, inArray: Boolean = true) {
    if (inArray) {
        gen.writeStartArray()
    }
    gen.writeNumber(c.getX())
    gen.writeNumber(c.getY())
    if (!c.getZ().isNaN()) {
        gen.writeNumber(c.getZ())
    }
    if (inArray) {
        gen.writeEndArray()
    }
}

class CoordinateSerializer : JsonSerializer<Coordinate>() {

    override fun handledType() = Coordinate::class.java

    override fun serialize(value: Coordinate, gen: JsonGenerator, provider: SerializerProvider) =
        serializeCoordinate(value, gen, true)
}

class CoordinateSequenceSerializer : JsonSerializer<CoordinateSequence>() {
    override fun handledType() = CoordinateSequence::class.java

    override fun serialize(value: CoordinateSequence, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeStartArray()
        (0 until value.size())
            .forEach { serializeCoordinate(value.getCoordinate(it), gen, true) }
        gen.writeEndArray()
    }
}

class EnvelopeSerializer(private val configuration: JTSGeoJsonConfiguration) : JsonSerializer<Envelope>() {
    override fun handledType() = Envelope::class.java

    override fun serialize(value: Envelope, gen: JsonGenerator, provider: SerializerProvider) {
        if (configuration.serializeEnvelopesAsObjects) {
            gen.writeStartObject()
            gen.writeObjectField(EnvelopeKey.minx.key, value.minX)
            gen.writeObjectField(EnvelopeKey.miny.key, value.minY)
            gen.writeObjectField(EnvelopeKey.maxx.key, value.maxX)
            gen.writeObjectField(EnvelopeKey.maxy.key, value.maxY)
            gen.writeEndObject()
        } else {
            // openlayers-compatible format
            gen.writeStartArray()
            gen.writeNumber(value.minX)
            gen.writeNumber(value.minY)
            gen.writeNumber(value.maxX)
            gen.writeNumber(value.maxY)
            gen.writeEndArray()
        }
    }
}

class GeometrySerializer : JsonSerializer<Geometry>() {

    override fun handledType() = Geometry::class.java

    override fun serialize(value: Geometry, gen: JsonGenerator, provider: SerializerProvider) =
        serializeGeometry(value, gen, provider)

    private fun serializeGeometry(
        value: Geometry,
        gen: JsonGenerator,
        provider: SerializerProvider
    ) =
        when (value) {
            is Point -> serializePoint(value, gen)
            is LineString -> serializeLineString(value, gen)
            is Polygon -> serializePolygon(value, gen)
            is MultiPoint -> serializeMultiPoint(value, gen)
            is MultiLineString -> serializeMultiLineString(value, gen)
            is MultiPolygon -> serializeMultiPolygon(value, gen)
            is GeometryCollection -> serializeGeometryCollection(value, gen, provider)
            else ->
                throw JsonMappingException.from(
                    provider,
                    "Geometry type ${value::class.java.name} is not supported for serialization"
                )
        }

    private fun serializeGeometryStructure(
        gen: JsonGenerator,
        geometryType: GeometryType,
        bodySerializeFun: () -> Unit
    ) {
        gen.writeStartObject()
        gen.writeStringField(Identifier.Type.ident, geometryType.ident)
        gen.writeArrayFieldStart(
            if (geometryType == GeometryType.GeometryCollection) {
                Identifier.Geometries.ident
            } else {
                Identifier.Coordinates.ident
            }
        )
        bodySerializeFun()
        gen.writeEndArray()
        gen.writeEndObject()
    }

    private fun serializeGeometryCollection(
        value: GeometryCollection,
        gen: JsonGenerator,
        provider: SerializerProvider
    ) {
        return serializeGeometryStructure(gen, GeometryType.GeometryCollection) {
            (0 until value.numGeometries)
                .forEach { serializeGeometry(value.getGeometryN(it), gen, provider) }
        }
    }

    private fun serializeMultiPoint(value: MultiPoint, gen: JsonGenerator) =
        serializeGeometryStructure(gen, GeometryType.MultiPoint) {
            (0 until value.numGeometries)
                .forEach { serializePointCoords(value.getGeometryN(it) as Point, gen) }
        }

    private fun serializeMultiLineString(value: MultiLineString, gen: JsonGenerator) =
        serializeGeometryStructure(gen, GeometryType.MultiLineString) {
            (0 until value.numGeometries)
                .forEach {
                    gen.writeStartArray()
                    serializeLineStringCoords(value.getGeometryN(it) as LineString, gen, false)
                    gen.writeEndArray()
                }
        }

    private fun serializeMultiPolygon(value: MultiPolygon, gen: JsonGenerator) =
        serializeGeometryStructure(gen, GeometryType.MultiPolygon) {
            (0 until value.numGeometries)
                .forEach {
                    gen.writeStartArray()
                    serializePolygonCoordinates(value.getGeometryN(it) as Polygon, gen)
                    gen.writeEndArray()
                }
        }

    private fun serializePolygon(value: Polygon, gen: JsonGenerator) =
        serializeGeometryStructure(gen, GeometryType.Polygon) {
            serializePolygonCoordinates(value, gen)
        }

    private fun serializePolygonCoordinates(value: Polygon, gen: JsonGenerator) {
        gen.writeStartArray()
        serializeLineStringCoords(value.exteriorRing, gen, true)
        gen.writeEndArray()
        (0 until value.numInteriorRing)
            .forEach {
                gen.writeStartArray()
                serializeLineStringCoords(value.getInteriorRingN(it), gen, true)
                gen.writeEndArray()
            }
        //gen.writeEndArray()
    }

    private fun serializeLineStringCoords(
        ring: LineString,
        gen: JsonGenerator,
        closeToRing: Boolean = false
    ) {
        (0 until ring.numPoints)
            .forEach { serializePointCoords(ring.getPointN(it), gen) }
        if (closeToRing && ring.numPoints > 2) {
            val p1 = ring.startPoint
            if (!p1.equals(ring.endPoint)) {
                serializePointCoords(p1, gen)
            }
        }
    }

    private fun serializeLineString(
        lineString: LineString,
        gen: JsonGenerator
    ) = serializeGeometryStructure(gen, GeometryType.LineString) {
        serializeLineStringCoords(lineString, gen, false)
    }

    private fun serializePoint(p: Point, gen: JsonGenerator) =
        serializeGeometryStructure(gen, GeometryType.Point) {
            serializePointCoords(p, gen, false)
        }

    private fun serializePointCoords(p: Point, gen: JsonGenerator, inArray: Boolean = true) =
        serializeCoordinate(p.coordinate, gen, inArray)

}