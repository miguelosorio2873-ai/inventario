package IG;

import DAO.ProductoDAO;
import DAO.CategoriaDAO;
import DAO.InventarioDAO;
import DAO.BitacoraDAO;
import Modelo.Producto;
import Modelo.Categoria;
import Modelo.MovimientoInventario;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.List;
import Utils.UI;
import Utils.Formato;

public class PanelProductos extends JPanel {

    private JTable tabla;
    private DefaultTableModel modelo;
    private JTextField campoBuscar;
    private ProductoDAO dao = new ProductoDAO();
    private JLabel lblPie;

    public PanelProductos() {
        setBackground(new Color(24, 24, 27));
        setLayout(new BorderLayout(0, 15));
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        // Header
        JPanel header = new JPanel(new BorderLayout(15, 0));
        header.setOpaque(false);

        JLabel titulo = new JLabel("Productos");
        try {
            com.formdev.flatlaf.extras.FlatSVGIcon icon = new com.formdev.flatlaf.extras.FlatSVGIcon(getClass().getResource("/IMG/box.svg")).derive(32, 32);
            icon.setColorFilter(new com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter(c -> Color.WHITE));
            titulo.setIcon(icon);
            titulo.setIconTextGap(10);
        } catch (Exception e) {}
        titulo.setFont(Utils.UI.TITULO);
        titulo.setForeground(Color.WHITE);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        acciones.setOpaque(false);

        campoBuscar = new JTextField(20);
        ((javax.swing.text.AbstractDocument) campoBuscar.getDocument()).setDocumentFilter(new Utils.ValidadorCampos(100, false, false));
        campoBuscar.setFont(Utils.UI.CAMPO);
        campoBuscar.setBackground(new Color(39, 39, 42));
        campoBuscar.setForeground(Color.WHITE);
        campoBuscar.setCaretColor(new Color(16, 185, 129));
        campoBuscar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(63, 63, 70)), BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        campoBuscar.putClientProperty("JTextField.placeholderText", "Buscar por cualquier característica...");
        campoBuscar.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { buscar(); }
        });

        JButton btnNuevo = crearBoton("➕ Nuevo", new Color(16, 185, 129));
        btnNuevo.addActionListener(e -> mostrarDialogo(null));

        JButton btnEditar = crearBoton("✏️ Editar", new Color(59, 130, 246));
        btnEditar.addActionListener(e -> editarSeleccionado());

        JButton btnReabastecer = crearBoton("🚚 Reabastecer", new Color(245, 158, 11));
        btnReabastecer.addActionListener(e -> reabastecerSeleccionado());

        JButton btnSacar = crearBoton("➖ Sacar", new Color(239, 68, 68));
        btnSacar.addActionListener(e -> sacarSeleccionado());

        JButton btnEliminar = crearBoton("🗑️ Eliminar", new Color(239, 68, 68));
        btnEliminar.addActionListener(e -> eliminarSeleccionado());

        // Aplicar permisos
        CX.SesionUsuario sesion = CX.SesionUsuario.getInstancia();
        btnNuevo.setEnabled(sesion.tienePermiso("Productos", "Crear"));
        btnEditar.setEnabled(sesion.tienePermiso("Productos", "Editar"));
        btnReabastecer.setEnabled(sesion.tienePermiso("Productos", "Editar"));
        btnSacar.setEnabled(sesion.tienePermiso("Productos", "Editar"));
        btnEliminar.setEnabled(sesion.tienePermiso("Productos", "Eliminar"));

        acciones.add(campoBuscar);
        acciones.add(btnNuevo);
        acciones.add(btnEditar);
        acciones.add(btnReabastecer);
        acciones.add(btnSacar);
        acciones.add(btnEliminar);

        header.add(titulo, BorderLayout.WEST);
        header.add(acciones, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Tabla
        String[] cols = {"ID", "SKU", "Nombre", "Categoría", "Presentación", "Unid/Pres", "Costo Pres.", "Costo/Unid.", "Precio", "Ganancia", "Cantidad", "Estado"};
        modelo = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tabla = crearTabla(modelo);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(new Color(39, 39, 42));
        add(scroll, BorderLayout.CENTER);

        lblPie = new JLabel(" ");
        lblPie.setFont(Utils.UI.TEXTO_NEGRITA);
        lblPie.setForeground(new Color(16, 185, 129));
        lblPie.setBorder(BorderFactory.createEmptyBorder(8, 2, 0, 2));
        add(lblPie, BorderLayout.SOUTH);

        cargarDatos();
    }

    private void cargarDatos() {
        modelo.setRowCount(0);
        double stockTotal = 0;
        try {
            double tasa = Utils.Config.getTasaVES();
            List<Producto> lista = dao.listarTodos();
            for (Producto p : lista) {
                String presentacion = (p.getPresentacion() != null && !p.getPresentacion().isEmpty()) ? p.getPresentacion() : "Unidad";
                double unid = p.getUnidadesPresentacion() > 0 ? p.getUnidadesPresentacion() : 1;
                stockTotal += p.getStockActual();
                modelo.addRow(new Object[]{
                    p.getId(), p.getSku(), p.getNombre(), p.getCategoriaNombre(),
                    presentacion, String.format("%.0f", unid),
                    Utils.Formato.usdBs(p.getCostoPresentacion()),
                    Utils.Formato.usdBs(p.getCostoPorUnidad()),
                    Utils.Formato.usdBs(p.getPrecioVenta()),
                    Utils.Formato.usdBs(p.getGananciaPorUnidad()),
                    String.format("%.0f", p.getStockActual()),
                    p.isState() ? "Activo" : "Inactivo"});
            }
            if (lblPie != null) lblPie.setText(String.format("Productos: %d | Stock total: %s unidades", lista.size(), String.format("%.0f", stockTotal)));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void buscar() {
        String texto = campoBuscar.getText().trim();
        modelo.setRowCount(0);
        double stockTotal = 0;
        try {
            double tasa = Utils.Config.getTasaVES();
            List<Producto> lista = texto.isEmpty() ? dao.listarTodos() : dao.buscarEnMemoria(texto);
            for (Producto p : lista) {
                String presentacion = (p.getPresentacion() != null && !p.getPresentacion().isEmpty()) ? p.getPresentacion() : "Unidad";
                double unid = p.getUnidadesPresentacion() > 0 ? p.getUnidadesPresentacion() : 1;
                stockTotal += p.getStockActual();
                modelo.addRow(new Object[]{
                    p.getId(), p.getSku(), p.getNombre(), p.getCategoriaNombre(),
                    presentacion, String.format("%.0f", unid),
                    Utils.Formato.usdBs(p.getCostoPresentacion()),
                    Utils.Formato.usdBs(p.getCostoPorUnidad()),
                    Utils.Formato.usdBs(p.getPrecioVenta()),
                    Utils.Formato.usdBs(p.getGananciaPorUnidad()),
                    String.format("%.0f", p.getStockActual()),
                    p.isState() ? "Activo" : "Inactivo"});
            }
            if (lblPie != null) lblPie.setText(String.format("Productos: %d | Stock total: %s unidades", lista.size(), String.format("%.0f", stockTotal)));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void mostrarDialogo(Producto prod) {
        boolean editando = prod != null;
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            editando ? "Editar Producto" : "Nuevo Producto", true);
        dlg.setSize(680, 700);
        dlg.setMinimumSize(new Dimension(660, 520));
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(new Color(39, 39, 42));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(5, 5, 5, 5);
        gc.gridx = 0; gc.weightx = 0.3;

        String skuInicial = editando ? prod.getSku() : "";
        if (!editando) {
            try { skuInicial = dao.siguienteSku(); } catch (SQLException e) { /* usar vacio */ }
        }
        JTextField tfSku = crearCampo(skuInicial, 50);
        JTextField tfNombre = crearCampo(editando ? prod.getNombre() : "", 100);
        JTextField tfDescripcion = crearCampo(editando ? (prod.getDescripcion() != null ? prod.getDescripcion() : "") : "", 255);
        JTextField tfPrecioVenta = crearCampoNumerico(editando ? String.valueOf(prod.getPrecioVenta()) : "", 15, true);
        JTextField tfCostoPres = crearCampoNumerico(editando && prod.getCostoPresentacion() > 0 ? String.valueOf(prod.getCostoPresentacion()) : (editando ? String.valueOf(prod.getCostoPromedio()) : ""), 15, true);
        JTextField tfUnidades = crearCampoNumerico(editando ? String.valueOf(prod.getUnidadesPresentacion() > 0 ? prod.getUnidadesPresentacion() : 1) : "1", 15, true);
        JComboBox<String> cbPresentacion = new JComboBox<>(new String[]{"Bulto", "Caja", "Paquete", "Unidad"});
        cbPresentacion.setBackground(new Color(45, 45, 45));
        cbPresentacion.setForeground(Color.WHITE);
        if (editando && prod.getPresentacion() != null) cbPresentacion.setSelectedItem(prod.getPresentacion());
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
        lblEquivalenteBs.setFont(Utils.UI.TEXTO_NEGRITA);

        // Label en vivo para el costo por unidad y la ganancia
        JLabel lblCostoGan = new JLabel();
        lblCostoGan.setForeground(new Color(251, 191, 36));
        lblCostoGan.setFont(Utils.UI.NOTA);

        javax.swing.event.DocumentListener listenerBs = new javax.swing.event.DocumentListener() {
            private void update() {
                try {
                    double val = parseSafeDouble(tfPrecioVenta.getText());
                    lblEquivalenteBs.setText(String.format("Equivale a: Bs%.2f", val * Utils.Config.getTasaVES()));
                } catch(Exception ex) {
                    lblEquivalenteBs.setText("Equivale a: Bs0.00");
                }
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
        };

        javax.swing.event.DocumentListener listenerCG = new javax.swing.event.DocumentListener() {
            private void update() {
                double precio = parseSafeDouble(tfPrecioVenta.getText());
                double costoPres = parseSafeDouble(tfCostoPres.getText());
                double unidades = parseSafeDouble(tfUnidades.getText());
                double costoUnid = (unidades > 0) ? costoPres / unidades : costoPres;
                double ganancia = precio - costoUnid;
                lblCostoGan.setText(String.format("Costo por unidad: %s | Ganancia por unidad: %s",
                    Utils.Formato.usdBs(costoUnid), Utils.Formato.usdBs(ganancia)));
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
        };

        tfPrecioVenta.getDocument().addDocumentListener(listenerBs);
        tfPrecioVenta.getDocument().addDocumentListener(listenerCG);
        tfCostoPres.getDocument().addDocumentListener(listenerCG);
        tfUnidades.getDocument().addDocumentListener(listenerCG);
        // Inicializar los labels con los valores del producto (edición) o vacíos (nuevo)
        listenerBs.insertUpdate(null);
        listenerCG.insertUpdate(null);

        String[] labels = {"SKU:", "Nombre:", "Descripción:", "Presentación:", "Precio Venta (USD):", "Costo de la presentación (USD):", "Unidades por presentación:", "Categoría:"};
        JComponent[] fields = {tfSku, tfNombre, tfDescripcion, cbPresentacion, tfPrecioVenta, tfCostoPres, tfUnidades, cbCategoria};

        for (int i = 0; i < labels.length; i++) {
            gc.gridy = i * 3; // dejar espacio extra para labels dinámicos abajo 
            gc.gridx = 0; gc.weightx = 0.4;
            JLabel lbl = new JLabel(labels[i]);
            lbl.setForeground(new Color(180, 180, 180));
            lbl.setFont(Utils.UI.TEXTO);
            form.add(lbl, gc);
            gc.gridx = 1; gc.weightx = 0.6;
            form.add(fields[i], gc);
            
            // Añadir los labels dinámicos debajo de los campos de precio
            if (i == 4) {
                gc.gridy = i * 3 + 1;
                gc.gridx = 0; gc.gridwidth = 2;
                gc.insets = new Insets(0, 5, 5, 5);
                form.add(lblEquivalenteBs, gc);
                gc.gridy = i * 3 + 2;
                form.add(lblCostoGan, gc);
                gc.insets = new Insets(5, 5, 5, 5); // restaurar insets originales
            }
        }
        gc.gridy = labels.length * 3; gc.gridx = 1; gc.gridwidth = 1;
        form.add(chkActivo, gc);

        JButton btnGuardar = crearBoton("💾 Guardar", new Color(16, 185, 129));
        btnGuardar.addActionListener(e -> {
            try {
                Producto p = editando ? prod : new Producto();
                p.setSku(tfSku.getText().trim());
                p.setNombre(tfNombre.getText().trim());
                p.setDescripcion(tfDescripcion.getText().trim());
                p.setPrecioVenta(parseSafeDouble(tfPrecioVenta.getText()));
                p.setPresentacion((String) cbPresentacion.getSelectedItem());
                p.setCostoPresentacion(parseSafeDouble(tfCostoPres.getText()));
                p.setUnidadesPresentacion(parseSafeDouble(tfUnidades.getText()));
                p.setState(chkActivo.isSelected());
                Categoria catSel = (Categoria) cbCategoria.getSelectedItem();
                if (catSel != null) p.setCategoriaId(catSel.getId());

                if (p.getNombre().isEmpty()) {
                    JOptionPane.showMessageDialog(dlg, "El nombre es obligatorio.");
                    return;
                }

                if (editando) {
                    dao.actualizar(p);
                } else {
                    insercionCompleta(p, dao, dlg);
                }

                dlg.dispose();
                cargarDatos();
                JOptionPane.showMessageDialog(this, editando ? "✅ Producto actualizado" : "✅ Producto creado");
            } catch (SQLException ex) {
                CX.ConexionBD.errorManager(ex);
            }
        });

        gc.gridy = labels.length * 3 + 1; gc.gridx = 0; gc.gridwidth = 2;
        gc.insets = new Insets(15, 5, 5, 5);
        form.add(btnGuardar, gc);

        // Ancho fijo para que las columnas del formulario no se compriman dentro del scroll
        form.setMinimumSize(new Dimension(600, 200));
        form.setPreferredSize(new Dimension(600, Math.max(640, form.getPreferredSize().height)));

        JScrollPane scroll = new JScrollPane(form, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(new Color(39, 39, 42));
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        dlg.add(scroll);
        dlg.setVisible(true);
    }

    private void insercionCompleta(Producto p, ProductoDAO dao, JDialog dlg) throws SQLException {
        long id = dao.insertar(p);
        if (id > 0) {
            double unid = p.getUnidadesPresentacion() > 0 ? p.getUnidadesPresentacion() : 1;
            double costoUnid = unid > 0 ? p.getCostoPresentacion() / unid : 0;
            MovimientoInventario mov = new MovimientoInventario();
            mov.setProductoId(id);
            mov.setTipoMovimiento("Entrada");
            mov.setCantidad(unid);
            mov.setPrecio(costoUnid);
            mov.setPrecioBalance(p.getCostoPresentacion());
            mov.setMotivo("Stock inicial");
            new InventarioDAO().registrarMovimiento(mov);
            new BitacoraDAO().registrar("Inventario", "Crear",
                "Producto creado: " + p.getNombre() + " con stock inicial de " + String.format("%.0f", unid) + " unidades");
        }
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

    private void reabastecerSeleccionado() {
        int row = tabla.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Seleccione un producto para reabastecer."); return; }
        long id = (long) modelo.getValueAt(row, 0);
        String nombre = (String) modelo.getValueAt(row, 2);
        double stock = parseSafeDouble(String.valueOf(modelo.getValueAt(row, 10)));

        Producto p;
        try { p = dao.obtenerPorId(id); } catch (SQLException e) { p = null; }
        final Producto prod = p;
        double unidPres = (prod != null && prod.getUnidadesPresentacion() > 0) ? prod.getUnidadesPresentacion() : 1;
        double costoPres = (prod != null) ? prod.getCostoPresentacion() : 0;
        String presentacion = (prod != null && prod.getPresentacion() != null && !prod.getPresentacion().isEmpty())
            ? prod.getPresentacion() : "Bulto";

        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Reabastecer - " + nombre, true);
        dlg.setSize(480, 480);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(new Color(39, 39, 42));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(6, 5, 6, 5);

        JLabel lblInfo = new JLabel("Cantidad actual: " + String.format("%.0f", stock) + " unidades  •  Compra por " + presentacion + "s");
        lblInfo.setForeground(new Color(245, 158, 11));
        lblInfo.setFont(Utils.UI.TEXTO_NEGRITA);
        gc.gridy = 0; gc.gridx = 0; gc.gridwidth = 2;
        form.add(lblInfo, gc);

        JTextField tfPresentaciones = crearCampoNumerico("", 15, true);
        JTextField tfUnidPres = crearCampoNumerico(String.format("%.0f", unidPres), 15, true);
        JTextField tfCostoPres = crearCampoNumerico(String.format("%.2f", costoPres), 15, true);
        JTextField tfMotivo = crearCampo("Compra de " + presentacion.toLowerCase() + "s");

        JLabel lblTotal = new JLabel();
        lblTotal.setForeground(new Color(52, 211, 153));
        lblTotal.setFont(Utils.UI.TEXTO_NEGRITA);
        javax.swing.event.DocumentListener listener = new javax.swing.event.DocumentListener() {
            private void update() {
                double pres = parseSafeDouble(tfPresentaciones.getText());
                double unid = parseSafeDouble(tfUnidPres.getText());
                double cp = parseSafeDouble(tfCostoPres.getText());
                double totalUnid = pres * unid;
                double costoUnid = (unid > 0) ? cp / unid : cp;
                double costoTotal = pres * cp;
                lblTotal.setText(String.format("Cantidad a añadir: %.0f unidades  |  Costo/Unid: %s  |  Costo total: %s",
                    totalUnid, Utils.Formato.usdBs(costoUnid), Utils.Formato.usdBs(costoTotal)));
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
        };
        tfPresentaciones.getDocument().addDocumentListener(listener);
        tfUnidPres.getDocument().addDocumentListener(listener);
        tfCostoPres.getDocument().addDocumentListener(listener);
        // Mostrar el total inicial (aunque las presentaciones aún estén en 0)
        listener.insertUpdate(null);

        gc.gridwidth = 1;
        gc.gridy = 1; gc.gridx = 0; gc.weightx = 0.35;
        JLabel l1 = new JLabel("Cant. de " + presentacion.toLowerCase() + "s:"); l1.setForeground(new Color(180, 180, 180));
        form.add(l1, gc);
        gc.gridx = 1; gc.weightx = 0.65; form.add(tfPresentaciones, gc);

        gc.gridy = 2; gc.gridx = 0; gc.weightx = 0.35;
        JLabel l2 = new JLabel("Unidades por " + presentacion.toLowerCase() + ":"); l2.setForeground(new Color(180, 180, 180));
        form.add(l2, gc);
        gc.gridx = 1; gc.weightx = 0.65; form.add(tfUnidPres, gc);

        gc.gridy = 3; gc.gridx = 0; gc.weightx = 0.35;
        JLabel l3 = new JLabel("Costo de cada " + presentacion.toLowerCase() + " (USD):"); l3.setForeground(new Color(180, 180, 180));
        form.add(l3, gc);
        gc.gridx = 1; gc.weightx = 0.65; form.add(tfCostoPres, gc);

        gc.gridy = 4; gc.gridx = 0; gc.gridwidth = 2;
        gc.insets = new Insets(0, 5, 5, 5);
        form.add(lblTotal, gc);

        gc.gridy = 5; gc.gridx = 0; gc.weightx = 0.35; gc.gridwidth = 1;
        gc.insets = new Insets(6, 5, 6, 5);
        JLabel l4 = new JLabel("Motivo:"); l4.setForeground(new Color(180, 180, 180));
        form.add(l4, gc);
        gc.gridx = 1; gc.weightx = 0.65; form.add(tfMotivo, gc);

        JButton btnGuardar = crearBoton("💾 Reabastecer", new Color(245, 158, 11));
        btnGuardar.addActionListener(e -> {
            try {
                double pres = parseSafeDouble(tfPresentaciones.getText());
                double unid = parseSafeDouble(tfUnidPres.getText());
                double cp = parseSafeDouble(tfCostoPres.getText());
                if (pres <= 0) {
                    JOptionPane.showMessageDialog(dlg, "Indique cuántos " + presentacion.toLowerCase() + "s está comprando (mayor a 0).");
                    return;
                }
                if (unid <= 0) {
                    JOptionPane.showMessageDialog(dlg, "Indique cuántas unidades trae cada " + presentacion.toLowerCase() + ".");
                    return;
                }
                double totalUnidades = pres * unid;
                double costoUnid = cp / unid;

                MovimientoInventario mov = new MovimientoInventario();
                mov.setProductoId(id);
                mov.setTipoMovimiento("Entrada");
                mov.setCantidad(totalUnidades);
                mov.setPrecio(costoUnid);
                mov.setPrecioBalance(cp * pres);
                mov.setMotivo(tfMotivo.getText().trim());

                new InventarioDAO().registrarMovimiento(mov);
                new BitacoraDAO().registrar("Inventario", "Crear",
                    "Reabastecimiento: " + nombre + " +" + String.format("%.0f", totalUnidades) + " unidades ("
                    + String.format("%.0f", pres) + " " + presentacion.toLowerCase() + " x " + String.format("%.0f", unid) + " unid)");
                dlg.dispose();
                cargarDatos();
                JOptionPane.showMessageDialog(this, "✅ Reabastecimiento registrado. Stock actualizado.");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg, "Verifique los valores numéricos.");
            } catch (SQLException ex) {
                CX.ConexionBD.errorManager(ex);
            }
        });

        gc.gridy = 6; gc.gridx = 0; gc.gridwidth = 2;
        gc.insets = new Insets(15, 5, 5, 5);
        form.add(btnGuardar, gc);

        dlg.add(form);
        dlg.setVisible(true);
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

    private void sacarSeleccionado() {
        int row = tabla.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Seleccione un producto para sacar."); return; }
        long id = (long) modelo.getValueAt(row, 0);
        String nombre = (String) modelo.getValueAt(row, 2);
        double stock = parseSafeDouble(String.valueOf(modelo.getValueAt(row, 10)));

        Producto prod;
        try { prod = dao.obtenerPorId(id); } catch (SQLException e) { prod = null; }
        String presentacion = (prod != null && prod.getPresentacion() != null && !prod.getPresentacion().isEmpty())
            ? prod.getPresentacion() : "Unidad";

        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Sacar / Salida - " + nombre, true);
        dlg.setSize(460, 380);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(new Color(39, 39, 42));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(6, 5, 6, 5);

        JLabel lblInfo = new JLabel("Cantidad actual: " + String.format("%.0f", stock) + " " + presentacion.toLowerCase() + "s");
        lblInfo.setForeground(new Color(239, 68, 68));
        lblInfo.setFont(Utils.UI.TEXTO_NEGRITA);
        gc.gridy = 0; gc.gridx = 0; gc.gridwidth = 2;
        form.add(lblInfo, gc);

        JTextField tfCantidad = crearCampoNumerico("", 15, true);
        JComboBox<String> cbMotivo = new JComboBox<>(new String[]{"Vencimiento", "Dañado", "Extraviado", "Devolución", "Otro"});
        cbMotivo.setBackground(new Color(45, 45, 45));
        cbMotivo.setForeground(Color.WHITE);
        JTextField tfMotivo = crearCampo("");

        cbMotivo.addActionListener(e ->
            tfMotivo.setEnabled("Otro".equals(cbMotivo.getSelectedItem())));
        tfMotivo.setEnabled(false);

        gc.gridwidth = 1;
        gc.gridy = 1; gc.gridx = 0; gc.weightx = 0.35;
        JLabel l1 = new JLabel("Cantidad a sacar:"); l1.setForeground(new Color(180, 180, 180));
        form.add(l1, gc);
        gc.gridx = 1; gc.weightx = 0.65; form.add(tfCantidad, gc);

        gc.gridy = 2; gc.gridx = 0; gc.weightx = 0.35;
        JLabel l2 = new JLabel("Motivo:"); l2.setForeground(new Color(180, 180, 180));
        form.add(l2, gc);
        gc.gridx = 1; gc.weightx = 0.65; form.add(cbMotivo, gc);

        gc.gridy = 3; gc.gridx = 0; gc.weightx = 0.35;
        JLabel l3 = new JLabel("Detalle motivo:"); l3.setForeground(new Color(180, 180, 180));
        form.add(l3, gc);
        gc.gridx = 1; gc.weightx = 0.65; form.add(tfMotivo, gc);

        JButton btnGuardar = crearBoton("➖ Sacar", new Color(239, 68, 68));
        btnGuardar.addActionListener(e -> {
            try {
                double cant = parseSafeDouble(tfCantidad.getText());
                if (cant <= 0) {
                    JOptionPane.showMessageDialog(dlg, "Indique una cantidad mayor a 0 para sacar.");
                    return;
                }
                if (cant > stock) {
                    JOptionPane.showMessageDialog(dlg,
                        String.format("Stock insuficiente. Disponible: %.0f %s.", stock, presentacion.toLowerCase() + "s"));
                    return;
                }
                String motivo = "Otro".equals(cbMotivo.getSelectedItem())
                    ? tfMotivo.getText().trim()
                    : String.valueOf(cbMotivo.getSelectedItem());
                if (motivo.isEmpty()) motivo = "Otro";

                MovimientoInventario mov = new MovimientoInventario();
                mov.setProductoId(id);
                mov.setTipoMovimiento("Salida");
                mov.setCantidad(cant);
                mov.setPrecio(0);
                mov.setPrecioBalance(0);
                mov.setMotivo(motivo);

                new InventarioDAO().registrarMovimiento(mov);
                new BitacoraDAO().registrar("Inventario", "Crear",
                    "Salida de stock: " + nombre + " -" + String.format("%.0f", cant) + " unidades (motivo: " + motivo + ")");
                dlg.dispose();
                cargarDatos();
                JOptionPane.showMessageDialog(this, "✅ Salida registrada. Stock actualizado.");
            } catch (SQLException ex) {
                CX.ConexionBD.errorManager(ex);
            }
        });

        gc.gridy = 4; gc.gridx = 0; gc.gridwidth = 2;
        gc.insets = new Insets(15, 5, 5, 5);
        form.add(btnGuardar, gc);

        dlg.add(form);
        dlg.setVisible(true);
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
        String cleanText = texto.replaceAll("[➕➖✏️🗑️🧹✅❌🔍🔄?🌙☀️💾📥📤🔧🔑📊🧾👀🚚⬆️⬇️📁⛔]", "").trim();
        JButton btn = new JButton(cleanText);
        
        try {
            String iconName = null;
            if (texto.contains("➕")) iconName = "plus.svg";
            else if (texto.contains("➖")) iconName = "minus.svg";
            else if (texto.contains("✏️")) iconName = "edit.svg";
            else if (texto.contains("🗑️")) iconName = "trash.svg";
            else if (texto.contains("🧹")) iconName = "clear.svg";
            else if (texto.contains("✅")) iconName = "check-circle.svg";
            else if (texto.contains("❌")) iconName = "x.svg";
            else if (texto.contains("🔍")) iconName = "search.svg";
            else if (texto.contains("🔄")) iconName = "refresh.svg";
            else if (texto.contains("🧾")) iconName = "file-invoice.svg";
            else if (texto.contains("🚚")) iconName = "truck.svg";
            else if (texto.contains("🌙")) iconName = "moon.svg";
            else if (texto.contains("☀️")) iconName = "sun.svg";
            else if (texto.contains("💾")) iconName = "save.svg";
            else if (texto.contains("📥")) iconName = "download.svg";
            else if (texto.contains("📤")) iconName = "upload.svg";
            else if (texto.contains("🔧")) iconName = "wrench.svg";
            else if (texto.contains("🔑")) iconName = "key.svg";
            else if (texto.contains("📊")) iconName = "chart-bar.svg";
            else if (texto.contains("👀")) iconName = "eye.svg";
            else if (texto.contains("⬆️")) iconName = "upload.svg";
            else if (texto.contains("⬇️")) iconName = "download.svg";

            if (iconName != null) {
                com.formdev.flatlaf.extras.FlatSVGIcon icon = new com.formdev.flatlaf.extras.FlatSVGIcon(PanelProductos.class.getResource("/IMG/" + iconName));
                icon = (com.formdev.flatlaf.extras.FlatSVGIcon) icon.derive(16, 16);
                icon.setColorFilter(new com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter(c -> Color.WHITE));
                btn.setIcon(icon);
                btn.setIconTextGap(8);
            }
        } catch (Exception e){}

        btn.setFont(Utils.UI.BOTON);
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
        tf.setFont(Utils.UI.CAMPO);
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
        tf.setFont(Utils.UI.CAMPO);
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
        tabla.setRowHeight(Utils.UI.FILA_ALTO);
        tabla.setFont(Utils.UI.TABLA);
        tabla.setShowVerticalLines(false);
        tabla.getTableHeader().setBackground(new Color(30, 30, 30));
        tabla.getTableHeader().setForeground(new Color(180, 180, 180));
        tabla.getTableHeader().setFont(Utils.UI.TABLA_ENCABEZADO);
        tabla.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(55, 55, 55)));
        tabla.setIntercellSpacing(new Dimension(0, 1));
        return tabla;
    }
}
