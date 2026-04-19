@echo off
setlocal enabledelayedexpansion

rem Lightweight Maven wrapper (no system Maven required).
rem Downloads Maven into .mvn/ on first run, then executes it.

rem Some Windows setups store JAVA_HOME with surrounding quotes, which breaks mvn.cmd parsing.
if defined JAVA_HOME set "JAVA_HOME=%JAVA_HOME:"=%"
if defined JDK_HOME set "JDK_HOME=%JDK_HOME:"=%"

set "MAVEN_VERSION=3.9.9"
set "MAVEN_DIR=%~dp0.mvn\apache-maven-%MAVEN_VERSION%"
set "MAVEN_BIN=%MAVEN_DIR%\bin\mvn.cmd"
set "MAVEN_ZIP=%~dp0.mvn\apache-maven-%MAVEN_VERSION%-bin.zip"
set "MAVEN_URL=https://archive.apache.org/dist/maven/maven-3/%MAVEN_VERSION%/binaries/apache-maven-%MAVEN_VERSION%-bin.zip"
set "MAVEN_REPO=%~dp0.mvn\repository"

if exist "%MAVEN_BIN%" goto run

echo Maven not found. Bootstrapping Maven %MAVEN_VERSION% into .mvn\ ...
if not exist "%~dp0.mvn" mkdir "%~dp0.mvn"

rem Download zip
powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  "$ErrorActionPreference='Stop';" ^
  "[Net.ServicePointManager]::SecurityProtocol=[Net.SecurityProtocolType]::Tls12;" ^
  "Invoke-WebRequest -Uri '%MAVEN_URL%' -OutFile '%MAVEN_ZIP%';" ^
  "Expand-Archive -LiteralPath '%MAVEN_ZIP%' -DestinationPath '%~dp0.mvn' -Force;" ^
  "Remove-Item -LiteralPath '%MAVEN_ZIP%' -Force;"

if not exist "%MAVEN_BIN%" (
  echo Failed to bootstrap Maven. Try installing Maven manually and re-run.
  exit /b 1
)

:run
if not exist "%MAVEN_REPO%" mkdir "%MAVEN_REPO%"
call "%MAVEN_BIN%" -Dmaven.repo.local="%MAVEN_REPO%" %*
