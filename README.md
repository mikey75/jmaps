![Maven Central](https://img.shields.io/maven-central/v/net.wirelabs/jmaps)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
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
- Supports caching (file or in-memory database) of map tiles with configurable timeout<br>

For changes always look at ReleaseNotes.txt 
  
# Usage: 
          <dependency>
            <groupId>net.wirelabs</groupId>
            <artifactId>jmaps-viewer</artifactId>
            <version>$version/version>
          </dependency>
          
Latest version can be checked here on the top bar maven status icon.

# !!! Warning !!!
Version 1.3 introduced a tiny error, that makes using map sources that have space in query params unusable.<br>
Details are in [Issue #49.](https://github.com/mikey75/jmaps/issues/49) Fix is already in 1.4.0 version. 
Do not use version 1.3 if you have such map sources.
# Samples:
jMaps comes with sample demo application. 
It is also an example of using the jMaps component in your own application.<br>
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
