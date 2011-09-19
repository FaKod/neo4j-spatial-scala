package org.neo4j.scala


import org.neo4j.gis.spatial._
import collection.mutable.Buffer
import com.vividsolutions.jts.geom._
import collection.JavaConversions._
import query.{SearchWithinDistance, SearchWithin}
import util.{AddGeometry, Coord}
import org.neo4j.graphdb.{PropertyContainer, Node, GraphDatabaseService}

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
   * *****
   * Search convenience defs
   * *****
   */

  def withSearchWithinDistance[T](point: Point, distance: Double)(operation: (SearchWithinDistance) => T): T = {
    val search = new SearchWithinDistance(point, distance)
    operation(search)
  }

  def searchWithinDistance(point: Point, distance: Double)(implicit layer: EditableLayer) = {
    val search = new SearchWithinDistance(point, distance)
    layer.getIndex.executeSearch(search)
    val result: Buffer[SpatialDatabaseRecord] = search.getResults
    result
  }

  /**
   * handles the searches with one Geometry parameter
   * f.e. SearchWithin
   */
  def search[T <: AbstractSearch](geometry: Geometry)(implicit layer: EditableLayer, m: ClassManifest[T]): Buffer[SpatialDatabaseRecord] = {
    val ctor = m.erasure.getConstructor(classOf[Geometry])
    val search = ctor.newInstance(geometry).asInstanceOf[T]
    layer.getIndex.executeSearch(search)
    search.getResults
  }

  /**
   * more functional convenience method to handle searches with one
   * Geometry parameter f.e. SearchWithin
   */
  def withSearch[T <: AbstractSearch](geometry: Geometry)(operation: (AbstractSearch) => Unit)(implicit m: ClassManifest[T]): Unit = {
    val ctor = m.erasure.getConstructor(classOf[Geometry])
    val search = ctor.newInstance(geometry).asInstanceOf[T]
    operation(search)
  }

  def executeSearch(implicit search: Search, layer: EditableLayer):Unit = layer.getIndex.executeSearch(search)

  /**
   * returns the result set of the spatial search as Scala Buffer
   */
  def getResults(implicit search: Search): Buffer[SpatialDatabaseRecord] = search.getResults

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

  //def executeSearch(search: Search)(implicit layer: EditableLayer): Unit = layer.getIndex.executeSearch(search)

  def add(implicit layer: EditableLayer) = new AddGeometry(layer)

}

/**
 * container trait to hold an instance of SpatialDatabaseRecord
 */
trait IsSpatialDatabaseRecord {
  val node: SpatialDatabaseRecord
}