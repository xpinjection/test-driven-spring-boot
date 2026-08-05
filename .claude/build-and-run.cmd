@echo off
set JAVA_HOME=C:\Users\mikal\.sdkman\candidates\java\current
set PATH=%JAVA_HOME%\bin;%PATH%

echo [1/2] Building Spring Boot uber jar (skipping tests)...
call "%~dp0..\mvnw.cmd" package -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo Build failed!
    exit /b %ERRORLEVEL%
)

echo [2/2] Running application with dev profile...
java ^
  -Dspring.profiles.active=dev ^
  -Dspring.security.user.name=admin ^
  -Dspring.security.user.password=xpinjection ^
  -Dspring.datasource.username=test ^
  -Dspring.datasource.password=test ^
  -Dspring.datasource.url=jdbc:postgresql://localhost:5432/library ^
  -jar "%~dp0..\target\library-0.1.0-SNAPSHOT.jar"

java -Dspring.profiles.active=dev -Dspring.security.user.name=admin -Dspring.security.user.password=xpinjection -Dspring.datasource.username=test -Dspring.datasource.password=test -jar ./target/library-0.1.0-SNAPSHOT.jar
