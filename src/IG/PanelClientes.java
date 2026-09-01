package IG;

import DAO.ClienteDAO;
import Modelo.Cliente;
import Utils.UI;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.List;

public class PanelClientes extends JPanel {

    private JTable tabla;
    private DefaultTableModel modelo;
    private JTextField campoBuscar;
    private ClienteDAO dao = new ClienteDAO();
    private JLabel lblPie;

    public PanelClientes() {
        setBackground(new Color(24, 24, 27));
        setLayout(new BorderLayout(0, 15));
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JPanel header = new JPanel(new BorderLayout(15, 0));
        header.setOpaque(false);

        JLabel titulo = new JLabel("Clientes");
        try {
            com.formdev.flatlaf.extras.FlatSVGIcon icon = new com.formdev.flatlaf.extras.FlatSVGIcon(getClass().getResource("/IMG/users.svg")).derive(32, 32);
            icon.setColorFilter(new com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter(c -> Color.WHITE));
            titulo.setIcon(icon);
            titulo.setIconTextGap(10);
        } catch (Exception e) {}
        titulo.setFont(Utils.UI.TITULO);
        titulo.setForeground(Color.WHITE);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        acciones.setOpaque(false);

        campoBuscar = PanelProductos.crearCampo("", 100);
        campoBuscar.setColumns(20);
        campoBuscar.putClientProperty("JTextField.placeholderText", "Buscar por cualquier característica...");
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
        btnNuevo.setEnabled(sesion.tienePermiso("Clientes", "Crear"));
        btnEditar.setEnabled(sesion.tienePermiso("Clientes", "Editar"));
        btnEliminar.setEnabled(sesion.tienePermiso("Clientes", "Eliminar"));

        acciones.add(campoBuscar); acciones.add(btnNuevo); acciones.add(btnEditar); acciones.add(btnEliminar);
        header.add(titulo, BorderLayout.WEST);
        header.add(acciones, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        String[] cols = {"ID", "Cédula", "Nombre", "Correo", "Teléfono"};
        modelo = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tabla = PanelProductos.crearTabla(modelo);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(new Color(39, 39, 42));
        add(scroll, BorderLayout.CENTER);
        lblPie = new JLabel(" ");
        lblPie.setFont(Utils.UI.TEXTO_NEGRITA);
        lblPie.setForeground(new Color(16, 185, 129));
        lblPie.setBorder(BorderFactory.createEmptyBorder(8, 2, 0, 2));
        add(lblPie, BorderLayout.SOUTH);
        cargar();
    }

    private void cargar() {
        modelo.setRowCount(0);
        try {
            for (Cliente c : dao.listarTodos())
                modelo.addRow(new Object[]{c.getId(), c.getCedula(), c.getNombre(), c.getCorreo(), c.getTelefono()});
        } catch (SQLException e) { JOptionPane.showMessageDialog(this, "Error: " + e.getMessage()); }
        actualizarPie();
    }

    private void buscar() {
        String t = campoBuscar.getText().trim();
        modelo.setRowCount(0);
        try {
            for (Cliente c : t.isEmpty() ? dao.listarTodos() : dao.buscarEnMemoria(t))
                modelo.addRow(new Object[]{c.getId(), c.getCedula(), c.getNombre(), c.getCorreo(), c.getTelefono()});
        } catch (SQLException e) { JOptionPane.showMessageDialog(this, "Error: " + e.getMessage()); }
        actualizarPie();
    }

    private void dialogo(Cliente cli) {
        boolean ed = cli != null;
        JTextField tfCed = PanelProductos.crearCampoNumerico(ed ? cli.getCedula() : "", 20, false);
        JTextField tfNom = PanelProductos.crearCampo(ed ? cli.getNombre() : "", 100);
        JTextField tfCorr = PanelProductos.crearCampo(ed ? (cli.getCorreo() != null ? cli.getCorreo() : "") : "", 100);
        JTextField tfTel = PanelProductos.crearCampoNumerico(ed ? (cli.getTelefono() != null ? cli.getTelefono() : "") : "", 20, false);

        JPanel p = new JPanel(new GridLayout(8, 1, 5, 3));
        p.setBackground(new Color(39, 39, 42));
        p.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        String[] lbs = {"Cédula:", "Nombre:", "Correo:", "Teléfono:"};
        JTextField[] fs = {tfCed, tfNom, tfCorr, tfTel};
        for (int i = 0; i < lbs.length; i++) {
            JLabel l = new JLabel(lbs[i]); l.setForeground(Color.WHITE);
            p.add(l); p.add(fs[i]);
        }

        int r = JOptionPane.showConfirmDialog(this, p, ed ? "Editar Cliente" : "Nuevo Cliente", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r == JOptionPane.OK_OPTION) {
            try {
                Cliente c = ed ? cli : new Cliente();
                c.setCedula(tfCed.getText().trim());
                c.setNombre(tfNom.getText().trim());
                c.setCorreo(tfCorr.getText().trim());
                c.setTelefono(tfTel.getText().trim());
                if (c.getNombre().isEmpty()) { JOptionPane.showMessageDialog(this, "Nombre obligatorio"); return; }
                if (ed) dao.actualizar(c); else dao.insertar(c);
                new DAO.BitacoraDAO().registrar("Clientes", ed ? "Editar" : "Crear",
                    (ed ? "Cliente actualizado: " : "Cliente creado: ") + c.getNombre() + " [Cedula: " + c.getCedula() + "]");
                cargar();
            } catch (SQLException e) { CX.ConexionBD.errorManager(e); }
        }
    }

    private void editarSel() {
        int row = tabla.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Seleccione un cliente."); return; }
        Cliente c = new Cliente();
        c.setId((long) modelo.getValueAt(row, 0));
        c.setCedula((String) modelo.getValueAt(row, 1));
        c.setNombre((String) modelo.getValueAt(row, 2));
        c.setCorreo((String) modelo.getValueAt(row, 3));
        c.setTelefono((String) modelo.getValueAt(row, 4));
        dialogo(c);
    }

    private void eliminarSel() {
        int row = tabla.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Seleccione un cliente."); return; }
        long id = (long) modelo.getValueAt(row, 0);
        if (JOptionPane.showConfirmDialog(this, "¿Eliminar?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try {
                String nombre = (String) modelo.getValueAt(row, 2);
                dao.eliminar(id);
                new DAO.BitacoraDAO().registrar("Clientes", "Eliminar", "Cliente eliminado: " + nombre + " [ID: " + id + "]");
                cargar();
            } catch (SQLException e) { CX.ConexionBD.errorManager(e); }
        }
    }

    private void actualizarPie() {
        if (lblPie != null) lblPie.setText("Clientes: " + modelo.getRowCount());
    }
}
