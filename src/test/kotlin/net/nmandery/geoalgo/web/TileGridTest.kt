package net.nmandery.geoalgo.web

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import org.locationtech.jts.geom.Envelope


class TileGridTest : StringSpec({
    "wgs84-extend-z1" {
        val tg = TileGrid(Envelope(-180.0, 180.0, -90.0, 90.0))
        tg.bounds(1, 0, 0).shouldBe(Envelope(-180.0, -90.0, 90.0, 0.0))
    }
})
