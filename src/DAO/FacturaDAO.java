package DAO;

import CX.ConexionBD;
import Modelo.Factura;
import Utils.AESUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FacturaDAO {

    public List<Factura> listarTodas() throws SQLException {
        List<Factura> lista = new ArrayList<>();
        String sql = "SELECT f.* FROM factura f " +
                     "ORDER BY f.id DESC";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public List<Factura> buscar(String texto) throws SQLException {
        List<Factura> lista = new ArrayList<>();
        String sql = "SELECT f.* FROM factura f " +
                     "WHERE f.numero_factura = ? " +
                     "ORDER BY f.id DESC";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, AESUtil.encriptar(texto));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public String generarNumeroFactura() throws SQLException {
        String sql = "SELECT MAX(id) FROM factura";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            int next = 1;
            if (rs.next()) next = rs.getInt(1) + 1;
            return String.format("FAC-%06d", next);
        }
    }

    public void insertar(Factura f) throws SQLException {
        String sql = "INSERT INTO factura (movimiento_id, cliente_id, numero_factura, metodo_pago, estado, subtotal, impuestos, total) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (f.getMovimientoId() != null) {
                ps.setLong(1, f.getMovimientoId());
            } else {
                ps.setNull(1, Types.BIGINT);
            }
            if (f.getClienteId() != null) {
                ps.setLong(2, f.getClienteId());
            } else {
                ps.setNull(2, Types.BIGINT);
            }
            ps.setString(3, AESUtil.encriptar(f.getNumeroFactura()));
            ps.setString(4, AESUtil.encriptar(f.getMetodoPago()));
            ps.setString(5, AESUtil.encriptar(f.getEstado()));
            ps.setDouble(6, f.getSubtotal());
            ps.setDouble(7, f.getImpuestos());
            ps.setDouble(8, f.getTotal());
            ps.executeUpdate();
        }
    }

    public void actualizarEstado(long id, String estado) throws SQLException {
        String sql = "UPDATE factura SET estado=? WHERE id=?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, AESUtil.encriptar(estado));
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }

    public void eliminar(long id) throws SQLException {
        String sql = "DELETE FROM factura WHERE id=?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    private Factura mapear(ResultSet rs) throws SQLException {
        Factura f = new Factura();
        f.setId(rs.getLong("id"));
        long movId = rs.getLong("movimiento_id");
        f.setMovimientoId(rs.wasNull() ? null : movId);
        long cliId = rs.getLong("cliente_id");
        f.setClienteId(rs.wasNull() ? null : cliId);
        f.setNumeroFactura(AESUtil.desencriptar(rs.getString("numero_factura")));
        f.setFechaEmision(rs.getTimestamp("fecha_emision"));
        f.setMetodoPago(AESUtil.desencriptar(rs.getString("metodo_pago")));
        f.setEstado(AESUtil.desencriptar(rs.getString("estado")));
        f.setSubtotal(rs.getDouble("subtotal"));
        f.setImpuestos(rs.getDouble("impuestos"));
        f.setTotal(rs.getDouble("total"));
        try {
            f.setClienteNombre(AESUtil.desencriptar(rs.getString("cliente_nombre")));
        } catch (Exception e) {}
        return f;
    }
}
