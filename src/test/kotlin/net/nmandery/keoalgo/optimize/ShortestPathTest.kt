package net.nmandery.keoalgo.optimize

import arrow.core.None
import arrow.core.Some
import io.kotlintest.fail
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
        when (val opti = points.optimizeOrderingForShortestPath(maxIterations = 50)) {
            is Some -> LineString(CoordinateArraySequence(opti.t.toTypedArray()), gf)
                .length
                .shouldBe(20.0)
            is None -> fail("optimizeNodes returned an empty option")
        }
    }

})

