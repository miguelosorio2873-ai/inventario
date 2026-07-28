package DAO;

import CX.ConexionBD;
import Modelo.MovimientoInventario;
import Utils.AESUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InventarioDAO {

    public List<MovimientoInventario> listarMovimientos() throws SQLException {
        List<MovimientoInventario> lista = new ArrayList<>();
        String sql = "SELECT i.* FROM inventario i " +
                     "ORDER BY i.id DESC";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public List<MovimientoInventario> listarMovimientosConNombres() throws SQLException {
        List<MovimientoInventario> lista = new ArrayList<>();
        String sql = "SELECT i.*, p.nombre as producto_nombre, pr.nombre_empresa as proveedor_nombre " +
                     "FROM inventario i " +
                     "LEFT JOIN producto p ON i.producto_id = p.id " +
                     "LEFT JOIN proveedor pr ON i.proveedor_id = pr.id " +
                     "ORDER BY i.id DESC";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public List<MovimientoInventario> buscarPorProducto(String texto) throws SQLException {
        // Simplificado para evitar JOINs complejos con nombres encriptados que requieren lógica de búsqueda distinta
        List<MovimientoInventario> lista = new ArrayList<>();
        for (MovimientoInventario m : listarMovimientos()) {
            if (m.getProductoNombre() != null && m.getProductoNombre().toLowerCase().contains(texto.toLowerCase())) {
                lista.add(m);
            }
        }
        return lista;
    }

    public long registrarMovimiento(MovimientoInventario m) throws SQLException {
        String sqlMov = "INSERT INTO inventario (producto_id, proveedor_id, precio, precio_balance, cantidad, tipo_movimiento, motivo) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)";
        String tipo = m.getTipoMovimiento();
        String sqlProd;
        double ajuste;
        if ("Ajuste".equals(tipo)) {
            sqlProd = "UPDATE producto SET stock_actual = ? WHERE id = ?";
            ajuste = m.getCantidad();
        } else {
            sqlProd = "UPDATE producto SET stock_actual = stock_actual + ? WHERE id = ?";
            ajuste = "Entrada".equals(tipo) ? m.getCantidad() : -m.getCantidad();
        }

        try (Connection con = ConexionBD.conectar()) {
            con.setAutoCommit(false);
            try {
                long id = -1;
                try (PreparedStatement ps = con.prepareStatement(sqlMov, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setLong(1, m.getProductoId());
                    if (m.getProveedorId() != null) ps.setLong(2, m.getProveedorId());
                    else ps.setNull(2, Types.BIGINT);
                    ps.setDouble(3, m.getPrecio());
                    ps.setDouble(4, m.getPrecioBalance());
                    ps.setDouble(5, m.getCantidad());
                    ps.setString(6, AESUtil.encriptar(tipo));
                    ps.setString(7, AESUtil.encriptar(m.getMotivo()));
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) id = keys.getLong(1);
                    }
                }

                try (PreparedStatement ps = con.prepareStatement(sqlProd)) {
                    ps.setDouble(1, ajuste);
                    ps.setLong(2, m.getProductoId());
                    ps.executeUpdate();
                }

                con.commit();
                return id;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    public double obtenerStockProducto(long productoId) throws SQLException {
        String sql = "SELECT stock_actual FROM producto WHERE id=?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, productoId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble("stock_actual");
            }
        }
        return 0;
    }

    public double obtenerVentasMes() throws SQLException {
        String encAnulada = AESUtil.encriptar("Anulada");
        String sql = "SELECT COALESCE(SUM(f.total), 0) FROM factura f " +
                     "WHERE MONTH(f.fecha_emision)=MONTH(CURRENT_DATE()) " +
                     "AND YEAR(f.fecha_emision)=YEAR(CURRENT_DATE()) " +
                     "AND f.estado != ?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, encAnulada);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        }
        return 0;
    }

    private MovimientoInventario mapear(ResultSet rs) throws SQLException {
        MovimientoInventario m = new MovimientoInventario();
        m.setId(rs.getLong("id"));
        m.setProductoId(rs.getLong("producto_id"));
        long provId = rs.getLong("proveedor_id");
        m.setProveedorId(rs.wasNull() ? null : provId);
        m.setPrecio(rs.getDouble("precio"));
        m.setPrecioBalance(rs.getDouble("precio_balance"));
        m.setCantidad(rs.getDouble("cantidad"));
        m.setTipoMovimiento(AESUtil.desencriptar(rs.getString("tipo_movimiento")));
        m.setFechaMovimiento(rs.getTimestamp("fecha_movimiento"));
        m.setMotivo(AESUtil.desencriptar(rs.getString("motivo")));
        // Intentar obtener nombres si existen en el ResultSet (JOINs)
        try {
            m.setProductoNombre(AESUtil.desencriptar(rs.getString("producto_nombre")));
        } catch (Exception e) {}
        try {
            m.setProveedorNombre(AESUtil.desencriptar(rs.getString("proveedor_nombre")));
        } catch (Exception e) {}
        return m;
    }
}
