# Inventario Pro - VB.NET WinForms

Aplicación de escritorio para gestión de inventario desarrollada en **Visual Basic .NET** con **Windows Forms** y **MySQL**.

## Requisitos

- .NET 8.0 SDK o superior
- Visual Studio 2022 (recomendado) o Visual Studio Code
- MySQL Server 8.0+

## Configuración de base de datos

La conexión usa variables de entorno (igual que los proyectos Python/Java):

| Variable | Valor por defecto | Descripción |
|----------|-------------------|-------------|
| `DB_HOST` | `localhost` | Servidor MySQL |
| `DB_PORT` | `3306` | Puerto |
| `DB_NAME` | `inventario` | Nombre de la base de datos |
| `DB_USER` | `root` | Usuario |
| `DB_PASS` | *(vacío)* | Contraseña |

Puedes configurarlas antes de ejecutar:

```powershell
$env:DB_HOST="localhost"
$env:DB_USER="root"
$env:DB_PASS="tu_password"
$env:DB_NAME="inventario"
```

## Restaurar paquetes y compilar

```powershell
cd c:\Users\e\Documents\proyectos\inventario\inventario_visual
dotnet restore
dotnet build
```

## Ejecutar

```powershell
dotnet run
```

O abrir `inventario_visual.sln` en Visual Studio y presionar **F5**.

## Estructura del proyecto

```
inventario_visual/
├── ConexionDB.vb          ' Conexión a MySQL
├── Models/              ' Clases de entidad
├── DAO/                 ' Acceso a datos
├── Core/                ' Sesión de usuario
├── Forms/               ' Formularios y paneles
└── Program.vb           ' Punto de entrada
```

## Funcionalidades implementadas

- Login simple contra tabla `usuarios`.
- Dashboard con menú lateral.
- Paneles de: Inicio, Productos, Categorías, Clientes, Proveedores, Inventario, Facturas, Usuarios, Reportes y Configuración.
- Grids de lectura con permisos básicos.
- Paneles de Reportes y Configuración son placeholders para continuar desarrollo.

## Notas

- El login compara contraseñas en texto plano para simplificar la demo. En producción se recomienda usar Argon2 o BCrypt.
- Los formularios de creación/edición de registros están pendientes; los grids muestran la información existente.
