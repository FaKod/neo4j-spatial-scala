package org.neo4j.scala.pipes.filtering

import org.neo4j.gis.spatial.pipes.filtering._
import com.tinkerpop.pipes.filter.FilterPipe.Filter

/**
 * Created with IntelliJ IDEA.
 * User: slauer
 * Date: 27.06.12
 * Time: 10:54
 *
 * Convenience objects for handling Filters
 *
 * For more detailed information for the wrapped filters
 * take a look to neo4j-spatial
 *
 */

/**
 * FilterProperty
 * Filter by property value.
 */
object FilterProperty {
  def apply(key: String, value: Any) = new FilterProperty(key, value)
  def apply(key: String, value: Any, comparison: Filter) = new FilterProperty(key, value, comparison)
}

/**
 * FilterPropertyNotNull
 * Find items which have a not null value for the given property
 */
object FilterPropertyNotNull {
  def apply(property: String) = new FilterPropertyNotNull(property)
}

/**
 * FilterPropertyNull
 * Find items which have a null value for the given property
 */
object FilterPropertyNull {
  def apply(property: String) = new FilterPropertyNull(property)
}
