# 📑 Documentación Técnica: Sistema de Inventario Pro (SIG)

Este documento detalla la arquitectura, el funcionamiento de cada componente y el sistema de seguridad de encriptación implementado.

---

## 🏗️ Arquitectura del Software
El sistema sigue un patrón de diseño **DAO (Data Access Object)**, que separa la lógica de negocio de la persistencia de datos.

### 📁 Desglose de Paquetes y Archivos

#### 1. `CX` (Conexión)
*   **`ConexionBD.java`**: Gestiona el enlace con MySQL utilizando JDBC. Contiene el método `conectar()` y un gestor de errores SQL (`errorManager`) para mostrar mensajes amigables al usuario en caso de fallos de integridad.

#### 2. `DAO` (Acceso a Datos)
*Contiene la lógica para interactuar con la base de datos. Todos estos archivos han sido actualizados para manejar encriptación.*
*   **`ProductoDAO.java`**: CRUD de productos. Gestiona SKUs, nombres y stocks.
*   **`UsuarioDAO.java`**: Maneja el login, registro y bloqueo de cuentas. Usa encriptación para datos personales y hashing para contraseñas.
*   **`ClienteDAO.java` / `ProveedorDAO.java`**: Gestionan las entidades comerciales.
*   **`FacturaDAO.java`**: Se encarga de la emisión de recibos y el histórico de ventas.
*   **`InventarioDAO.java`**: Controla las entradas y salidas física de mercancía.

#### 3. `Modelo` (Entidades)
*Archivos POJO que definen la estructura de los datos en memoria.*
*   `Producto.java`, `Cliente.java`, `Factura.java`, etc.

#### 4. `Utils` (Utilidades y Seguridad)
*   **`AESUtil.java`**: El núcleo de la seguridad. Implementa cifrado simétrico **AES-128**. Es el responsable de que los datos en la base de datos sean ilegibles sin la clave maestra.
*   **`SeguridadArgon2.java`**: Implementa el algoritmo **Argon2id** para las contraseñas. A diferencia de AES, esto no es reversible (es un hash), lo que lo hace extremadamente seguro.
*   **`Config.java`**: Carga configuraciones desde el archivo `application.properties`.
*   **`LimitadorCaracteres.java`**: Asegura que los datos ingresados en la interfaz no excedan los límites de la base de datos.
*   **`SecureReset.java`**: Lógica para el restablecimiento seguro de cuentas.

#### 5. `IG` (Interfaz Gráfica)
*Contiene todos los formularios (JFrame) y paneles (JPanel) diseñados con una estética moderna (FlatLaf).*
*   `LOG.java`, `Dashboard.java`, `PanelProductos.java`, etc.

---

## 🔐 El Proceso de Encriptación

El sistema utiliza **Cifrado en Reposo**. Esto significa que los datos se protegen antes de tocar el disco duro de la base de datos.

### 🛠️ ¿Cómo funciona AES-128?
1.  **Clave Maestra**: Se utiliza una llave de 16 caracteres (`AntigravityKey26`) definida en `AESUtil`.
2.  **Cifrado**: El texto plano se convierte en un bloque de bytes cifrados.
3.  **Codificación**: Los bytes se convierten a una cadena **Base64** para que puedan guardarse de forma segura en columnas `VARCHAR` o `TEXT` de MySQL.

### 🔄 Flujo de Datos (Data Flow)

**Escritura (Guardar un Cliente):**
1.  El usuario escribe "Juan" en la interfaz (`IG`).
2.  El programa llama al método `dao.insertar(cliente)`.
3.  Dentro del DAO, se ejecuta: `AESUtil.encriptar("Juan")`.
4.  El resultado es una cadena como `EE9l2JAuaFQubSKKPo...`.
5.  Esa cadena es la que se envía mediante SQL a la base de datos.

**Lectura (Ver la lista de Clientes):**
1.  El programa ejecuta `SELECT * FROM cliente`.
2.  El ResultSet devuelve la cadena cifrada.
3.  El DAO llama al método `mapear()` y ejecuta: `AESUtil.desencriptar("EE9l2JAuaFQubSKKPo...")`.
4.  La cadena vuelve a ser "Juan".
5.  El objeto `Cliente` con el nombre real se muestra en la tabla de la interfaz.

---

## 📊 Vistas y Cálculos Especiales
*   **`vista_stock`**: Es una vista programada dentro de MySQL. Debido a que los tipos de movimiento ('entrada', 'salida') están encriptados, la vista ha sido programada para reconocer los códigos cifrados exactos. Esto permite que el cálculo de inventario sea instantáneo desde la base de datos sin comprometer la seguridad.

---

## ⚙️ Configuración y Dependencias
*   **`application.properties`**: Define la URL de conexión y parámetros variables.
*   **`pom.xml`**: Gestiona las librerías críticas:
    *   `mysql-connector-j`: Para hablar con la base de datos.
    *   `flatlaf`: Para la interfaz premium.
    *   `argon2-jvm`: Para el hashing de contraseñas de alta seguridad.
    *   `apache-poi`: Para la exportación a Excel.
