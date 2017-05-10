@echo off
java -XX:MaxDirectMemorySize=1024m -jar "%~dp0\..\jbake-core.jar" %*
