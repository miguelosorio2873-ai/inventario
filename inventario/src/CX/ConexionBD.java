package CX;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class ConexionBD {

    private static final String url = "jdbc:mysql://localhost:3306/inventario_db";
    private static final String user = "root";
    private static final String clave = "";


    public static Connection conectar() {
        Connection nuevaConexion = null;
        try {
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
