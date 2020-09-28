package net.nmandery.keo.geojson

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import org.locationtech.jts.geom.Geometry

/**
 * TODO: find a nice way to implement features using generics while preserving the jackson annotations
 *
 * Using this generic class requires adding all jackson annotations
 * on the subclass.
 */
@JsonPropertyOrder("type", "id", "geometry", "properties")
@JsonTypeName("Feature")
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
    property = "type"
)
open class GenericFeature<G : Geometry, T : Any> {

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
/**
 * Using this generic class requires adding all jackson annotations
 * on the subclass.
 */
@JsonPropertyOrder("type", "features")
@JsonTypeName("FeatureCollection")
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
    property = "type"
)
open class GenericFeatureCollection<F : GenericFeature<*, *>>() {
    var features = emptyList<F>()

    constructor(features: List<F>) : this() {
        this.features = features
    }

    fun size() = features.size
}


/**
 * non-generic feature implementation. Reduces boilerplate when features are not read
 * and are just serialized from kotlin objects.
 *
 */
@JsonPropertyOrder("type", "id", "geometry", "properties")
@JsonTypeName("Feature")
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
    property = "type"
)
class Feature {

    var geometry: Geometry? = null

    var properties: Any? = null

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val id: Any? = null

    constructor(geometry: Geometry, properties: Any) {
        this.properties = properties
        this.geometry = geometry
    }

    constructor() {}
}


/**
 * non-generic featurecollection implementation. Reduces boilerplate when features are not read
 * and are just serialized from kotlin objects.
 *
 */
@JsonPropertyOrder("type", "features")
@JsonTypeName("FeatureCollection")
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
    property = "type"
)
class FeatureCollection() {
    var features = emptyList<Feature>()

    constructor(features: List<Feature>) : this() {
        this.features = features
    }

    fun size() = features.size
}

