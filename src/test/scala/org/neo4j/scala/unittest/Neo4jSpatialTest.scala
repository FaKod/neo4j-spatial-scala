package org.neo4j.scala.unittest

import org.neo4j.gis.spatial.query.SearchWithin
import collection.mutable.Buffer
import org.neo4j.gis.spatial.NullListener
import com.vividsolutions.jts.geom.Envelope
import collection.JavaConversions._
import org.neo4j.graphdb.Direction
import org.specs2.mutable.SpecificationWithJUnit
import org.neo4j.scala.util.LinRing
import org.neo4j.scala.{SimpleSpatialDatabaseServiceProvider, EmbeddedGraphDatabaseServiceProvider, Neo4jSpatialWrapper}

class Neo4jSpatialSpec extends SpecificationWithJUnit with Neo4jSpatialWrapper with EmbeddedGraphDatabaseServiceProvider with SimpleSpatialDatabaseServiceProvider {

  def neo4jStoreDir = "/tmp/temp-neo-spatial-test"

  "NeoSpatialWrapper" should {

    Runtime.getRuntime.addShutdownHook(new Thread() {
      override def run() {
        ds.gds.shutdown
      }
    })

    "allow usage of Neo4jWrapper" in {

      withSpatialTx {
        implicit db =>

        val start = createNode
        val end = createNode
        start --> "foo" --> end
        start.getSingleRelationship("foo", Direction.OUTGOING).getOtherNode(start) must beEqualTo(end)
      }
    }

    "simplify layer, node and search usage" in {

      withSpatialTx {
        implicit db =>

        // remove existing layer
          try {
            deleteLayer("test", new NullListener)
          }
          catch {
            case _ =>
          }

        val cities = createNode
        val federalStates = createNode

        withLayer(getOrCreateEditableLayer("test")) {
          implicit layer =>

          // adding Point
          val munich = add newPoint ((15.3, 56.2))
          munich("City") = "Munich"
          cities --> "isCity" --> munich

          // adding new Polygon
          val bayernBuffer = Buffer[(Double, Double)]((15, 56), (16, 56), (15, 57), (16, 57), (15, 56))
          val bayern = add newPolygon (LinRing(bayernBuffer))
          bayern("FederalState") = "Bayern"
          federalStates --> "isFederalState" --> bayern

          munich --> "CapitalCityOf" --> bayern

          withSearch[SearchWithin](bayern.getGeometry) {
            implicit s =>
              executeSearch
            for (r <- getResults)
              r.getProperty("City") must beEqualTo("Munich")
          }

          withSearch[SearchWithin](toGeometry(new Envelope(15.0, 16.0, 56.0, 57.0))) {
            implicit s =>
              executeSearch
            getResults.size must_== 2
          }
        }
      }
      success
    }
  }
}