Neo4j Spatial Scala wrapper library
=======================

Building
--------

```bash
$ git clone git://github.com/FaKod/neo4j-spatial-scala.git
$ cd neo4j-spatial-scala
$ mvn clean install
```

This library needs [Neo4j-Scala](http://github.com/FaKod/neo4j-scala) that you have to "mvn install" first.

Or try to maven fetch it with a Github Maven Repo:
```xml
<repositories>
  <repository>
    <id>fakod-snapshots</id>
    <url>https://raw.github.com/FaKod/fakod-mvn-repo/master/snapshots</url>
  </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>org.neo4j</groupId>
    <artifactId>neo4j-spatial-scala</artifactId>
    <version>0.1.0-SNAPSHOT</version>
  </dependency>
</dependencies>
```

Using this library
==================

Spatial Database Service Provider
------------------------------
Neo4j Spatial Scala Wrapper needs a Spatial Database Service Provider, it has to implement SpatialDatabaseServiceProvider trait.
One possibility is to use the SimpleSpatialDatabaseServiceProvider for embedded Neo4j instances where you simply have to define a Neo4j storage directory. A class using the wrapper is f.e.:

```scala
class MyNeo4jClass extends SomethingClass with Neo4jSpatialWrapper with EmbeddedGraphDatabaseServiceProvider with SimpleSpatialDatabaseServiceProvider {
  def neo4jStoreDir = "/tmp/temp-spatial-neo"
  . . .
}
```


Transaction Wrapping
--------------------
Transactions are wrapped by withSpatialTx. After leaving the "scope" success is called (or rollback if an exception is raised):

```scala
withSpatialTx {
 implicit neo =>
   deleteLayer("test", new NullListener)
   val layerNames = getLayerNames
}
```


Using Layer
-----------
To ease the Layer handling a Layer scope can be provided:

```scala
// return or create a Layer instance
   withLayer(getOrCreateEditableLayer("test")) {
    implicit layer =>
    // adding a new Point to Layer "test"
    val point = add newPoint ((15.3, 56.2))
}
```


Adding Spatial Features to a Layer
---------------------------------
Tuples are used for locations, List of Tuples for geometries with more than one location. 
Case class serialization can by added by "using ..."

```scala
case class City(name: String) 
case class FederalState(name: String)
. . .
val point = add newPoint ((15.3, 56.2))
val munich = add newPoint ((15.3, 56.2)) using City("Munich")

val ring = LinRing((15.0, 56.0) ::(16.0, 56.0) ::(15.0, 57.0) ::(16.0, 57.0) ::(15.0, 56.0) :: Nil)
val bayern = add newPolygon (ring) using FederalState("Bayern")

munich --> "CapitalCityOf" --> bayern
```

	
Searching Things
----------------
Spatial searches start with <code>Search</code> and the type of search, like:

* within
* withinDistance
* coveredBy
* intersect
* … more to come ...

For instance:

```scala
// search all cities within envelope
val sw = Search within Envelope(15.0, 16.0, 56.0, 57.0)
for (r <- sw; city <- r.toCC[City]) yield city
```


