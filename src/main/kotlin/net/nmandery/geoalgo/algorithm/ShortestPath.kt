package net.nmandery.geoalgo.algorithm

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
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

fun List<Coordinate>.optimizeOrderingForShortestPath(maxIterations: Int = 50): Option<List<Coordinate>> {

    if (isEmpty()) {
        return Some(emptyList())
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

    val solution = Solutions.bestOf(vra.searchSolutions()) ?: return None

    // SolutionPrinter.print(solution)

    // in case there are nodes not assigned, the ordering was not successful
    if (solution.unassignedJobs?.isEmpty() != true) {
        return None
    }
    return Some(solution.routes
        .flatMap { it.activities.mapNotNull { it.location?.coordinate?.toJts() } })
}

fun CoordinateSequence.optimizeOrderingForShortestPath(maxIterations: Int = 50) =
    when (val l = toCoordinateArray().asList().optimizeOrderingForShortestPath(maxIterations = maxIterations)) {
        is Some -> Some(CoordinateArraySequence(l.t.toTypedArray()))
        is None -> None
    }

fun List<Point>.optimizeOrderingForShortestPath(gf: GeometryFactory, maxIterations: Int = 50) =
    when (val l = mapNotNull { it.coordinate }.optimizeOrderingForShortestPath(maxIterations = maxIterations)) {
        is Some -> Some(l.t.mapNotNull { Point(CoordinateArraySequence(arrayOf(it)), gf) })
        is None -> None
    }

fun LineString.optimizeOrderingForShortestPath(gf: GeometryFactory, maxIterations: Int = 50) =
    when (val l = coordinateSequence.optimizeOrderingForShortestPath(maxIterations = maxIterations)) {
        is Some -> Some(LineString(l.t, gf))
        is None -> None
    }

