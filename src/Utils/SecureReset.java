package Utils;

import java.sql.*;
import CX.ConexionBD;

/**
 * Utilidad para resetear la contraseña del administrador al formato Argon2id.
 * Ejecuta esta clase para poder loguearte si pierdes acceso.
 */
public class SecureReset {
    public static void main(String[] args) {
        String query = "UPDATE usuario SET password = ?, intentos_fallidos = 0, bloqueado_hasta = NULL WHERE id = 1";
        String plainPassword = "123"; // Cambia esto si lo deseas
        
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(query)) {
            
            System.out.println("Generando hash Argon2id (puede tardar un momento)...");
            
            // Generamos el hash seguro usando Argon2id (método instalado en src/FL)
            String hashed = SeguridadArgon2.generarHash(plainPassword);
            
            ps.setString(1, hashed);
            int rows = ps.executeUpdate();
            
            if (rows > 0) {
                System.out.println("✅ Exito: Contrasena del administrador (ID 1) actualizada.");
                System.out.println("Nueva contrasena plana: " + plainPassword);
                System.out.println("Hash generado: " + hashed);
            } else {
                System.out.println("❌ Error: No se encontro el usuario con ID 1.");
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error de base de datos: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Error inesperado: " + e.getMessage());
        }
    }
}
