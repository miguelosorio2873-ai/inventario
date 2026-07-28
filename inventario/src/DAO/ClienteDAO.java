package DAO;

import CX.ConexionBD;
import Modelo.Cliente;
import Utils.AESUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO {

    public List<Cliente> listarTodos() throws SQLException {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT * FROM cliente ORDER BY id DESC";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public List<Cliente> buscar(String texto) throws SQLException {
        List<Cliente> lista = new ArrayList<>();
        String enc = AESUtil.encriptar(texto);
        String sql = "SELECT * FROM cliente WHERE nombre LIKE ? OR cedula LIKE ? OR nombre = ? OR cedula = ? ORDER BY id DESC";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + texto + "%");
            ps.setString(2, "%" + texto + "%");
            ps.setString(3, enc);
            ps.setString(4, enc);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public void insertar(Cliente c) throws SQLException {
        String sql = "INSERT INTO cliente (cedula, nombre, correo, telefono) VALUES (?, ?, ?, ?)";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, AESUtil.encriptar(c.getCedula()));
            ps.setString(2, AESUtil.encriptar(c.getNombre()));
            ps.setString(3, AESUtil.encriptar(c.getCorreo()));
            ps.setString(4, AESUtil.encriptar(c.getTelefono()));
            ps.executeUpdate();
        }
    }

    public void actualizar(Cliente c) throws SQLException {
        String sql = "UPDATE cliente SET cedula=?, nombre=?, correo=?, telefono=? WHERE id=?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, AESUtil.encriptar(c.getCedula()));
            ps.setString(2, AESUtil.encriptar(c.getNombre()));
            ps.setString(3, AESUtil.encriptar(c.getCorreo()));
            ps.setString(4, AESUtil.encriptar(c.getTelefono()));
            ps.setLong(5, c.getId());
            ps.executeUpdate();
        }
    }

    public void eliminar(long id) throws SQLException {
        String sql = "DELETE FROM cliente WHERE id=?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    public int contarTotal() throws SQLException {
        String sql = "SELECT COUNT(*) FROM cliente";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    private Cliente mapear(ResultSet rs) throws SQLException {
        Cliente c = new Cliente();
        c.setId(rs.getLong("id"));
        c.setCedula(AESUtil.desencriptar(rs.getString("cedula")));
        c.setNombre(AESUtil.desencriptar(rs.getString("nombre")));
        c.setCorreo(AESUtil.desencriptar(rs.getString("correo")));
        c.setTelefono(AESUtil.desencriptar(rs.getString("telefono")));
        return c;
    }
}
