package org.neo4j.scala.util

import org.neo4j.gis.spatial._
import collection.mutable.Buffer
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence
import com.vividsolutions.jts.geom._
import org.neo4j.graphdb.Node

/**
 * Really simple and very incomplete list of spatial utility classes
 *
 * @author Christopher Schmidt
 */

private[scala] class AddGeometry(layer: EditableLayer) {
  private val gf = layer.getGeometryFactory

  /**
   * Points
   */
  def newPoint(coordinate: Coordinate): SpatialDatabaseRecord = layer.add(gf.createPoint(coordinate))

  def newMultiPoint(points: Array[Point]) = layer.add(gf.createMultiPoint(points))

  def newMultiPoint(coordinates: CoordinateArraySequence) = layer.add(gf.createMultiPoint(coordinates))

  /**
   * Polygon
   */
  def newPolygon(shell: LinearRing, holes: Array[LinearRing] = null) = layer.add(gf.createPolygon(shell, holes))

  def newMultiPolygon(polygons: Array[Polygon]) = layer.add(gf.createMultiPolygon(polygons))

  /**
   * Line String
   */
  def newLineString(coordinates: Array[Coordinate]) = layer.add(gf.createLineString(coordinates))

  def newLineString(coordinates: CoordinateArraySequence) = layer.add(gf.createLineString(coordinates))

  def newMultiLineString(lineStrings: Array[LineString]) = layer.add(gf.createMultiLineString(lineStrings))
}

private[scala] class UpdateGeometry(node: Node, layer: EditableLayer) {
  private val gf = layer.getGeometryFactory

  /**
   * Points
   */
  def withPoint(coordinate: Coordinate): Unit = layer.update(node.getId, gf.createPoint(coordinate))

  def withMultiPoint(points: Array[Point]): Unit = layer.update(node.getId, gf.createMultiPoint(points))

  def withMultiPoint(coordinates: CoordinateArraySequence): Unit = layer.update(node.getId, gf.createMultiPoint(coordinates))

  /**
   * Polygon
   */
  def withPolygon(shell: LinearRing, holes: Array[LinearRing] = null): Unit = layer.update(node.getId, gf.createPolygon(shell, holes))

  def withMultiPolygon(polygons: Array[Polygon]): Unit = layer.update(node.getId, gf.createMultiPolygon(polygons))

  /**
   * Line String
   */
  def withLineString(coordinates: Array[Coordinate]): Unit = layer.update(node.getId, gf.createLineString(coordinates))

  def withLineString(coordinates: CoordinateArraySequence): Unit = layer.update(node.getId, gf.createLineString(coordinates))

  def withMultiLineString(lineStrings: Array[LineString]): Unit = layer.update(node.getId, gf.createMultiLineString(lineStrings))
}


/**
 * convenience object for
 * handling Coordinates
 */
object Coord {
  def apply(x: Double, y: Double) = new Coordinate(x, y)
}

/**
 * convenience object for
 * handling CoordinateArraySequence
 */
object CoordArraySequence {
  def apply(b: Buffer[(Double, Double)]) = {
    val a = for (t <- b; c = Coord(t._1, t._2)) yield c
    new CoordinateArraySequence(a.toArray)
  }
}

/**
 * convenience object for
 * handling LinearRing
 */
object LinRing {
  def apply(cs: CoordinateSequence)(implicit layer: EditableLayer) =
    new LinearRing(cs, layer.getGeometryFactory)

  def apply(b: Buffer[(Double, Double)])(implicit layer: EditableLayer) =
    new LinearRing(CoordArraySequence(b), layer.getGeometryFactory)

  def apply(l: List[(Double, Double)])(implicit layer: EditableLayer) =
    new LinearRing(CoordArraySequence(l toBuffer), layer.getGeometryFactory)
}

/**
 * convenience object for
 * handling LineString
 */
object lineString {
  def apply(cs: CoordinateSequence)(implicit layer: EditableLayer) =
    new LineString(cs, layer.getGeometryFactory)

  def apply(b: Buffer[(Double, Double)])(implicit layer: EditableLayer) =
    new LineString(CoordArraySequence(b), layer.getGeometryFactory)
}

/**
 * factory for Envelope
 */
object Envelope {
  def apply(x1: Double, x2: Double, y1: Double, y2: Double) =
    new Envelope(x1, x2, y1, y2)

  def apply(coordinate: com.vividsolutions.jts.geom.Coordinate, coordinate1: com.vividsolutions.jts.geom.Coordinate) =
    new Envelope(coordinate, coordinate1)

  def apply(p: com.vividsolutions.jts.geom.Coordinate) =
    new Envelope(p)

  def apply(env: com.vividsolutions.jts.geom.Envelope) =
    new Envelope(env)
}