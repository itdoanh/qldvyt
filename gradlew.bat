@echo off
REM Shim gradlew -> dung Gradle 8.8 tai local (tools\gradle) de khong can cai dat them.
setlocal
set SCRIPT_DIR=%~dp0
set GRADLE_HOME=%SCRIPT_DIR%tools\gradle\gradle-8.8
if not exist "%GRADLE_HOME%\bin\gradle.bat" (
  echo [QLDVYT] Chua thay Gradle tai %GRADLE_HOME%
  echo Hay doi download xong hoac chay: powershell -File scripts\cai-gradle.ps1
  exit /b 1
)
if "%JAVA_HOME%"=="" set JAVA_HOME=C:\Program Files\Android\openjdk\jdk-21.0.8
call "%GRADLE_HOME%\bin\gradle.bat" %*
