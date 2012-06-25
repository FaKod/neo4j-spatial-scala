package org.neo4j.scala


import org.neo4j.gis.spatial._
import com.vividsolutions.jts.geom._
import pipes.GeoPipeline
import org.neo4j.graphdb.{Node, GraphDatabaseService}
import util.{UpdateGeometry, AddGeometry, Coord}
import org.neo4j.collections.rtree.Listener


/**
 * Util and implicit Trait for spatial stuff
 * extended by spatial wrapper
 */
trait Neo4jSpatialWrapperUtil {

  object Search {
    def within(geometry: Geometry)(implicit layer: EditableLayer) =
      new GeoPipelineIterator(GeoPipeline.startWithinSearch(layer, geometry))

    def withinDistance(geometry: Geometry, distance: Double)(implicit layer: EditableLayer) =
      new GeoPipelineIterator(GeoPipeline.startNearestNeighborSearch(layer, geometry.getCoordinate, distance))

    def coveredBy(geometry: Geometry)(implicit layer: EditableLayer) =
      new GeoPipelineIterator(GeoPipeline.startCoveredBySearch(layer, geometry))

    def intersect(geometry: Geometry)(implicit layer: EditableLayer) =
      new GeoPipelineIterator(GeoPipeline.startIntersectSearch(layer, geometry))
  }

  /**
   * Abstract trait for the different search classes
   */
  abstract trait Search {
    val geometry: Geometry
  }

  /**
   * Maps GeoPipeline to Scala Iterator[SpatialDatabaseRecord]
   * @todo Can the iteration be a list of SpatialDatabaseRecords?
   *
   * @param gp GeoPipeline
   */
  class GeoPipelineIterator(private val gp: GeoPipeline) extends Iterator[SpatialDatabaseRecord] {
    def hasNext = gp.hasNext

    def next() = gp.next().getRecord

    /**
     * allows to append search method calls
     * @param that other GeoPipelineIterator
     * @return GeoPipelineIterator this
     */
    def +(that: GeoPipelineIterator) = {
      gp.addPipe(that.gp)
      this
    }
  }

  /**
   * creates a Scala Iterator from GeoPipeline
   */
  implicit def geoPipelineToIterator(gp: GeoPipeline): Iterator[SpatialDatabaseRecord] = new GeoPipelineIterator(gp)

  /**
   * allows to append search method calls
   * @param gp GeoPipeline
   * @return GeoPipeline old pipeline
   */
  implicit def richGeoPipeline(gp: GeoPipeline) = new {
    def +(that: GeoPipeline) = {
      gp.addPipe(that)
      gp
    }
  }

  /**
   * node convenience defs
   */

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

  implicit def toGeometry(envelope: Envelope)(implicit layer: EditableLayer): Geometry =
    getGeometryFactory.toGeometry(envelope)

  //def executeSearch(search: Search)(implicit layer: EditableLayer): Unit = layer.getIndex.executeSearch(search)

  def add(implicit layer: EditableLayer) = new AddGeometry(layer)

  def update(node: Node)(implicit layer: EditableLayer) = new UpdateGeometry(node, layer)

}

/**
 * container trait to hold an instance of SpatialDatabaseRecord
 */
trait IsSpatialDatabaseRecord {
  val node: SpatialDatabaseRecord
}