@rem Script to build and run the example application
@rem Set your maven and java binaries here
@set MVN=%USERPROFILE%\devtools\maven\bin\mvn
@set JAVA=%USERPROFILE%\.jdks\jdk-17.0.12\bin\java


@cd jmaps-viewer
@call %MVN% clean install
@cd ../jmaps-example
@call %MVN% clean package
@call %JAVA% -jar target/jmaps-example.jar



