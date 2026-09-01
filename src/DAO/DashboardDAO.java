package DAO;

import CX.ConexionBD;
import Utils.AESUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DashboardDAO {

    /** Cantidad total de productos vendidos hoy (excluye anuladas). */
    public double productosVendidosHoy() throws SQLException {
        String anulada = AESUtil.encriptar("Anulada");
        String sql = "SELECT COALESCE(SUM(df.cantidad),0) FROM detalle_factura df " +
                     "JOIN factura f ON df.factura_id = f.id " +
                     "WHERE date(f.fecha_emision)=date('now','localtime') AND f.estado != ?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, anulada);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0;
            }
        }
    }

    /** Total vendido en USD hoy (excluye anuladas). */
    public double ventasHoy() throws SQLException {
        String anulada = AESUtil.encriptar("Anulada");
        String sql = "SELECT COALESCE(SUM(f.total),0) FROM factura f " +
                     "WHERE date(f.fecha_emision)=date('now','localtime') AND f.estado != ?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, anulada);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0;
            }
        }
    }

    /** Ganancia (precio - costo) x cantidad de hoy, excluyendo anuladas. */
    public double gananciaHoy() throws SQLException {
        String anulada = AESUtil.encriptar("Anulada");
        String sql = "SELECT COALESCE(SUM((df.precio_unitario - COALESCE(p.costo_promedio,0)) * df.cantidad),0) " +
                     "FROM detalle_factura df " +
                     "JOIN factura f ON df.factura_id = f.id " +
                     "LEFT JOIN producto p ON df.producto_id = p.id " +
                     "WHERE date(f.fecha_emision)=date('now','localtime') AND f.estado != ?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, anulada);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0;
            }
        }
    }

    /**
     * Serie temporal de ventas por día dentro de un mes/año (para gráfica trending).
     * @param anio año (4 dígitos)
     * @param mes  1-12, o 0 para todo el año
     * @return lista ordenada de pares [dia, total]
     */
    public List<double[]> ventasSecuenciales(int anio, int mes) throws SQLException {
        String anulada = AESUtil.encriptar("Anulada");
        String condFecha = " strftime('%Y', f.fecha_emision)=? ";
        if (mes > 0) condFecha += " AND strftime('%m', f.fecha_emision)=? ";
        String sql = "SELECT CAST(strftime('%d', f.fecha_emision) AS INTEGER) as dia, " +
                     "SUM(f.total) as total " +
                     "FROM factura f WHERE f.estado != ? AND " + condFecha +
                     " GROUP BY strftime('%d', f.fecha_emision) ORDER BY dia";
        List<double[]> lista = new ArrayList<>();
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            int idx = 1;
            ps.setString(idx++, anulada);
            ps.setString(idx++, String.format("%04d", anio));
            if (mes > 0) ps.setString(idx, String.format("%02d", mes));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new double[]{rs.getInt("dia"), rs.getDouble("total")});
                }
            }
        }
        return lista;
    }

    /**
     * Top productos más vendidos (por cantidad) dentro de los últimos N días.
     * Devuelve pares nombre -> cantidad.
     */
    public Map<String, Double> productosMasVendidos(int dias) throws SQLException {
        String anulada = AESUtil.encriptar("Anulada");
        String sql = "SELECT p.nombre as nombre, SUM(df.cantidad) as cant " +
                     "FROM detalle_factura df " +
                     "JOIN factura f ON df.factura_id = f.id " +
                     "JOIN producto p ON df.producto_id = p.id " +
                     "WHERE f.estado != ? AND date(f.fecha_emision) >= date('now','localtime','-" + dias + " days') " +
                     "GROUP BY df.producto_id, p.nombre " +
                     "ORDER BY cant DESC LIMIT 10";
        Map<String, Double> mapa = new LinkedHashMap<>();
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, anulada);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    mapa.put(desencriptarCampo(rs.getString("nombre")), rs.getDouble("cant"));
                }
            }
        }
        return mapa;
    }

    /**
     * Por cada uno de los últimos N días con ventas: ganancia en USD y la
     * "pérdida" en Bs por el cambio de la tasa. La pérdida se calcula comparando
     * la ganancia valuada a la tasa actual vs una tasa de referencia de compra
     * (por ejemplo, 25% menor a la actual). Si la tasa sube, el equivalente en
     * Bs de la ganancia "valdría menos" que si se hubiera cobrado antes; aquí se
     * muestra ese impacto como pérdida estimada en Bs.
     */
    public List<double[]> perdidaDolarPorDia(int dias) throws SQLException {
        String anulada = AESUtil.encriptar("Anulada");
        String sql = "SELECT date(f.fecha_emision) as dia, " +
                     "SUM((df.precio_unitario - COALESCE(p.costo_promedio,0)) * df.cantidad) as ganancia " +
                     "FROM detalle_factura df " +
                     "JOIN factura f ON df.factura_id = f.id " +
                     "LEFT JOIN producto p ON df.producto_id = p.id " +
                     "WHERE f.estado != ? AND date(f.fecha_emision) >= date('now','localtime','-" + dias + " days') " +
                     "GROUP BY date(f.fecha_emision) " +
                     "ORDER BY date(f.fecha_emision)";
        double tasaActual = Utils.Config.getTasaVES();
        double tasaReferencia = tasaActual * 0.75; // referencia de compra (75% de la actual)
        List<double[]> lista = new ArrayList<>();
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, anulada);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    double gananciaUsd = rs.getDouble("ganancia");
                    // Bs que "se dejarían de ganar" por la subida de la tasa
                    double perdidaBs = gananciaUsd * (tasaActual - tasaReferencia);
                    lista.add(new double[]{gananciaUsd, perdidaBs});
                }
            }
        }
        return lista;
    }

    /**
     * Resumen de ventas dentro de un rango de fechas (valores en USD salvo los indicados).
     * Claves devueltas: total, ganancia, productos (cantidad), facturas, ticket p/venta.
     * @param desde fecha inicial "yyyy-MM-dd" (inclusive)
     * @param hasta  fecha final   "yyyy-MM-dd" (inclusive)
     */
    public Map<String, Double> resumenVentas(String desde, String hasta) throws SQLException {
        String anulada = AESUtil.encriptar("Anulada");
        String sql = "SELECT " +
                     "COALESCE(SUM(f.total),0) as total, " +
                     "COALESCE(SUM((df.precio_unitario - COALESCE(p.costo_promedio,0)) * df.cantidad),0) as ganancia, " +
                     "COALESCE(SUM(df.cantidad),0) as productos, " +
                     "COUNT(DISTINCT f.id) as facturas " +
                     "FROM detalle_factura df " +
                     "JOIN factura f ON df.factura_id = f.id " +
                     "LEFT JOIN producto p ON df.producto_id = p.id " +
                     "WHERE f.estado != ? AND date(f.fecha_emision) BETWEEN ? AND ?";
        Map<String, Double> r = new LinkedHashMap<>();
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, anulada);
            ps.setString(2, desde);
            ps.setString(3, hasta);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double total = rs.getDouble("total");
                    double facturas = rs.getDouble("facturas");
                    r.put("total", total);
                    r.put("ganancia", rs.getDouble("ganancia"));
                    r.put("productos", rs.getDouble("productos"));
                    r.put("facturas", facturas);
                    r.put("ticket", facturas > 0 ? total / facturas : 0);
                }
            }
        }
        return r;
    }

    /** Filas de ventas (una por factura) dentro de un rango de fechas, para exportar a Excel. */
    public List<Map<String, Object>> resumenVentasExcel(String desde, String hasta) throws SQLException {
        String anulada = AESUtil.encriptar("Anulada");
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = "SELECT f.numero_factura as numero, f.fecha_emision as fecha, " +
                     "f.metodo_pago as metodo, f.estado as estado, " +
                     "COALESCE(SUM(df.cantidad),0) as productos, " +
                     "COALESCE(f.total,0) as total " +
                     "FROM factura f " +
                     "LEFT JOIN detalle_factura df ON df.factura_id = f.id " +
                     "WHERE f.estado != ? AND date(f.fecha_emision) BETWEEN ? AND ? " +
                     "GROUP BY f.id ORDER BY f.fecha_emision";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, anulada);
            ps.setString(2, desde);
            ps.setString(3, hasta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> fila = new LinkedHashMap<>();
                    fila.put("numero", desencriptarCampo(rs.getString("numero")));
                    java.sql.Timestamp ts = rs.getTimestamp("fecha");
                    fila.put("fecha", ts != null ? new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(ts) : "");
                    fila.put("metodo", desencriptarCampo(rs.getString("metodo")));
                    fila.put("estado", desencriptarCampo(rs.getString("estado")));
                    fila.put("productos", rs.getDouble("productos"));
                    fila.put("total", rs.getDouble("total"));
                    lista.add(fila);
                }
            }
        }
        return lista;
    }

    private static String desencriptarCampo(String v) {
        if (v == null) return "";
        try {
            return AESUtil.desencriptar(v);
        } catch (Exception e) {
            return normalizarMetodo(v);
        }
    }

    private static String normalizarMetodo(String m) {
        if (m == null) return "";
        return m.replace("Ã³", "ó").replace("Ã©", "é")
                .replace("Ã­", "í").replace("Ã¡", "á").replace("Ãº", "ú");
    }
}
