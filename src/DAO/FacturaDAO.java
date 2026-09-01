package DAO;

import CX.ConexionBD;
import Modelo.Factura;
import Modelo.DetalleFactura;
import Utils.AESUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FacturaDAO {

    public List<Factura> listarTodasConCliente() throws SQLException {
        List<Factura> lista = new ArrayList<>();
        String sql = "SELECT f.*, c.nombre as cliente_nombre FROM factura f " +
                     "LEFT JOIN cliente c ON f.cliente_id = c.id ORDER BY f.id DESC";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public List<Factura> buscar(String texto) throws SQLException {
        List<Factura> lista = new ArrayList<>();
        String enc = AESUtil.encriptar(texto);
        String sql = "SELECT f.*, c.nombre as cliente_nombre FROM factura f " +
                     "LEFT JOIN cliente c ON f.cliente_id = c.id " +
                     "WHERE f.numero_factura LIKE ? OR f.numero_factura = ? " +
                     "ORDER BY f.id DESC";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + texto + "%");
            ps.setString(2, enc);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    /**
     * Busca facturas por CUALQUIER característica (número, cliente, método de pago,
     * estado, ID, fecha, total o nombre de producto). Las columnas relevantes están
     * encriptadas (AES), por lo que se hace el filtrado en memoria tras desencriptar.
     */
    public List<Factura> buscarEnMemoria(String texto) throws SQLException {
        String q = texto == null ? "" : texto.trim().toLowerCase();
        List<Factura> lista = listarTodasConCliente();
        if (q.isEmpty()) return lista;

        List<Factura> res = new ArrayList<>();
        for (Factura f : lista) {
            StringBuilder s = new StringBuilder();
            s.append(f.getId()).append(' ');
            if (f.getNumeroFactura() != null) s.append(f.getNumeroFactura()).append(' ');
            if (f.getMetodoPago() != null) s.append(f.getMetodoPago()).append(' ');
            if (f.getEstado() != null) s.append(f.getEstado()).append(' ');
            if (f.getClienteNombre() != null) s.append(f.getClienteNombre()).append(' ');
            if (f.getFechaEmision() != null) s.append(f.getFechaEmision()).append(' ');
            s.append(String.format("%.2f", f.getSubtotal())).append(' ');
            s.append(String.format("%.2f", f.getTotal())).append(' ');
            try {
                for (DetalleFactura df : new DetalleFacturaDAO().listarPorFactura(f.getId())) {
                    if (df.getProductoNombre() != null) s.append(df.getProductoNombre()).append(' ');
                }
            } catch (SQLException ignored) {}
            if (s.toString().toLowerCase().contains(q)) res.add(f);
        }
        return res;
    }

    public String generarNumeroFactura() throws SQLException {
        String sql = "SELECT MAX(id) FROM factura";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            int next = 1;
            if (rs.next()) next = rs.getInt(1) + 1;
            return String.format("FAC-%03d", next);
        }
    }

    /**
     * Registra una venta completa en una transaccion:
     * 1. Inserta la factura
     * 2. Inserta los detalles (line items)
     * 3. Registra movimientos de salida en inventario por cada item
     * 4. Actualiza el stock de cada producto
     */
    public long registrarVenta(Factura f) throws SQLException {
        String sqlFactura = "INSERT INTO factura (cliente_id, numero_factura, metodo_pago, estado, subtotal, impuestos, total) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?)";
        String sqlDetalle = "INSERT INTO detalle_factura (factura_id, producto_id, cantidad, precio_unitario, subtotal) " +
                            "VALUES (?, ?, ?, ?, ?)";
        String sqlInventario = "INSERT INTO inventario (producto_id, precio, precio_balance, cantidad, tipo_movimiento, motivo) " +
                               "VALUES (?, ?, ?, ?, ?, ?)";
        String sqlStock = "UPDATE producto SET stock_actual = stock_actual - ? WHERE id = ?";

        try (Connection con = ConexionBD.conectar()) {
            con.setAutoCommit(false);
            try {
                // 1. Insertar factura
                long facturaId = -1;
                try (PreparedStatement ps = con.prepareStatement(sqlFactura, Statement.RETURN_GENERATED_KEYS)) {
                    if (f.getClienteId() != null) ps.setLong(1, f.getClienteId());
                    else ps.setNull(1, Types.BIGINT);
                    ps.setString(2, AESUtil.encriptar(f.getNumeroFactura()));
                    ps.setString(3, AESUtil.encriptar(f.getMetodoPago()));
                    ps.setString(4, AESUtil.encriptar(f.getEstado()));
                    ps.setDouble(5, f.getSubtotal());
                    ps.setDouble(6, f.getImpuestos());
                    ps.setDouble(7, f.getTotal());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) facturaId = keys.getLong(1);
                    }
                }

                // 2. Insertar detalles + 3. Inventario + 4. Stock
                String tipoSalida = AESUtil.encriptar("Salida");
                try (PreparedStatement psDet = con.prepareStatement(sqlDetalle);
                     PreparedStatement psInv = con.prepareStatement(sqlInventario);
                     PreparedStatement psStock = con.prepareStatement(sqlStock)) {

                    for (DetalleFactura df : f.getDetalles()) {
                        // Detalle
                        psDet.setLong(1, facturaId);
                        psDet.setLong(2, df.getProductoId());
                        psDet.setDouble(3, df.getCantidad());
                        psDet.setDouble(4, df.getPrecioUnitario());
                        psDet.setDouble(5, df.getSubtotal());
                        psDet.executeUpdate();

                        // Inventario (salida)
                        psInv.setLong(1, df.getProductoId());
                        psInv.setDouble(2, df.getPrecioUnitario());
                        psInv.setDouble(3, df.getPrecioUnitario() * df.getCantidad());
                        psInv.setDouble(4, df.getCantidad());
                        psInv.setString(5, tipoSalida);
                        psInv.setString(6, AESUtil.encriptar("Venta " + f.getNumeroFactura()));
                        psInv.executeUpdate();

                        // Stock
                        psStock.setDouble(1, df.getCantidad());
                        psStock.setLong(2, df.getProductoId());
                        psStock.executeUpdate();
                    }
                }

                con.commit();
                return facturaId;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    /**
     * Anula una factura restaurando el stock de cada producto en una transaccion:
     * 1. Lee los detalles de la factura
     * 2. Incrementa el stock_actual de cada producto
     * 3. Registra un movimiento de entrada en inventario por cada item
     * 4. Marca la factura como Anulada
     */
    public void anularConRestauracion(long facturaId, String numeroFactura) throws SQLException {
        String sqlDetalles = "SELECT producto_id, cantidad, precio_unitario FROM detalle_factura WHERE factura_id=?";
        String sqlStock = "UPDATE producto SET stock_actual = stock_actual + ? WHERE id=?";
        String sqlInv = "INSERT INTO inventario (producto_id, precio, precio_balance, cantidad, tipo_movimiento, motivo) " +
                        "VALUES (?, ?, ?, ?, ?, ?)";
        String sqlEstado = "UPDATE factura SET estado=? WHERE id=?";

        try (Connection con = ConexionBD.conectar()) {
            con.setAutoCommit(false);
            try {
                List<DetalleFactura> items = new ArrayList<>();
                try (PreparedStatement ps = con.prepareStatement(sqlDetalles)) {
                    ps.setLong(1, facturaId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            DetalleFactura df = new DetalleFactura();
                            df.setProductoId(rs.getLong("producto_id"));
                            df.setCantidad(rs.getDouble("cantidad"));
                            df.setPrecioUnitario(rs.getDouble("precio_unitario"));
                            items.add(df);
                        }
                    }
                }

                String tipoEntrada = AESUtil.encriptar("Entrada");
                try (PreparedStatement psStock = con.prepareStatement(sqlStock);
                     PreparedStatement psInv = con.prepareStatement(sqlInv)) {
                    for (DetalleFactura df : items) {
                        psStock.setDouble(1, df.getCantidad());
                        psStock.setLong(2, df.getProductoId());
                        psStock.executeUpdate();

                        psInv.setLong(1, df.getProductoId());
                        psInv.setDouble(2, df.getPrecioUnitario());
                        psInv.setDouble(3, df.getPrecioUnitario() * df.getCantidad());
                        psInv.setDouble(4, df.getCantidad());
                        psInv.setString(5, tipoEntrada);
                        psInv.setString(6, AESUtil.encriptar("Anulacion factura " + numeroFactura));
                        psInv.executeUpdate();
                    }
                }

                try (PreparedStatement ps = con.prepareStatement(sqlEstado)) {
                    ps.setString(1, AESUtil.encriptar("Anulada"));
                    ps.setLong(2, facturaId);
                    ps.executeUpdate();
                }

                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    /**
     * Marca una factura fiada como Pagada (sin restaurar ni tocar stock).
     */
    public void marcarPagada(long facturaId) throws SQLException {
        String sql = "UPDATE factura SET estado=? WHERE id=?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, AESUtil.encriptar("Pagada"));
            ps.setLong(2, facturaId);
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
