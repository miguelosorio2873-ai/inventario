@echo off
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
cd /d "%~dp0inventario_python"
set PYTHON=python
if exist "%~dp0..\inventario\.venv\Scripts\python.exe" set PYTHON="%~dp0..\inventario\.venv\Scripts\python.exe"
echo [Python] Usando: %PYTHON%
set INV_DB_HOST=localhost
set INV_DB_PORT=3306
set INV_DB_NAME=inventario_db
set INV_DB_USER=root
set INV_DB_PASS=
%PYTHON% main.py
echo.
echo [Python] Finalizo con codigo: %errorlevel%
pause
goto menu

:java
cls
echo [Java] Verificando build...
cd /d "%~dp0inventario"
if not exist build\IG\LOG.class (
    echo Compilando...
    javac -proc:none -cp "lib\flatlaf-3.5.2.jar;lib\flatlaf-extras-3.5.2.jar;lib\jsvg-1.6.1.jar;lib\mysql-connector-j-9.0.0.jar;lib\argon2-jvm-nolibs-2.11.jar;lib\log4j-api-2.20.0.jar;lib\log4j-core-2.20.0.jar;lib\poi-5.2.3.jar;lib\poi-ooxml-5.2.3.jar;lib\commons-collections4-4.4.jar;lib\commons-compress-1.21.jar;lib\xmlbeans-5.1.1.jar;lib\commons-io-2.11.0.jar;lib\commons-math3-3.6.1.jar;lib\SparseBitSet-1.2.jar;lib\jna-5.8.0.jar;." -d build -sourcepath src src/IG/LOG.java
    if %errorlevel% neq 0 (
        echo [ERROR] Compilacion fallida.
        pause
        goto menu
    )
    if exist "src\IMG" xcopy /E /I /Y /Q "src\IMG" "build\IMG" > nul
)
cls
echo [Java] Iniciando...
set DB_URL=jdbc:mysql://localhost:3306/inventario_db
set DB_USER=root
set DB_PASSWORD=
java -cp "build;lib\flatlaf-3.5.2.jar;lib\flatlaf-extras-3.5.2.jar;lib\jsvg-1.6.1.jar;lib\mysql-connector-j-9.0.0.jar;lib\argon2-jvm-nolibs-2.11.jar;lib\argon2-jvm-2.11.jar;lib\log4j-api-2.20.0.jar;lib\log4j-core-2.20.0.jar;lib\poi-5.2.3.jar;lib\poi-ooxml-5.2.3.jar;lib\commons-collections4-4.4.jar;lib\commons-compress-1.21.jar;lib\xmlbeans-5.1.1.jar;lib\commons-io-2.11.0.jar;lib\commons-math3-3.6.1.jar;lib\SparseBitSet-1.2.jar;lib\jna-5.8.0.jar;." IG.LOG
echo.
echo [Java] Finalizo con codigo: %errorlevel%
pause
goto menu

:vbnet
cls
echo [VB.NET] Iniciando...
cd /d "%~dp0inventario_visual"
set DB_HOST=localhost
set DB_PORT=3306
set DB_NAME=inventario_db
set DB_USER=root
set DB_PASS=
dotnet run --configuration Release
echo.
echo [VB.NET] Finalizo con codigo: %errorlevel%
pause
goto menu
