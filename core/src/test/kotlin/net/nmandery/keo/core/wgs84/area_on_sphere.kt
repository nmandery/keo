package net.nmandery.keo.core.wgs84

import io.kotlintest.matchers.doubles.shouldBeBetween
import io.kotlintest.specs.StringSpec
import org.locationtech.jts.geom.Polygon
import org.locationtech.jts.io.WKTReader

class AreaOnSphereTest : StringSpec({

    "area polygon" {
        val wkt = "POLYGON((-71.1776848522251 42.3902896512902,-71.1776843766326 42.3903829478009," +
                "-71.1775844305465 42.3903826677917,-71.1775825927231 42.3902893647987,-71.1776848522251 42.3902896512902))"
        val geom = WKTReader().read(wkt) as Polygon
        geom.areaOnSphereApprox().shouldBeBetween(86.1, 86.4, 0.0)

        /*
        Result with PostGIS:

        # select st_area(st_geomfromewkt('SRID=4326;POLYGON((-71.1776848522251 42.3902896512902,-71.1776843766326 42.3903829478009,-71.1775844305465 42.3903826677917,-71.1775825927231 42.3902893647987,-71.1776848522251 42.3902896512902))')::geography);
        st_area
        ------------------
        86.2776043117046
        (1 row)
        */
    }
})