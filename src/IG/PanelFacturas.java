package IG;

import DAO.FacturaDAO;
import DAO.ClienteDAO;
import Modelo.Factura;
import Modelo.Cliente;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

public class PanelFacturas extends JPanel {

    private JTable tabla;
    private DefaultTableModel modelo;
    private JTextField campoBuscar;
    private FacturaDAO dao = new FacturaDAO();
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public PanelFacturas() {
        setBackground(new Color(24, 24, 27));
        setLayout(new BorderLayout(0, 15));
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        // Header
        JPanel header = new JPanel(new BorderLayout(15, 0));
        header.setOpaque(false);

        JLabel titulo = new JLabel("Gestión de Facturas");
        try {
            com.formdev.flatlaf.extras.FlatSVGIcon icon = new com.formdev.flatlaf.extras.FlatSVGIcon(getClass().getResource("/IMG/file-invoice.svg")).derive(24, 24);
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
        campoBuscar.putClientProperty("JTextField.placeholderText", "Buscar por N° factura o cliente...");
        campoBuscar.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { buscar(); }
        });

        JButton btnNueva = PanelProductos.crearBoton("➕ Nueva Factura", new Color(16, 185, 129));
        btnNueva.addActionListener(e -> dialogoNuevaFactura());

        JButton btnAnular = PanelProductos.crearBoton("❌ Anular", new Color(239, 68, 68));
        btnAnular.addActionListener(e -> anularSeleccionada());

        JButton btnPagar = PanelProductos.crearBoton("✅ Marcar Pagada", new Color(59, 130, 246));
        btnPagar.addActionListener(e -> cambiarEstado("Pagada"));

        acciones.add(campoBuscar);
        acciones.add(btnNueva);
        acciones.add(btnPagar);
        acciones.add(btnAnular);

