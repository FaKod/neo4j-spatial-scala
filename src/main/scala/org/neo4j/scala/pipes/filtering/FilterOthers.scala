package org.neo4j.scala.pipes.filtering

import org.neo4j.gis.spatial.Layer
import org.neo4j.gis.spatial.pipes.filtering._

/**
 * Created with IntelliJ IDEA.
 * User: slauer
 * Date: 27.06.12
 * Time: 06:35
 *
 * Convenience objects for handling Filters
 *
 * For more detailed information for the wrapped filters
 * take a look to neo4j-spatial
 */

/**
 * FilterCQL
 * Filter geometries using a CQL query
 */
object FilterCQL {
  def apply (cqlPredicate: String)(implicit layer: Layer) = new FilterCQL(layer, cqlPredicate)
}

/**
 * FilterEmpty
 * Find empty geometries.
 */

object FilterEmpty {
  def apply = new FilterEmpty
}

/**
 * FilterInvalid
 * Find invalid geometries.
 */
object FilterInvalid {
  def apply = new FilterInvalid
}

/**
 * FilterValid
 * Find valid geometries
 */
 object FilterValid {
  def apply() = new FilterValid
}


