package net.nmandery.keo.core

import org.locationtech.jts.geom.Envelope

/**
 * create a grid in the envelope
 *
 * The grid will be nCellsX wide and nCellsY high
 */
fun Envelope.makeGrid(nCellsX: Long, nCellsY: Long): Sequence<Envelope> {
    if (area == 0.0) {
        return emptySequence()
    }
    val cellWidth = width / nCellsX.toDouble()
    val cellHeight = height / nCellsY.toDouble()
    return (0 until nCellsX).asSequence()
        .flatMap { x ->
            (0 until nCellsY).asSequence()
                .map { y ->
                    Envelope(
                        minX + (x * cellWidth),
                        minX + ((x + 1) * cellWidth),

                        minY + (y * cellHeight),
                        minY + ((y + 1) * cellHeight),
                    )
                }
        }
}


fun Envelope.makeGridUsingCellSize(xSize: Double, ySize: Double) =
    makeGrid(
        (width / xSize).coerceAtLeast(1.0).toLong(),
        (height / ySize).coerceAtLeast(1.0).toLong()
    )