package IG;

import DAO.FacturaDAO;
import DAO.ProductoDAO;
import DAO.DetalleFacturaDAO;
import Modelo.Factura;
import Modelo.Producto;
import Modelo.DetalleFactura;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import Utils.UI;

public class PanelFacturas extends JPanel {

    private JTable tabla;
    private DefaultTableModel modelo;
    private JTextField campoBuscar;
    private FacturaDAO dao = new FacturaDAO();
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private JLabel lblPie;

    public PanelFacturas() {
        setBackground(new Color(24, 24, 27));
        setLayout(new BorderLayout(0, 15));
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JPanel header = new JPanel(new BorderLayout(15, 0));
        header.setOpaque(false);

        JLabel titulo = new JLabel("Ventas");
        try {
            com.formdev.flatlaf.extras.FlatSVGIcon icon = new com.formdev.flatlaf.extras.FlatSVGIcon(getClass().getResource("/IMG/file-invoice.svg")).derive(32, 32);
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

        JButton btnNueva = PanelProductos.crearBoton("➕ Nueva Venta", new Color(16, 185, 129));
        btnNueva.addActionListener(e -> dialogoNuevaVenta());

        JButton btnVerDetalle = PanelProductos.crearBoton("👀 Ver Detalle", new Color(59, 130, 246));
        btnVerDetalle.addActionListener(e -> verDetalle());

        JButton btnAnular = PanelProductos.crearBoton("❌ Anular", new Color(239, 68, 68));
        btnAnular.addActionListener(e -> anularSeleccionada());

        CX.SesionUsuario sesion = CX.SesionUsuario.getInstancia();
        btnNueva.setEnabled(sesion.tienePermiso("Ventas", "Crear"));
        btnAnular.setEnabled(sesion.tienePermiso("Ventas", "Eliminar"));

        acciones.add(campoBuscar);
        acciones.add(btnNueva);
        acciones.add(btnVerDetalle);
        acciones.add(btnAnular);

        header.add(titulo, BorderLayout.WEST);
        header.add(acciones, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        String[] cols = {"ID", "N Factura", "Fecha", "Metodo Pago", "Subtotal", "Total", "Total Bs", "Estado", "Productos"};
        modelo = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tabla = PanelProductos.crearTabla(modelo);

        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    String estado = (String) table.getValueAt(row, 7);
                    if ("Anulada".equals(estado)) c.setForeground(new Color(239, 68, 68));
                    else if ("Pagada".equals(estado)) c.setForeground(new Color(16, 185, 129));
                    else if ("Pendiente".equals(estado)) c.setForeground(new Color(245, 158, 11));
                    else c.setForeground(Color.WHITE);
                    c.setBackground(new Color(39, 39, 42));
                }
                return c;
            }
        });

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
            for (Factura f : dao.listarTodasConCliente()) {
                modelo.addRow(new Object[]{
                    f.getId(), f.getNumeroFactura(),
                    f.getFechaEmision() != null ? sdf.format(f.getFechaEmision()) : "",
                    f.getMetodoPago() != null ? f.getMetodoPago() : "",
                    String.format(Locale.US, "$%.2f", f.getSubtotal()),
                    String.format(Locale.US, "$%.2f", f.getTotal()),
                    Utils.Formato.bs(f.getTotal()),
                    f.getEstado(),
                    productosDeFactura(f.getId())
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private String productosDeFactura(long facturaId) {
        try {
            List<DetalleFactura> detalles = new DetalleFacturaDAO().listarPorFactura(facturaId);
            if (detalles.isEmpty()) return "—";
            StringBuilder sb = new StringBuilder();
            for (DetalleFactura df : detalles) {
                if (sb.length() > 0) sb.append(", ");
                String nom = df.getProductoNombre();
                sb.append(nom != null ? nom : "ID:" + df.getProductoId());
            }
            return sb.toString();
        } catch (SQLException e) {
            return "—";
        }
    }

    private void buscar() {
        String t = campoBuscar.getText().trim();
        modelo.setRowCount(0);
        double sumaUsd = 0;
        try {
            List<Factura> lista = t.isEmpty() ? dao.listarTodasConCliente() : dao.buscarEnMemoria(t);
            int pagadas = 0;
            for (Factura f : lista) {
                if (!"Anulada".equals(f.getEstado())) {
                    sumaUsd += f.getTotal();
                    pagadas++;
                }
                modelo.addRow(new Object[]{
                    f.getId(), f.getNumeroFactura(),
                    f.getFechaEmision() != null ? sdf.format(f.getFechaEmision()) : "",
                    f.getMetodoPago() != null ? f.getMetodoPago() : "",
                    String.format(Locale.US, "$%.2f", f.getSubtotal()),
                    String.format(Locale.US, "$%.2f", f.getTotal()),
                    Utils.Formato.bs(f.getTotal()),
                    f.getEstado(),
                    productosDeFactura(f.getId())
                });
            }
            if (lblPie != null) lblPie.setText(String.format("Ventas: %d | Total (sin anuladas): %s (%s)", pagadas, Utils.Formato.usd(sumaUsd), Utils.Formato.bsMiles(sumaUsd)));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void dialogoNuevaVenta() {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Nueva Venta", true);
        dlg.setSize(850, 620);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(new Color(39, 39, 42));

        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setOpaque(false);
        main.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // â”€â”€ Top: datos de la factura â”€â”€
        JPanel topPanel = new JPanel(new GridBagLayout());
        topPanel.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(4, 5, 4, 5);

        JTextField tfNumero = PanelProductos.crearCampo("", 50);
        tfNumero.setEditable(false);
        try { tfNumero.setText(dao.generarNumeroFactura()); } catch (SQLException e) { tfNumero.setText("FAC-001"); }

        JComboBox<String> cbMetodo = new JComboBox<>(new String[]{"Efectivo", "Pago Móvil", "Transferencia", "Punto de Venta", "Divisas (USD)", "Zelle"});
        cbMetodo.setBackground(new Color(45, 45, 45));
        cbMetodo.setForeground(Color.WHITE);

        int row = 0;
        addFormRow(topPanel, gc, row++, "N Factura:", tfNumero);
        addFormRow(topPanel, gc, row++, "Metodo Pago:", cbMetodo);

        // --- Cliente (para ventas al fiado, se busca por nombre o cédula) ---
        final Modelo.Cliente[] clienteElegido = new Modelo.Cliente[1];
        JTextField tfCliente = PanelProductos.crearCampo("", 100);
        tfCliente.setBackground(new Color(45, 45, 45));
        tfCliente.setForeground(Color.WHITE);
        tfCliente.setCaretColor(Color.WHITE);
        tfCliente.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        tfCliente.putClientProperty("JTextField.placeholderText", "Buscar cliente por nombre o cedula (opcional)...");
        tfCliente.setPreferredSize(new Dimension(360, 32));

        final JPopupMenu popupCliente = new JPopupMenu();
        popupCliente.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80)));
        JList<Modelo.Cliente> listaClientes = new JList<>();
        listaClientes.setBackground(new Color(45, 45, 45));
        listaClientes.setForeground(Color.WHITE);
        listaClientes.setSelectionBackground(new Color(16, 185, 129, 80));
        listaClientes.setSelectionForeground(Color.WHITE);
        listaClientes.setFixedCellHeight(24);
        listaClientes.setCellRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean sel, boolean focus) {
                super.getListCellRendererComponent(list, value, index, sel, focus);
                if (value instanceof Modelo.Cliente) {
                    Modelo.Cliente c = (Modelo.Cliente) value;
                    setText((c.getCedula() == null ? "" : c.getCedula()) + " - " + (c.getNombre() == null ? "" : c.getNombre()));
                }
                setOpaque(true);
                return this;
            }
        });
        JScrollPane scrollCli = new JScrollPane(listaClientes);
        scrollCli.setPreferredSize(new Dimension(360, 140));
        scrollCli.setBorder(null);
        scrollCli.getViewport().setBackground(new Color(45, 45, 45));
        popupCliente.add(scrollCli);

        final boolean[] seleccionandoCliente = {false};
        java.util.function.Consumer<Modelo.Cliente> elegirCliente = c -> {
            clienteElegido[0] = c;
            seleccionandoCliente[0] = true;
            tfCliente.setText(c.getNombre());
            seleccionandoCliente[0] = false;
            popupCliente.setVisible(false);
        };

        final java.util.List<Modelo.Cliente> todosClientes = new ArrayList<>();
        try { todosClientes.addAll(new DAO.ClienteDAO().listarTodos()); } catch (SQLException ex) {}

        java.util.function.Consumer<String> filtrarClientes = q -> {
            DefaultListModel<Modelo.Cliente> m = new DefaultListModel<>();
            if (!q.isEmpty()) {
                for (Modelo.Cliente c : todosClientes) {
                    String nom = c.getNombre() == null ? "" : c.getNombre().toLowerCase();
                    String ced = c.getCedula() == null ? "" : c.getCedula().toLowerCase();
                    if (nom.contains(q) || ced.contains(q)) m.addElement(c);
                }
            }
            listaClientes.setModel(m);
            listaClientes.clearSelection();
            if (!q.isEmpty() && m.size() > 0) {
                if (!popupCliente.isVisible()) popupCliente.show(tfCliente, 0, tfCliente.getHeight());
            } else {
                popupCliente.setVisible(false);
            }
            tfCliente.requestFocusInWindow();
        };

        tfCliente.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { if (!seleccionandoCliente[0]) filtrarClientes.accept(tfCliente.getText().trim().toLowerCase()); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { if (!seleccionandoCliente[0]) filtrarClientes.accept(tfCliente.getText().trim().toLowerCase()); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) {}
        });

        listaClientes.addMouseListener(new MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent e) {
                int idx = listaClientes.locationToIndex(e.getPoint());
                if (idx < 0) return;
                java.awt.Rectangle r = listaClientes.getCellBounds(idx, idx);
                if (r != null && r.contains(e.getPoint())) {
                    Modelo.Cliente c = listaClientes.getModel().getElementAt(idx);
                    if (c != null) elegirCliente.accept(c);
                }
            }
        });
        tfCliente.addKeyListener(new KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (!popupCliente.isVisible() || listaClientes.getModel().getSize() == 0) return;
                int idx = listaClientes.getSelectedIndex();
                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    int n = idx < 0 ? 0 : Math.min(idx + 1, listaClientes.getModel().getSize() - 1);
                    listaClientes.setSelectedIndex(n); listaClientes.ensureIndexIsVisible(n); e.consume();
                } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                    int n = idx <= 0 ? 0 : idx - 1;
                    listaClientes.setSelectedIndex(n); listaClientes.ensureIndexIsVisible(n); e.consume();
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    Modelo.Cliente c = listaClientes.getSelectedValue();
                    if (c == null && listaClientes.getModel().getSize() > 0) c = listaClientes.getModel().getElementAt(0);
                    if (c != null) { elegirCliente.accept(c); e.consume(); }
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    popupCliente.setVisible(false); e.consume();
                }
            }
        });

        addFormRow(topPanel, gc, row++, "Cliente:", tfCliente);

        main.add(topPanel, BorderLayout.NORTH);

        // â”€â”€ Center: tabla de productos a vender â”€â”€
        String[] colsDetalle = {"ID Prod.", "Producto", "Precio $", "Cant.", "Subtotal $", "Bs"};
        DefaultTableModel modeloDetalle = new DefaultTableModel(colsDetalle, 0) {
            public boolean isCellEditable(int r, int c) { return c == 3; } // solo Cant. editable
        };
        JTable tablaDetalle = new JTable(modeloDetalle);
        tablaDetalle.setBackground(new Color(30, 30, 30));
        tablaDetalle.setForeground(Color.WHITE);
        tablaDetalle.setGridColor(new Color(55, 55, 55));
        tablaDetalle.setSelectionBackground(new Color(16, 185, 129, 50));
        tablaDetalle.setSelectionForeground(Color.WHITE);
        tablaDetalle.setRowHeight(Utils.UI.FILA_ALTO);
        tablaDetalle.setFont(Utils.UI.TABLA);
        tablaDetalle.getTableHeader().setBackground(new Color(25, 25, 25));
        tablaDetalle.getTableHeader().setForeground(Color.WHITE);
        tablaDetalle.getTableHeader().setFont(Utils.UI.TABLA_ENCABEZADO);
        tablaDetalle.getColumnModel().getColumn(0).setPreferredWidth(50);
        tablaDetalle.getColumnModel().getColumn(1).setPreferredWidth(200);
        tablaDetalle.getColumnModel().getColumn(2).setPreferredWidth(80);
        tablaDetalle.getColumnModel().getColumn(3).setPreferredWidth(60);
        tablaDetalle.getColumnModel().getColumn(4).setPreferredWidth(80);

        // Cargar productos con stock
        List<Producto> productosDisponibles = new ArrayList<>();
        try {
            for (Producto p : new ProductoDAO().listarTodos()) {
                if (p.isState() && p.getStockActual() > 0) {
                    productosDisponibles.add(p);
                }
            }
        } catch (SQLException e) {}

        // Listener para recalcular subtotal al cambiar cantidad
        tablaDetalle.getModel().addTableModelListener(e -> {
            if (e.getColumn() == 3) {
                int r = e.getFirstRow();
                if (r >= 0) {
                    try {
                        double cant = PanelProductos.parseSafeDouble(String.valueOf(tablaDetalle.getValueAt(r, 3)));
                        double precio = PanelProductos.parseSafeDouble(String.valueOf(tablaDetalle.getValueAt(r, 2)));
                        tablaDetalle.setValueAt(String.format("$%.2f", cant * precio), r, 4);
                        actualizarTotales(modeloDetalle, null, null);
                    } catch (Exception ex) {}
                }
            }
        });

        JScrollPane scrollDetalle = new JScrollPane(tablaDetalle);
        scrollDetalle.setBorder(BorderFactory.createLineBorder(new Color(55, 55, 55)));
        scrollDetalle.getViewport().setBackground(new Color(30, 30, 30));

        // Botones para agregar/quitar productos
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        btnPanel.setOpaque(false);

        // â”€â”€ Buscador inteligente de producto: campo de texto + lista de sugerencias â”€â”€
        final Producto[] productoElegido = new Producto[1];
        JTextField edProducto = new JTextField();
        edProducto.setBackground(new Color(45, 45, 45));
        edProducto.setForeground(Color.WHITE);
        edProducto.setCaretColor(Color.WHITE);
        edProducto.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        edProducto.setPreferredSize(new Dimension(360, 32));
        edProducto.putClientProperty("JTextField.placeholderText", "Buscar producto por nombre o SKU...");

        // Lista de sugerencias
        JList<Producto> listaSugerencias = new JList<>();
        listaSugerencias.setBackground(new Color(45, 45, 45));
        listaSugerencias.setForeground(Color.WHITE);
        listaSugerencias.setSelectionBackground(new Color(16, 185, 129, 80));
        listaSugerencias.setSelectionForeground(Color.WHITE);
        listaSugerencias.setFixedCellHeight(24);
        listaSugerencias.setCellRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean sel, boolean focus) {
                super.getListCellRendererComponent(list, value, index, sel, focus);
                if (value instanceof Producto) {
                    Producto p = (Producto) value;
                    setText(String.format("[%s] %s - Stock: %.0f - %s - %s",
                        p.getSku(), p.getNombre(), p.getStockActual(),
                        Utils.Formato.usd(p.getPrecioVenta()), Utils.Formato.bs(p.getPrecioVenta())));
                }
                setOpaque(true);
                return this;
            }
        });

        // Popup de sugerencias. JPopupMenu es NO-focusable, por lo que NO roba el
        // foco del campo de texto: el usuario puede seguir escribiendo (el cursor no se va).
        final JPopupMenu popupSugerencias = new JPopupMenu();
        popupSugerencias.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80)));
        JScrollPane scrollSug = new JScrollPane(listaSugerencias);
        scrollSug.setPreferredSize(new Dimension(360, 160));
        scrollSug.setBorder(null);
        scrollSug.getViewport().setBackground(new Color(45, 45, 45));
        popupSugerencias.add(scrollSug);

        // Configurar un modelo predeterminado para la lista de sugerencias
        DefaultListModel<Producto> modeloLista = new DefaultListModel<>();
        listaSugerencias.setModel(modeloLista);

        // Flag para no re-filtrar/re-abrir el popup mientras aplicamos una seleccion
        final boolean[] seleccionando = {false};

        // Accion al elegir una sugerencia (se define antes de usarse en los listeners)
        java.util.function.Consumer<Producto> seleccionarProducto = p -> {
            productoElegido[0] = p;
            seleccionando[0] = true;
            edProducto.setText(p.getNombre());
            seleccionando[0] = false;
            popupSugerencias.setVisible(false);
            edProducto.requestFocusInWindow();
        };

        // Seleccionar con clic en la lista (mouseReleased es fiable dentro del popup)
        listaSugerencias.addMouseListener(new MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent e) {
                int idx = listaSugerencias.locationToIndex(e.getPoint());
                if (idx < 0) return;
                java.awt.Rectangle r = listaSugerencias.getCellBounds(idx, idx);
                if (r != null && r.contains(e.getPoint())) {
                    Producto p = listaSugerencias.getModel().getElementAt(idx);
                    if (p != null) seleccionarProducto.accept(p);
                }
            }
        });

        // Al escribir, filtrar y mostrar coincidencias sin seleccionar ninguna
        edProducto.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
            private void filtrar() {
                if (seleccionando[0]) return;
                final String q = edProducto.getText().trim().toLowerCase();

                // Si el usuario modifico el texto y ya no coincide con el producto elegido, limpiarlo
                if (productoElegido[0] != null) {
                    String nombre = productoElegido[0].getNombre().toLowerCase();
                    String sku = String.valueOf(productoElegido[0].getSku()).toLowerCase();
                    if (!q.equals(nombre) && !q.equals(sku)) {
                        productoElegido[0] = null;
                    }
                }

                DefaultListModel<Producto> m = new DefaultListModel<>();
                if (!q.isEmpty()) {
                    for (Producto p : productosDisponibles) {
                        if (p.getNombre().toLowerCase().contains(q)
                            || String.valueOf(p.getSku()).toLowerCase().contains(q)) {
                            m.addElement(p);
                        }
                    }
                }

                modeloLista.clear();
                for (int i = 0; i < m.size(); i++) modeloLista.addElement(m.get(i));
                listaSugerencias.clearSelection();

                if (!q.isEmpty() && modeloLista.size() > 0) {
                    if (!popupSugerencias.isVisible()) {
                        popupSugerencias.show(edProducto, 0, edProducto.getHeight());
                    }
                } else {
                    popupSugerencias.setVisible(false);
                }
                // Mantener el foco en el campo para que el usuario pueda seguir escribiendo
                edProducto.requestFocusInWindow();
            }
        });

        // Teclado sobre el campo: flechas para navegar la lista, Enter para elegir, Escape para cerrar
        edProducto.addKeyListener(new KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (!popupSugerencias.isVisible() || modeloLista.size() == 0) {
                    return;
                }
                int idx = listaSugerencias.getSelectedIndex();
                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    int n = idx < 0 ? 0 : Math.min(idx + 1, modeloLista.size() - 1);
                    listaSugerencias.setSelectedIndex(n);
                    listaSugerencias.ensureIndexIsVisible(n);
                    e.consume();
                } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                    int n = idx <= 0 ? 0 : idx - 1;
                    listaSugerencias.setSelectedIndex(n);
                    listaSugerencias.ensureIndexIsVisible(n);
                    e.consume();
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    Producto p = listaSugerencias.getSelectedValue();
                    if (p == null && modeloLista.size() > 0) {
                        p = modeloLista.getElementAt(0);
                    }
                    if (p != null) {
                        seleccionarProducto.accept(p);
                        e.consume();
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    popupSugerencias.setVisible(false);
                    e.consume();
                }
            }
        });

        JLabel lbCantidad = new JLabel("Cantidad:");
        lbCantidad.setForeground(Color.WHITE);
        lbCantidad.setFont(Utils.UI.TEXTO);
        JTextField tfCantidad = PanelProductos.crearCampoNumerico("1", 8, true);
        tfCantidad.setPreferredSize(new Dimension(70, 32));
        tfCantidad.setHorizontalAlignment(JTextField.CENTER);

        JButton btnAgregar = PanelProductos.crearBoton("➕ Agregar", new Color(16, 185, 129));
        btnAgregar.addActionListener(e -> {
            Producto sel = null;
            if (productoElegido[0] != null) {
                sel = productoElegido[0];
            } else {
                String t = edProducto.getText().trim().toLowerCase();
                for (Producto p : productosDisponibles) {
                    if (p.getNombre().toLowerCase().equals(t)
                        || String.valueOf(p.getSku()).toLowerCase().equals(t)) { sel = p; break; }
                }
            }
            if (sel == null) {
                JOptionPane.showMessageDialog(dlg, "Seleccione un producto de la lista desplegable.");
                return;
            }
            double cant = PanelProductos.parseSafeDouble(tfCantidad.getText());
            if (cant <= 0) {
                JOptionPane.showMessageDialog(dlg, "Ingrese una cantidad mayor a 0.");
                return;
            }
            if (sel.getStockActual() < cant) {
                JOptionPane.showMessageDialog(dlg,
                    String.format("Stock insuficiente para '%s'. Disponible: %.0f, solicitada: %.2f",
                        sel.getNombre(), sel.getStockActual(), cant));
                return;
            }
            // Verificar si ya esta en la tabla
            for (int i = 0; i < modeloDetalle.getRowCount(); i++) {
                if ((long) modeloDetalle.getValueAt(i, 0) == sel.getId()) {
                    double actual = PanelProductos.parseSafeDouble(String.valueOf(modeloDetalle.getValueAt(i, 3)));
                    double nueva = actual + cant;
                    if (sel.getStockActual() < nueva) {
                        JOptionPane.showMessageDialog(dlg,
                            String.format("Stock insuficiente para '%s'. Disponible: %.0f, total solicitado: %.2f",
                                sel.getNombre(), sel.getStockActual(), nueva));
                        return;
                    }
                    modeloDetalle.setValueAt(String.format("%.2f", nueva), i, 3);
                    double precio = PanelProductos.parseSafeDouble(String.valueOf(modeloDetalle.getValueAt(i, 2)));
                    modeloDetalle.setValueAt(String.format("$%.2f", nueva * precio), i, 4);
                    modeloDetalle.setValueAt(Utils.Formato.bs(nueva * precio), i, 5);
                    actualizarTotales(modeloDetalle, null, null);
                    return;
                }
            }
            modeloDetalle.addRow(new Object[]{
                sel.getId(), sel.getNombre(),
                String.format("%.2f", sel.getPrecioVenta()), String.format("%.2f", cant),
                String.format("$%.2f", cant * sel.getPrecioVenta()),
                Utils.Formato.bs(cant * sel.getPrecioVenta())
            });
            actualizarTotales(modeloDetalle, null, null);
        });

        JButton btnQuitar = PanelProductos.crearBoton("➖ Quitar", new Color(239, 68, 68));
        btnQuitar.addActionListener(e -> {
            int sel = tablaDetalle.getSelectedRow();
            if (sel >= 0) {
                modeloDetalle.removeRow(sel);
                actualizarTotales(modeloDetalle, null, null);
            }
        });

        btnPanel.add(edProducto);
        btnPanel.add(lbCantidad);
        btnPanel.add(tfCantidad);
        btnPanel.add(btnAgregar);
        btnPanel.add(btnQuitar);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 5));
        centerPanel.setOpaque(false);
        centerPanel.add(btnPanel, BorderLayout.NORTH);
        centerPanel.add(scrollDetalle, BorderLayout.CENTER);
        main.add(centerPanel, BorderLayout.CENTER);

        // â”€â”€ Bottom: totales â”€â”€
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);

        JLabel lblSubtotal = new JLabel("Subtotal: $0.00");
        JLabel lblTotal = new JLabel("TOTAL: $0.00");        for (JLabel lbl : new JLabel[]{lblSubtotal, lblTotal}) {
            lbl.setForeground(Color.WHITE);
            lbl.setFont(Utils.UI.TEXTO_NEGRITA);
        }
        lblTotal.setFont(Utils.UI.TEXTO_NEGRITA);
        lblTotal.setForeground(new Color(16, 185, 129));

        JPanel totalesPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        totalesPanel.setOpaque(false);
        totalesPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 15));
        totalesPanel.add(lblSubtotal);
        totalesPanel.add(lblTotal);

        // Guardar
        JPanel guardarPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        guardarPanel.setOpaque(false);
        JButton btnFiar = PanelProductos.crearBoton("📝 Fiar", new Color(245, 158, 11));
        guardarPanel.add(btnFiar);
        JButton btnGuardar = PanelProductos.crearBoton("✅ Registrar Venta", new Color(16, 185, 129));
        guardarPanel.add(btnGuardar);

        bottomPanel.add(totalesPanel, BorderLayout.CENTER);
        bottomPanel.add(guardarPanel, BorderLayout.EAST);
        main.add(bottomPanel, BorderLayout.SOUTH);

        // Funcion para actualizar totales
        actualizarTotales(modeloDetalle, lblSubtotal, lblTotal);

        // Re-llamar para configurar los labels
        final JLabel _sub = lblSubtotal, _tot = lblTotal;
        modeloDetalle.addTableModelListener(e -> actualizarTotales(modeloDetalle, _sub, _tot));

        java.util.function.Consumer<Boolean> guardarVenta = esFiado -> {
            if (modeloDetalle.getRowCount() == 0) {
                JOptionPane.showMessageDialog(dlg, "Debe agregar al menos un producto.");
                return;
            }
            try {
                // Calcular totales
                double subtotal = 0;
                List<DetalleFactura> detalles = new ArrayList<>();
                for (int i = 0; i < modeloDetalle.getRowCount(); i++) {
                    long prodId = (long) modeloDetalle.getValueAt(i, 0);
                    double cant = PanelProductos.parseSafeDouble(String.valueOf(modeloDetalle.getValueAt(i, 3)));
                    double precio = PanelProductos.parseSafeDouble(String.valueOf(modeloDetalle.getValueAt(i, 2)));
                    if (cant <= 0) {
                        JOptionPane.showMessageDialog(dlg, "Cantidad invalida en fila " + (i + 1));
                        return;
                    }
                    // Validar stock
                    ProductoDAO pdao = new ProductoDAO();
                    Producto prod = pdao.obtenerPorId(prodId);
                    if (prod != null && cant > prod.getStockActual()) {
                        JOptionPane.showMessageDialog(dlg,
                            String.format("Stock insuficiente para '%s'. Disponible: %.0f, solicitado: %.0f",
                                prod.getNombre(), prod.getStockActual(), cant));
                        return;
                    }
                    double sub = cant * precio;
                    subtotal += sub;
                    DetalleFactura df = new DetalleFactura();
                    df.setProductoId(prodId);
                    df.setCantidad(cant);
                    df.setPrecioUnitario(precio);
                    df.setSubtotal(sub);
                    detalles.add(df);
                }

                double impuestos = 0;
                double total = subtotal;

                Modelo.Cliente cliente = null;
                if (esFiado) {
                    cliente = resolverClienteFiado(dlg, tfCliente.getText(), clienteElegido[0]);
                    if (cliente == null) return; // Usuario canceló el registro del cliente
                }

                Factura f = new Factura();
                f.setNumeroFactura(tfNumero.getText().trim());
                if (esFiado) {
                    f.setMetodoPago("Fiado");
                    f.setEstado("Pendiente");
                } else {
                    f.setMetodoPago((String) cbMetodo.getSelectedItem());
                    f.setEstado("Pagada");
                }
                if (cliente != null) {
                    f.setClienteId(cliente.getId());
                    f.setClienteNombre(cliente.getNombre());
                }
                f.setSubtotal(subtotal);
                f.setImpuestos(impuestos);
                f.setTotal(total);
                f.setDetalles(detalles);

                long id = dao.registrarVenta(f);
                String tipo = esFiado ? "Venta al fiado" : "Venta registrada";
                new DAO.BitacoraDAO().registrar("Ventas", "Crear",
                    tipo + ": " + f.getNumeroFactura() + " | Total: $" + String.format("%.2f", total) +
                    (cliente != null ? " | Cliente: " + cliente.getNombre() : "") + " | Metodo: " + f.getMetodoPago() + " | Items: " + detalles.size());
                dlg.dispose();
                cargar();
                JOptionPane.showMessageDialog(this, String.format(
                    esFiado
                        ? "Venta registrada al fiado!\nFactura: %s\nTotal: %s (%s)\nCliente: %s\nStock actualizado automaticamente."
                        : "Venta registrada!\nFactura: %s\nTotal: %s (%s)\nStock actualizado automaticamente.",
                    f.getNumeroFactura(), Utils.Formato.usd(total), Utils.Formato.bsMiles(total),
                    cliente != null ? cliente.getNombre() : ""));
            } catch (SQLException ex) {
                CX.ConexionBD.errorManager(ex);
            }
        };

        btnFiar.addActionListener(e -> guardarVenta.accept(true));
        btnGuardar.addActionListener(e -> guardarVenta.accept(false));

        dlg.add(main);
        dlg.setVisible(true);
    }

    private void actualizarTotales(DefaultTableModel modeloDetalle, JLabel lblSub, JLabel lblTot) {
        double subtotal = 0;
        for (int i = 0; i < modeloDetalle.getRowCount(); i++) {
            try {
                subtotal += PanelProductos.parseSafeDouble(String.valueOf(modeloDetalle.getValueAt(i, 4)).replace("$", ""));
            } catch (Exception e) {}
        }
        double total = subtotal;
        if (lblSub != null) lblSub.setText(String.format("Subtotal: %s (%s)", Utils.Formato.usd(subtotal), Utils.Formato.bsMiles(subtotal)));
        if (lblTot != null) lblTot.setText(String.format("TOTAL: %s (%s)", Utils.Formato.usd(total), Utils.Formato.bsMiles(total)));
    }

    /**
     * Resuelve el cliente para una venta al fiado:
     * 1. Si ya eligió uno de la lista, lo usa.
     * 2. Si no, busca por cédula exacta. Si existe, lo usa.
     * 3. Si no existe, pide cédula + nombre + apellido y lo registra.
     * Devuelve null si el usuario cancela el diálogo de registro.
     */
    private Modelo.Cliente resolverClienteFiado(java.awt.Component parent, String texto, Modelo.Cliente elegido) {
        if (elegido != null) return elegido;
        String typed = texto == null ? "" : texto.trim();

        try {
            if (!typed.isEmpty()) {
                Modelo.Cliente porCedula = new DAO.ClienteDAO().buscarPorCedula(typed);
                if (porCedula != null) return porCedula;
            }
        } catch (SQLException e) {}

        JTextField tfCedula = PanelProductos.crearCampo(typed, 100);
        JTextField tfNombre = PanelProductos.crearCampo("", 100);
        JTextField tfApellido = PanelProductos.crearCampo("", 100);
        Object[] cuerpo = {
            "El cliente no está registrado. Ingrese sus datos:", "Cédula:", tfCedula,
            "Nombre:", tfNombre, "Apellido:", tfApellido
        };
        int r = JOptionPane.showConfirmDialog(parent, cuerpo, "Registrar Cliente", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r != JOptionPane.OK_OPTION) return null;

        String cedula = tfCedula.getText().trim();
        String nombre = tfNombre.getText().trim();
        String apellido = tfApellido.getText().trim();
        if (cedula.isEmpty() || nombre.isEmpty() || apellido.isEmpty()) {
            JOptionPane.showMessageDialog(parent, "Debe completar cédula, nombre y apellido.",
                "Datos incompletos", JOptionPane.WARNING_MESSAGE);
            return resolverClienteFiado(parent, typed, null);
        }

        Modelo.Cliente cli = new Modelo.Cliente();
        cli.setCedula(cedula);
        cli.setNombre((nombre + " " + apellido).trim());
        cli.setCorreo("");
        cli.setTelefono("");
        try {
            new DAO.ClienteDAO().insertar(cli);
            Modelo.Cliente creado = new DAO.ClienteDAO().buscarPorCedula(cedula);
            new DAO.BitacoraDAO().registrar("Clientes", "Crear",
                "Cliente registrado desde venta al fiado: " + cli.getNombre() + " [Cédula: " + cedula + "]");
            return creado != null ? creado : cli;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(parent, "Error al registrar cliente: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    private void verDetalle() {
        int row = tabla.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Seleccione una factura."); return; }
        long facturaId = (long) modelo.getValueAt(row, 0);
        String numero = (String) modelo.getValueAt(row, 1);
        String estado = (String) modelo.getValueAt(row, 7);

        try {
            List<DetalleFactura> detalles = new DetalleFacturaDAO().listarPorFactura(facturaId);
            if (detalles.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Esta factura no tiene detalles (ventas anteriores sin detalle).");
                return;
            }

            StringBuilder sb = new StringBuilder();
            sb.append("=== ").append(numero).append(" ===\n");
            sb.append("Estado: ").append(estado).append("\n\n");
            sb.append(String.format("%-20s %8s %10s %14s %14s\n", "Producto", "Cant.", "Precio", "Subtotal", "Bs"));
            sb.append("-".repeat(66)).append("\n");

            for (DetalleFactura df : detalles) {
                sb.append(String.format("%-20s %8.0f %10.2f %14.2f %14.2f\n",
                    df.getProductoNombre() != null ? df.getProductoNombre() : "ID:" + df.getProductoId(),
                    df.getCantidad(), df.getPrecioUnitario(), df.getSubtotal(),
                    df.getSubtotal() * Utils.Config.getTasaVES()));
            }
            sb.append("-".repeat(66)).append("\n");
            double subDet = PanelProductos.parseSafeDouble(modelo.getValueAt(row, 4).toString().replace("$", ""));
            double totDet = PanelProductos.parseSafeDouble(modelo.getValueAt(row, 5).toString().replace("$", ""));
            sb.append(String.format("Subtotal: $%.2f  (%s)\n", subDet, Utils.Formato.bsMiles(subDet)));
            sb.append(String.format("TOTAL: $%.2f  (%s)\n", totDet, Utils.Formato.bsMiles(totDet)));

            JTextArea ta = new JTextArea(sb.toString());
            ta.setFont(new Font("Monospaced", Font.PLAIN, 13));
            ta.setBackground(new Color(30, 30, 30));
            ta.setForeground(Color.WHITE);
            ta.setEditable(false);
            ta.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JPanel cuerpo = new JPanel(new BorderLayout(5, 10));
            cuerpo.setBackground(new Color(30, 30, 30));
            cuerpo.add(ta, BorderLayout.CENTER);

            boolean esFiada = estado != null && "Pendiente".equalsIgnoreCase(estado);

            JOptionPane pane = new JOptionPane(cuerpo, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION);
            JDialog d = pane.createDialog(this, "Detalle de " + numero);

            if (esFiada) {
                JButton btnPagar = PanelProductos.crearBoton("✅ Marcar Pagado", new Color(16, 185, 129));
                JPanel pBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                pBtn.setOpaque(false);
                pBtn.add(btnPagar);
                cuerpo.add(pBtn, BorderLayout.SOUTH);
                btnPagar.addActionListener(ae -> {
                    try {
                        dao.marcarPagada(facturaId);
                        new DAO.BitacoraDAO().registrar("Ventas", "Editar",
                            "Venta fiada " + numero + " marcada como Pagada");
                        cargar();
                        d.dispose();
                    } catch (SQLException ex) {
                        CX.ConexionBD.errorManager(ex);
                    }
                });
            }

            d.setVisible(true);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void anularSeleccionada() {
        int row = tabla.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Seleccione una factura."); return; }
        long id = (long) modelo.getValueAt(row, 0);
        String numero = (String) modelo.getValueAt(row, 1);
        int r = JOptionPane.showConfirmDialog(this,
            "Anular esta factura?\nEl stock de los productos se restaurara automaticamente.",
            "Confirmar Anulacion", JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) {
            try {
                dao.anularConRestauracion(id, numero);
                new DAO.BitacoraDAO().registrar("Ventas", "Anular",
                    "Factura " + numero + " anulada, stock restaurado");
                cargar();
                JOptionPane.showMessageDialog(this, "Factura anulada. El stock fue restaurado.");
            } catch (SQLException e) {
                CX.ConexionBD.errorManager(e);
            }
        }
    }

    private void addFormRow(JPanel form, GridBagConstraints gc, int row, String label, JComponent field) {
        gc.gridy = row;
        gc.gridx = 0;
        gc.weightx = 0.25;
        gc.gridwidth = 1;
        JLabel lbl = new JLabel(label);
        lbl.setForeground(new Color(180, 180, 180));
        lbl.setFont(Utils.UI.TEXTO);
        form.add(lbl, gc);
        gc.gridx = 1;
        gc.weightx = 0.75;
        form.add(field, gc);
    }
}

