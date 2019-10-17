package net.nmandery.keoalgo.io.geojson

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
        addDeserializer(Geometry::class.java, GeometryDeserializer<Geometry>(GenericGeometryParser(gf)))
        addDeserializer(Point::class.java, GeometryDeserializer<Point>(PointParser(gf)))
        addDeserializer(LineString::class.java, GeometryDeserializer<LineString>(LineStringParser(gf)))
        addDeserializer(Polygon::class.java, GeometryDeserializer<Polygon>(PolygonParser(gf)))
        addDeserializer(MultiPoint::class.java, GeometryDeserializer<MultiPoint>(MultiPointParser(gf)))
        addDeserializer(MultiLineString::class.java, GeometryDeserializer<MultiLineString>(MultiLineStringParser(gf)))
        addDeserializer(MultiPolygon::class.java, GeometryDeserializer<MultiPolygon>(MultiPolygonParser(gf)))
        addDeserializer(
            GeometryCollection::class.java,
            GeometryDeserializer<GeometryCollection>(GeometryCollectionParser(gf))
        )
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
