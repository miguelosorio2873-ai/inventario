package IG;

import DAO.InventarioDAO;
import DAO.ProductoDAO;
import DAO.ProveedorDAO;
import Modelo.MovimientoInventario;
import Modelo.Producto;
import Modelo.Proveedor;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;

public class PanelInventario extends JPanel {

    private JTable tabla;
    private DefaultTableModel modelo;
    private JTextField campoBuscar;
    private InventarioDAO dao = new InventarioDAO();
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public PanelInventario() {
        setBackground(new Color(24, 24, 27));
        setLayout(new BorderLayout(0, 15));
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        // Header
        JPanel header = new JPanel(new BorderLayout(15, 0));
        header.setOpaque(false);

        JLabel titulo = new JLabel("Control de Inventario");
        try {
            com.formdev.flatlaf.extras.FlatSVGIcon icon = new com.formdev.flatlaf.extras.FlatSVGIcon(getClass().getResource("/IMG/chart-bar.svg")).derive(24, 24);
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
        campoBuscar.putClientProperty("JTextField.placeholderText", "Buscar por producto...");
        campoBuscar.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { buscar(); }
        });

        JButton btnEntrada = PanelProductos.crearBoton("📥 Entrada", new Color(16, 185, 129));
        btnEntrada.addActionListener(e -> dialogoMovimiento("entrada"));

        JButton btnSalida = PanelProductos.crearBoton("📤 Salida", new Color(245, 158, 11));
        btnSalida.addActionListener(e -> dialogoMovimiento("salida"));

        JButton btnAjuste = PanelProductos.crearBoton("🔧 Ajuste", new Color(99, 102, 241));
        btnAjuste.addActionListener(e -> dialogoMovimiento("ajuste"));

        // Aplicar permisos
        CX.SesionUsuario sesion = CX.SesionUsuario.getInstancia();
        btnEntrada.setEnabled(sesion.tienePermiso("Inventario", "Crear"));
        btnSalida.setEnabled(sesion.tienePermiso("Inventario", "Eliminar"));
        btnAjuste.setEnabled(sesion.tienePermiso("Inventario", "Editar"));

        acciones.add(campoBuscar);
        acciones.add(btnEntrada);
        acciones.add(btnSalida);
        acciones.add(btnAjuste);

