#!/bin/sh
# Script to build and run the example application
# Set your maven and java binaries here
MVN=$HOME/stuff/dev/mvn/bin/mvn
JAVA=$HOME/stuff/dev/jdks/jdk17/bin/java

cd jmaps-viewer && $MVN -DskipTests=true clean install && cd ..
cd jmaps-example && $MVN -DskipTests=true clean package
$JAVA -jar target/jmaps-example.jar
