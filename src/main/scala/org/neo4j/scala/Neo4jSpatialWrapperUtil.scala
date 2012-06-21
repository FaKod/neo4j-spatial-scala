package org.neo4j.scala


import org.neo4j.gis.spatial._
import collection.mutable.Buffer
import com.vividsolutions.jts.geom._
import collection.JavaConversions._
import pipes.GeoPipeline
import org.neo4j.graphdb.{PropertyContainer, Node, GraphDatabaseService}
import util.{UpdateGeometry, AddGeometry, Coord}
import org.neo4j.collections.rtree.Listener

/**
 * Util and implicit Trait for spatial stuff
 * extended by spatial wrapper
 *
 * @author Christopher Schmidt
 * Date: 14.04.11
 * Time: 06:15
 */
trait Neo4jSpatialWrapperUtil {


  /**
   *
   * @param search
   * @param layer
   * @tparam A :< Search
   * @return
   */
  def search[A <: Search](search: A)(implicit layer: EditableLayer): Either[Throwable, GeoPipeline] = {
    search match {
      case s: Within => Right(GeoPipeline.startWithinSearch(layer, s.geometry))
      case s: WithinDistance => Right(GeoPipeline.startNearestNeighborSearch(layer, s.geometry.getCoordinate, s.distance))
      case s: CoveredBy => Right(GeoPipeline.startCoveredBySearch(layer, s.geometry))
      case s: Intersect => Right(GeoPipeline.startIntersectSearch(layer, s.geometry))
      case _ => Left(throw new IllegalArgumentException("unsupported search type"))
    }
  }

  implicit def IsSpatialDatabaseRecordToNode(r: IsSpatialDatabaseRecord): Node = r.node.getGeomNode

  implicit def record2relationshipBuilder(record: IsSpatialDatabaseRecord) =
    new NodeRelationshipMethods(record.node)

  /**
   * uses RichPropertyContainer with a SpatialDatabaseRecord to allow f.e.
   * <code>node("key") = value</code>
   */
  implicit def propertyContainer2RichPropertyContainer(spatialDatabaseRecord: SpatialDatabaseRecord) =
    new RichPropertyContainer(spatialDatabaseRecord)


  /**
   * Database Record convenience defs
   */

  // converts SpatialDatabaseRecord to Node
  implicit def spatialDatabaseRecordToNode(sdr: SpatialDatabaseRecord): Node = sdr.getGeomNode

  // delegation to Neo4jWrapper
  implicit def node2relationshipBuilder(sdr: SpatialDatabaseRecord) = new NodeRelationshipMethods(sdr.getGeomNode)

  // allows <code> val munich = add newPoint ((15.3, 56.2)) using City() </code>
  implicit def node2Serialization(sdr: SpatialDatabaseRecord) = new SpatialDatabaseRecordSerializator(sdr)

  implicit def nodeToSpatialDatabaseRecord(node: Node)(implicit layer: Layer): SpatialDatabaseRecord =
    new SpatialDatabaseRecord(layer, node)

  /**
   * delegates to Neo4jWrapper.toCC[T]
   */
  implicit def nodeToCaseClass(sdr: SpatialDatabaseRecord) = new {
    def toCC[T: Manifest] = Neo4jWrapper.toCC[T](sdr.getGeomNode)
  }

  /**
   * DatabaseService Wrapper
   */

  def deleteLayer(name: String, monitor: Listener)(implicit db: CombinedDatabaseService) =
    db.sds.deleteLayer(name, monitor)

  def getOrCreateEditableLayer(name: String)(implicit db: CombinedDatabaseService): EditableLayer =
    db.sds.getOrCreateEditableLayer(name)

  def createEditableLayer(name: String)(implicit db: CombinedDatabaseService): EditableLayer =
    db.sds.createLayer(name, classOf[WKBGeometryEncoder], classOf[EditableLayerImpl]).asInstanceOf[EditableLayer]

  def getLayerNames(implicit db: CombinedDatabaseService) = db.sds.getLayerNames

  def getLayer(name: String)(implicit db: CombinedDatabaseService): Layer =
    db.sds.getLayer(name)

  /**
   * methods from Neo4jWrapper usage should still be possible
   */
  implicit def databaseServiceToGraphDatabaseService(ds: CombinedDatabaseService): GraphDatabaseService = ds.gds

  /**
   * Layer Wrapper
   */

  implicit def tupleToCoordinate(t: (Double, Double)): Coordinate = Coord(t._1, t._2)

  def getGeometryFactory(implicit layer: EditableLayer) = layer.getGeometryFactory

  def toGeometry(envelope: Envelope)(implicit layer: EditableLayer): Geometry = getGeometryFactory.toGeometry(envelope)

  def add(implicit layer: EditableLayer) = new AddGeometry(layer)

  def update(node : Node)(implicit layer: EditableLayer) = new UpdateGeometry(node, layer)

}

/**
 * container trait to hold an instance of SpatialDatabaseRecord
 */
trait IsSpatialDatabaseRecord {
  val node: SpatialDatabaseRecord
}


/**
 * Abstract class for the different search classes
 */
abstract trait Search {
  val geometry: Geometry
}

/*
 * case classes for the different searchtypes
 */
case class Within(geometry: Geometry) extends Search
case class WithinDistance(geometry: Geometry, distance: Double) extends Search
case class CoveredBy(geometry: Geometry) extends Search
case class Intersect(geometry: Geometry) extends Search

object GeoPiplineImplicits {
  implicit def geoPipelineToBuffer(pipeline: GeoPipeline) = new {
    def toSDRBuffer: Buffer[SpatialDatabaseRecord] = {
      pipeline.toSpatialDatabaseRecordList.toBuffer
    }
  }

  implicit def geoPipelineToList(pipeline: GeoPipeline) = new {
    def toSDRList: List[SpatialDatabaseRecord] = {
      import collection.JavaConversions._
      pipeline.toSpatialDatabaseRecordList.toList
    }
  }
}