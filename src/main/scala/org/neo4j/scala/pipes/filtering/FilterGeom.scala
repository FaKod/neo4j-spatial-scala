package org.neo4j.scala.pipes.filtering

import com.vividsolutions.jts.geom.{Envelope, GeometryFactory, Geometry}
import org.neo4j.gis.spatial.pipes.filtering._

/**
 * Created with IntelliJ IDEA.
 * User: stefan
 * Date: 27.06.12
 * Time: 06:29
 *
 * Convenience objects for handling Filters
 *
 * For more detailed information for the wrapped filters
 * take a look to neo4j-spatial
 *
 */


/**
 * FilterContain
 * Find geometries that contain the given geometry
 */
object FilterContain {
  def apply(geom: Geometry) = new FilterContain(geom)
}

/**
 * FilterCover
 * Find geometries that covers the given geometry
 */
object FilterCover {
  def apply(geom: Geometry) = new FilterCover(geom)
}

/**
 * FilterCoveredBy
 * Find geometries covered by the given geometry
 */
object FilterCoveredBy {
  def apply(geom: Geometry) = new FilterCoveredBy(geom)
}

/**
 * FilterCross
 * Find geometries that have some but not all interior points in common with the given geometry
 */
object FilterCross {
  def apply(geom: Geometry) = new FilterCross(geom)
}

/**
 * FilterDisjoint
 * Find geometries that have no point in common with the given geometry
 */
object FilterDisjoint {
  def apply(geom: Geometry) = new FilterDisjoint(geom)
}

/**
 * FilterEqualExact
 * Find geometries equal to the given geometry
 */
object FilterEqualExact {
  def apply(geom: Geometry) = new FilterEqualExact(geom)
}

/**
 * FilterEqualNorm
 * Find geometries equal to the given geometry (with the same number of vertices, in the same locations)
 */
object FilterEqualNorm {
  def apply(geom: Geometry) = new FilterEqualNorm(geom)
}

/**
 * FilterEqualTopo
 * Find geometries equal to the given geometry
 */
object FilterEqualTopo {
  def apply(geom: Geometry) = new FilterEqualTopo(geom)
}

/**
 * FilterIntersect
 * Find geometries that intersects the given geometry.
 */
object FilterIntersect {
  def apply(geom: Geometry) = new FilterIntersect(geom)
}

/**
 * FilterOverlap
 * Find geometries that overlap the given geometry
 */
object FilterOverlap {
  def apply(geom: Geometry) = new FilterOverlap(geom)
}

/**
 * FilterTouch
 * Find geometries that touch the given geometry.
 */
object FilterTouch {
  def apply(geom: Geometry) = new FilterTouch(geom)
}

/**
 * FilterWithin
 * Find geometries that are within the given geometry.
 */
object FilterWithin {
  def apply(geom: Geometry) = new FilterWithin(geom)
}

/**
 * FilterIntersectWindow
 * Find geometries that intersects the given rectangle
 */
object FilterIntersectWindow {
  def apply(xmin: Double, ymin: Double, xmax: Double, ymax: Double)(implicit geomFac: GeometryFactory) = {
    new FilterIntersectWindow(geomFac, xmin, ymin, xmax, ymax)
  }
  def apply(env: Envelope)(implicit geomFac: GeometryFactory) = {
    new FilterIntersectWindow(geomFac, env)
  }
}

/**
 * FilterInRelation
 * Returned geometries have the specified relation with the given geometry
 */
object FilterInRelation {
  def apply(geom: Geometry, intersectionPattern: String) = new FilterInRelation(geom, intersectionPattern)
}





