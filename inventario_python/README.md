# Inventario Pro - Versión Python

Sistema de gestión de inventario en Python con la misma funcionalidad que la versión Java.

## Requisitos

1. XAMPP con MySQL/MariaDB ejecutándose en localhost:3306
2. Base de datos `inventario_db` importada desde `inventario.sql`
3. Python 3.9+

## Instalación

```bash
pip install -r requirements.txt
```

## Ejecución

```bash
python main.py
```

## Módulos

- **Login**: Autenticación con Argon2 y preguntas de seguridad
- **Dashboard**: Panel principal con sidebar y permisos
- **Productos**: CRUD completo con categorías
- **Categorías**: CRUD completo
- **Clientes**: CRUD completo
- **Proveedores**: CRUD completo
- **Inventario**: Movimientos de entrada/salida/ajuste
- **Facturas**: Gestión de facturas
- **Usuarios**: Gestión de usuarios con permisos
- **Reportes**: Generador personalizable con filtros y exportación a Excel
- **Configuración**: Ajustes del sistema
