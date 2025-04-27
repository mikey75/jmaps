![Maven Central](https://img.shields.io/maven-central/v/net.wirelabs/jmaps)
![CI](https://github.com/mikey75/jmaps/actions/workflows/maven-build.yml/badge.svg)
![Last commit](https://img.shields.io/github/last-commit/mikey75/jmaps)


# Java Swing component to display and interact with online maps.

- Supports OpenStreetMap style slippy maps
- Supports WMTS maps
- Supports QUAD tile maps (e.g virtual earth)
- Supports most CRS projections (via proj4j)
- Supports multiple layer maps with transparency/zoom offsets
- Comes with example application and example map definitions.
- Supports user overlays - you can plot anything on the map (waypoints, GPS tracks etc)
- Supports caching (file or in-memory database) of map tiles with configurable timeout
  
# Usage: 

Currently latest deployed to maven central is version 1.3

          <dependency>
            <groupId>net.wirelabs</groupId>
            <artifactId>jmaps-viewer</artifactId>
            <version>1.3</version>
          </dependency>

# Samples:
jMaps comes with sample demo application. 
It is also an example of using the jMaps component in your own application.
Here's a few pictures telling thousand words ;)
- WMTS Map of Czech Republic
![sample1](images/1.png "Sample 1")
- Poland raster topographic WMTS map
![sample2](images/2.png "Sample 2")
- OpenStreetMap with loaded GPX track
![sample3](images/3.png "Sample 3")
- Example of multilayer map (2 layers - one is a base topo map, the other is elevation profile). 
You can enable/disable given layer from the view.
![sample4](images/4.png "Sample 4")
- Virtual Earth Maps (example of quad tiles) 
![sample5](images/5.png "Sample 5")

# Running example app locally
Your JAVA_HOME should point to java 17:
```
cd jmaps-viewer && mvn clean install && cd ..
cd jmaps-example && mvn clean package && cd ..
cd jmaps-example && java -jar target/jmaps-example.jar
