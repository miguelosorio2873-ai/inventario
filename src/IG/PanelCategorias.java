package IG;

import DAO.CategoriaDAO;
import Modelo.Categoria;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class PanelCategorias extends JPanel {

    private JTable tabla;
    private DefaultTableModel modelo;
    private CategoriaDAO dao = new CategoriaDAO();

    public PanelCategorias() {
        setBackground(new Color(24, 24, 27));
        setLayout(new BorderLayout(0, 15));
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel titulo = new JLabel("Categorías");
        try {
            com.formdev.flatlaf.extras.FlatSVGIcon icon = new com.formdev.flatlaf.extras.FlatSVGIcon(getClass().getResource("/IMG/tags.svg")).derive(24, 24);
            icon.setColorFilter(new com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter(c -> Color.WHITE));
            titulo.setIcon(icon);
            titulo.setIconTextGap(10);
        } catch (Exception e) {}
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titulo.setForeground(Color.WHITE);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        acciones.setOpaque(false);

        JButton btnNuevo = PanelProductos.crearBoton("➕ Nueva", new Color(16, 185, 129));
        btnNuevo.addActionListener(e -> dialogo(null));
        JButton btnEditar = PanelProductos.crearBoton("✏️ Editar", new Color(59, 130, 246));
        btnEditar.addActionListener(e -> editarSel());
        JButton btnEliminar = PanelProductos.crearBoton("🗑️ Eliminar", new Color(239, 68, 68));
        btnEliminar.addActionListener(e -> eliminarSel());

        // Aplicar permisos
        CX.SesionUsuario sesion = CX.SesionUsuario.getInstancia();
        btnNuevo.setEnabled(sesion.tienePermiso("Categorias", "Crear"));
        btnEditar.setEnabled(sesion.tienePermiso("Categorias", "Editar"));
        btnEliminar.setEnabled(sesion.tienePermiso("Categorias", "Eliminar"));

        acciones.add(btnNuevo); acciones.add(btnEditar); acciones.add(btnEliminar);
        header.add(titulo, BorderLayout.WEST);
        header.add(acciones, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        String[] cols = {"ID", "Nombre", "Descripción"};
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
            for (Categoria c : dao.listarTodas())
                modelo.addRow(new Object[]{c.getId(), c.getNombre(), c.getDescripcion()});
        } catch (SQLException e) { JOptionPane.showMessageDialog(this, "Error: " + e.getMessage()); }
    }

    private void dialogo(Categoria cat) {
        boolean ed = cat != null;
        JTextField tfNombre = PanelProductos.crearCampo(ed ? cat.getNombre() : "", 100);
        JTextField tfDesc = PanelProductos.crearCampo(ed ? (cat.getDescripcion() != null ? cat.getDescripcion() : "") : "", 255);

        JPanel p = new JPanel(new GridLayout(4, 1, 5, 5));
        p.setBackground(new Color(39, 39, 42));
        p.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JLabel l1 = new JLabel("Nombre:"); l1.setForeground(Color.WHITE);
        JLabel l2 = new JLabel("Descripción:"); l2.setForeground(Color.WHITE);
        p.add(l1); p.add(tfNombre); p.add(l2); p.add(tfDesc);

        int r = JOptionPane.showConfirmDialog(this, p, ed ? "Editar Categoría" : "Nueva Categoría", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r == JOptionPane.OK_OPTION) {
            try {
                Categoria c = ed ? cat : new Categoria();
                c.setNombre(tfNombre.getText().trim());
                c.setDescripcion(tfDesc.getText().trim());
                if (c.getNombre().isEmpty()) { JOptionPane.showMessageDialog(this, "Nombre obligatorio"); return; }
                if (ed) dao.actualizar(c); else dao.insertar(c);
                cargar();
            } catch (SQLException e) { CX.ConexionBD.errorManager(e); }
        }
    }

    private void editarSel() {
        int row = tabla.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Seleccione una categoría."); return; }
        long id = (long) modelo.getValueAt(row, 0);
        Categoria c = new Categoria(id, (String) modelo.getValueAt(row, 1), (String) modelo.getValueAt(row, 2));
        dialogo(c);
    }

    private void eliminarSel() {
        int row = tabla.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Seleccione una categoría."); return; }
        long id = (long) modelo.getValueAt(row, 0);
        if (JOptionPane.showConfirmDialog(this, "¿Eliminar?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try { dao.eliminar(id); cargar(); } catch (SQLException e) { CX.ConexionBD.errorManager(e); }
        }
    }
}
