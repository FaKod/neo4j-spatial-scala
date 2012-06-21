package org.neo4j.scala.unittest


import com.vividsolutions.jts.geom.Envelope
import org.specs2.mutable.SpecificationWithJUnit
import org.neo4j.scala.util.LinRing
import java.util.Date
import org.neo4j.scala._
import org.neo4j.graphdb.{Node, Direction}
import org.neo4j.collections.rtree.NullListener


case class Cities(creationDate: String = new Date().toString)

case class FederalStates(creationDate: String = new Date().toString)

case class City(name: String, creationDate: String = new Date().toString)

case class FederalState(name: String, creationDate: String = new Date().toString)

class Neo4jSpatialSpec extends SpecificationWithJUnit with Neo4jSpatialWrapper with EmbeddedGraphDatabaseServiceProvider with SimpleSpatialDatabaseServiceProvider {

  def neo4jStoreDir = "/tmp/temp-neo-spatial-test"

  import GeoPiplineImplicits._

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

              search(Within(toGeometry(new Envelope(15.0, 16.0, 56.0, 57.0)))) match {
                case Right(x) => x.toSDRList.size must beEqualTo (0)
              }

              update(newNode) withPoint ((15.3, 56.2))

              search(Within(toGeometry(new Envelope(15.0, 16.0, 56.0, 57.0)))) match {
                case Right(x) => x.toSDRList.size must beEqualTo(1)
              }
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

              val list = search(Within(bayern.getGeometry)) match {
                case Right(x) => x.toSDRList
              }

              list.size must beEqualTo(2)

              for (r <- list; c <- r.toCC[City]) {
                var oo = Neo4jWrapper.deSerialize[City](r)
                oo must beEqualTo(c)
                println("oo: " + oo + " c: " + c)
              }

              search(Within(toGeometry(new Envelope(15.0, 16.0, 56.0, 57.0)))) match {
                case Right(x) => x.toSDRList.size must beEqualTo(3)
              }
          }
      }
      success
    }
  }
}