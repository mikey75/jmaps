@rem Script to build and run the example application
@rem Set your maven and java binaries here
@set MVN=%USERPROFILE%\devtools\maven\bin\mvn
@set JAVA=%USERPROFILE%\.jdks\temurin-11.0.22\bin\java

@call %MVN% clean package install
@call cd jmaps-example && %JAVA% -jar target\jmaps-example.jar
@cd ..
