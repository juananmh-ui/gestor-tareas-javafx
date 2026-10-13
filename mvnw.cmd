@echo off
setlocal EnableExtensions EnableDelayedExpansion

set "MAVEN_VERSION=3.9.11"
set "PROJECT_DIR=%~dp0"
set "MAVEN_DIR=%PROJECT_DIR%.mvn\apache-maven-%MAVEN_VERSION%"
set "MAVEN_ZIP=%PROJECT_DIR%.mvn\apache-maven-%MAVEN_VERSION%-bin.zip"
set "MAVEN_URL=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/%MAVEN_VERSION%/apache-maven-%MAVEN_VERSION%-bin.zip"
set "MAVEN_SHA512=03e2d65d4483a3396980629f260e25cac0d8b6f7f2791e4dc20bc83f9514db8d0f05b0479e699a5f34679250c49c8e52e961262ded468a20de0be254d8207076"
set "MAVEN_MARKER=%MAVEN_DIR%\.distribution-sha512"

if defined JAVA_HOME if exist "%JAVA_HOME%\bin\javac.exe" goto java_ready

for /f "delims=" %%I in ('where javac 2^>nul') do (
    set "JAVAC_EXE=%%~fI"
    goto javac_found
)

echo ERROR: Se necesita un JDK 21 o posterior y no se ha encontrado javac. 1>&2
exit /b 1

:javac_found
for %%I in ("%JAVAC_EXE%\..\..") do set "JAVA_HOME=%%~fI"

:java_ready
for /f "tokens=2" %%V in ('"%JAVA_HOME%\bin\javac.exe" -version 2^>^&1') do set "JAVA_VERSION=%%V"
for /f "tokens=1 delims=." %%M in ("!JAVA_VERSION!") do set "JAVA_MAJOR=%%M"
if "!JAVA_MAJOR!"=="1" for /f "tokens=2 delims=." %%M in ("!JAVA_VERSION!") do set "JAVA_MAJOR=%%M"
if not defined JAVA_MAJOR (
    echo ERROR: No se pudo determinar la version del JDK. 1>&2
    exit /b 1
)
if !JAVA_MAJOR! LSS 21 (
    echo ERROR: Se necesita JDK 21 o posterior; se ha encontrado !JAVA_VERSION!. 1>&2
    exit /b 1
)

if exist "%MAVEN_DIR%\bin\mvn.cmd" if exist "%MAVEN_MARKER%" (
    set /p CACHED_SHA512=<"%MAVEN_MARKER%"
    if /I "!CACHED_SHA512!"=="%MAVEN_SHA512%" goto run_maven
)

echo Preparando Apache Maven %MAVEN_VERSION%...
if not exist "%PROJECT_DIR%.mvn" mkdir "%PROJECT_DIR%.mvn"

set "MAVEN_WRAPPER_URL=%MAVEN_URL%"
set "MAVEN_WRAPPER_ZIP=%MAVEN_ZIP%"
set "MAVEN_WRAPPER_DIR=%MAVEN_DIR%"
set "MAVEN_WRAPPER_SHA512=%MAVEN_SHA512%"
set "MAVEN_WRAPPER_MARKER=%MAVEN_MARKER%"

powershell.exe -NoProfile -ExecutionPolicy Bypass -Command ^
  "$ErrorActionPreference = 'Stop';" ^
  "$ProgressPreference = 'SilentlyContinue';" ^
  "$url = $env:MAVEN_WRAPPER_URL;" ^
  "$zip = $env:MAVEN_WRAPPER_ZIP;" ^
  "$mavenDir = $env:MAVEN_WRAPPER_DIR;" ^
  "if (Test-Path -LiteralPath $mavenDir) { Remove-Item -LiteralPath $mavenDir -Recurse -Force };" ^
  "Invoke-WebRequest -Uri $url -OutFile $zip;" ^
  "$stream = [System.IO.File]::OpenRead($zip);" ^
  "$sha512 = [System.Security.Cryptography.SHA512]::Create();" ^
  "try { $actual = ([System.BitConverter]::ToString($sha512.ComputeHash($stream))).Replace('-', '').ToLowerInvariant() } finally { $stream.Dispose(); $sha512.Dispose() };" ^
  "if ($actual -ne $env:MAVEN_WRAPPER_SHA512) { throw 'La suma SHA-512 de Maven no coincide.' };" ^
  "Expand-Archive -Path $zip -DestinationPath (Split-Path -Parent $mavenDir) -Force;" ^
  "Set-Content -LiteralPath $env:MAVEN_WRAPPER_MARKER -Value $actual -NoNewline;" ^
  "Remove-Item -LiteralPath $zip -Force"

if errorlevel 1 (
    echo ERROR: No se pudo preparar Maven. Comprueba la conexion a Internet. 1>&2
    exit /b 1
)

:run_maven
call "%MAVEN_DIR%\bin\mvn.cmd" %*
exit /b %ERRORLEVEL%
