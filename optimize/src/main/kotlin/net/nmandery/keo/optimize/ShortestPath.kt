package net.nmandery.keo.optimize

import com.graphhopper.jsprit.core.algorithm.box.Jsprit
import com.graphhopper.jsprit.core.problem.Location
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem
import com.graphhopper.jsprit.core.problem.job.Service
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl
import com.graphhopper.jsprit.core.util.ManhattanCosts
import com.graphhopper.jsprit.core.util.Solutions
import org.locationtech.jts.geom.*
import org.locationtech.jts.geom.impl.CoordinateArraySequence
import com.graphhopper.jsprit.core.util.Coordinate as JspritCoordinate

private fun Coordinate.toJsprit() = JspritCoordinate.newInstance(x, y)

private fun JspritCoordinate.toJts() = Coordinate(x, y)

/**
 * reorder the Coordinates in the List to form the shortest possible path
 */
fun List<Coordinate>.optimizeOrderingForShortestPath(maxIterations: Int = 50): List<Coordinate>? {
    if (isEmpty()) {
        return emptyList()
    }

    // based on
    // https://github.com/graphhopper/jsprit/blob/master/jsprit-examples/src/main/java/com/graphhopper/jsprit/examples/CircleExample.java

    val builder = VehicleRoutingProblem.Builder.newInstance()
    val vehicle = VehicleImpl.Builder.newInstance("v")
        .setStartLocation(
            Location.Builder.newInstance().setCoordinate(first().toJsprit()).build()
        )
        .setType(
            VehicleTypeImpl.Builder.newInstance("t")
                .setCostPerDistance(10.0)
                .build()
        )
        .build()
    builder.addVehicle(vehicle)

    for (indexedC in withIndex()) {
        val service = Service.Builder.newInstance(indexedC.index.toString())
            .setLocation(
                Location.Builder.newInstance()
                    .setCoordinate(indexedC.value.toJsprit())
                    .build()
            )
            .build()
        builder.addJob(service)
    }
    builder.setRoutingCost(ManhattanCosts())

    val vrp = builder.build()
    val vra = Jsprit.createAlgorithm(vrp)
    vra.maxIterations = maxIterations

    val solution = Solutions.bestOf(vra.searchSolutions()) ?: return null

    // SolutionPrinter.print(solution)

    // in case there are nodes not assigned, the ordering was not successful
    if (solution.unassignedJobs?.isEmpty() != true) {
        return null
    }
    return solution.routes
        .flatMap { it.activities.mapNotNull { it.location?.coordinate?.toJts() } }
}

/**
 * reorder the Coordinates in the CoordinateSequence to form the shortest possible path
 */
fun CoordinateSequence.optimizeOrderingForShortestPath(maxIterations: Int = 50): CoordinateArraySequence? {
    val optimized = toCoordinateArray().asList().optimizeOrderingForShortestPath(maxIterations = maxIterations)
    return if (optimized != null) {
        CoordinateArraySequence(optimized.toTypedArray())
    } else {
        null
    }
}

/**
 * reorder the Points in the List to form the shortest possible path
 */
fun List<Point>.optimizeOrderingForShortestPath(gf: GeometryFactory, maxIterations: Int = 50): List<Point>? {
    val l = mapNotNull { it.coordinate }.optimizeOrderingForShortestPath(maxIterations = maxIterations)
    return l?.map { Point(CoordinateArraySequence(arrayOf(it)), gf) }
}


/**
 * reorder the Points in the LineString to form the shortest possible path
 */
fun LineString.optimizeOrderingForShortestPath(gf: GeometryFactory, maxIterations: Int = 50): LineString? {
    val l = coordinateSequence.optimizeOrderingForShortestPath(maxIterations = maxIterations)
    return if (l != null) {
        LineString(l, gf)
    } else {
        null
    }
}

