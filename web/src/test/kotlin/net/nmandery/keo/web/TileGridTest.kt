package net.nmandery.keo.web

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import org.locationtech.jts.geom.Envelope


class TileGridTest : StringSpec({
    "wgs84-z1-bounds" {
        val tg = TileGrid(Envelope(-180.0, 180.0, -90.0, 90.0))
        tg.bounds(1, 0, 0).shouldBe(Envelope(-180.0, 0.0, -90.0, 90.0))
    }

    "wgs84-tileExtend-z5" {
        val tg = TileGrid(Envelope(-180.0, 180.0, -90.0, 90.0))
        tg.tileExtend(5).shouldBe(Pair(32, 16))
    }

    "contains" {
        val tg = TileGrid(Envelope(-180.0, 180.0, -90.0, 90.0), maxZoom = 4)
        tg.contains(Tile(2,3, 3)).shouldBe(true)
    }

    "not-contains" {
        val tg = TileGrid(Envelope(-180.0, 180.0, -90.0, 90.0), maxZoom = 4)
        tg.contains(Tile(20,30, 3)).shouldBe(false)
        tg.contains(Tile(2,3, 5)).shouldBe(false)
    }
})
