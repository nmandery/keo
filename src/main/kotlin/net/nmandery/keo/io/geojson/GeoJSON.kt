package net.nmandery.keo.io.geojson

import arrow.core.Eval
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.locationtech.jts.geom.*


internal enum class Identifier(val ident: String) {
    Coordinates("coordinates"),
    Geometries("geometries"),
    Type("type")
}

internal enum class GeometryType(val ident: String) {
    Point("Point"),
    LineString("LineString"),
    Polygon("Polygon"),
    MultiPoint("MultiPoint"),
    MultiLineString("MultiLineString"),
    MultiPolygon("MultiPolygon"),
    GeometryCollection("GeometryCollection"),
}

private val objectMapper = Eval.later {
    val om = jacksonObjectMapper()
    om.registerJTSGeoJSON()
    om
}

fun Geometry.geojsonGeometry() = objectMapper.value.writeValueAsString(this)


class JTSGeoJSON(val gf: GeometryFactory = GeometryFactory()) : SimpleModule("JTSGeoJSON") {

    init {
        addSerializer(Geometry::class.java, GeometrySerializer())
        addSerializer(Coordinate::class.java, CoordinateSerializer())
        addSerializer(CoordinateSequence::class.java, CoordinateSequenceSerializer())

        addDeserializer(Geometry::class.java, GeometryDeserializer(GenericGeometryParser(gf)))
        addDeserializer(Point::class.java, GeometryDeserializer(PointParser(gf)))
        addDeserializer(LineString::class.java, GeometryDeserializer(LineStringParser(gf)))
        addDeserializer(Polygon::class.java, GeometryDeserializer(PolygonParser(gf)))
        addDeserializer(MultiPoint::class.java, GeometryDeserializer(MultiPointParser(gf)))
        addDeserializer(MultiLineString::class.java, GeometryDeserializer(MultiLineStringParser(gf)))
        addDeserializer(MultiPolygon::class.java, GeometryDeserializer(MultiPolygonParser(gf)))
        addDeserializer(GeometryCollection::class.java, GeometryDeserializer(GeometryCollectionParser(gf)))
        addDeserializer(Coordinate::class.java, CoordinateDeserializer())
        addDeserializer(CoordinateSequence::class.java, CoordinateSequenceDeserializer(gf.coordinateSequenceFactory))
    }

    override fun setupModule(context: SetupContext) {
        super.setupModule(context)
    }
}

fun ObjectMapper.registerJTSGeoJSON(gf: GeometryFactory) {
    this.registerModule(JTSGeoJSON(gf))
}

fun ObjectMapper.registerJTSGeoJSON() {
    this.registerModule(JTSGeoJSON())
}
