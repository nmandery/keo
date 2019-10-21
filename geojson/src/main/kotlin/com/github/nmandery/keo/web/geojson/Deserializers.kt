package com.github.nmandery.keo.web.geojson

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import org.locationtech.jts.geom.*

class GeometryDeserializer<T : Geometry>(private val geometryParser: GeometryParser<T>) : JsonDeserializer<T>() {
    override fun deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext): T {
        val root = jsonParser.codec.readTree<JsonNode>(jsonParser)
        return geometryParser.parse(root, deserializationContext)
    }
}

class CoordinateDeserializer() : JsonDeserializer<Coordinate>() {
    override fun deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext): Coordinate {
        val root = jsonParser.codec.readTree<JsonNode>(jsonParser)
        return readCoordinate(root, deserializationContext)
    }
}

class CoordinateSequenceDeserializer(private val coordinateSequenceFactory: CoordinateSequenceFactory) :
    JsonDeserializer<CoordinateSequence>() {
    override fun deserialize(
        jsonParser: JsonParser,
        deserializationContext: DeserializationContext
    ): CoordinateSequence {
        val root = jsonParser.codec.readTree<JsonNode>(jsonParser)
        return coordinateSequenceFactory.create(
            readCoordinates(root, deserializationContext)
        )
    }
}

class EnvelopeDeserializer() : JsonDeserializer<Envelope>() {
    override fun deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext): Envelope {
        val node = jsonParser.codec.readTree<JsonNode>(jsonParser)
        return readEnvelope(node, deserializationContext)
    }
}
