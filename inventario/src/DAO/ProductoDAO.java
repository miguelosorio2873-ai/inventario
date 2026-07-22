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
        String sql = "SELECT p.* FROM producto p ORDER BY p.id";
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
                     "ORDER BY p.id";
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
                     "WHERE p.nombre LIKE ? OR p.sku LIKE ? OR p.nombre = ? OR p.sku = ?";
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

    public List<Producto> listarStockBajo() throws SQLException {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT p.* FROM producto p WHERE p.state = TRUE";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Producto p = mapear(rs);
                if (p.getStockActual() <= p.getStockMinimo()) {
                    lista.add(p);
                }
            }
        }
        return lista;
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

    public void insertar(Producto p) throws SQLException {
        String sql = "INSERT INTO producto (categoria_id, sku, nombre, descripcion, precio_venta, costo_promedio, stock_minimo, state, imagen) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (p.getCategoriaId() > 0) ps.setLong(1, p.getCategoriaId());
            else ps.setNull(1, Types.BIGINT);
            ps.setString(2, AESUtil.encriptar(p.getSku()));
            ps.setString(3, AESUtil.encriptar(p.getNombre()));
            ps.setString(4, AESUtil.encriptar(p.getDescripcion()));
            ps.setDouble(5, p.getPrecioVenta());
            ps.setDouble(6, p.getCostoPromedio());
            ps.setDouble(7, p.getStockMinimo());
            ps.setBoolean(8, p.isState());
            ps.setString(9, AESUtil.encriptar(p.getImagen()));
            ps.executeUpdate();
        }
    }

    public void actualizar(Producto p) throws SQLException {
        String sql = "UPDATE producto SET categoria_id=?, sku=?, nombre=?, descripcion=?, precio_venta=?, " +
                     "costo_promedio=?, stock_minimo=?, state=?, imagen=? WHERE id=?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (p.getCategoriaId() > 0) ps.setLong(1, p.getCategoriaId());
            else ps.setNull(1, Types.BIGINT);
            ps.setString(2, AESUtil.encriptar(p.getSku()));
            ps.setString(3, AESUtil.encriptar(p.getNombre()));
            ps.setString(4, AESUtil.encriptar(p.getDescripcion()));
            ps.setDouble(5, p.getPrecioVenta());
            ps.setDouble(6, p.getCostoPromedio());
            ps.setDouble(7, p.getStockMinimo());
            ps.setBoolean(8, p.isState());
            ps.setString(9, AESUtil.encriptar(p.getImagen()));
            ps.setLong(10, p.getId());
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

    public int contarStockBajo() throws SQLException {
        int count = 0;
        for (Producto p : listarTodos()) {
            if (p.isState() && p.getStockActual() <= p.getStockMinimo()) {
                count++;
            }
        }
        return count;
    }

    private Producto mapear(ResultSet rs) throws SQLException {
        Producto p = new Producto();
        p.setId(rs.getLong("id"));
        p.setCategoriaId(rs.getLong("categoria_id"));
        p.setSku(AESUtil.desencriptar(rs.getString("sku")));
        p.setNombre(AESUtil.desencriptar(rs.getString("nombre")));
        p.setDescripcion(AESUtil.desencriptar(rs.getString("descripcion")));
        p.setPrecioVenta(rs.getDouble("precio_venta"));
        p.setCostoPromedio(rs.getDouble("costo_promedio"));
        p.setStockMinimo(rs.getDouble("stock_minimo"));
        p.setState(rs.getBoolean("state"));
        p.setImagen(AESUtil.desencriptar(rs.getString("imagen")));
        try {
            p.setCategoriaNombre(AESUtil.desencriptar(rs.getString("categoria_nombre")));
        } catch (Exception e) {
        }
        p.setStockActual(rs.getDouble("stock_actual"));
        return p;
    }
}
