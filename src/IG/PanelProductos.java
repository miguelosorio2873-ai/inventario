package IG;

import DAO.ProductoDAO;
import DAO.CategoriaDAO;
import Modelo.Producto;
import Modelo.Categoria;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.List;

public class PanelProductos extends JPanel {

    private JTable tabla;
    private DefaultTableModel modelo;
    private JTextField campoBuscar;
    private ProductoDAO dao = new ProductoDAO();

    public PanelProductos() {
        setBackground(new Color(24, 24, 27));
        setLayout(new BorderLayout(0, 15));
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        // Header
        JPanel header = new JPanel(new BorderLayout(15, 0));
        header.setOpaque(false);

        JLabel titulo = new JLabel("Productos");
        try {
            com.formdev.flatlaf.extras.FlatSVGIcon icon = new com.formdev.flatlaf.extras.FlatSVGIcon(getClass().getResource("/IMG/box.svg")).derive(24, 24);
            icon.setColorFilter(new com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter(c -> Color.WHITE));
            titulo.setIcon(icon);
            titulo.setIconTextGap(10);
        } catch (Exception e) {}
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titulo.setForeground(Color.WHITE);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        acciones.setOpaque(false);

        campoBuscar = new JTextField(20);
        ((javax.swing.text.AbstractDocument) campoBuscar.getDocument()).setDocumentFilter(new Utils.ValidadorCampos(100, false, false));
        campoBuscar.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        campoBuscar.setBackground(new Color(39, 39, 42));
        campoBuscar.setForeground(Color.WHITE);
        campoBuscar.setCaretColor(new Color(16, 185, 129));
        campoBuscar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(63, 63, 70)), BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        campoBuscar.putClientProperty("JTextField.placeholderText", "Buscar por nombre o SKU...");
        campoBuscar.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { buscar(); }
        });

        JButton btnNuevo = crearBoton("➕ Nuevo", new Color(16, 185, 129));
        btnNuevo.addActionListener(e -> mostrarDialogo(null));

        JButton btnEditar = crearBoton("✏️ Editar", new Color(59, 130, 246));
        btnEditar.addActionListener(e -> editarSeleccionado());

        JButton btnEliminar = crearBoton("🗑️ Eliminar", new Color(239, 68, 68));
        btnEliminar.addActionListener(e -> eliminarSeleccionado());

        // Aplicar permisos
        CX.SesionUsuario sesion = CX.SesionUsuario.getInstancia();
        btnNuevo.setEnabled(sesion.tienePermiso("Productos", "Crear"));
        btnEditar.setEnabled(sesion.tienePermiso("Productos", "Editar"));
        btnEliminar.setEnabled(sesion.tienePermiso("Productos", "Eliminar"));

        acciones.add(campoBuscar);
        acciones.add(btnNuevo);
        acciones.add(btnEditar);
        acciones.add(btnEliminar);

        header.add(titulo, BorderLayout.WEST);
        header.add(acciones, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Tabla
        String[] cols = {"ID", "SKU", "Nombre", "Categoría", "Precio ($)", "Precio (Bs)", "Costo ($)", "Stock", "Mínimo", "Estado"};
        modelo = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tabla = crearTabla(modelo);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(new Color(39, 39, 42));
        add(scroll, BorderLayout.CENTER);

        cargarDatos();
    }

    private void cargarDatos() {
        modelo.setRowCount(0);
        try {
            double tasa = Utils.Config.getTasaVES();
            List<Producto> lista = dao.listarTodos();
            for (Producto p : lista) {
                modelo.addRow(new Object[]{p.getId(), p.getSku(), p.getNombre(),
                    p.getCategoriaNombre(), String.format("$%.2f", p.getPrecioVenta()),
                    String.format("Bs%.2f", p.getPrecioVenta() * tasa),
                    String.format("$%.2f", p.getCostoPromedio()), String.format("%.0f", p.getStockActual()),
                    String.format("%.0f", p.getStockMinimo()), p.isState() ? "Activo" : "Inactivo"});
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void buscar() {
        String texto = campoBuscar.getText().trim();
        modelo.setRowCount(0);
        try {
            double tasa = Utils.Config.getTasaVES();
            List<Producto> lista = texto.isEmpty() ? dao.listarTodos() : dao.buscar(texto);
            for (Producto p : lista) {
                modelo.addRow(new Object[]{p.getId(), p.getSku(), p.getNombre(),
                    p.getCategoriaNombre(), String.format("$%.2f", p.getPrecioVenta()),
                    String.format("Bs%.2f", p.getPrecioVenta() * tasa),
                    String.format("$%.2f", p.getCostoPromedio()), String.format("%.0f", p.getStockActual()),
                    String.format("%.0f", p.getStockMinimo()), p.isState() ? "Activo" : "Inactivo"});
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void mostrarDialogo(Producto prod) {
        boolean editando = prod != null;
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            editando ? "Editar Producto" : "Nuevo Producto", true);
        dlg.setSize(500, 550);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(new Color(39, 39, 42));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(5, 5, 5, 5);
        gc.gridx = 0; gc.weightx = 0.3;

        JTextField tfSku = crearCampo(editando ? prod.getSku() : "", 50);
        JTextField tfNombre = crearCampo(editando ? prod.getNombre() : "", 100);
        JTextField tfDescripcion = crearCampo(editando ? (prod.getDescripcion() != null ? prod.getDescripcion() : "") : "", 255);
        JTextField tfPrecioVenta = crearCampoNumerico(editando ? String.valueOf(prod.getPrecioVenta()) : "", 15, true);
        JTextField tfCosto = crearCampoNumerico(editando ? String.valueOf(prod.getCostoPromedio()) : "", 15, true);
        JTextField tfStockMin = crearCampoNumerico(editando ? String.valueOf(prod.getStockMinimo()) : "", 15, true);
        JComboBox<Categoria> cbCategoria = new JComboBox<>();
        cbCategoria.setBackground(new Color(45, 45, 45));
        cbCategoria.setForeground(Color.WHITE);
        JCheckBox chkActivo = new JCheckBox("Activo", editando ? prod.isState() : true);
        chkActivo.setForeground(Color.WHITE);
        chkActivo.setOpaque(false);

        try {
            List<Categoria> cats = new CategoriaDAO().listarTodas();
            for (Categoria c : cats) {
                cbCategoria.addItem(c);
                if (editando && c.getId() == prod.getCategoriaId()) cbCategoria.setSelectedItem(c);
            }
        } catch (SQLException e) { /* ignore */ }

        // Label en vivo para mostrar Bs
        JLabel lblEquivalenteBs = new JLabel();
        lblEquivalenteBs.setForeground(new Color(52, 211, 153));
        lblEquivalenteBs.setFont(new Font("Segoe UI", Font.BOLD, 12));
        javax.swing.event.DocumentListener listenerBs = new javax.swing.event.DocumentListener() {
            private void update() {
                try {
                    double val = Double.parseDouble(tfPrecioVenta.getText().trim().replace(",", "."));
                    lblEquivalenteBs.setText(String.format("Equivale a: Bs%.2f", val * Utils.Config.getTasaVES()));
                } catch(Exception ex) {
                    lblEquivalenteBs.setText("Equivale a: Bs0.00");
                }
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
        };
        tfPrecioVenta.getDocument().addDocumentListener(listenerBs);
        // Llamar inicial manual paramétriza en edición
        if (editando) tfPrecioVenta.setText(String.valueOf(prod.getPrecioVenta()));

        String[] labels = {"SKU:", "Nombre:", "Descripción:", "Precio (USD):", "Costo (USD):", "Stock Mínimo:", "Categoría:"};
        JComponent[] fields = {tfSku, tfNombre, tfDescripcion, tfPrecioVenta, tfCosto, tfStockMin, cbCategoria};

        for (int i = 0; i < labels.length; i++) {
            gc.gridy = i * 2; // dejar un espacio extra vertical para labels abajo 
            gc.gridx = 0; gc.weightx = 0.3;
            JLabel lbl = new JLabel(labels[i]);
            lbl.setForeground(new Color(180, 180, 180));
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            form.add(lbl, gc);
            gc.gridx = 1; gc.weightx = 0.7;
            form.add(fields[i], gc);
            
            // Añadir el label dinámico de Bs debajo del campo del precio
            if (i == 3) {
                gc.gridy = i * 2 + 1;
                gc.insets = new Insets(0, 5, 5, 5);
                form.add(lblEquivalenteBs, gc);
                gc.insets = new Insets(5, 5, 5, 5); // restaurar insets originales
            }
        }
        gc.gridy = labels.length * 2; gc.gridx = 1;
        form.add(chkActivo, gc);

        JButton btnGuardar = crearBoton("💾 Guardar", new Color(16, 185, 129));
        btnGuardar.addActionListener(e -> {
            try {
                Producto p = editando ? prod : new Producto();
                p.setSku(tfSku.getText().trim());
                p.setNombre(tfNombre.getText().trim());
                p.setDescripcion(tfDescripcion.getText().trim());
                p.setPrecioVenta(parseSafeDouble(tfPrecioVenta.getText()));
                p.setCostoPromedio(parseSafeDouble(tfCosto.getText()));
                p.setStockMinimo(parseSafeDouble(tfStockMin.getText()));
                p.setState(chkActivo.isSelected());
                Categoria catSel = (Categoria) cbCategoria.getSelectedItem();
                if (catSel != null) p.setCategoriaId(catSel.getId());

                if (p.getNombre().isEmpty()) {
                    JOptionPane.showMessageDialog(dlg, "El nombre es obligatorio.");
                    return;
                }

                if (editando) dao.actualizar(p);
                else dao.insertar(p);

                dlg.dispose();
                cargarDatos();
                JOptionPane.showMessageDialog(this, editando ? "✅ Producto actualizado" : "✅ Producto creado");
            } catch (SQLException ex) {
                CX.ConexionBD.errorManager(ex);
            }
        });

        gc.gridy = labels.length * 2 + 1; gc.gridx = 0; gc.gridwidth = 2;
        gc.insets = new Insets(15, 5, 5, 5);
        form.add(btnGuardar, gc);

        dlg.add(form);
        dlg.setVisible(true);
    }

    private void editarSeleccionado() {
        int row = tabla.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Seleccione un producto."); return; }
        long id = (long) modelo.getValueAt(row, 0);
        try {
            Producto p = dao.obtenerPorId(id);
            if (p != null) mostrarDialogo(p);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void eliminarSeleccionado() {
        int row = tabla.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Seleccione un producto."); return; }
        long id = (long) modelo.getValueAt(row, 0);
        int r = JOptionPane.showConfirmDialog(this, "¿Eliminar este producto?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) {
            try {
                dao.eliminar(id);
                cargarDatos();
                JOptionPane.showMessageDialog(this, "✅ Producto eliminado");
            } catch (SQLException e) {
                CX.ConexionBD.errorManager(e);
            }
        }
    }

    static double parseSafeDouble(String text) {
        if (text == null || text.trim().isEmpty() || text.trim().equals("-") || text.trim().equals(".") || text.trim().equals(",")) {
            return 0.0;
        }
        try {
            return Double.parseDouble(text.trim().replace(",", "."));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    static JButton crearBoton(String texto, Color color) {
        String cleanText = texto.replaceAll("[➕✏️🗑️🧹✅❌🔍🔄?🌙☀️💾📥📤🔧🔑📊🧾👀]", "").trim();
        JButton btn = new JButton(cleanText);
        
        try {
            String iconName = null;
            if (texto.contains("➕")) iconName = "plus.svg";
            else if (texto.contains("✏️")) iconName = "edit.svg";
            else if (texto.contains("🗑️")) iconName = "trash.svg";
            else if (texto.contains("🧹")) iconName = "clear.svg";
            else if (texto.contains("✅")) iconName = "check-circle.svg";
            else if (texto.contains("❌")) iconName = "x.svg";
            else if (texto.contains("🔍")) iconName = "search.svg";
            else if (texto.contains("🔄")) iconName = "refresh.svg";
            else if (texto.contains("?")) iconName = "file-invoice.svg";
            else if (texto.contains("🌙")) iconName = "moon.svg";
            else if (texto.contains("☀️")) iconName = "sun.svg";
            else if (texto.contains("💾")) iconName = "save.svg";
            else if (texto.contains("📥")) iconName = "download.svg";
            else if (texto.contains("📤")) iconName = "upload.svg";
            else if (texto.contains("🔧")) iconName = "wrench.svg";
            else if (texto.contains("🔑")) iconName = "key.svg";
            else if (texto.contains("📊")) iconName = "chart-bar.svg";
            else if (texto.contains("🧾")) iconName = "file-invoice.svg";
            else if (texto.contains("👀")) iconName = "eye.svg";

            if (iconName != null) {
                com.formdev.flatlaf.extras.FlatSVGIcon icon = new com.formdev.flatlaf.extras.FlatSVGIcon(PanelProductos.class.getResource("/IMG/" + iconName));
                icon = (com.formdev.flatlaf.extras.FlatSVGIcon) icon.derive(16, 16);
                icon.setColorFilter(new com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                btn.setIcon(icon);
                btn.setIconTextGap(8);
            }
        } catch (Exception e){}

        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(color);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(color.darker()); }
            public void mouseExited(MouseEvent e) { btn.setBackground(color); }
        });
        return btn;
    }

    static JTextField crearCampo(String texto) {
        return crearCampo(texto, 255); // Límite por defecto de 255 basado en la BD
    }

    static JTextField crearCampo(String texto, int limite) {
        JTextField tf = new JTextField(texto);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setBackground(new Color(45, 45, 45));
        tf.setForeground(Color.WHITE);
        tf.setCaretColor(new Color(16, 185, 129));
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(63, 63, 70)),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        
        if (limite > 0 && tf.getDocument() instanceof javax.swing.text.AbstractDocument) {
            ((javax.swing.text.AbstractDocument) tf.getDocument()).setDocumentFilter(new Utils.ValidadorCampos(limite, false, false));
        }

        // Seleccionar todo al ganar foco
        tf.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                tf.selectAll();
            }
        });

        return tf;
    }

    static JTextField crearCampoNumerico(String texto, int limite, boolean decimal) {
        JTextField tf = new JTextField(texto);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setBackground(new Color(45, 45, 45));
        tf.setForeground(Color.WHITE);
        tf.setCaretColor(new Color(16, 185, 129));
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(63, 63, 70)),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        
        if (limite > 0 && tf.getDocument() instanceof javax.swing.text.AbstractDocument) {
            ((javax.swing.text.AbstractDocument) tf.getDocument()).setDocumentFilter(new Utils.ValidadorCampos(limite, true, decimal));
        }

        // Seleccionar todo al ganar foco
        tf.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                tf.selectAll();
            }
        });

        return tf;
    }

    static JTable crearTabla(DefaultTableModel modelo) {
        JTable tabla = new JTable(modelo);
        tabla.setBackground(new Color(39, 39, 42));
        tabla.setForeground(Color.WHITE);
        tabla.setGridColor(new Color(55, 55, 55));
        tabla.setSelectionBackground(new Color(16, 185, 129, 50));
        tabla.setSelectionForeground(Color.WHITE);
        tabla.setRowHeight(34);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabla.setShowVerticalLines(false);
        tabla.getTableHeader().setBackground(new Color(30, 30, 30));
        tabla.getTableHeader().setForeground(new Color(180, 180, 180));
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabla.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(55, 55, 55)));
        tabla.setIntercellSpacing(new Dimension(0, 1));
        return tabla;
    }
}
