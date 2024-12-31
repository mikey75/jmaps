#!/bin/sh
# Script to build and run the example application
# Set your maven and java binaries here
MVN=$HOME/stuff/dev/mvn/bin/mvn
JAVA=$HOME/stuff/dev/jdks/jdk17/bin/java

cd jmaps-viewer && $MVN clean install && cd ..
cd jmaps-example && $MVN clean package
$JAVA -jar target/jmaps-example.jar