        header.add(titulo, BorderLayout.WEST);
        header.add(acciones, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Tabla
        String[] cols = {"ID", "Producto", "Proveedor", "Tipo", "Cantidad", "Precio", "Balance", "Fecha", "Motivo"};
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
            List<MovimientoInventario> lista = dao.listarMovimientos();
            for (MovimientoInventario m : lista) {
                String tipo = m.getTipoMovimiento();
                String tipoLabel = tipo.substring(0, 1).toUpperCase() + tipo.substring(1);
                modelo.addRow(new Object[]{
                    m.getId(),
                    m.getProductoNombre() != null ? m.getProductoNombre() : "N/A",
                    m.getProveedorNombre() != null ? m.getProveedorNombre() : "—",
                    tipoLabel,
                    String.format("%.0f", m.getCantidad()),
                    String.format("$%.2f", m.getPrecio()),
                    String.format("$%.2f", m.getPrecioBalance()),
                    m.getFechaMovimiento() != null ? sdf.format(m.getFechaMovimiento()) : "",
                    m.getMotivo() != null ? m.getMotivo() : ""
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void buscar() {
        String t = campoBuscar.getText().trim();
        modelo.setRowCount(0);
        try {
            List<MovimientoInventario> lista = t.isEmpty() ? dao.listarMovimientos() : dao.buscarPorProducto(t);
            for (MovimientoInventario m : lista) {
                String tipo = m.getTipoMovimiento();
                String tipoLabel = tipo.substring(0, 1).toUpperCase() + tipo.substring(1);
                modelo.addRow(new Object[]{
                    m.getId(),
                    m.getProductoNombre() != null ? m.getProductoNombre() : "N/A",
                    m.getProveedorNombre() != null ? m.getProveedorNombre() : "—",
                    tipoLabel,
                    String.format("%.0f", m.getCantidad()),
                    String.format("$%.2f", m.getPrecio()),
                    String.format("$%.2f", m.getPrecioBalance()),
                    m.getFechaMovimiento() != null ? sdf.format(m.getFechaMovimiento()) : "",
                    m.getMotivo() != null ? m.getMotivo() : ""
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void dialogoMovimiento(String tipo) {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            "Registrar " + tipo.substring(0, 1).toUpperCase() + tipo.substring(1), true);
        dlg.setSize(480, 420);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(new Color(39, 39, 42));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(6, 5, 6, 5);

        // Producto combo
        JComboBox<Producto> cbProducto = new JComboBox<>();
        cbProducto.setBackground(new Color(45, 45, 45));
        cbProducto.setForeground(Color.WHITE);
        cbProducto.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Producto) {
                    Producto p = (Producto) value;
                    setText(p.getSku() + " - " + p.getNombre() + " (Stock: " + String.format("%.0f", p.getStockActual()) + ")");
                }
                setBackground(isSelected ? new Color(16, 185, 129, 50) : new Color(45, 45, 45));
                setForeground(Color.WHITE);
                return this;
            }
        });

        // Proveedor combo (solo para entradas)
        JComboBox<Proveedor> cbProveedor = new JComboBox<>();
        cbProveedor.setBackground(new Color(45, 45, 45));
        cbProveedor.setForeground(Color.WHITE);
        cbProveedor.addItem(null); // opción vacía
        cbProveedor.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Proveedor) {
                    setText(((Proveedor) value).getNombreEmpresa());
                } else {
                    setText("— Ninguno —");
                }
                setBackground(isSelected ? new Color(16, 185, 129, 50) : new Color(45, 45, 45));
                setForeground(Color.WHITE);
                return this;
            }
        });

        JTextField tfCantidad = PanelProductos.crearCampoNumerico("1", 15, true);
        JTextField tfPrecio = PanelProductos.crearCampoNumerico("", 15, true);
        JTextField tfMotivo = PanelProductos.crearCampo("", 255);

        // Cargar datos en combos
        try {
            for (Producto p : new ProductoDAO().listarTodos()) cbProducto.addItem(p);
            for (Proveedor pr : new ProveedorDAO().listarTodos()) cbProveedor.addItem(pr);
        } catch (SQLException e) { /* ignore */ }

        // Layout
        int row = 0;
        addFormRow(form, gc, row++, "Producto:", cbProducto);
        if (tipo.equals("entrada")) {
            addFormRow(form, gc, row++, "Proveedor:", cbProveedor);
        }
        addFormRow(form, gc, row++, "Cantidad:", tfCantidad);
        addFormRow(form, gc, row++, "Precio unitario:", tfPrecio);
        addFormRow(form, gc, row++, "Motivo:", tfMotivo);

        JButton btnGuardar = PanelProductos.crearBoton("💾 Registrar", new Color(16, 185, 129));
        btnGuardar.addActionListener(e -> {
            try {
                Producto prodSel = (Producto) cbProducto.getSelectedItem();
                if (prodSel == null) {
                    JOptionPane.showMessageDialog(dlg, "Seleccione un producto.");
                    return;
                }
                double cantidad = PanelProductos.parseSafeDouble(tfCantidad.getText());
                double precio = PanelProductos.parseSafeDouble(tfPrecio.getText());
                
                // Para entradas y salidas, nos aseguramos que la cantidad sea positiva antes de procesar
                if (tipo.equals("entrada") || tipo.equals("salida")) {
                    cantidad = Math.abs(cantidad);
                    if (cantidad == 0) {
                        JOptionPane.showMessageDialog(dlg, "La cantidad debe ser mayor a 0.");
                        return;
                    }
                }
                
                double cantidadFinal = tipo.equals("salida") ? -cantidad : cantidad;

                // Validación de stock para cualquier movimiento que disminuya el inventario
                if (cantidadFinal < 0) {
                    double stockDisponible = dao.obtenerStockProducto(prodSel.getId());
                    if (Math.abs(cantidadFinal) > stockDisponible) {
                        JOptionPane.showMessageDialog(dlg, 
                            "<html><body style='width: 250px;'><b>⚠️ Advertencia de Stock</b><br><br>" +
                            "No se puede realizar el movimiento porque no hay suficiente stock.<br><br>" +
                            "Producto: <b>" + prodSel.getNombre() + "</b><br>" +
                            "Stock Disponible: <b style='color: #F59E0B;'>" + String.format("%.0f", stockDisponible) + "</b><br>" +
                            "Cantidad a Retirar: <b style='color: #EF4444;'>" + String.format("%.0f", Math.abs(cantidadFinal)) + "</b></body></html>",
                            "Stock Insuficiente", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }

                MovimientoInventario mov = new MovimientoInventario();
                mov.setProductoId(prodSel.getId());
                mov.setTipoMovimiento(tipo);
                mov.setCantidad(cantidadFinal);
                mov.setPrecio(precio);
                mov.setPrecioBalance(precio * Math.abs(cantidadFinal));
                mov.setMotivo(tfMotivo.getText().trim());

                if (tipo.equals("entrada")) {
                    Proveedor provSel = (Proveedor) cbProveedor.getSelectedItem();
                    if (provSel != null) mov.setProveedorId(provSel.getId());
                }

                dao.registrarMovimiento(mov);
                dlg.dispose();
                cargar();
                JOptionPane.showMessageDialog(this, "✅ Movimiento registrado correctamente.");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg, "Verifique los valores numéricos.");
            } catch (SQLException ex) {
                CX.ConexionBD.errorManager(ex);
            }
        });

        gc.gridy = row;
        gc.gridx = 0;
        gc.gridwidth = 2;
        gc.insets = new Insets(15, 5, 5, 5);
        form.add(btnGuardar, gc);

        dlg.add(form);
        dlg.setVisible(true);
    }

    private void addFormRow(JPanel form, GridBagConstraints gc, int row, String label, JComponent field) {
        gc.gridy = row;
        gc.gridx = 0;
        gc.weightx = 0.3;
        gc.gridwidth = 1;
        JLabel lbl = new JLabel(label);
        lbl.setForeground(new Color(180, 180, 180));
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        form.add(lbl, gc);
        gc.gridx = 1;
        gc.weightx = 0.7;
        form.add(field, gc);
    }
}
