package com.github.nmandery.keo.web.optimize

import io.kotlintest.matchers.types.shouldNotBeNull
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.LineString
import org.locationtech.jts.geom.impl.CoordinateArraySequence

class ShortestPathTest : StringSpec({

    val gf = GeometryFactory()

    "rectangle" {
        val points = listOf(
            Coordinate(0.0, 0.0),
            Coordinate(10.0, 5.0),
            Coordinate(5.0, 5.0),
            Coordinate(0.0, 5.0),
            Coordinate(10.0, 0.0)
        )
        val opti = points.optimizeOrderingForShortestPath(maxIterations = 50)
        opti.shouldNotBeNull()
        LineString(CoordinateArraySequence(opti.toTypedArray()), gf).length.shouldBe(20.0)
    }

})

