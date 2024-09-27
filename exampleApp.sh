#!/bin/sh
# Script to build and run the example application
# Set your maven and java binaries here
MVN=$HOME/stuff/dev/mvn/bin/mvn
JAVA=$HOME/.jdks/temurin-11.0.22/bin/java

cd jmaps-viewer && $MVN clean install && cd ..
cd jmaps-example && $MVN clean install && cd ..
cd jmaps-example && $JAVA -jar target/jmaps-example-1.2-SNAPSHOT-jar-with-dependencies.jar

