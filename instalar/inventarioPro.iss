; Script Inno Setup para InventarioPro
; Empaqueta el app-image generado por jpackage (Java embebido)
; Instalacion por usuario -> la app escribe BD/config en su propia carpeta sin admin

#define AppName "InventarioPro"
#define AppVersion "1.0.0"
#define AppExe "InventarioPro.exe"
#define AppDir "..\jpackage-out\InventarioPro"
#define AppURL ""

[Setup]
AppId={{83A2F1D0-5B4A-4A6C-9D2E-0C8B1E3F4A5B}
AppName={#AppName}
AppVersion={#AppVersion}
AppPublisher=InventarioPro
AppVerName={#AppName} {#AppVersion}
DefaultDirName={localappdata}\Applications\{#AppName}
DisableProgramGroupPage=yes
DefaultGroupName={#AppName}
UninstallDisplayIcon={app}\{#AppExe}
Compression=lzma2
SolidCompression=yes
WizardStyle=modern
; Icono del instalador (caja del login)
SetupIconFile=box.ico
OutputDir=.
OutputBaseFilename=setup-InventarioPro-{#AppVersion}
PrivilegesRequired=lowest
CloseApplications=yes
ShowLanguageDialog=no

[Languages]
Name: "spanish"; MessagesFile: "compiler:Languages\Spanish.isl"

[Tasks]
Name: "desktopicon"; Description: "Crear acceso directo en el escritorio"; GroupDescription: "Iconos adicionales:"

[Files]
Source: "{#AppDir}\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "{#AppDir}\app\*"; DestDir: "{app}\app"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "{#AppDir}\runtime\*"; DestDir: "{app}\runtime"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{group}\{#AppName}"; Filename: "{app}\{#AppExe}"
Name: "{autodesktop}\{#AppName}"; Filename: "{app}\{#AppExe}"; Tasks: desktopicon

[Run]
Filename: "{app}\{#AppExe}"; Description: "Ejecutar {#AppName} ahora"; Flags: nowait postinstall skipifsilent