package DAO;

import CX.ConexionBD;
import Modelo.Proveedor;
import Utils.AESUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProveedorDAO {

    public List<Proveedor> listarTodos() throws SQLException {
        List<Proveedor> lista = new ArrayList<>();
        String sql = "SELECT * FROM proveedor ORDER BY nombre_empresa";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public List<Proveedor> buscar(String texto) throws SQLException {
        List<Proveedor> lista = new ArrayList<>();
        String sql = "SELECT * FROM proveedor WHERE nombre_empresa LIKE ? OR nit_cedula LIKE ? ORDER BY nombre_empresa";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + texto + "%");
            ps.setString(2, AESUtil.encriptar(texto)); // Búsqueda exacta de NIT encriptado
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public void insertar(Proveedor p) throws SQLException {
        String sql = "INSERT INTO proveedor (nombre_empresa, nit_cedula, telefono, direccion, correo, nombre_contacto) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, AESUtil.encriptar(p.getNombreEmpresa()));
            ps.setString(2, AESUtil.encriptar(p.getNitCedula()));
            ps.setString(3, AESUtil.encriptar(p.getTelefono()));
            ps.setString(4, AESUtil.encriptar(p.getDireccion()));
            ps.setString(5, AESUtil.encriptar(p.getCorreo()));
            ps.setString(6, AESUtil.encriptar(p.getNombreContacto()));
            ps.executeUpdate();
        }
    }

    public void actualizar(Proveedor p) throws SQLException {
        String sql = "UPDATE proveedor SET nombre_empresa=?, nit_cedula=?, telefono=?, direccion=?, correo=?, nombre_contacto=? WHERE id=?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, AESUtil.encriptar(p.getNombreEmpresa()));
            ps.setString(2, AESUtil.encriptar(p.getNitCedula()));
            ps.setString(3, AESUtil.encriptar(p.getTelefono()));
            ps.setString(4, AESUtil.encriptar(p.getDireccion()));
            ps.setString(5, AESUtil.encriptar(p.getCorreo()));
            ps.setString(6, AESUtil.encriptar(p.getNombreContacto()));
            ps.setLong(7, p.getId());
            ps.executeUpdate();
        }
    }

    public void eliminar(long id) throws SQLException {
        String sql = "DELETE FROM proveedor WHERE id=?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    private Proveedor mapear(ResultSet rs) throws SQLException {
        Proveedor p = new Proveedor();
        p.setId(rs.getLong("id"));
        p.setNombreEmpresa(AESUtil.desencriptar(rs.getString("nombre_empresa")));
        p.setNitCedula(AESUtil.desencriptar(rs.getString("nit_cedula")));
        p.setTelefono(AESUtil.desencriptar(rs.getString("telefono")));
        p.setDireccion(AESUtil.desencriptar(rs.getString("direccion")));
        p.setCorreo(AESUtil.desencriptar(rs.getString("correo")));
        p.setNombreContacto(AESUtil.desencriptar(rs.getString("nombre_contacto")));
        return p;
    }
}
