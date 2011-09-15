package org.neo4j.scala

import org.neo4j.gis.spatial.SpatialDatabaseService
import org.neo4j.graphdb.GraphDatabaseService

/**
 * SpatialDatabaseServiceProvider Interface, needs an instance of
 * DatabaseService and SpatialDatabaseService
 */
trait SpatialDatabaseServiceProvider {
  self: GraphDatabaseServiceProvider =>

  val sds:SpatialDatabaseService
}

/**
 * provides a simple spatial database service from a given GraphDatabaseService
 */
trait SimpleSpatialDatabaseServiceProvider {
  self: GraphDatabaseServiceProvider =>

  val sds = new SpatialDatabaseService(ds.gds)
}

/**
 * extended store for combined GraphDatabaseService and SpatialDatabaseService
 * used by Neo4jSpatialWrapper
 */
case class CombinedDatabaseService(gds: GraphDatabaseService, sds: SpatialDatabaseService) extends DatabaseService