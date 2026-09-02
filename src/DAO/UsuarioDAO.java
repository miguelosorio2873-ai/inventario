package DAO;

import CX.ConexionBD;
import CX.SesionUsuario;
import Modelo.Usuario;
import Utils.AESUtil;
import Utils.SeguridadArgon2;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    public Usuario login(String email, String password) throws SQLException {
        String sql = "SELECT id, nombre, email, password, rol, permisos, intentos_fallidos, bloqueado_hasta, licencia_activa, licencia_vencimiento FROM usuario WHERE email = ?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, AESUtil.encriptar(email));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long id = rs.getLong("id");
                    String hashDB = rs.getString("password");
                    int intentos = rs.getInt("intentos_fallidos");
                    Timestamp bloqueadoHasta = rs.getTimestamp("bloqueado_hasta");

                    if (bloqueadoHasta != null) {
                        if (bloqueadoHasta.after(new Timestamp(System.currentTimeMillis()))) {
                            long minutosRestantes = (bloqueadoHasta.getTime() - System.currentTimeMillis()) / 60000;
                            if (minutosRestantes <= 0) minutosRestantes = 1;
                            throw new SQLException("🚫 Cuenta bloqueada temporalmente. Intente en " + minutosRestantes + " minutos.");
                        } else {
                            // El tiempo de bloqueo ya pasó. Reseteamos para dar 5 nuevas oportunidades.
                            resetearIntentos(id, con);
                            intentos = 0;
                        }
                    }

                    boolean esValida = false;
                    boolean requiereMigracion = false;
                    if (hashDB != null && hashDB.startsWith("$argon2")) {
                        esValida = SeguridadArgon2.verificar(hashDB, password);
                    } else if (hashDB != null && hashDB.equals(encriptarSHA256(password))) {
                        esValida = true;
                        requiereMigracion = true;
                    }

                    if (esValida) {
                        if (requiereMigracion) migrarAArgon2(id, password, con);
                        resetearIntentos(id, con);
                        Usuario logeado = mapearLogin(rs);
                        SesionUsuario.getInstancia().iniciarSesion(logeado.getId(), logeado.getNombre(), logeado.getRol(),
                            logeado.getPermisos(), logeado.isLicenciaActiva(), logeado.getLicenciaVencimiento());
                        return logeado;
                    } else {
                        registrarFallo(id, intentos, con);
                        throw new SQLException("❌ Credenciales incorrectas.");
                    }
                } else throw new SQLException("❌ Usuario no registrado.");
            }
        }
    }

    public Usuario buscarPorEmail(String email) throws SQLException {
        String sql = "SELECT * FROM usuario WHERE email = ?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, AESUtil.encriptar(email));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapearCompleto(rs);
            }
        }
        return null;
    }

    private void migrarAArgon2(long id, String plaintext, Connection con) throws SQLException {
        String query = "UPDATE usuario SET password = ? WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, SeguridadArgon2.generarHash(plaintext));
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }

    private String encriptarSHA256(String texto) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(texto.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) { return ""; }
    }

    private void registrarFallo(long id, int intentosActuales, Connection con) throws SQLException {
        int nuevos = intentosActuales + 1;
        String sql = "UPDATE usuario SET intentos_fallidos = ?, bloqueado_hasta = ? WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, nuevos);
            ps.setTimestamp(2, (nuevos >= 5) ? Timestamp.valueOf(LocalDateTime.now().plusMinutes(15)) : null);
            ps.setLong(3, id);
            ps.executeUpdate();
        }
    }

    private void resetearIntentos(long id, Connection con) throws SQLException {
        String sql = "UPDATE usuario SET intentos_fallidos = 0, bloqueado_hasta = NULL WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    public void insertar(Usuario u) throws SQLException {
        String sql = "INSERT INTO usuario (nombre, email, password, rol, permisos, pregunta_1, respuesta_1, pregunta_2, respuesta_2, pregunta_3, respuesta_3, pregunta_4, respuesta_4, licencia_activa, licencia_vencimiento) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, AESUtil.encriptar(u.getNombre()));
            ps.setString(2, AESUtil.encriptar(u.getEmail()));
            ps.setString(3, SeguridadArgon2.generarHash(u.getPassword()));
            ps.setString(4, AESUtil.encriptar(u.getRol()));
            ps.setString(5, u.getPermisos()); // Permisos no se encriptan para facilitar busqueda si fuera necesario, o si se prefiere encriptar se puede
            ps.setString(6, AESUtil.encriptar(u.getPregunta1()));
            ps.setString(7, AESUtil.encriptar(u.getRespuesta1()));
            ps.setString(8, AESUtil.encriptar(u.getPregunta2()));
            ps.setString(9, AESUtil.encriptar(u.getRespuesta2()));
            ps.setString(10, AESUtil.encriptar(u.getPregunta3()));
            ps.setString(11, AESUtil.encriptar(u.getRespuesta3()));
            ps.setString(12, AESUtil.encriptar(u.getPregunta4()));
            ps.setString(13, AESUtil.encriptar(u.getRespuesta4()));
            ps.setInt(14, u.isLicenciaActiva() ? 1 : 0);
            ps.setString(15, u.getLicenciaVencimiento());
            ps.executeUpdate();
        }
    }

    public void actualizar(Usuario u) throws SQLException {
        // El propietario ('miguel') no puede ser degradado: conserva rol Admin.
        if (esPropietario(u.getId())) {
            u.setRol("Admin");
            u.setPermisos(""); // Admin con acceso total (permisos vacios = todos)
        }
        String sql = "UPDATE usuario SET nombre=?, email=?, rol=?, permisos=?, pregunta_1=?, respuesta_1=?, pregunta_2=?, respuesta_2=?, pregunta_3=?, respuesta_3=?, pregunta_4=?, respuesta_4=?, licencia_activa=?, licencia_vencimiento=? WHERE id=?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, AESUtil.encriptar(u.getNombre()));
            ps.setString(2, AESUtil.encriptar(u.getEmail()));
            ps.setString(3, AESUtil.encriptar(u.getRol()));
            ps.setString(4, u.getPermisos());
            ps.setString(5, AESUtil.encriptar(u.getPregunta1()));
            ps.setString(6, AESUtil.encriptar(u.getRespuesta1()));
            ps.setString(7, AESUtil.encriptar(u.getPregunta2()));
            ps.setString(8, AESUtil.encriptar(u.getRespuesta2()));
            ps.setString(9, AESUtil.encriptar(u.getPregunta3()));
            ps.setString(10, AESUtil.encriptar(u.getRespuesta3()));
            ps.setString(11, AESUtil.encriptar(u.getPregunta4()));
            ps.setString(12, AESUtil.encriptar(u.getRespuesta4()));
            ps.setInt(13, u.isLicenciaActiva() ? 1 : 0);
            ps.setString(14, u.getLicenciaVencimiento());
            ps.setLong(15, u.getId());
            ps.executeUpdate();
        }
    }

    public void cambiarPassword(long id, String nuevaPassword) throws SQLException {
        String sql = "UPDATE usuario SET password=? WHERE id=?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, SeguridadArgon2.generarHash(nuevaPassword));
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }

    public List<Usuario> listarTodos() throws SQLException {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT * FROM usuario ORDER BY id"; // Cambiado de nombre a id ya que nombre está encriptado
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapearCompleto(rs));
        }
        return lista;
    }

    public Usuario buscarPorId(long id) throws SQLException {
        String sql = "SELECT * FROM usuario WHERE id = ?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapearCompleto(rs);
            }
        }
        return null;
    }

    private Usuario mapearLogin(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getLong("id"));
        u.setNombre(AESUtil.desencriptar(rs.getString("nombre")));
        u.setEmail(AESUtil.desencriptar(rs.getString("email")));
        u.setRol(AESUtil.desencriptar(rs.getString("rol")));
        u.setPermisos(rs.getString("permisos"));
        u.setLicenciaActiva(rs.getInt("licencia_activa") == 1);
        u.setLicenciaVencimiento(rs.getString("licencia_vencimiento"));
        return u;
    }

    private Usuario mapearCompleto(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getLong("id"));
        u.setNombre(AESUtil.desencriptar(rs.getString("nombre")));
        u.setEmail(AESUtil.desencriptar(rs.getString("email")));
        u.setRol(AESUtil.desencriptar(rs.getString("rol")));
        u.setPermisos(rs.getString("permisos"));
        u.setLicenciaActiva(rs.getInt("licencia_activa") == 1);
        u.setLicenciaVencimiento(rs.getString("licencia_vencimiento"));
        u.setPregunta1(AESUtil.desencriptar(rs.getString("pregunta_1")));
        u.setRespuesta1(AESUtil.desencriptar(rs.getString("respuesta_1")));
        u.setPregunta2(AESUtil.desencriptar(rs.getString("pregunta_2")));
        u.setRespuesta2(AESUtil.desencriptar(rs.getString("respuesta_2")));
        u.setPregunta3(AESUtil.desencriptar(rs.getString("pregunta_3")));
        u.setRespuesta3(AESUtil.desencriptar(rs.getString("respuesta_3")));
        u.setPregunta4(AESUtil.desencriptar(rs.getString("pregunta_4")));
        u.setRespuesta4(AESUtil.desencriptar(rs.getString("respuesta_4")));
        return u;
    }

    /**
     * Identifica si un usuario es el propietario intocable ('miguel').
     * Se compara por el email desencriptado.
     */
    public static boolean esPropietario(long id) throws SQLException {
        return esEmailPropietario(emailPorId(id));
    }

    public static boolean esEmailPropietario(String correo) {
        String email = correo == null ? "" : correo.trim();
        return email.equalsIgnoreCase("miguelosorio2873@gmail.com");
    }

    private static String emailPorId(long id) throws SQLException {
        String sql = "SELECT email FROM usuario WHERE id = ?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return AESUtil.desencriptar(rs.getString("email"));
            }
        }
        return null;
    }

    /**
     * Verifica la contraseña (Argon2) de un usuario por su correo (desencriptado).
     * Usado para autorizar la renovación de la licencia por el propietario.
     */
    public boolean verificarClavePropietario(String correo, String password) throws SQLException {
        String sql = "SELECT email, password FROM usuario";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String em = AESUtil.desencriptar(rs.getString("email"));
                if (em.equalsIgnoreCase(correo == null ? "" : correo.trim())) {
                    String hash = rs.getString("password");
                    if (hash != null && hash.startsWith("$argon2")) {
                        return SeguridadArgon2.verificar(hash, password);
                    }
                    return hash != null && hash.equals(encriptarSHA256(password));
                }
            }
        }
        return false;
    }

    public void eliminar(long id) throws SQLException {
        // El propietario ('miguel') no puede ser eliminado por nadie.
        if (esPropietario(id)) {
            throw new SQLException("🚫 El usuario propietario ('miguel') no puede ser eliminado.");
        }
        String sql = "DELETE FROM usuario WHERE id=?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    /** Actualiza la configuración de licencia de un usuario (activa + fecha de vencimiento). */
    public void actualizarLicencia(long id, boolean activa, String vencimiento) throws SQLException {
        String sql = "UPDATE usuario SET licencia_activa = ?, licencia_vencimiento = ? WHERE id = ?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, activa ? 1 : 0);
            ps.setString(2, vencimiento);
            ps.setLong(3, id);
            ps.executeUpdate();
        }
    }
}
