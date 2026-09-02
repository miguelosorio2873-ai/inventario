# empaquetar.ps1 - Pipeline completo de empaquetado de InventarioPro
# Compila -> uber-jar -> app-image (jpackage) -> setup.exe (Inno Setup)
# Uso:  powershell -ExecutionPolicy Bypass -File empaquetar.ps1

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $root

$javaHome  = if ($env:JAVA_HOME) { $env:JAVA_HOME } else { "C:\Users\e\jdk-21" }
$jpackage  = Join-Path $javaHome "bin\jpackage.exe"
$jar       = Join-Path $javaHome "bin\jar.exe"
$iscc      = "C:\Program Files (x86)\Inno Setup 6\ISCC.exe"

$buildDir    = Join-Path $root "build"
$distDir     = Join-Path $root "dist"
$manifest    = Join-Path $distDir "MANIFEST.MF"
$uberJar     = Join-Path $distDir "InventarioPro.jar"
$appInput    = Join-Path $root "app-input"
$appImage    = Join-Path $root "jpackage-out\InventarioPro"
$iconSvg     = Join-Path $root "src\IMG\box.svg"
$iconIco     = Join-Path $root "instalar\box.ico"
$appIcoInput = Join-Path $appInput "InventarioPro.ico"
$issFile     = Join-Path $root "instalar\inventarioPro.iss"
$libDir      = Join-Path $root "lib"
$dbJars      = (Join-Path $root "target\classes\DB")
$flJars      = (Join-Path $root "target\classes\FL")
$propsSrc    = Join-Path $root "src\application.properties"

function Step($msg) { Write-Host ""; Write-Host "== $msg ==" -ForegroundColor Cyan }

# 1. Compilar
Step "1/5 Compilando fuentes (.vscode\build.ps1)"
& powershell -ExecutionPolicy Bypass -File (Join-Path $root ".vscode\build.ps1")
if ($LASTEXITCODE -ne 0) { throw "Fallo la compilacion (exit $LASTEXITCODE)" }

# 2. Empaquetar uber-jar con clases + recursos (SIN dependencias jar embebidas)
#    Las dependencias van como jars aparte en app-input, no dentro del uber-jar.
#    Se prepara dist/classes (limpiado) copiando build/ y filtrando *.jar y
#    las carpetas de dependencias (deps, FL, DB/*.jar). Solo se conserva
#    DB/inventario_db.sql como recurso legitimo.
Step "2/5 Generando uber-jar InventarioPro.jar"
$classesDir = Join-Path $distDir "classes"
if (Test-Path $classesDir) { Remove-Item $classesDir -Recurse -Force }
Copy-Item $buildDir $classesDir -Recurse -Force

# eliminar carpetas de dependencias dentro de dist/classes
foreach ($sub in @("deps", "FL")) {
    $p = Join-Path $classesDir $sub
    if (Test-Path $p) { Remove-Item $p -Recurse -Force }
}
# eliminar jars sueltos (ej: DB/mysql-connector...jar), conservando .sql/.properties/.svg/.png
Get-ChildItem -Path $classesDir -Recurse -Filter *.jar -ErrorAction SilentlyContinue | Remove-Item -Force

if (-not (Test-Path $manifest)) {
    @"
Manifest-Version: 1.0
Main-Class: IG.LOG
Created-By: InventarioPro Build

"@ | Set-Content -LiteralPath $manifest -Encoding ASCII
}
if (Test-Path $uberJar) { Remove-Item $uberJar -Force }
& $jar cfm $uberJar $manifest -C $classesDir .
if ($LASTEXITCODE -ne 0) { throw "Fallo al empaquetar el uber-jar" }
Write-Host "  uber-jar: $uberJar ($((Get-Item $uberJar).Length) bytes)"

# 3. Preparar app-input (jar + deps + properties + icono)
Step "3/5 Preparando app-input"
if (Test-Path $appInput) { Remove-Item $appInput -Recurse -Force }
New-Item -ItemType Directory -Path $appInput -Force | Out-Null

Copy-Item $uberJar (Join-Path $appInput "InventarioPro.jar") -Force
Copy-Item $propsSrc (Join-Path $appInput "application.properties") -Force

# dependencias (deduplicadas)
$seen = New-Object 'System.Collections.Generic.HashSet[string]'
$depSources = @($libDir, $dbJars, $flJars)
foreach ($dir in $depSources) {
    if (Test-Path -LiteralPath $dir) {
        Get-ChildItem -LiteralPath $dir -Filter *.jar | ForEach-Object {
            if ($seen.Add([string]$_.Name)) {
                Copy-Item -LiteralPath $_.FullName -Destination (Join-Path $appInput $_.Name) -Force
            }
        }
    }
}
Write-Host "  jars en app-input: $($seen.Count)"

# icono: usa box.ico ya generado (caja del login) como InventarioPro.ico
Step "3.1/5 Icono"
if (-not (Test-Path $iconIco)) { throw "Falta $iconIco (generarlo desde box.svg antes)" }
Copy-Item $iconIco $appIcoInput -Force
Write-Host "  icono: box.ico"

# 4. app-image con jpackage
Step "4/5 Generando app-image (Java embebido)"
if (Test-Path (Join-Path $root "jpackage-out")) { Remove-Item (Join-Path $root "jpackage-out") -Recurse -Force }
& $jpackage --type app-image --input $appInput --main-jar "InventarioPro.jar" --main-class "IG.LOG" --name "InventarioPro" --app-version "1.0.0" --icon $appIcoInput --dest (Join-Path $root "jpackage-out")
if ($LASTEXITCODE -ne 0) { throw "Fallo jpackage" }
Write-Host "  app-image: $appImage"

# 5. setup.exe con Inno Setup
Step "5/5 Generando setup.exe"
$oldSetup = Join-Path $root "instalar\setup-InventarioPro-1.0.0.exe"
if (Test-Path $oldSetup) { Remove-Item $oldSetup -Force }
& $iscc $issFile | Out-Null
if ($LASTEXITCODE -ne 0) { throw "Fallo ISCC" }

Write-Host ""
Write-Host "DONE. Instalador: $oldSetup ($((Get-Item $oldSetup).Length) bytes)" -ForegroundColor Green