@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion
title Inventario Dev Launcher

:menu
cls
echo ============================================
echo      INVENTARIO - Dev Launcher
echo ============================================
echo.
echo  1) Python  (customtkinter)
echo  2) Java    (FlatLaf + Swing)
echo  3) VB.NET  (WinForms)
echo.
echo  0) Salir
echo.
echo ============================================
set /p op="Seleccione [0-3]: "

if "%op%"=="1" goto python
if "%op%"=="2" goto java
if "%op%"=="3" goto vbnet
if "%op%"=="0" exit /b
goto menu

:python
cls
set "PYTHON_EXE=C:\Users\daniel\AppData\Local\Programs\Python\Python312\python.exe"
if not exist "%PYTHON_EXE%" (
    for /f %%i in ('where python 2^>nul') do set "PYTHON_EXE=%%i"
)
if not exist "%PYTHON_EXE%" (
    echo [ERROR] No se encontro Python
    pause
    goto menu
)
cd /d "%~dp0inventario_python"
echo [Python] Usando: %PYTHON_EXE%
set INV_DB_HOST=localhost
set INV_DB_PORT=3306
set INV_DB_NAME=inventario_db
set INV_DB_USER=root
set INV_DB_PASS=
"%PYTHON_EXE%" main.py
echo.
echo [Python] Finalizo con codigo: %errorlevel%
pause
goto menu

:java
cls
cd /d "%~dp0inventario"
set "BUILD_DIR=build"
set "LIB_DIR=lib"

if not exist "%BUILD_DIR%\IG\LOG.class" (
    echo [Java] Compilando...
    set "CP="
    for %%j in ("%LIB_DIR%\*.jar") do set "CP=!CP!;%%~fj"
    dir /s /b "src\*.java" > "%TEMP%\sources.txt" 2>nul
    javac -proc:none -cp "!CP!" -d "%BUILD_DIR%" @"%TEMP%\sources.txt"
    if !errorlevel! neq 0 (
        echo [ERROR] Compilacion fallida
        pause
        goto menu
    )
    if exist "src\IMG" xcopy /E /I /Y /Q "src\IMG" "%BUILD_DIR%\IMG" > nul
    echo [OK] Compilacion exitosa
)

cls
echo [Java] Iniciando...
set "CP=%BUILD_DIR%"
for %%j in ("%LIB_DIR%\*.jar") do set "CP=!CP!;%%~fj"
set DB_URL=jdbc:mysql://localhost:3306/inventario_db
set DB_USER=root
set DB_PASSWORD=
java -cp "%CP%" IG.LOG
echo.
echo [Java] Finalizo con codigo: %errorlevel%
pause
goto menu

:vbnet
cls
cd /d "%~dp0inventario_visual"
set "DOTNET_EXE="
if exist "C:\Users\daniel\AppData\Local\dotnet\dotnet.exe" (
    set "DOTNET_EXE=C:\Users\daniel\AppData\Local\dotnet\dotnet.exe"
) else (
    for /f %%i in ('where dotnet 2^>nul') do set "DOTNET_EXE=%%i"
)
if not defined DOTNET_EXE (
    echo [ERROR] .NET SDK no esta instalado.
    echo No se puede ejecutar VB.NET sin el SDK.
    echo Descarguelo desde: https://dotnet.microsoft.com/download
    pause
    goto menu
)
echo [VB.NET] Iniciando...
set DB_HOST=localhost
set DB_PORT=3306
set DB_NAME=inventario_db
set DB_USER=root
set DB_PASS=
"%DOTNET_EXE%" run --configuration Release
echo.
echo [VB.NET] Finalizo con codigo: %errorlevel%
pause
goto menu
