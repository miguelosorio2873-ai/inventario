package IG;

import DAO.InventarioDAO;
import Modelo.MovimientoInventario;
import Utils.Formato;
import Utils.UI;
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
    private JLabel lblPie;

    public PanelInventario() {
        setBackground(new Color(24, 24, 27));
        setLayout(new BorderLayout(0, 15));
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        // Header
        JPanel header = new JPanel(new BorderLayout(15, 0));
        header.setOpaque(false);

        JLabel titulo = new JLabel("Control de Inventario");
        try {
            com.formdev.flatlaf.extras.FlatSVGIcon icon = new com.formdev.flatlaf.extras.FlatSVGIcon(getClass().getResource("/IMG/chart-bar.svg")).derive(32, 32);
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

       acciones.add(campoBuscar);

        header.add(titulo, BorderLayout.WEST);
        header.add(acciones, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Tabla
        String[] cols = {"ID", "Producto", "Proveedor", "Tipo", "Cantidad", "Precio", "Balance", "Fecha", "Motivo"};
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
                    Object tipo = table.getValueAt(row, 3);
                    String t = tipo != null ? tipo.toString() : "";
                    if ("Entrada".equalsIgnoreCase(t)) c.setForeground(new Color(16, 185, 129));
                    else if ("Salida".equalsIgnoreCase(t)) c.setForeground(new Color(239, 68, 68));
                    else if ("Ajuste".equalsIgnoreCase(t)) c.setForeground(new Color(245, 158, 11));
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
        double suma = 0;
        try {
            List<MovimientoInventario> lista = dao.listarMovimientosConNombres();
            for (MovimientoInventario m : lista) {
                String tipo = m.getTipoMovimiento();
                String tipoLabel = tipo.substring(0, 1).toUpperCase() + tipo.substring(1);
                suma += m.getPrecioBalance();
                modelo.addRow(new Object[]{
                    m.getId(),
                    m.getProductoNombre() != null ? m.getProductoNombre() : "N/A",
                    m.getProveedorNombre() != null ? m.getProveedorNombre() : "—",
                    tipoLabel,
                    String.format("%.0f", m.getCantidad()),
                    Utils.Formato.usdBs(m.getPrecio()),
                    Utils.Formato.usdBs(m.getPrecioBalance()),
                    m.getFechaMovimiento() != null ? sdf.format(m.getFechaMovimiento()) : "",
                    m.getMotivo() != null ? m.getMotivo() : ""
                });
            }
            if (lblPie != null) lblPie.setText(String.format("Movimientos: %d | Total balance: %s", lista.size(), Utils.Formato.usd(suma)));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void buscar() {
        String t = campoBuscar.getText().trim();
        modelo.setRowCount(0);
        double suma = 0;
        try {
            List<MovimientoInventario> lista = t.isEmpty() ? dao.listarMovimientosConNombres() : dao.buscarPorProducto(t);
            for (MovimientoInventario m : lista) {
                String tipo = m.getTipoMovimiento();
                String tipoLabel = tipo.substring(0, 1).toUpperCase() + tipo.substring(1);
                suma += m.getPrecioBalance();
                modelo.addRow(new Object[]{
                    m.getId(),
                    m.getProductoNombre() != null ? m.getProductoNombre() : "N/A",
                    m.getProveedorNombre() != null ? m.getProveedorNombre() : "—",
                    tipoLabel,
                    String.format("%.0f", m.getCantidad()),
                    Utils.Formato.usdBs(m.getPrecio()),
                    Utils.Formato.usdBs(m.getPrecioBalance()),
                    m.getFechaMovimiento() != null ? sdf.format(m.getFechaMovimiento()) : "",
                    m.getMotivo() != null ? m.getMotivo() : ""
                });
            }
            if (lblPie != null) lblPie.setText(String.format("Movimientos: %d | Total balance: %s", lista.size(), Utils.Formato.usd(suma)));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
}
