package net.nmandery.keoalgo.web

import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.Envelope
import kotlin.math.pow


data class Tile(val x: Int, val y: Int, val z: Int)

/*
Resources on tilegrid implementations:

https://github.com/t-rex-tileserver/t-rex/blob/master/tile-grid/src/grid.rs
https://github.com/openlayers/openlayers/blob/master/src/ol/tilegrid/TileGrid.js

 */
class TileGrid(val extend: Envelope, val tileSize: Int = 1024, val maxZoom: Int = 26) {

    val resolutions = resolutionsFromExtent(extend, tileSize, maxZoom)
    val origin = topLeftFromExtend(extend)

    private fun topLeftFromExtend(extend: Envelope) = Coordinate(extend.minX, extend.maxY, 0.0)

    private fun resolutionsFromExtent(extend: Envelope, tileSize: Int, maxZoom: Int): List<Double> {
        val height = extend.maxY - extend.minY
        val width = extend.maxX - extend.minX
        val maxResolution = (width / tileSize.toDouble()).coerceAtLeast(height / tileSize.toDouble())

        return (0..maxZoom)
            .map { maxResolution / 2.0.pow(it.toDouble()) }
            .toList()
    }

    fun getResolution(z: Int) = resolutions[z]

    fun bounds(z: Int, x: Int, y: Int): Envelope {
        val r = getResolution(z)
        val minx = origin.x + (x * tileSize * r)
        val miny = origin.y - ((y + 1) * tileSize * r)
        return Envelope(
            minx,
            minx + (tileSize * r),
            miny,
            miny + (tileSize * r)
        )
    }

    fun bounds(tile: Tile) = bounds(tile.z, tile.x, tile.y)

    /**
     * extend in number of tiles for the given z
     */
    fun tileExtend(z: Int): Pair<Int, Int> {
        val r = getResolution(z)
        return Pair(
            ((extend.maxX - extend.minX) / (tileSize * r)).toInt(),
            ((extend.maxY - extend.minY) / (tileSize * r)).toInt()
        )
    }
}