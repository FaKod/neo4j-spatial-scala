Neo4j Spatial Scala wrapper library
=======================

I tried to add some wrapper stuff for the Neo4j Spatial implementation. 
So you are able to create a city Munich as follows:

    val munich = add newPoint ((15.3, 56.2))
    munich.setProperty("City", "Munich")

and attached it to a federal state like Bavaria:

    val bayernBuffer = Buffer[(Double, Double)]((15, 56), (16, 56), (15, 57), (16, 57), (15, 56))
    val bayern = add newPolygon (LinRing(bayernBuffer))
    bayern.setProperty("FederalState", "Bayern")
    federalStates --> "isFederalState" --> bayern

Additionally I added some examples like those pattern shown in the [Neo4j Design Guide](http://wiki.neo4j.org/content/Design_Guide):

    . . .
	class FedaralState(val node: SpatialDatabaseRecord) extends . . . {

	  object FedaralState {
	    val KEY_FEDSTATE_NAME = "federalState"
	  }

	  def name = node.getProperty(FedaralState.KEY_FEDSTATE_NAME)

	  def name_=(n: String) {
	    node.setProperty(FedaralState.KEY_FEDSTATE_NAME, n)
	  }

	  def getCapitalCity(implicit layer: EditableLayer) = {
	    val o = node.getSingleRelationship("CapitalCityOf", Direction.INCOMING).getOtherNode(node)
	    new City(new SpatialDatabaseRecord(layer, o))
	  }
	}
	. . .
	
that finaly result in code as follows:

     /**
      * create Munich and "attach" it to the cities node
      */
     val munich = NewSpatialNode[City]((15.3, 56.2))
     munich.name = "Munich"
     cities --> "isCity" --> munich

     /**
      * create a polygon called Bayern, "attach" it to the federal state node and
      * "attach" the capital city Munich
      */
     val bayernBuffer = Buffer[(Double, Double)]((15, 56), (16, 56), (15, 57), (16, 57), (15, 56))
     val bayern = NewSpatialNode[FedaralState](bayernBuffer)
     bayern.name = "Bayern"
     federalStates --> "isFederalState" --> bayern
     munich --> "CapitalCityOf" --> bayern
	
Lookes rather nice IMHO, but is still very incomplete...

