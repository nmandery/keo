package net.nmandery.keo.geojson

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

internal enum class EnvelopeKey(val key: String) {
    maxx("maxx"),
    minx("minx"),
    maxy("maxy"),
    miny("miny")
}

private val objectMapper: ObjectMapper by lazy {
    val om = jacksonObjectMapper()
    om.registerJTSGeoJSON()
    om
}

fun Geometry.geojsonGeometry() = objectMapper.writeValueAsString(this)

data class JTSGeoJsonConfiguration(

    /**
     * serialize Envelope object to JSON objects.
     *
     * The default is using arrays int the openlayers-compatible value order
     * [minx, miny, maxx, maxy]
     *
     * https://openlayers.org/en/latest/apidoc/module-ol_extent.html#~Extent
     */
    val serializeEnvelopesAsObjects: Boolean = false
)

class JTSGeoJSON(
    val configuration: JTSGeoJsonConfiguration = JTSGeoJsonConfiguration(),
    val gf: GeometryFactory = GeometryFactory()
) : SimpleModule("JTSGeoJSON") {

    init {
        addSerializer(Geometry::class.java, GeometrySerializer())
        addSerializer(Coordinate::class.java, CoordinateSerializer())
        addSerializer(CoordinateSequence::class.java,
            CoordinateSequenceSerializer()
        )
        addSerializer(Envelope::class.java, EnvelopeSerializer(configuration))

        addDeserializer(Geometry::class.java,
            GeometryDeserializer(
                GenericGeometryParser(
                    gf
                )
            )
        )
        addDeserializer(Point::class.java,
            GeometryDeserializer(PointParser(gf))
        )
        addDeserializer(LineString::class.java,
            GeometryDeserializer(
                LineStringParser(
                    gf
                )
            )
        )
        addDeserializer(Polygon::class.java,
            GeometryDeserializer(
                PolygonParser(
                    gf
                )
            )
        )
        addDeserializer(MultiPoint::class.java,
            GeometryDeserializer(
                MultiPointParser(
                    gf
                )
            )
        )
        addDeserializer(MultiLineString::class.java,
            GeometryDeserializer(
                MultiLineStringParser(
                    gf
                )
            )
        )
        addDeserializer(MultiPolygon::class.java,
            GeometryDeserializer(
                MultiPolygonParser(
                    gf
                )
            )
        )
        addDeserializer(GeometryCollection::class.java,
            GeometryDeserializer(
                GeometryCollectionParser(
                    gf
                )
            )
        )
        addDeserializer(Coordinate::class.java, CoordinateDeserializer())
        addDeserializer(CoordinateSequence::class.java,
            CoordinateSequenceDeserializer(gf.coordinateSequenceFactory)
        )
        addDeserializer(Envelope::class.java, EnvelopeDeserializer())
    }

    override fun setupModule(context: SetupContext) {
        super.setupModule(context)
    }
}

fun ObjectMapper.registerJTSGeoJSON(
    configuration: JTSGeoJsonConfiguration = JTSGeoJsonConfiguration(),
    gf: GeometryFactory = GeometryFactory()
) {
    this.registerModule(JTSGeoJSON(configuration, gf))
}
