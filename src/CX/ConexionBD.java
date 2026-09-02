package CX;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JOptionPane;

public class ConexionBD {

    private static final String URL = "jdbc:sqlite:" + resolverRutaBaseDatos();

    private static String resolverRutaBaseDatos() {
        java.io.File raiz = buscarRaizProyecto();
        java.io.File db = new java.io.File(raiz, "inventario.db");
        return db.getAbsolutePath().replace("\\", "/");
    }

    private static boolean esRaizProyecto(java.io.File d) {
        if (d == null) return false;
        if (new java.io.File(d, "nbproject").isDirectory()) return true;
        return new java.io.File(d, "pom.xml").isFile() && new java.io.File(d, "src").isDirectory();
    }

    private static java.io.File buscarRaizProyecto() {
        java.io.File actual = new java.io.File(System.getProperty("user.dir")).getAbsoluteFile();

        java.io.File porCwd = actual;
        if (esRaizProyecto(porCwd)) return porCwd;

        java.io.File[] hijos = actual.listFiles(java.io.File::isDirectory);
        if (hijos != null) {
            for (java.io.File h : hijos) {
                if (esRaizProyecto(h)) return h;
            }
        }

        java.io.File asc = actual.getParentFile();
        while (asc != null) {
            if (esRaizProyecto(asc)) return asc;
            asc = asc.getParentFile();
        }

        // Modo instalado (no hay estructura de proyecto): usar el directorio del
        // codigo/jar (dona se encuentra el ejecutable y la base de datos).
        java.io.File claseDir = directorioDelCodigo();
        if (claseDir != null) return claseDir;
        return actual;
    }

    /** Directorio donde se encuentra el .class de esta clase (el app desplegada). */
    private static java.io.File directorioDelCodigo() {
        try {
            java.net.URI uri = ConexionBD.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            java.io.File f = new java.io.File(uri);
            if (f.isDirectory()) return f.getAbsoluteFile();
            if (f.isFile()) return f.getParentFile();
        } catch (Exception e) {
            // ignorar
        }
        return null;
    }

    /** Devuelve la ruta absoluta del archivo de base de datos (inventario.db). */
    public static String getRutaBaseDatos() {
        return resolverRutaBaseDatos();
    }

    /** Devuelve el archivo real de la base de datos. */
    public static File getArchivoBaseDatos() {
        return new File(getRutaBaseDatos());
    }

    public static Connection conectar() {
        Connection nuevaConexion = null;
        try {
            Class.forName("org.sqlite.JDBC");
            Connection real = DriverManager.getConnection(URL);
            try (Statement stmt = real.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
            }
            if (!tieneReplica()) {
                nuevaConexion = real;
            } else {
                final Connection fReal = real;
                final Object lock = new Object();
                nuevaConexion = (Connection) java.lang.reflect.Proxy.newProxyInstance(
                    Connection.class.getClassLoader(),
                    new Class<?>[]{Connection.class},
                    (proxy, method, args) -> {
                        String name = method.getName();
                        Object result = method.invoke(fReal, args);
                        if ((name.equals("createStatement") || name.equals("prepareStatement")
                             || name.equals("prepareCall")) && result instanceof java.sql.Statement) {
                            java.sql.Statement st = (java.sql.Statement) result;
                            Class<?> tipo = java.sql.Statement.class;
                            if (name.equals("prepareStatement")) tipo = java.sql.PreparedStatement.class;
                            else if (name.equals("prepareCall")) tipo = java.sql.CallableStatement.class;
                            return envolverStatement(st, lock, tipo);
                        }
                        return result;
                    });
            }
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Conexion fallida: " + e.getMessage());
        }
        return nuevaConexion;
    }

    /** Envuelve un Statement para detectar escrituras persistentes y sincronizar la replica. */
    private static java.sql.Statement envolverStatement(java.sql.Statement st, Object lock, Class<?> tipo) {
        return (java.sql.Statement) java.lang.reflect.Proxy.newProxyInstance(
            Statement.class.getClassLoader(),
            new Class<?>[]{tipo},
            (proxy, method, args) -> {
                String name = method.getName();
                Object result = method.invoke(st, args);
                // Solo escrituras DML/DDL, ignoramos queries de lectura.
                if (name.equals("executeUpdate") || name.equals("execute")) {
                    String sql = primerSql(args);
                    if (sql != null && esEscritura(sql)) {
                        sincronizarReplica(lock);
                    }
                }
                return result;
            });
    }

