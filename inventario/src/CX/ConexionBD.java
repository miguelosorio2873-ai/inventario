package CX;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import javax.swing.JOptionPane;

public class ConexionBD {

    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/inventario_db";
    private static final String DEFAULT_USER = "";
    private static final String DEFAULT_PASSWORD = "";

    private static Properties cargarConfig() {
        Properties props = new Properties();
        String configPath = System.getProperty("db.config", "config/db.properties");
        File f = new File(configPath);
        if (f.exists()) {
            try (FileInputStream in = new FileInputStream(f)) {
                props.load(in);
            } catch (IOException e) {
                System.err.println("No se pudo cargar " + configPath + ": " + e.getMessage());
            }
        }
        return props;
    }

    private static String getConfig(Properties props, String key, String envVar, String defaultValue) {
        String env = System.getenv(envVar);
        if (env != null && !env.isEmpty()) return env;
        String prop = props.getProperty(key);
        if (prop != null && !prop.isEmpty()) return prop;
        return defaultValue;
    }

    public static Connection conectar() {
        Connection nuevaConexion = null;
        try {
            Properties props = cargarConfig();
            String url = getConfig(props, "db.url", "DB_URL", DEFAULT_URL);
            String user = getConfig(props, "db.user", "DB_USER", DEFAULT_USER);
            String clave = getConfig(props, "db.password", "DB_PASSWORD", DEFAULT_PASSWORD);

            if (user.isEmpty()) {
                System.err.println("Conexión fallida: usuario de base de datos no configurado.");
                return null;
            }

            Class.forName("com.mysql.cj.jdbc.Driver");
            nuevaConexion = DriverManager.getConnection(url, user, clave);
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Conexión fallida: " + e.getMessage());
        }
        return nuevaConexion;
    }

    public static void errorManager(SQLException e) {
        int code = e.getErrorCode();
        String mensaje = e.getMessage();

        switch (code) {
            case 1452:
                JOptionPane.showMessageDialog(null, "Registro referenciado no encontrado");
                break;
            case 1062:
                JOptionPane.showMessageDialog(null, "El registro ya existe (duplicado)");
                break;
            case 1048:
                JOptionPane.showMessageDialog(null, "Campo obligatorio vacío");
                break;
            default:
                JOptionPane.showMessageDialog(null, "Error BD [" + code + "]: " + mensaje);
                break;
        }
    }
}
