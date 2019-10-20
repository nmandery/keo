package net.nmandery.keo.geojson

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import org.locationtech.jts.geom.Geometry

/**
 * TODO: find a nice way to implement features using generics while preserving the jackson annotations
 */
@JsonPropertyOrder("type", "id", "geometry", "properties")
@JsonTypeName("Feature")
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
    property = "type"
)
open class Feature<G : Geometry, T : Any> {

    var geometry: G? = null

    var properties: T? = null

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val id: Any? = null

    constructor(geometry: G, properties: T) {
        this.properties = properties
        this.geometry = geometry
    }

    constructor() {}
}

@JsonPropertyOrder("type", "features")
@JsonTypeName("FeatureCollection")
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
    property = "type"
)
open class FeatureCollection<F : Feature<*, *>>() {
    var features = emptyList<F>()

    constructor(features: List<F>) : this() {
        this.features = features
    }

    fun size() = features.size
}
