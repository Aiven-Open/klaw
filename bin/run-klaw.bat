@echo off

SET CURRENTDIR="%cd%"
set DIR=%~dp0..\
cd %DIR%

set version=2.7.0
set core_lib=.\core\target\klaw-%version%.jar
set cluster_lib=.\cluster-api\target\cluster-api-%version%.jar
set core_config=.\core\target\classes\application.properties
set cluster_config=.\cluster-api\target\classes\application.properties
set core_log_file=core.log
set clusterapi_log_file=cluster-api.log

echo Starting Klaw servers ..
echo ---------- Klaw Core ----------
if not exist "%core_lib%" (
  echo %core_lib% doesn't exist. Exiting ..
  exit /b 1
)
tasklist /FI "IMAGENAME eq java.exe" 2>NUL | find /I "core" >NUL
if %errorlevel% equ 0 (
  echo Core is already running
) else (
  echo Starting core
  start /B java -jar %core_lib% --spring.config.location=%core_config% > %core_log_file%
  tasklist /FI "IMAGENAME eq java.exe" 2>NUL | find /I "core" >NUL
)

echo ---------- Klaw Cluster api ----------
if not exist "%cluster_lib%" (
  echo %cluster_lib% doesn't exist. Exiting ..
  exit /b 1
)
tasklist /FI "IMAGENAME eq java.exe" 2>NUL | find /I "cluster-api" >NUL
if %errorlevel% equ 0 (
  echo Cluster-api is already running
) else (
  echo Starting cluster-api
  start /B java -jar %cluster_lib% --spring.config.location=%cluster_config% > %clusterapi_log_file%
  tasklist /FI "IMAGENAME eq java.exe" 2>NUL | find /I "cluster-api" >NUL
)
cd %CURRENTDIR%
echo Logging to %core_log_file% %clusterapi_log_file%