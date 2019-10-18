package net.nmandery.keo.io.geojson

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import org.locationtech.jts.geom.Geometry

class GeometryDeserializer<T : Geometry>(private val geometryParser: GeometryParser<T>) : JsonDeserializer<T>() {
    override fun deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext): T {
        val root = jsonParser.codec.readTree<JsonNode>(jsonParser)
        return geometryParser.parse(root, deserializationContext)
    }
}