        header.add(titulo, BorderLayout.WEST);
        header.add(acciones, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Tabla
        String[] cols = {"ID", "N° Factura", "Cliente", "Fecha", "Método Pago", "Subtotal", "Impuestos", "Total", "Estado"};
        modelo = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tabla = PanelProductos.crearTabla(modelo);

        // Color por estado
        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    String estado = (String) table.getValueAt(row, 8);
                    if ("Anulada".equals(estado)) {
                        c.setForeground(new Color(239, 68, 68));
                    } else if ("Pagada".equals(estado)) {
                        c.setForeground(new Color(16, 185, 129));
                    } else if ("Pendiente".equals(estado)) {
                        c.setForeground(new Color(245, 158, 11));
                    } else {
                        c.setForeground(Color.WHITE);
                    }
                    c.setBackground(new Color(39, 39, 42));
                }
                return c;
            }
        });

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(new Color(39, 39, 42));
        add(scroll, BorderLayout.CENTER);

        cargar();
    }

    private void cargar() {
        modelo.setRowCount(0);
        try {
            for (Factura f : dao.listarTodas()) {
                modelo.addRow(new Object[]{
                    f.getId(),
                    f.getNumeroFactura(),
                    f.getClienteNombre() != null ? f.getClienteNombre() : "—",
                    f.getFechaEmision() != null ? sdf.format(f.getFechaEmision()) : "",
                    f.getMetodoPago() != null ? f.getMetodoPago() : "—",
                    String.format("$%.2f", f.getSubtotal()),
                    String.format("$%.2f", f.getImpuestos()),
                    String.format("$%.2f", f.getTotal()),
                    f.getEstado()
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
            for (Factura f : t.isEmpty() ? dao.listarTodas() : dao.buscar(t)) {
                modelo.addRow(new Object[]{
                    f.getId(),
                    f.getNumeroFactura(),
                    f.getClienteNombre() != null ? f.getClienteNombre() : "—",
                    f.getFechaEmision() != null ? sdf.format(f.getFechaEmision()) : "",
                    f.getMetodoPago() != null ? f.getMetodoPago() : "—",
                    String.format("$%.2f", f.getSubtotal()),
                    String.format("$%.2f", f.getImpuestos()),
                    String.format("$%.2f", f.getTotal()),
                    f.getEstado()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void dialogoNuevaFactura() {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Nueva Factura", true);
        dlg.setSize(480, 450);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(new Color(39, 39, 42));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(6, 5, 6, 5);

        // N° factura (auto)
        JTextField tfNumero = PanelProductos.crearCampo("", 50);
        tfNumero.setEditable(false);
        try {
            tfNumero.setText(dao.generarNumeroFactura());
        } catch (SQLException e) { tfNumero.setText("FAC-000001"); }

        // Cliente combo
        JComboBox<Cliente> cbCliente = new JComboBox<>();
        cbCliente.setBackground(new Color(45, 45, 45));
        cbCliente.setForeground(Color.WHITE);
        cbCliente.addItem(null);
        cbCliente.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Cliente) {
                    Cliente c = (Cliente) value;
                    setText(c.getCedula() + " - " + c.getNombre());
                } else {
                    setText("— Sin cliente —");
                }
                setBackground(isSelected ? new Color(16, 185, 129, 50) : new Color(45, 45, 45));
                setForeground(Color.WHITE);
                return this;
            }
        });
        try {
            for (Cliente c : new ClienteDAO().listarTodos()) cbCliente.addItem(c);
        } catch (SQLException e) { /* ignore */ }

        // Método pago
        JComboBox<String> cbMetodo = new JComboBox<>(new String[]{"Efectivo", "Tarjeta", "Transferencia"});
        cbMetodo.setBackground(new Color(45, 45, 45));
        cbMetodo.setForeground(Color.WHITE);

        // Estado
        JComboBox<String> cbEstado = new JComboBox<>(new String[]{"Pendiente", "Pagada"});
        cbEstado.setBackground(new Color(45, 45, 45));
        cbEstado.setForeground(Color.WHITE);

        JTextField tfSubtotal = PanelProductos.crearCampoNumerico("", 15, true);
        JTextField tfImpuestos = PanelProductos.crearCampoNumerico("", 15, true);
        JTextField tfTotal = PanelProductos.crearCampoNumerico("", 15, true);
        tfTotal.setEditable(false);

        // Auto-calcular total
        KeyAdapter calcListener = new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                try {
                    double sub = PanelProductos.parseSafeDouble(tfSubtotal.getText());
                    double imp = PanelProductos.parseSafeDouble(tfImpuestos.getText());
                    tfTotal.setText(String.format("%.2f", sub + imp));
                } catch (NumberFormatException ex) { /* ignore */ }
            }
        };
        tfSubtotal.addKeyListener(calcListener);
        tfImpuestos.addKeyListener(calcListener);

        // Layout
        int row = 0;
        addFormRow(form, gc, row++, "N° Factura:", tfNumero);
        addFormRow(form, gc, row++, "Cliente:", cbCliente);
        addFormRow(form, gc, row++, "Método Pago:", cbMetodo);
        addFormRow(form, gc, row++, "Estado:", cbEstado);
        addFormRow(form, gc, row++, "Subtotal:", tfSubtotal);
        addFormRow(form, gc, row++, "Impuestos:", tfImpuestos);
        addFormRow(form, gc, row++, "Total:", tfTotal);

        JButton btnGuardar = PanelProductos.crearBoton("💾 Guardar Factura", new Color(16, 185, 129));
        btnGuardar.addActionListener(e -> {
            try {
                double sub = PanelProductos.parseSafeDouble(tfSubtotal.getText());
                double imp = PanelProductos.parseSafeDouble(tfImpuestos.getText());

                Factura f = new Factura();
                f.setNumeroFactura(tfNumero.getText().trim());
                f.setMetodoPago((String) cbMetodo.getSelectedItem());
                f.setEstado((String) cbEstado.getSelectedItem());
                f.setSubtotal(sub);
                f.setImpuestos(imp);
                f.setTotal(sub + imp);

                Cliente cliSel = (Cliente) cbCliente.getSelectedItem();
                if (cliSel != null) f.setClienteId(cliSel.getId());

                dao.insertar(f);
                dlg.dispose();
                cargar();
                JOptionPane.showMessageDialog(this, "✅ Factura creada: " + f.getNumeroFactura());
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

    private void cambiarEstado(String nuevoEstado) {
        int row = tabla.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Seleccione una factura."); return; }
        long id = (long) modelo.getValueAt(row, 0);
        String estadoActual = (String) modelo.getValueAt(row, 8);
        if ("Anulada".equals(estadoActual)) {
            JOptionPane.showMessageDialog(this, "No se puede modificar una factura anulada.");
            return;
        }
        try {
            dao.actualizarEstado(id, nuevoEstado);
            cargar();
            JOptionPane.showMessageDialog(this, "✅ Estado actualizado a: " + nuevoEstado);
        } catch (SQLException e) {
            CX.ConexionBD.errorManager(e);
        }
    }

    private void anularSeleccionada() {
        int row = tabla.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Seleccione una factura."); return; }
        int r = JOptionPane.showConfirmDialog(this, "¿Anular esta factura?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) {
            cambiarEstado("Anulada");
        }
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
