package DAO;

import CX.ConexionBD;
import Modelo.Categoria;
import Utils.AESUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDAO {

    public List<Categoria> listarTodas() throws SQLException {
        List<Categoria> lista = new ArrayList<>();
        String sql = "SELECT * FROM categorias ORDER BY nombre";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public void insertar(Categoria c) throws SQLException {
        String sql = "INSERT INTO categorias (nombre, descripcion) VALUES (?, ?)";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, AESUtil.encriptar(c.getNombre()));
            ps.setString(2, AESUtil.encriptar(c.getDescripcion()));
            ps.executeUpdate();
        }
    }

    public void actualizar(Categoria c) throws SQLException {
        String sql = "UPDATE categorias SET nombre=?, descripcion=? WHERE id=?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, AESUtil.encriptar(c.getNombre()));
            ps.setString(2, AESUtil.encriptar(c.getDescripcion()));
            ps.setLong(3, c.getId());
            ps.executeUpdate();
        }
    }

    public void eliminar(long id) throws SQLException {
        String sql = "DELETE FROM categorias WHERE id=?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    private Categoria mapear(ResultSet rs) throws SQLException {
        return new Categoria(
            rs.getLong("id"), 
            AESUtil.desencriptar(rs.getString("nombre")), 
            AESUtil.desencriptar(rs.getString("descripcion"))
        );
    }
}