    private static String primerSql(Object[] args) {
        if (args == null || args.length == 0) return null;
        Object a0 = args[0];
        if (a0 instanceof String) return (String) a0;
        if (a0 instanceof java.sql.PreparedStatement) {
            // En Statement.execute() sin SQL para Prepared, detectamos igual marcandolo.
            return "*";
        }
        return null;
    }

    private static boolean esEscritura(String sql) {
        if (sql == null || sql.isEmpty()) return false;
        String s = sql.trim().toUpperCase();
        // Por defecto, marcar como escritura (los Statements usados son de escritura).
        return !s.startsWith("SELECT") && !s.startsWith("PRAGMA") && !s.startsWith("WITH");
    }

    private static volatile boolean replicaSincronizando = false;

    /** Copia la BD principal sobre la replica configurada (espejo). */
    public static void sincronizarReplica() {
        Object lock = new Object();
        sincronizarReplica(lock);
    }

    private static void sincronizarReplica(Object lock) {
        String rutaReplica = Utils.Config.getRutaReplica();
        if (rutaReplica == null || rutaReplica.trim().isEmpty()) return;
        if (replicaSincronizando) return;
        replicaSincronizando = true;
        try {
            File principal = getArchivoBaseDatos();
            if (!principal.exists()) return;
            File replica = new File(rutaReplica);
            if (replica.getParentFile() != null) replica.getParentFile().mkdirs();
            java.nio.file.Files.copy(principal.toPath(), replica.toPath(),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            System.err.println("Error sincronizando replica: " + e.getMessage());
        } finally {
            replicaSincronizando = false;
        }
    }

    private static boolean tieneReplica() {
        String ruta = Utils.Config.getRutaReplica();
        return ruta != null && !ruta.trim().isEmpty();
    }

    // ---- Respaldo automatico ----

    private static java.util.Timer timerRespaldo = null;

    /** Ejecuta un respaldo automatico al mismo archivo respaldo_inventario.db (sobrescribe). */
    public static void ejecutarRespaldoAutomatico() {
        String dir = Utils.Config.getRespaldoDirectorio();
        if (dir == null || dir.trim().isEmpty()) return;
        File destino = new File(dir);
        if (destino.isDirectory()) {
            destino = new File(dir, "respaldo_inventario.db");
        } else if (!destino.getName().toLowerCase().endsWith(".db")) {
            destino = new File(dir, "respaldo_inventario.db");
        }
        try {
            respaldarBaseDatos(destino);
            System.out.println("Respaldo automatico completado: " + destino.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("Respaldo automatico fallido: " + e.getMessage());
        }
    }

    /** Programa el respaldo automatico segun Config (frecuencia y unidad). */
    public static void programarRespaldoAutomatico() {
        detenerRespaldoAutomatico();
        double frecuencia = Utils.Config.getRespaldoFrecuencia();
        if (frecuencia <= 0) return;
        long ms;
        switch (Utils.Config.getRespaldoUnidad()) {
            case "HORAS": ms = (long)(frecuencia * 3600_000L); break;
            case "DIAS":  ms = (long)(frecuencia * 86400_000L); break;
            default:      ms = (long)(frecuencia * 60_000L); break;
        }
        if (ms <= 0) return;
        timerRespaldo = new java.util.Timer("RespaldoAutomatico", true);
        timerRespaldo.scheduleAtFixedRate(new java.util.TimerTask() {
            public void run() { ejecutarRespaldoAutomatico(); }
        }, ms, ms);
    }

    public static void detenerRespaldoAutomatico() {
        if (timerRespaldo != null) {
            timerRespaldo.cancel();
            timerRespaldo = null;
        }
    }

    public static void inicializarBaseDatos() {
        try (Connection con = conectar(); Statement stmt = con.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS categorias (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT NOT NULL, descripcion TEXT)");

            // Categorias predeterminadas (solo si la tabla esta vacia)
            ResultSet rsCat = stmt.executeQuery("SELECT COUNT(*) FROM categorias");
            rsCat.next();
            if (rsCat.getInt(1) == 0) {
                String[] cats = {"General", "Electronica", "Alimentos", "Bebidas", "Ropa", "Hogar", "Deportes", "Salud", "Papeleria", "Otros"};
                for (String cat : cats) {
                    String enc = Utils.AESUtil.encriptar(cat);
                    stmt.executeUpdate("INSERT INTO categorias (nombre, descripcion) VALUES ('" + enc + "', '" + enc + "')");
                }
            }
            rsCat.close();
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS cliente (id INTEGER PRIMARY KEY AUTOINCREMENT, cedula TEXT UNIQUE, nombre TEXT NOT NULL, correo TEXT, telefono TEXT)");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS proveedor (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre_empresa TEXT NOT NULL, nit_cedula TEXT, telefono TEXT, direccion TEXT, correo TEXT, nombre_contacto TEXT)");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS producto (id INTEGER PRIMARY KEY AUTOINCREMENT, categoria_id INTEGER, sku TEXT UNIQUE, nombre TEXT NOT NULL, descripcion TEXT, precio_venta REAL NOT NULL DEFAULT 0, costo_promedio REAL DEFAULT 0, stock_actual REAL DEFAULT 0, state INTEGER NOT NULL DEFAULT 1, presentacion TEXT, unidades_presentacion REAL DEFAULT 0, costo_presentacion REAL DEFAULT 0, imagen TEXT, FOREIGN KEY (categoria_id) REFERENCES categorias(id) ON DELETE SET NULL)");
            // Migración: agrega columnas de presentación si no existen (BD creadas antes)
            String[][] colsProducto = {{"presentacion", "TEXT"}, {"unidades_presentacion", "REAL DEFAULT 0"}, {"costo_presentacion", "REAL DEFAULT 0"}};
            for (String[] cc : colsProducto) {
                boolean existe = false;
                try (ResultSet pr = stmt.executeQuery("PRAGMA table_info(producto)")) {
                    while (pr.next()) {
                        if (cc[0].equalsIgnoreCase(pr.getString("name"))) { existe = true; break; }
                    }
                } catch (SQLException e) {}
                if (!existe) {
                    try {
                        stmt.executeUpdate("ALTER TABLE producto ADD COLUMN " + cc[0] + " " + cc[1]);
                    } catch (SQLException e) {}
                }
            }
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS inventario (id INTEGER PRIMARY KEY AUTOINCREMENT, producto_id INTEGER NOT NULL, proveedor_id INTEGER, precio REAL NOT NULL DEFAULT 0, precio_balance REAL NOT NULL DEFAULT 0, cantidad REAL NOT NULL DEFAULT 0, tipo_movimiento TEXT, fecha_movimiento DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, motivo TEXT, FOREIGN KEY (producto_id) REFERENCES producto(id) ON DELETE CASCADE, FOREIGN KEY (proveedor_id) REFERENCES proveedor(id) ON DELETE SET NULL)");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS factura (id INTEGER PRIMARY KEY AUTOINCREMENT, movimiento_id INTEGER, cliente_id INTEGER, numero_factura TEXT UNIQUE, fecha_emision DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, metodo_pago TEXT, estado TEXT, subtotal REAL NOT NULL DEFAULT 0, impuestos REAL NOT NULL DEFAULT 0, total REAL NOT NULL DEFAULT 0, FOREIGN KEY (movimiento_id) REFERENCES inventario(id) ON DELETE SET NULL, FOREIGN KEY (cliente_id) REFERENCES cliente(id) ON DELETE SET NULL)");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS usuario (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT NOT NULL, email TEXT NOT NULL UNIQUE, password TEXT, rol TEXT, pregunta_1 TEXT, pregunta_2 TEXT, pregunta_3 TEXT, pregunta_4 TEXT, respuesta_1 TEXT, respuesta_2 TEXT, respuesta_3 TEXT, respuesta_4 TEXT, intentos_fallidos INTEGER DEFAULT 0, bloqueado_hasta DATETIME, ultimo_login DATETIME, permisos TEXT)");
            // Migración: licencia por usuario (activa + fecha de vencimiento).
            String[][] colsUsuario = {{"licencia_activa", "INTEGER DEFAULT 0"}, {"licencia_vencimiento", "TEXT"}};
            for (String[] cc : colsUsuario) {
                boolean existe = false;
                try (ResultSet pr = stmt.executeQuery("PRAGMA table_info(usuario)")) {
                    while (pr.next()) { if (cc[0].equalsIgnoreCase(pr.getString("name"))) { existe = true; break; } }
                } catch (SQLException e) {}
                if (!existe) {
                    try { stmt.executeUpdate("ALTER TABLE usuario ADD COLUMN " + cc[0] + " " + cc[1]); } catch (SQLException e) {}
                }
            }
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS detalle_factura (id INTEGER PRIMARY KEY AUTOINCREMENT, factura_id INTEGER NOT NULL, producto_id INTEGER NOT NULL, cantidad REAL NOT NULL DEFAULT 0, precio_unitario REAL NOT NULL DEFAULT 0, subtotal REAL NOT NULL DEFAULT 0, FOREIGN KEY (factura_id) REFERENCES factura(id) ON DELETE CASCADE, FOREIGN KEY (producto_id) REFERENCES producto(id) ON DELETE RESTRICT)");

            // Migración: congelar el equivalente en Bs de cada factura/detalle con su tasa del momento.
            // Agrega columnas si no existen (BD creadas previamente solo guardaban USD).
            String[][] colsFactura = {{"tasa_ves", "REAL DEFAULT 0"}, {"subtotal_bs", "REAL DEFAULT 0"}, {"total_bs", "REAL DEFAULT 0"}};
            for (String[] cc : colsFactura) {
                boolean existe = false;
                try (ResultSet pr = stmt.executeQuery("PRAGMA table_info(factura)")) {
                    while (pr.next()) { if (cc[0].equalsIgnoreCase(pr.getString("name"))) { existe = true; break; } }
                } catch (SQLException e) {}
                if (!existe) {
                    try { stmt.executeUpdate("ALTER TABLE factura ADD COLUMN " + cc[0] + " " + cc[1]); } catch (SQLException e) {}
                }
            }
            String[][] colsDetalle = {{"tasa_ves", "REAL DEFAULT 0"}, {"precio_unitario_bs", "REAL DEFAULT 0"}, {"subtotal_bs", "REAL DEFAULT 0"}};
            for (String[] cc : colsDetalle) {
                boolean existe = false;
                try (ResultSet pr = stmt.executeQuery("PRAGMA table_info(detalle_factura)")) {
                    while (pr.next()) { if (cc[0].equalsIgnoreCase(pr.getString("name"))) { existe = true; break; } }
                } catch (SQLException e) {}
                if (!existe) {
                    try { stmt.executeUpdate("ALTER TABLE detalle_factura ADD COLUMN " + cc[0] + " " + cc[1]); } catch (SQLException e) {}
                }
            }
            // Histórico de tasas por día (se puebla desde hoy en adelante).
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS tasa_cambio (fecha TEXT PRIMARY KEY, tasa_ves REAL NOT NULL, actualizado DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP)");

            // Backfill: facturas antiguas sin total_bs (solo guardaban USD) se congelan.
            // Usa la tasa del historial de su fecha_emision si existe; si no, la vigente.
            try {
                double tasaActual = Utils.Config.getTasaVES();
                int updFact = 0, updDet = 0;
                try (ResultSet rsF = stmt.executeQuery("SELECT id, fecha_emision, subtotal, total FROM factura WHERE total_bs = 0 OR total_bs IS NULL")) {
                    while (rsF.next()) {
                        long idF = rsF.getLong("id");
                        double sub = rsF.getDouble("subtotal");
                        double tot = rsF.getDouble("total");
                        double tasa = tasaPorFecha(stmt, rsF.getTimestamp("fecha_emision"), tasaActual);
                        stmt.executeUpdate("UPDATE factura SET tasa_ves = " + tasa +
                            ", subtotal_bs = " + Math.round(sub * tasa * 100) / 100.0 +
                            ", total_bs = " + Math.round(tot * tasa * 100) / 100.0 + " WHERE id = " + idF);
                        updFact++;
                        stmt.executeUpdate("UPDATE detalle_factura SET tasa_ves = " + tasa +
                            ", precio_unitario_bs = ROUND(precio_unitario * " + tasa + ", 2), subtotal_bs = ROUND(subtotal * " + tasa + ", 2) WHERE factura_id = " + idF + " AND (subtotal_bs = 0 OR subtotal_bs IS NULL)");
                        updDet++;
                    }
                }
                if (updFact + updDet > 0) System.out.println("Facturas congeladas en Bs: " + updFact + " factura(s), " + updDet + " detalle(s).");
            } catch (SQLException e) { System.err.println("Error congelando Bs: " + e.getMessage()); }
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS bitacora (id INTEGER PRIMARY KEY AUTOINCREMENT, usuario_id INTEGER, usuario_nombre TEXT, modulo TEXT, accion TEXT, detalle TEXT, fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE SET NULL)");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS tasa_cambio (fecha TEXT PRIMARY KEY, tasa_ves REAL NOT NULL, actualizado DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP)");

            ResultSet rsUsr = stmt.executeQuery("SELECT COUNT(*) FROM usuario");
            rsUsr.next();
            if (rsUsr.getInt(1) == 0) {
                String hash = Utils.SeguridadArgon2.generarHash("Admin1234@");
                String[] emails = {"miguelosorio2873@gmail.com", "@ezequiel"};
                for (String email : emails) {
                    String encEmail = Utils.AESUtil.encriptar(email);
                    String encNombre = Utils.AESUtil.encriptar(email.contains("@ezequiel") ? "Ezequiel" : "Miguel");
                    String encRol = Utils.AESUtil.encriptar("Admin");
                    stmt.executeUpdate("INSERT INTO usuario (nombre, email, password, rol, permisos) VALUES ('" + encNombre + "', '" + encEmail + "', '" + hash + "', '" + encRol + "', NULL)");
                }
                System.out.println("Usuarios predeterminados creados.");
            }
            rsUsr.close();

            System.out.println("Base de datos SQLite inicializada correctamente.");
        } catch (SQLException e) {
            System.err.println("Error inicializando BD: " + e.getMessage());
        }
    }

    /** Tasa del historial (tasa_cambio) para una fecha; si no existe, FALLBACK. */
    private static double tasaPorFecha(Statement stmt, Timestamp fecha, double fallback) {
        if (fecha == null) return fallback;
        try {
            String f = new SimpleDateFormat("yyyy-MM-dd").format(new Date(fecha.getTime()));
            try (ResultSet rs = stmt.executeQuery("SELECT tasa_ves FROM tasa_cambio WHERE fecha = '" + f + "'")) {
                if (rs.next()) return rs.getDouble("tasa_ves");
            }
            try (ResultSet rs = stmt.executeQuery("SELECT tasa_ves FROM tasa_cambio WHERE fecha < '" + f + "' ORDER BY fecha DESC LIMIT 1")) {
                if (rs.next()) return rs.getDouble("tasa_ves");
            }
        } catch (SQLException e) {
            // ignorar y usar fallback
        }
        return fallback;
    }

    public static void errorManager(SQLException e) {
        String state = e.getSQLState();
        String mensaje = e.getMessage();
        if (mensaje != null && mensaje.contains("FOREIGN KEY")) {
            JOptionPane.showMessageDialog(null, "Registro referenciado no encontrado");
        } else if (mensaje != null && mensaje.contains("UNIQUE constraint")) {
            JOptionPane.showMessageDialog(null, "El registro ya existe (duplicado)");
        } else if (mensaje != null && mensaje.contains("NOT NULL constraint")) {
            JOptionPane.showMessageDialog(null, "Campo obligatorio vacio");
        } else {
            JOptionPane.showMessageDialog(null, "Error BD [" + state + "]: " + mensaje);
        }
    }

    /** Crea una copia de respaldo consistente de la base de datos (aun con conexiones abiertas). */
    public static boolean respaldarBaseDatos(java.io.File destino) throws SQLException, java.io.IOException {
        destino.getParentFile().mkdirs();
        String dest = destino.getAbsolutePath().replace("\\", "/");
        try (Connection con = conectar(); Statement stmt = con.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = OFF");
            // VACUUM INTO crea un archivo de respaldo válido e independiente.
            stmt.execute("VACUUM INTO '" + dest.replace("'", "''") + "'");
        }
        return destino.isFile() && destino.length() > 0;
    }

    /**
     * Restaura la base de datos desde un archivo de respaldo.
     * Reemplaza el archivo actual tras validar que el respaldo es una BD SQLite.
     */
    public static boolean restaurarBaseDatos(java.io.File origen) throws Exception {
        if (origen == null || !origen.isFile()) {
            throw new IllegalArgumentException("El archivo de respaldo no existe.");
        }
        // Validar encabezado SQLite (16 bytes): "SQLite format 3" + NUL.
        byte[] cab = new byte[16];
        try (java.io.FileInputStream fis = new java.io.FileInputStream(origen)) {
            int leidos = fis.read(cab);
            if (leidos < 16) throw new IllegalArgumentException("El archivo no es una base de datos SQLite válida.");
        }
        String header = new String(cab, java.nio.charset.StandardCharsets.ISO_8859_1);
        if (!header.startsWith("SQLite format 3")) {
            throw new IllegalArgumentException("El archivo seleccionado no es un respaldo válido de base de datos.");
        }

        File actual = getArchivoBaseDatos();
        File bak = new File(actual.getAbsolutePath() + ".bak");
        try {
            if (actual.exists()) {
                // Copia de seguridad del backup actual por seguridad.
                java.nio.file.Files.copy(actual.toPath(), bak.toPath(),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
            // Reemplazar el archivo activo por el respaldo.
            java.nio.file.Files.copy(origen.toPath(), actual.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (Exception e) {
            // Si fallo, intentar restaurar el archivo previo.
            if (bak.exists()) {
                java.nio.file.Files.copy(bak.toPath(), actual.toPath(),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
            throw e;
        }
    }
}
