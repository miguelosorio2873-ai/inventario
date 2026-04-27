package IG;

import DAO.ProveedorDAO;
import Modelo.Proveedor;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.List;

public class PanelProveedores extends JPanel {

    private JTable tabla;
    private DefaultTableModel modelo;
    private JTextField campoBuscar;
    private ProveedorDAO dao = new ProveedorDAO();

    public PanelProveedores() {
        setBackground(new Color(24, 24, 27));
        setLayout(new BorderLayout(0, 15));
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JPanel header = new JPanel(new BorderLayout(15, 0));
        header.setOpaque(false);

        JLabel titulo = new JLabel("Proveedores");
        try {
            com.formdev.flatlaf.extras.FlatSVGIcon icon = new com.formdev.flatlaf.extras.FlatSVGIcon(getClass().getResource("/IMG/truck.svg")).derive(24, 24);
            icon.setColorFilter(new com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter(c -> Color.WHITE));
            titulo.setIcon(icon);
            titulo.setIconTextGap(10);
        } catch (Exception e) {}
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titulo.setForeground(Color.WHITE);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        acciones.setOpaque(false);

        campoBuscar = PanelProductos.crearCampo("", 100);
        campoBuscar.setColumns(20);
        campoBuscar.putClientProperty("JTextField.placeholderText", "Buscar por empresa o NIT...");
        campoBuscar.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { buscar(); }
        });

        JButton btnNuevo = PanelProductos.crearBoton("➕ Nuevo", new Color(16, 185, 129));
        btnNuevo.addActionListener(e -> dialogo(null));
        JButton btnEditar = PanelProductos.crearBoton("✏️ Editar", new Color(59, 130, 246));
        btnEditar.addActionListener(e -> editarSel());
        JButton btnEliminar = PanelProductos.crearBoton("🗑️ Eliminar", new Color(239, 68, 68));
        btnEliminar.addActionListener(e -> eliminarSel());

        // Aplicar permisos
        CX.SesionUsuario sesion = CX.SesionUsuario.getInstancia();
        btnNuevo.setEnabled(sesion.tienePermiso("Proveedores", "Crear"));
        btnEditar.setEnabled(sesion.tienePermiso("Proveedores", "Editar"));
        btnEliminar.setEnabled(sesion.tienePermiso("Proveedores", "Eliminar"));

        acciones.add(campoBuscar); acciones.add(btnNuevo); acciones.add(btnEditar); acciones.add(btnEliminar);
        header.add(titulo, BorderLayout.WEST);
        header.add(acciones, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        String[] cols = {"ID", "Empresa", "NIT/Cédula", "Teléfono", "Dirección", "Correo", "Contacto"};
        modelo = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tabla = PanelProductos.crearTabla(modelo);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(new Color(39, 39, 42));
        add(scroll, BorderLayout.CENTER);
        cargar();
    }

    private void cargar() {
        modelo.setRowCount(0);
        try {
            for (Proveedor p : dao.listarTodos())
                modelo.addRow(new Object[]{p.getId(), p.getNombreEmpresa(), p.getNitCedula(), p.getTelefono(), p.getDireccion(), p.getCorreo(), p.getNombreContacto()});
        } catch (SQLException e) { JOptionPane.showMessageDialog(this, "Error: " + e.getMessage()); }
    }

    private void buscar() {
        String t = campoBuscar.getText().trim();
        modelo.setRowCount(0);
        try {
            for (Proveedor p : t.isEmpty() ? dao.listarTodos() : dao.buscar(t))
                modelo.addRow(new Object[]{p.getId(), p.getNombreEmpresa(), p.getNitCedula(), p.getTelefono(), p.getDireccion(), p.getCorreo(), p.getNombreContacto()});
        } catch (SQLException e) { JOptionPane.showMessageDialog(this, "Error: " + e.getMessage()); }
    }

    private void dialogo(Proveedor prov) {
        boolean ed = prov != null;
        JTextField tfEmp = PanelProductos.crearCampo(ed ? prov.getNombreEmpresa() : "", 100);
        JTextField tfNit = PanelProductos.crearCampo(ed ? (prov.getNitCedula() != null ? prov.getNitCedula() : "") : "", 20);
        JTextField tfTel = PanelProductos.crearCampo(ed ? (prov.getTelefono() != null ? prov.getTelefono() : "") : "", 20);
        JTextField tfDir = PanelProductos.crearCampo(ed ? (prov.getDireccion() != null ? prov.getDireccion() : "") : "", 200);
        JTextField tfCorr = PanelProductos.crearCampo(ed ? (prov.getCorreo() != null ? prov.getCorreo() : "") : "", 100);
        JTextField tfCont = PanelProductos.crearCampo(ed ? (prov.getNombreContacto() != null ? prov.getNombreContacto() : "") : "", 100);

        JPanel p = new JPanel(new GridLayout(12, 1, 5, 3));
        p.setBackground(new Color(39, 39, 42));
        p.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        String[] lbs = {"Empresa:", "NIT/Cédula:", "Teléfono:", "Dirección:", "Correo:", "Contacto:"};
        JTextField[] fs = {tfEmp, tfNit, tfTel, tfDir, tfCorr, tfCont};
        for (int i = 0; i < lbs.length; i++) {
            JLabel l = new JLabel(lbs[i]); l.setForeground(Color.WHITE);
            p.add(l); p.add(fs[i]);
        }

        int r = JOptionPane.showConfirmDialog(this, p, ed ? "Editar Proveedor" : "Nuevo Proveedor", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r == JOptionPane.OK_OPTION) {
            try {
                Proveedor pr = ed ? prov : new Proveedor();
                pr.setNombreEmpresa(tfEmp.getText().trim());
                pr.setNitCedula(tfNit.getText().trim());
                pr.setTelefono(tfTel.getText().trim());
                pr.setDireccion(tfDir.getText().trim());
                pr.setCorreo(tfCorr.getText().trim());
                pr.setNombreContacto(tfCont.getText().trim());
                if (pr.getNombreEmpresa().isEmpty()) { JOptionPane.showMessageDialog(this, "Empresa obligatoria"); return; }
                if (ed) dao.actualizar(pr); else dao.insertar(pr);
                cargar();
            } catch (SQLException e) { CX.ConexionBD.errorManager(e); }
        }
    }

    private void editarSel() {
        int row = tabla.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Seleccione un proveedor."); return; }
        Proveedor p = new Proveedor();
        p.setId((long) modelo.getValueAt(row, 0));
        p.setNombreEmpresa((String) modelo.getValueAt(row, 1));
        p.setNitCedula((String) modelo.getValueAt(row, 2));
        p.setTelefono((String) modelo.getValueAt(row, 3));
        p.setDireccion((String) modelo.getValueAt(row, 4));
        p.setCorreo((String) modelo.getValueAt(row, 5));
        p.setNombreContacto((String) modelo.getValueAt(row, 6));
        dialogo(p);
    }

    private void eliminarSel() {
        int row = tabla.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Seleccione un proveedor."); return; }
        long id = (long) modelo.getValueAt(row, 0);
        if (JOptionPane.showConfirmDialog(this, "¿Eliminar?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try { dao.eliminar(id); cargar(); } catch (SQLException e) { CX.ConexionBD.errorManager(e); }
        }
    }
}
