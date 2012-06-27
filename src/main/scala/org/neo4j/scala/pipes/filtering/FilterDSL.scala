package org.neo4j.scala.pipes.filtering

import org.neo4j.gis.spatial.pipes.{AbstractFilterGeoPipe, GeoPipeline}

/**
 * Created with IntelliJ IDEA.
 * User: stefan
 * Date: 01.07.12
 * Time: 19:08
 * To change this template use File | Settings | File Templates.
 */

object FilterDSL {
  def filter(implicit gp: GeoPipeline) = new {
    def add(o: AbstractFilterGeoPipe): GeoPipeline = {
      gp.addPipe(o)
      gp
    }
  }
}
