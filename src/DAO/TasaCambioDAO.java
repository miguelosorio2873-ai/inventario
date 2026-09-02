package DAO;

import CX.ConexionBD;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Histórico de tasas de cambio (Bs por $) guardadas por día.
 * Permite que las vistas con fechas usen la tasa vigente de cada día
 * en lugar de la tasa actual, una vez que se va poblando el historial.
 */
public class TasaCambioDAO {

    private static final SimpleDateFormat SDF_FECHA = new SimpleDateFormat("yyyy-MM-dd");

    /** Guarda (o actualiza) la tasa del día indicado. */
    public void guardar(String fecha, double tasa) throws SQLException {
        String sql = "INSERT INTO tasa_cambio (fecha, tasa_ves) VALUES (?, ?) " +
                     "ON CONFLICT(fecha) DO UPDATE SET tasa_ves = excluded.tasa_ves, actualizado = CURRENT_TIMESTAMP";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, fecha);
            ps.setDouble(2, tasa);
            ps.executeUpdate();
        }
    }

    /** Guarda la tasa de HOY usando una fecha tipo Date. */
    public void guardarHoy(double tasa) throws SQLException {
        guardar(SDF_FECHA.format(new Date()), tasa);
    }

    /**
     * Devuelve la tasa correspondiente a una fecha:
     * 1. La exacta de ese día si existe en el historial.
     * 2. Si no, la más reciente ANTERIOR a esa fecha.
     * 3. Si no hay historial, la tasa vigente de Config (fallback).
     */
    public double obtenerPorFecha(Date fecha) {
        if (fecha != null) {
            try {
                String f = SDF_FECHA.format(fecha);
                double exacta = buscarTasa(f);
                if (exacta > 0) return exacta;
                double anterior = buscarAnterior(f);
                if (anterior > 0) return anterior;
            } catch (SQLException e) {
                // ignorar y caer al fallback
            }
        }
        return Utils.Config.getTasaVES();
    }

    /**
     * Re-fija el Bs congelado de las facturas usando la tasa del historial de su
     * fecha de emisión, cuando la tasa guardada aún es un valor provisional
     * (de la migración) distinto del histórico real de ese día. Devuelve cuántas
     * facturas se ajustaron.
     */
    public int reconciliarFacturasConHistorico() {
        int ajustadas = 0;
        String sql = "SELECT id, date(fecha_emision) f, subtotal, total, tasa_ves FROM factura " +
                     "WHERE fecha_emision IS NOT NULL";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String f = rs.getString("f");
                double t = buscarTasa(f);
                if (t <= 0) continue;
                double actual = rs.getDouble("tasa_ves");
                if (actual > 0 && Math.abs(actual - t) < 0.0001) continue; // ya correcta
                long id = rs.getLong("id");
                double sub = rs.getDouble("subtotal");
                double tot = rs.getDouble("total");
                try (PreparedStatement up = con.prepareStatement(
                    "UPDATE factura SET tasa_ves = ?, subtotal_bs = ?, total_bs = ? WHERE id = ?")) {
                    up.setDouble(1, t);
                    up.setDouble(2, Math.round(sub * t * 100) / 100.0);
                    up.setDouble(3, Math.round(tot * t * 100) / 100.0);
                    up.setLong(4, id);
                    up.executeUpdate();
                }
                try (PreparedStatement up = con.prepareStatement(
                    "UPDATE detalle_factura SET tasa_ves = ?, precio_unitario_bs = ROUND(precio_unitario * ?, 2), subtotal_bs = ROUND(subtotal * ?, 2) WHERE factura_id = ?")) {
                    up.setDouble(1, t);
                    up.setDouble(2, t);
                    up.setDouble(3, t);
                    up.setLong(4, id);
                    up.executeUpdate();
                }
                ajustadas++;
            }
        } catch (Exception e) {
            System.out.println("Error reconciliando facturas con histórico: " + e.getMessage());
        }
        if (ajustadas > 0) System.out.println("Facturas re-congeladas con tasa del día: " + ajustadas + ".");
        return ajustadas;
    }

    /**
     * Construye el histórico de tasas de los días PASADOS que tienen registros
     * (facturas/movimientos) y que aún no están en tasa_cambio. Descarga cada
     * tasa desde la API de dolarapi (BCV) y la guarda. Devuelve cuántas agregó.
     */
    public int construirHistoricoPendiente() {
        int agregadas = 0;
        try {
            String hoy = SDF_FECHA.format(new Date());
            java.util.Set<String> fechas = new java.util.LinkedHashSet<>();
            String sql = "SELECT DISTINCT date(fecha_emision) f FROM factura WHERE fecha_emision IS NOT NULL " +
                         "UNION SELECT DISTINCT date(fecha_movimiento) f FROM inventario WHERE fecha_movimiento IS NOT NULL";
            try (Connection con = ConexionBD.conectar();
                 PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String f = rs.getString("f");
                    if (f != null && f.compareTo(hoy) < 0) fechas.add(f); // solo días pasados
                }
            }
            for (String f : fechas) {
                if (buscarTasa(f) > 0) continue;            // ya registrada
                double tasa = resolverTasaConPropagacion(f);
                if (tasa > 0) {
                    guardar(f, tasa);
                    agregadas++;
                }
            }
        } catch (Exception e) {
            System.out.println("Error construyendo histórico de tasas: " + e.getMessage());
        }
        if (agregadas > 0) System.out.println("Histórico de tasas: " + agregadas + " día(s) registrado(s).");
        return agregadas;
    }

    /**
     * Devuelve la tasa de una fecha; si esa fecha no tiene cotización (fin de
     * semana/feriado), retrocede día a día hasta una fecha con tasa y la usa,
     * registrando además las fechas intermedias con el mismo valor.
     */
    private double resolverTasaConPropagacion(String fecha) {
        try {
            java.time.LocalDate d = java.time.LocalDate.parse(fecha);
            for (int back = 0; back < 10; back++) {
                String f = d.minusDays(back).toString();
                double existente = buscarTasa(f);
                double tasa = existente > 0 ? existente : descargarTasaHistorica(f);
                if (tasa > 0) {
                    // Registra el retroceso y luego la fecha solicitada (y las intermedias)
                    for (int k = back; k >= 0; k--) {
                        String ff = d.minusDays(k).toString();
                        try { guardar(ff, tasa); } catch (SQLException ignore) {}
                    }
                    return tasa;
                }
            }
        } catch (Exception e) {
            // ignorar
        }
        return -1;
    }

    /** Descarga la tasa BCV (promedio) de una fecha YYYY-MM-DD desde dolarapi. */
    private double descargarTasaHistorica(String fecha) {
        try {
            String[] p = fecha.split("-");
            String url = "https://ve.dolarapi.com/v1/historicos/dolares/" + p[0] + "/" + p[1] + "/" + p[2];
            java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofSeconds(6))
                .build();
            java.net.http.HttpRequest req = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(url))
                .header("Accept", "application/json")
                .timeout(java.time.Duration.ofSeconds(12))
                .GET()
                .build();
            java.net.http.HttpResponse<String> resp = client.send(req, java.net.http.HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) { System.out.println("API histórico " + fecha + " -> HTTP " + resp.statusCode()); return -1; }
            return parsearPromedioOficial(resp.body());
        } catch (Exception e) {
            System.out.println("No se pudo descargar tasa histórica de " + fecha + ": " + e.getMessage());
            return -1;
        }
    }

    /**
     * Extrae el "promedio" del objeto cuyo "fuente" sea "oficial" (BCV).
     * Si no lo halla, toma el primer valor "promedio" disponible.
     */
    private double parsearPromedioOficial(String body) {
        int idxFuente = body.indexOf("\"fuente\":\"oficial\"");
        int ini = idxFuente >= 0 ? idxFuente : 0;
        int idxProm = body.indexOf("\"promedio\"", ini);
        if (idxProm < 0) { System.out.println("Campo 'promedio' no hallado en histórico"); return -1; }
        int colon = body.indexOf(':', idxProm);
        int end = body.indexOf(',', colon);
        if (end < 0) end = body.indexOf('}', colon);
        if (colon < 0 || end < 0) return -1;
        try { return Double.parseDouble(body.substring(colon + 1, end).trim()); }
        catch (Exception e) { return -1; }
    }

    private double buscarTasa(String fecha) throws SQLException {
        String sql = "SELECT tasa_ves FROM tasa_cambio WHERE fecha = ?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, fecha);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble("tasa_ves");
            }
        }
        return -1;
    }

    private double buscarAnterior(String fecha) throws SQLException {
        String sql = "SELECT tasa_ves FROM tasa_cambio WHERE fecha < ? ORDER BY fecha DESC LIMIT 1";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, fecha);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble("tasa_ves");
            }
        }
        return -1;
    }
}