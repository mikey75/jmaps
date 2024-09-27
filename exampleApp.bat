@rem Script to build and run the example application
@rem Set your maven and java binaries here
@set MVN=%USERPROFILE%\devtools\maven\bin\mvn
@set JAVA=%USERPROFILE%\.jdks\temurin-11.0.22\bin\java
@cd jmaps-viewer
@call %MVN% clean install
@cd ..
@cd jmaps-example
@call %MVN% clean install
@call %JAVA% -jar target\jmaps-example-1.2-SNAPSHOT-jar-with-dependencies.jar
@cd ..
