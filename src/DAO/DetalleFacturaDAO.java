package DAO;

import CX.ConexionBD;
import Modelo.DetalleFactura;
import Utils.AESUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DetalleFacturaDAO {

    public List<DetalleFactura> listarPorFactura(long facturaId) throws SQLException {
        List<DetalleFactura> lista = new ArrayList<>();
        String sql = "SELECT df.*, p.nombre as producto_nombre, p.sku as producto_sku " +
                     "FROM detalle_factura df " +
                     "LEFT JOIN producto p ON df.producto_id = p.id " +
                     "WHERE df.factura_id = ? ORDER BY df.id";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, facturaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    private DetalleFactura mapear(ResultSet rs) throws SQLException {
        DetalleFactura df = new DetalleFactura();
        df.setId(rs.getLong("id"));
        df.setFacturaId(rs.getLong("factura_id"));
        df.setProductoId(rs.getLong("producto_id"));
        df.setCantidad(rs.getDouble("cantidad"));
        df.setPrecioUnitario(rs.getDouble("precio_unitario"));
        df.setSubtotal(rs.getDouble("subtotal"));
        df.setTasaVes(rs.getDouble("tasa_ves"));
        double pub = rs.getDouble("precio_unitario_bs");
        double subb = rs.getDouble("subtotal_bs");
        // Respaldo si la linea no fue congelada (subtotal_bs=0): recalc con tasa vigente.
        if (subb <= 0) {
            double t = df.getTasaVes() > 0 ? df.getTasaVes() : Utils.Config.getTasaVES();
            pub = Math.round(df.getPrecioUnitario() * t * 100) / 100.0;
            subb = Math.round(df.getSubtotal() * t * 100) / 100.0;
        }
        df.setPrecioUnitarioBs(pub);
        df.setSubtotalBs(subb);
        try { df.setProductoNombre(AESUtil.desencriptar(rs.getString("producto_nombre"))); } catch (Exception e) {}
        try { df.setProductoSku(AESUtil.desencriptar(rs.getString("producto_sku"))); } catch (Exception e) {}
        return df;
    }
}
