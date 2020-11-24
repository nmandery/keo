package net.nmandery.keo.core

import io.kotlintest.matchers.sequences.shouldContain
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import org.locationtech.jts.geom.Envelope

class GridTest : StringSpec({

    "empty envelope" {
        Envelope(10.0, 10.0, 20.0, 30.0)
            .makeGrid(10, 10)
            .shouldBe(emptySequence())

        Envelope(10.0, 10.0, 20.0, 30.0)
            .makeGridUsingCellSize(2.0, 2.0)
            .shouldBe(emptySequence())
    }

    "simple" {
        Envelope(10.0, 20.0, 20.0, 30.0)
            .makeGrid(10, 10)
            .also {
                it.shouldContain(Envelope(11.0, 12.0, 22.0, 23.0))
            }
            .count().shouldBe(100)
    }

    "sliceBy" {
        Envelope(10.0, 30.0, 20.0, 30.0)
            .toPolygon()
            .sliceBy(
                Envelope(0.0, 20.0, 20.0, 30.0)
                    .makeGrid(10, 10)
                    .map { it.toPolygon() }
            ).count().shouldBe(50)
    }
})