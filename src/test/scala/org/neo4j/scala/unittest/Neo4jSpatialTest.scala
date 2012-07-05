package org.neo4j.scala.unittest

import org.specs2.mutable.SpecificationWithJUnit
import org.neo4j.scala.util.{Envelope, LinRing}
import java.util.Date
import org.neo4j.scala.{Neo4jWrapper, SimpleSpatialDatabaseServiceProvider, EmbeddedGraphDatabaseServiceProvider, Neo4jSpatialWrapper}
import org.neo4j.graphdb.{Node, Direction}
import org.neo4j.collections.rtree.NullListener
import org.neo4j.gis.spatial.pipes.GeoPipeline
import com.vividsolutions.jts.geom._
import org.neo4j.scala.pipes.filtering.{FilterCoveredBy, FilterWithin}


case class Cities(creationDate: String = new Date().toString)

case class FederalStates(creationDate: String = new Date().toString)

case class City(name: String, creationDate: String = new Date().toString)

case class FederalState(name: String, creationDate: String = new Date().toString)

class Neo4jSpatialSpec extends SpecificationWithJUnit with Neo4jSpatialWrapper with EmbeddedGraphDatabaseServiceProvider with SimpleSpatialDatabaseServiceProvider {

  def neo4jStoreDir = "/tmp/temp-neo-spatial-test"

  private def count(i: GeoPipelineIterator) = {
    val r = for (t <- i) yield t
    r.size
  }

  "NeoSpatialWrapper" should {

    Runtime.getRuntime.addShutdownHook(new Thread() {
      override def run() {
        ds.gds.shutdown
      }
    })

    "allow usage of Neo4jWrapper" in {

      withTx {
        implicit db =>

          val start = createNode
          val end = createNode
          start --> "foo" --> end
          start.getSingleRelationship("foo", Direction.OUTGOING).getOtherNode(start) must beEqualTo(end)
      }
    }

    "allow updates" in {
      withSpatialTx {
        implicit db =>

        // remove existing layer
          try {
            deleteLayer("test", new NullListener)
          }
          catch {
            case _ =>
          }

          withLayer(getOrCreateEditableLayer("test")) {
            implicit layer =>
              val newNode: Node = add newPoint ((0.0, 0.0)) using City("München")

              count(Search within Envelope(15.0, 16.0, 56.0, 57.0)) must beEqualTo(0)

              update(newNode) withPoint ((15.3, 56.2))

              count(Search within Envelope(15.0, 16.0, 56.0, 57.0)) must beEqualTo(1)

              val r = (Search within Envelope(15.0, 16.0, 56.0, 57.0)) +
                (Search within Envelope(15.1, 16.1, 56.1, 57.1))

              count(r) must beEqualTo(1)
          }
      }
      success
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

          val cities = createNode(Cities())
          val federalStates = createNode(FederalStates())

          withLayer(getOrCreateEditableLayer("test")) {
            implicit layer =>

            // adding Point without serialized Case Class
              val munichTMP = add newPoint ((15.3, 56.2))

              // adding Point and Case Class
              val munich = add newPoint ((15.3, 56.2)) using City("Munich")
              munich("Additional Property") = "something"


              // adding new Polygon
              val lineRing = LinRing((15.0, 56.0) ::(16.0, 56.0) ::(15.0, 57.0) ::(16.0, 57.0) ::(15.0, 56.0) :: Nil)
              val bayern = add newPolygon (lineRing) using FederalState("Bayern")

              cities --> "isCity" --> munich
              munich --> "CapitalCityOf" --> bayern
              federalStates --> "isFederalState" --> bayern

              val r = Search within(bayern.getGeometry)

              r.size must beEqualTo(2)

              for (r <- r; c <- r.toCC[City]) {
                var oo = Neo4jWrapper.deSerialize[City](r)
                oo must beEqualTo(c)
                println("oo: " + oo + " c: " + c)
              }

              count(Search within Envelope(15.0, 16.0, 56.0, 57.0)) must beEqualTo(3)
          }
      }
      success
    }

    "test geoPipline with filter" in {

      withSpatialTx {

        implicit db =>

        // remove existing layer
          try {
            deleteLayer("filter", new NullListener)
          }
          catch {
            case _ =>
          }

          withLayer(getOrCreateEditableLayer("filter")) {
            implicit layer =>

              val gf = layer.getGeometryFactory

              //adding 2 Points without serialized Case Class
              val munichTMP = add newPoint ((15.3, 56.2))
              val munich = add newPoint ((15.6, 56.6))

              //searching the points
              implicit val pipeline1 = GeoPipeline.startCoveredBySearch(layer, gf.toGeometry(new Envelope(15, 56, 16, 57)))
              pipeline1.size must_== 2

              //add a Pipe/Filter to the resultset
              import org.neo4j.scala.pipes.filtering.FilterDSL._
              val pipeline2 = filter add FilterCoveredBy (gf.toGeometry(new Envelope(15.5, 56.5, 16.5, 57.5)))

              count(new GeoPipelineIterator(pipeline2)) must_== 1

              //only to show GeoPipeline works
              pipeline2.size() must_== 3

          }
      }
    }

  }
}
