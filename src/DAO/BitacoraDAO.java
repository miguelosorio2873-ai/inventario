package DAO;

import CX.ConexionBD;
import CX.SesionUsuario;
import Modelo.Bitacora;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BitacoraDAO {

    public void registrar(String modulo, String accion, String detalle) {
        String sql = "INSERT INTO bitacora (usuario_id, usuario_nombre, modulo, accion, detalle) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            SesionUsuario sesion = SesionUsuario.getInstancia();
            Long userId = sesion.getUsuarioId();
            if (userId != null) ps.setLong(1, userId);
            else ps.setNull(1, Types.BIGINT);
            ps.setString(2, sesion.getNombreUsuario() != null ? sesion.getNombreUsuario() : "Sistema");
            ps.setString(3, modulo);
            ps.setString(4, accion);
            ps.setString(5, detalle);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error bitacora: " + e.getMessage());
        }
    }

    public List<Bitacora> listarTodas() throws SQLException {
        List<Bitacora> lista = new ArrayList<>();
        String sql = "SELECT * FROM bitacora ORDER BY id DESC LIMIT 500";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public List<Bitacora> buscar(String texto) throws SQLException {
        List<Bitacora> lista = new ArrayList<>();
        String sql = "SELECT * FROM bitacora WHERE usuario_nombre LIKE ? OR modulo LIKE ? OR accion LIKE ? OR detalle LIKE ? ORDER BY id DESC LIMIT 500";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            String t = "%" + texto + "%";
            ps.setString(1, t);
            ps.setString(2, t);
            ps.setString(3, t);
            ps.setString(4, t);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public List<Bitacora> filtrarPorModulo(String modulo) throws SQLException {
        List<Bitacora> lista = new ArrayList<>();
        String sql = "SELECT * FROM bitacora WHERE modulo = ? ORDER BY id DESC LIMIT 500";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, modulo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    private Bitacora mapear(ResultSet rs) throws SQLException {
        Bitacora b = new Bitacora();
        b.setId(rs.getLong("id"));
        long uid = rs.getLong("usuario_id");
        b.setUsuarioId(rs.wasNull() ? null : uid);
        b.setUsuarioNombre(rs.getString("usuario_nombre"));
        b.setModulo(rs.getString("modulo"));
        b.setAccion(rs.getString("accion"));
        b.setDetalle(rs.getString("detalle"));
        b.setFecha(rs.getTimestamp("fecha"));
        return b;
    }
}
