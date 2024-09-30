#!/bin/sh
# Script to build and run the example application
# Set your maven and java binaries here
MVN=$HOME/stuff/dev/mvn/bin/mvn
JAVA=$HOME/.jdks/temurin-11.0.22/bin/java

$MVN clean package install
cd jmaps-example && $JAVA -jar target/jmaps-example.jar
cd ..

