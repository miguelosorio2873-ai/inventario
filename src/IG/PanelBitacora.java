package IG;

import DAO.BitacoraDAO;
import Modelo.Bitacora;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import Utils.UI;

public class PanelBitacora extends JPanel {

    private JTable tabla;
    private DefaultTableModel modelo;
    private JTextField campoBuscar;
    private JComboBox<String> cbFiltro;
    private JComboBox<String> cbAccion;
    private JCheckBox chkFecha;
    private JSpinner spFechaIni;
    private JSpinner spFechaFin;
    private BitacoraDAO dao = new BitacoraDAO();
    private JLabel lblPie;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a");

    public PanelBitacora() {
        setBackground(new Color(24, 24, 27));
        setLayout(new BorderLayout(0, 15));
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JPanel header = new JPanel(new BorderLayout(15, 0));
        header.setOpaque(false);

        JLabel titulo = new JLabel("Bitácora");
        try {
            com.formdev.flatlaf.extras.FlatSVGIcon icon = new com.formdev.flatlaf.extras.FlatSVGIcon("IMG/scroll.svg").derive(32, 32);
            icon.setColorFilter(new com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter(c -> Color.WHITE));
            titulo.setIcon(icon);
            titulo.setIconTextGap(12);
        } catch (Exception e) {}
        titulo.setFont(UI.TITULO);
        titulo.setForeground(Color.WHITE);

        JPanel acciones = new JPanel();
        acciones.setOpaque(false);
        acciones.setLayout(new BoxLayout(acciones, BoxLayout.Y_AXIS));

        JPanel fila1 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        fila1.setOpaque(false);
        campoBuscar = PanelProductos.crearCampo("", 100);
        campoBuscar.setColumns(20);
        campoBuscar.putClientProperty("JTextField.placeholderText", "Buscar en bitácora...");
        campoBuscar.setFont(UI.CAMPO);
        campoBuscar.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { buscar(); }
        });

        cbFiltro = new JComboBox<>(new String[]{
            "Todos", "Login", "Ventas", "Clientes", "Inventario", "Usuarios", "Configuracion"
        });
        cbFiltro.addActionListener(e -> {
            actualizarAccionesPorModulo();
            buscar();
        });

        cbAccion = new JComboBox<>(accionesDeModulo("Todos"));
        cbAccion.setBackground(new Color(45, 45, 45));
        cbAccion.setForeground(Color.WHITE);
        cbAccion.setFont(UI.CAMPO);
        cbAccion.addActionListener(e -> buscar());

        JButton btnLimpiar = PanelProductos.crearBoton("🔄 Refrescar", new Color(59, 130, 246));
        btnLimpiar.setFont(UI.BOTON);
        btnLimpiar.addActionListener(e -> cargar());

        fila1.add(campoBuscar);
        fila1.add(cbFiltro);
        fila1.add(cbAccion);
        fila1.add(btnLimpiar);
        acciones.add(fila1);

        JPanel fila2 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        fila2.setOpaque(false);
        cbFiltro.setBackground(new Color(45, 45, 45));
        cbFiltro.setForeground(Color.WHITE);
        cbFiltro.setFont(UI.CAMPO);

        JLabel lblRango = new JLabel("Rango de fecha:");
        lblRango.setFont(UI.NOTA);
        lblRango.setForeground(new Color(180, 180, 180));
        fila2.add(lblRango);

        SpinnerDateModel mIni = new SpinnerDateModel();
        spFechaIni = new JSpinner(mIni);
        spFechaIni.setEditor(new JSpinner.DateEditor(spFechaIni, "yyyy-MM-dd"));
        spFechaIni.setFont(UI.CAMPO);
        spFechaIni.setPreferredSize(new Dimension(110, 28));
        spFechaIni.setEnabled(false);

        SpinnerDateModel mFin = new SpinnerDateModel();
        spFechaFin = new JSpinner(mFin);
        spFechaFin.setEditor(new JSpinner.DateEditor(spFechaFin, "yyyy-MM-dd"));
        spFechaFin.setFont(UI.CAMPO);
        spFechaFin.setPreferredSize(new Dimension(110, 28));
        spFechaFin.setEnabled(false);

        chkFecha = new JCheckBox("Filtrar por fecha");
        chkFecha.setFont(UI.CAMPO);
        chkFecha.setForeground(Color.WHITE);
        chkFecha.setBackground(new Color(24, 24, 27));
        chkFecha.addActionListener(e -> {
            spFechaIni.setEnabled(chkFecha.isSelected());
            spFechaFin.setEnabled(chkFecha.isSelected());
            if (!chkFecha.isSelected()) buscar();
        });

        fila2.add(chkFecha);
        fila2.add(spFechaIni);
        fila2.add(new JLabel("a"));
        fila2.add(spFechaFin);

        spFechaIni.addChangeListener(e -> buscar());
        spFechaFin.addChangeListener(e -> buscar());

        acciones.add(fila2);

        header.add(titulo, BorderLayout.WEST);
        header.add(acciones, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        String[] cols = {"", "ID", "Fecha", "Usuario", "Módulo", "Acción", "Detalle"};
        modelo = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tabla = PanelProductos.crearTabla(modelo);
        tabla.setFont(UI.TABLA);
        tabla.setRowHeight(UI.FILA_ALTO);
        tabla.getTableHeader().setFont(UI.TABLA_ENCABEZADO);

        tabla.getColumnModel().getColumn(0).setPreferredWidth(44);
        tabla.getColumnModel().getColumn(0).setMaxWidth(44);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(50);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(150);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(150);
        tabla.getColumnModel().getColumn(4).setPreferredWidth(120);
        tabla.getColumnModel().getColumn(5).setPreferredWidth(120);
        tabla.getColumnModel().getColumn(6).setPreferredWidth(450);

        // Icono en columna 0 (índice de acción)
        tabla.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column);
                lbl.setHorizontalAlignment(CENTER);
                lbl.setIcon(null);
                lbl.setBackground(isSelected ? new Color(16, 185, 129, 50) : new Color(39, 39, 42));

                String accion = table.getValueAt(row, 5) != null ? table.getValueAt(row, 5).toString() : "";
                String modulo = table.getValueAt(row, 4) != null ? table.getValueAt(row, 4).toString() : "";
                String iconName = getIconForAccion(accion, modulo);
                if (iconName != null) {
                    try {
                        com.formdev.flatlaf.extras.FlatSVGIcon ico = new com.formdev.flatlaf.extras.FlatSVGIcon("IMG/" + iconName);
                        ico = (com.formdev.flatlaf.extras.FlatSVGIcon) ico.derive(22, 22);
                        ico.setColorFilter(new com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter(c -> getColorForAccion(accion)));
                        lbl.setIcon(ico);
                    } catch (Exception e) {}
                }
                return lbl;
            }
        });

        // Color de texto por acción
        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (c instanceof JLabel) {
                    ((JLabel) c).setFont(UI.TABLA);
                }
                if (!isSelected) {
                    c.setBackground(new Color(39, 39, 42));
                    if (column == 0) {
                        c.setForeground(new Color(39, 39, 42));
                        return c;
                    }
                    String accion = table.getValueAt(row, 5) != null ? table.getValueAt(row, 5).toString() : "";
                    String modulo = table.getValueAt(row, 4) != null ? table.getValueAt(row, 4).toString() : "";

                    if (accion.contains("Reabastec")) {
                        c.setForeground(new Color(251, 146, 60));
                    } else if (accion.contains("Crear") || accion.contains("Agregar") || accion.contains("Nuevo")) {
                        c.setForeground(new Color(16, 185, 129));
                    } else if (accion.contains("Editar") || accion.contains("Cambiar") || accion.contains("Contraseña")) {
                        c.setForeground(new Color(59, 130, 246));
                    } else if (accion.contains("Eliminar") || accion.contains("Anular")) {
                        c.setForeground(new Color(239, 68, 68));
                    } else if (accion.contains("Venta")) {
                        c.setForeground(new Color(245, 158, 11));
                    } else if (accion.contains("Login") || accion.contains("Inicio")) {
                        c.setForeground(new Color(56, 189, 248));
                    } else if (accion.contains("Cerrar Sesión") || accion.contains("Cerrar")) {
                        c.setForeground(new Color(239, 68, 68));
                    } else {
                        c.setForeground(new Color(200, 200, 200));
                    }

                    // Colorear módulo
                    if (column == 4) {
                        c.setForeground(getColorForModulo(modulo));
                    }
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

    private String getIconForAccion(String accion, String modulo) {
        if (accion == null) return "alert-triangle.svg";
        if (accion.contains("Reabastec") || accion.contains("Compra") || accion.contains("Entrada")) return "truck.svg";
        if (accion.contains("Crear") || accion.contains("Agregar") || accion.contains("Nuevo")) {
            if (modulo != null && modulo.contains("Venta")) return "file-invoice.svg";
            if (modulo != null && modulo.contains("Usuarios")) return "users.svg";
            return "plus.svg";
        }
        if (accion.contains("Editar") || accion.contains("Cambiar")) {
            if (modulo != null && modulo.contains("Configura")) return "cog.svg";
            if (accion.contains("Contraseña") || accion.contains("password")) return "key.svg";
            return "edit.svg";
        }
        if (accion.contains("Eliminar") || accion.contains("Anular") || accion.contains("Borrar")) return "trash.svg";
        if (accion.contains("Venta")) return "dollar-sign.svg";
        if (accion.contains("Login") || accion.contains("Inicio de Sesión")) return "key.svg";
        if (accion.contains("Cerrar Sesión") || accion.contains("Cerrar")) return "sign-out.svg";
        if (accion.contains("Guardar") || accion.contains("Registrar") || accion.contains("Actualizar")) return "save.svg";
        if (accion.contains("Exportar") || accion.contains("Descargar")) return "download.svg";
        return "alert-triangle.svg";
    }

    private Color getColorForAccion(String accion) {
        if (accion == null) return new Color(200, 200, 200);
        if (accion.contains("Reabastec") || accion.contains("Compra") || accion.contains("Entrada")) return new Color(251, 146, 60);
        if (accion.contains("Crear") || accion.contains("Agregar") || accion.contains("Nuevo")) return new Color(16, 185, 129);
        if (accion.contains("Editar") || accion.contains("Cambiar") || accion.contains("Contraseña")) return new Color(59, 130, 246);
        if (accion.contains("Eliminar") || accion.contains("Anular") || accion.contains("Borrar")) return new Color(239, 68, 68);
        if (accion.contains("Venta")) return new Color(245, 158, 11);
        if (accion.contains("Login") || accion.contains("Inicio de Sesión")) return new Color(56, 189, 248);
        if (accion.contains("Cerrar Sesión") || accion.contains("Cerrar")) return new Color(239, 68, 68);
        return new Color(200, 200, 200);
    }

    private Color getColorForModulo(String modulo) {
        if (modulo == null) return new Color(200, 200, 200);
        switch (modulo) {
            case "Login": return new Color(56, 189, 248);
            case "Ventas": return new Color(245, 158, 11);
            case "Clientes": return new Color(34, 211, 238);
            case "Inventario": return new Color(99, 102, 241);
            case "Usuarios": return new Color(236, 72, 153);
            case "Configuracion": return new Color(156, 163, 175);
            default: return new Color(200, 200, 200);
        }
    }

    private void cargar() {
        buscar();
    }

    private void buscar() {
        String texto = campoBuscar.getText().trim().toLowerCase();
        String modulo = (String) cbFiltro.getSelectedItem();
        String accion = (String) cbAccion.getSelectedItem();
        Date fechaIni = chkFecha != null && chkFecha.isSelected() ? (Date) spFechaIni.getValue() : null;
        Date fechaFin = chkFecha != null && chkFecha.isSelected() ? (Date) spFechaFin.getValue() : null;

        modelo.setRowCount(0);
        try {
            List<Bitacora> lista = dao.listarTodas();
            for (Bitacora b : lista) {
                if (!"Todos".equals(modulo) && modulo != null && !modulo.equals(b.getModulo())) continue;
                if (!"Todas las acciones".equals(accion) && accion != null && !accionAplica(b.getAccion(), accion)) continue;
                if (fechaIni != null && b.getFecha() != null && b.getFecha().before(fechaIni)) continue;
                if (fechaFin != null && b.getFecha() != null && b.getFecha().after(fechaFin)) continue;
                if (!texto.isEmpty() && !filaCoincide(b, texto)) continue;

                modelo.addRow(new Object[]{
                    "",
                    b.getId(),
                    b.getFecha() != null ? sdf.format(b.getFecha()) : "",
                    b.getUsuarioNombre() != null ? b.getUsuarioNombre() : "",
                    b.getModulo() != null ? b.getModulo() : "",
                    b.getAccion() != null ? b.getAccion() : "",
                    b.getDetalle() != null ? b.getDetalle() : ""
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
        actualizarPie();
    }

    private void actualizarPie() {
        if (lblPie != null) lblPie.setText("Registros: " + modelo.getRowCount());
    }

    private String[] accionesDeModulo(String modulo) {
        switch (modulo) {
            case "Ventas":
                return new String[]{"Todas las acciones", "Registrar Venta", "Anular", "Editar"};
            case "Clientes":
                return new String[]{"Todas las acciones", "Crear", "Editar", "Eliminar"};
            case "Inventario":
                return new String[]{"Todas las acciones", "Crear", "Editar", "Eliminar"};
            case "Usuarios":
                return new String[]{"Todas las acciones", "Crear", "Editar", "Eliminar"};
            case "Configuracion":
                return new String[]{"Todas las acciones", "Editar"};
            case "Login":
                return new String[]{"Todas las acciones", "Login", "Cerrar Sesión"};
            default:
                return new String[]{"Todas las acciones", "Crear", "Registrar Venta", "Anular", "Editar", "Eliminar", "Login", "Exportar"};
        }
    }

    private void actualizarAccionesPorModulo() {
        String modulo = (String) cbFiltro.getSelectedItem();
        if (modulo == null) return;
        String seleccionActual = (String) cbAccion.getSelectedItem();
        cbAccion.setModel(new DefaultComboBoxModel<>(accionesDeModulo(modulo)));
        if (seleccionActual != null && contieneEn(cbAccion, seleccionActual)) {
            cbAccion.setSelectedItem(seleccionActual);
        } else {
            cbAccion.setSelectedIndex(0);
        }
    }

    private boolean contieneEn(JComboBox<String> combo, String valor) {
        DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) combo.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            if (model.getElementAt(i).equals(valor)) return true;
        }
        return false;
    }

    private boolean accionAplica(String accionReal, String filtro) {
        if (accionReal == null) return false;
        String n = accionReal.toLowerCase();
        switch (filtro) {
            case "Registrar Venta": return n.contains("venta") || n.contains("registrada");
            case "Anular": return n.contains("anul");
            case "Crear": return n.contains("crear") || n.contains("agregar") || n.contains("nuevo");
            case "Editar": return n.contains("editar") || n.contains("cambiar") || n.contains("actualiz");
            case "Eliminar": return n.contains("eliminar") || n.contains("borrar");
            case "Login": return n.contains("login") || n.contains("inicio") || n.contains("cerrar sesión") || n.contains("contraseña") || n.contains("password");
            case "Cerrar Sesión": return n.contains("cerrar sesión");
            case "Exportar": return n.contains("export") || n.contains("descargar");
            default: return true;
        }
    }

    private boolean filaCoincide(Bitacora b, String texto) {
        if (String.valueOf(b.getId()).contains(texto)) return true;
        if (b.getFecha() != null && sdf.format(b.getFecha()).toLowerCase().contains(texto)) return true;
        if (b.getUsuarioNombre() != null && b.getUsuarioNombre().toLowerCase().contains(texto)) return true;
        if (b.getModulo() != null && b.getModulo().toLowerCase().contains(texto)) return true;
        if (b.getAccion() != null && b.getAccion().toLowerCase().contains(texto)) return true;
        if (b.getDetalle() != null && b.getDetalle().toLowerCase().contains(texto)) return true;
        return false;
    }
}
