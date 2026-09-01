package DAO;

import CX.ConexionBD;
import Modelo.Producto;
import Utils.AESUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    public List<Producto> listarTodos() throws SQLException {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT p.* FROM producto p ORDER BY p.id DESC";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public List<Producto> listarTodosConCategoria() throws SQLException {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT p.*, c.nombre as categoria_nombre FROM producto p " +
                     "LEFT JOIN categorias c ON p.categoria_id = c.id " +
                     "ORDER BY p.id DESC";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public List<Producto> buscar(String texto) throws SQLException {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT p.* FROM producto p " +
                     "WHERE p.nombre LIKE ? OR p.sku LIKE ? OR p.nombre = ? OR p.sku = ? " +
                     "ORDER BY p.id DESC";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + texto + "%");
            ps.setString(2, "%" + texto + "%");
            ps.setString(3, AESUtil.encriptar(texto));
            ps.setString(4, AESUtil.encriptar(texto));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    /**
     * Busca productos por CUALQUIER característica (id, SKU, nombre, descripción,
     * categoría, presentación, precio, costo, stock, estado). Se filtra en memoria
     * tras desencriptar porque las columnas están encriptadas (AES).
     */
    public List<Producto> buscarEnMemoria(String texto) throws SQLException {
        String q = texto == null ? "" : texto.trim().toLowerCase();
        List<Producto> lista = listarTodosConCategoria();
        if (q.isEmpty()) return lista;
        List<Producto> res = new ArrayList<>();
        for (Producto p : lista) {
            StringBuilder s = new StringBuilder();
            s.append(p.getId()).append(' ');
            if (p.getSku() != null) s.append(p.getSku()).append(' ');
            if (p.getNombre() != null) s.append(p.getNombre()).append(' ');
            if (p.getDescripcion() != null) s.append(p.getDescripcion()).append(' ');
            if (p.getCategoriaNombre() != null) s.append(p.getCategoriaNombre()).append(' ');
            if (p.getPresentacion() != null) s.append(p.getPresentacion()).append(' ');
            s.append(String.format("%.2f", p.getPrecioVenta())).append(' ');
            s.append(String.format("%.2f", p.getCostoPresentacion())).append(' ');
            s.append(String.format("%.2f", p.getCostoPorUnidad())).append(' ');
            s.append(String.format("%.2f", p.getStockActual())).append(' ');
            s.append(p.isState() ? "Activo" : "Inactivo").append(' ');
            if (s.toString().toLowerCase().contains(q)) res.add(p);
        }
        return res;
    }

    public Producto obtenerPorId(long id) throws SQLException {
        String sql = "SELECT p.* FROM producto p WHERE p.id=?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        }
        return null;
    }

    public long insertar(Producto p) throws SQLException {
        String sql = "INSERT INTO producto (categoria_id, sku, nombre, descripcion, precio_venta, costo_promedio, stock_actual, state, presentacion, unidades_presentacion, costo_presentacion, imagen) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (p.getCategoriaId() > 0) ps.setLong(1, p.getCategoriaId());
            else ps.setNull(1, Types.BIGINT);
            ps.setString(2, AESUtil.encriptar(p.getSku()));
            ps.setString(3, AESUtil.encriptar(p.getNombre()));
            ps.setString(4, AESUtil.encriptar(p.getDescripcion()));
            ps.setDouble(5, p.getPrecioVenta());
            ps.setDouble(6, p.getCostoPorUnidad());
            ps.setDouble(7, p.getStockActual());
            ps.setBoolean(8, p.isState());
            ps.setString(9, p.getPresentacion());
            ps.setDouble(10, p.getUnidadesPresentacion());
            ps.setDouble(11, p.getCostoPresentacion());
            ps.setString(12, p.getImagen());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        return -1;
    }

    public void actualizar(Producto p) throws SQLException {
        String sql = "UPDATE producto SET categoria_id=?, sku=?, nombre=?, descripcion=?, precio_venta=?, " +
                     "costo_promedio=?, stock_actual=?, state=?, presentacion=?, unidades_presentacion=?, costo_presentacion=?, imagen=? WHERE id=?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (p.getCategoriaId() > 0) ps.setLong(1, p.getCategoriaId());
            else ps.setNull(1, Types.BIGINT);
            ps.setString(2, AESUtil.encriptar(p.getSku()));
            ps.setString(3, AESUtil.encriptar(p.getNombre()));
            ps.setString(4, AESUtil.encriptar(p.getDescripcion()));
            ps.setDouble(5, p.getPrecioVenta());
            ps.setDouble(6, p.getCostoPorUnidad());
            ps.setDouble(7, p.getStockActual());
            ps.setBoolean(8, p.isState());
            ps.setString(9, p.getPresentacion());
            ps.setDouble(10, p.getUnidadesPresentacion());
            ps.setDouble(11, p.getCostoPresentacion());
            ps.setString(12, p.getImagen());
            ps.setLong(13, p.getId());
            ps.executeUpdate();
        }
    }

    public void eliminar(long id) throws SQLException {
        String sql = "DELETE FROM producto WHERE id=?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    public int contarTotal() throws SQLException {
        String sql = "SELECT COUNT(*) FROM producto WHERE state=TRUE";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public String siguienteSku() throws SQLException {
        String sql = "SELECT COUNT(*) FROM producto";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            int total = 0;
            if (rs.next()) total = rs.getInt(1);
            return String.format("PRD-%04d", total + 1);
        }
    }

    private Producto mapear(ResultSet rs) throws SQLException {
        Producto p = new Producto();
        p.setId(rs.getLong("id"));
        p.setCategoriaId(rs.getLong("categoria_id"));
        p.setSku(AESUtil.desencriptar(rs.getString("sku")));
        p.setNombre(AESUtil.desencriptar(rs.getString("nombre")));
        p.setDescripcion(AESUtil.desencriptar(rs.getString("descripcion")));
        p.setPrecioVenta(rs.getDouble("precio_venta"));
        p.setState(rs.getBoolean("state"));
        p.setImagen(AESUtil.desencriptar(rs.getString("imagen")));
        try { p.setPresentacion(rs.getString("presentacion")); } catch (Exception e) {}
        try { p.setUnidadesPresentacion(rs.getDouble("unidades_presentacion")); } catch (Exception e) {}
        try { p.setCostoPresentacion(rs.getDouble("costo_presentacion")); } catch (Exception e) {}
        try {
            p.setCategoriaNombre(AESUtil.desencriptar(rs.getString("categoria_nombre")));
        } catch (Exception e) {
        }
        p.setStockActual(rs.getDouble("stock_actual"));
        return p;
    }
}
