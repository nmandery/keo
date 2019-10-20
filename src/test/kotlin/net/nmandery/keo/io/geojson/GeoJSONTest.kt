package net.nmandery.keo.io.geojson

import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.matchers.string.shouldNotBeEmpty
import io.kotlintest.matchers.types.shouldNotBeNull
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import org.locationtech.jts.geom.*
import org.locationtech.jts.geom.impl.CoordinateArraySequenceFactory
import org.locationtech.jts.io.WKTReader

class Animal(
    var name: String = "",
    var age: Int = 0
)

@JsonPropertyOrder("type", "id", "geometry", "properties")
@JsonTypeName("Feature")
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
    property = "type"
)
class AnimalFeature : Feature<Point, Animal> {
    constructor(location: Point, animal: Animal) : super(location, animal)
    constructor() : super()
}

@JsonPropertyOrder("type", "features")
@JsonTypeName("FeatureCollection")
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
    property = "type"
)
class AnimalFeatureCollection : FeatureCollection<AnimalFeature> {
    constructor(features: List<AnimalFeature>) : super(features)
    constructor() : super()
}


class GeoJSONTest : StringSpec({

    val gf = GeometryFactory()

    fun makePoint(x: Double, y: Double) =
        Point(CoordinateArraySequenceFactory.instance().create(arrayOf(Coordinate(x, y))), gf)

    fun objectMapper(): ObjectMapper {
        val om = jacksonObjectMapper()
        om.registerJTSGeoJSON()
        return om
    }

    fun testUsingWkt(wkt: String) {
        val wktReader = WKTReader()
        val geomIn = wktReader.read(wkt)
        geomIn.shouldNotBeNull()

        val om = objectMapper()
        val geomSerialized = om.writeValueAsString(geomIn)
        geomSerialized.shouldNotBeEmpty()

        val geomOut = om.readValue(geomSerialized, geomIn.javaClass)
        geomOut.shouldNotBeNull()
        if (geomIn is GeometryCollection) {
            (geomOut is GeometryCollection).shouldBe(true)
            val gcOut = geomOut as GeometryCollection
            gcOut.numGeometries.shouldBe(geomIn.numGeometries)
            (0 until geomIn.numGeometries)
                .forEach { gcOut.getGeometryN(it).shouldBe(geomIn.getGeometryN(it)) }
        } else {
            geomOut.shouldBe(geomIn)
            geomOut.equals(geomIn).shouldBe(true)
        }
    }

    "point" {
        testUsingWkt("POINT(15 20)")
    }

    "linestring" {
        testUsingWkt("LINESTRING(0 0, 10 10, 20 25, 50 60)")
    }

    "polygon" {
        testUsingWkt("POLYGON((0 0,10 0,10 10,0 10,0 0),(5 5,7 5,7 7,5 7, 5 5))")
    }

    "multipoint" {
        testUsingWkt("MULTIPOINT(0 0, 20 20, 60 60)")
    }

    "multilinestring" {
        testUsingWkt("MULTILINESTRING((10 10, 20 20), (15 15, 30 15))")
    }

    "multipolygon" {
        testUsingWkt("MULTIPOLYGON(((0 0,10 0,10 10,0 10,0 0)),((5 5,7 5,7 7,5 7, 5 5)))")
    }

    "geometrycollection" {
        testUsingWkt("GEOMETRYCOLLECTION(POINT(10 10), POINT(30 30), LINESTRING(15 15, 20 20))")
    }

    fun compareAnimalFeatures(f: AnimalFeature, expected: AnimalFeature) {
        expected.shouldNotBeNull()
        expected.geometry?.shouldBe(f.geometry)
        expected.properties?.name.shouldBe(f.properties?.name)
        expected.properties?.age.shouldBe(f.properties?.age)
    }

    "feature" {
        val dog = AnimalFeature(
            makePoint(32.6, 12.3),
            Animal("Brutus", 4)
        )
        val om = objectMapper()
        val dogSerialized = om.writeValueAsString(dog)
        dogSerialized.shouldContain(""""type":"Feature"""")

        val dogDeserialized = om.readValue(dogSerialized, AnimalFeature::class.java)
        compareAnimalFeatures(dog, dogDeserialized)
    }

    "featurecollection" {
        val fc = AnimalFeatureCollection(
            listOf(
                AnimalFeature(
                    makePoint(32.6, 12.3),
                    Animal("Brutus", 4)
                ),
                AnimalFeature(
                    makePoint(45.1, 19.8),
                    Animal("Tweety", 2)

                )
            )
        )

        val om = objectMapper()
        val fcSerialized = om.writeValueAsString(fc)
        fcSerialized.shouldContain(""""type":"Feature"""")
        fcSerialized.shouldContain(""""type":"FeatureCollection"""")

        val fcDeserialized = om.readValue(fcSerialized, AnimalFeatureCollection::class.java)
        fcDeserialized.size().shouldBe(fc.size())
        (0 until fc.size())
            .forEach { compareAnimalFeatures(fcDeserialized.features[it], fc.features[it]) }
    }

    "coordinate deserialize 2d" {
        val x = 45.3
        val y = 23.6
        val om = objectMapper()
        val c = om.readValue("[$x, $y]", Coordinate::class.java)
        c.shouldNotBeNull()
        c.getX().shouldBe(x)
        c.getY().shouldBe(y)
        c.getZ().isNaN().shouldBe(true)
    }

    "coordinate deserialize 3d" {
        val x = 45.3
        val y = 23.6
        val z = 12.3
        val om = objectMapper()
        val c = om.readValue("[$x, $y, $z]", Coordinate::class.java)
        c.shouldNotBeNull()
        c.getX().shouldBe(x)
        c.getY().shouldBe(y)
        c.getZ().shouldBe(z)
    }

    "coordinatesequence deserialize 2d" {
        val x1 = 45.3
        val y1 = 23.6
        val x2 = 45.3
        val y2 = 23.6
        val om = objectMapper()
        val cs = om.readValue("[[$x1, $y1], [$x2, $y2]]", CoordinateSequence::class.java)
        cs.shouldNotBeNull()
        cs.size().shouldBe(2)
        cs.getCoordinate(0).getX().shouldBe(x1)
        cs.getCoordinate(0).getY().shouldBe(y1)
        cs.getCoordinate(0).getZ().isNaN().shouldBe(true)
        cs.getCoordinate(1).getX().shouldBe(x2)
        cs.getCoordinate(1).getY().shouldBe(y2)
        cs.getCoordinate(1).getZ().isNaN().shouldBe(true)
    }

    "coordinatesequence deserialize 3d" {
        val x1 = 45.3
        val y1 = 23.6
        val z1 = 12.3
        val x2 = 45.3
        val y2 = 23.6
        val z2 = 12.3
        val om = objectMapper()
        val cs = om.readValue("[[$x1, $y1, $z1], [$x2, $y2, $z2]]", CoordinateSequence::class.java)
        cs.shouldNotBeNull()
        cs.size().shouldBe(2)
        cs.getCoordinate(0).getX().shouldBe(x1)
        cs.getCoordinate(0).getY().shouldBe(y1)
        cs.getCoordinate(0).getZ().shouldBe(z1)
        cs.getCoordinate(1).getX().shouldBe(x2)
        cs.getCoordinate(1).getY().shouldBe(y2)
        cs.getCoordinate(1).getZ().shouldBe(z2)
    }

    "coordinate serialize 2d" {
        val c = Coordinate(23.4, 12.3)
        val om = objectMapper()
        om.writeValueAsString(c).shouldBe("[23.4,12.3]")
    }

    "coordinate serialize 3d" {
        val c = Coordinate(23.4, 12.3, 1.3)
        val om = objectMapper()
        om.writeValueAsString(c).shouldBe("[23.4,12.3,1.3]")
    }

    "coordinatesequence serialize 2d" {
        val cs = gf.coordinateSequenceFactory.create(
            arrayOf(
                Coordinate(23.4, 12.3), Coordinate(33.4, 22.3)
            )
        )
        val om = objectMapper()
        om.writeValueAsString(cs).shouldBe("[[23.4,12.3],[33.4,22.3]]")
    }

    "coordinatesequence serialize 3d" {
        val cs = gf.coordinateSequenceFactory.create(
            arrayOf(
                Coordinate(23.4, 12.3, 1.3), Coordinate(33.4, 22.3, 11.3)
            )
        )
        val om = objectMapper()
        om.writeValueAsString(cs).shouldBe("[[23.4,12.3,1.3],[33.4,22.3,11.3]]")
    }
})
