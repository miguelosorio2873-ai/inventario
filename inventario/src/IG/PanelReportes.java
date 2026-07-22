package IG;

import DAO.*;
import Modelo.*;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class PanelReportes extends JPanel {

    private JComboBox<String> comboTipo;
    private JPanel panelColumnas;
    private JPanel panelFiltros;
    private JCheckBox chkFecha;
    private JSpinner spFechaInicio;
    private JSpinner spFechaFin;
    private JTable tablaPreview;
    private DefaultTableModel modeloPreview;
    private JLabel lblRegistros;

    private final Map<String, String[]> columnasPorTipo = new LinkedHashMap<>();
    private final Map<String, Set<String>> columnasSeleccionadas = new HashMap<>();
    private List<String> columnasActuales = new ArrayList<>();
    private List<Object[]> datosActuales = new ArrayList<>();
    private List<Object[]> datosCompletos = new ArrayList<>();
    private JTextField txtBuscar;
    private JComboBox<String> comboFiltroEspecifico;
    private JPanel panelFiltroEspecifico;
    private final Map<String, String[]> filtrosPorTipo = new LinkedHashMap<>();

    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");

    public PanelReportes() {
        setBackground(new Color(24, 24, 27));
        setLayout(new BorderLayout(0, 15));
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        initColumnas();

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel titulo = new JLabel("Generador de Reportes");
        try {
            com.formdev.flatlaf.extras.FlatSVGIcon icon = new com.formdev.flatlaf.extras.FlatSVGIcon(getClass().getResource("/IMG/chart-pie.svg")).derive(24, 24);
            icon.setColorFilter(new com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter(c -> Color.WHITE));
            titulo.setIcon(icon);
            titulo.setIconTextGap(10);
        } catch (Exception e) {}
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titulo.setForeground(Color.WHITE);
        header.add(titulo, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // Panel izquierdo de configuracion
        JPanel panelConfig = new JPanel(new BorderLayout(0, 10));
        panelConfig.setBackground(new Color(39, 39, 42));
        panelConfig.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(63, 63, 70), 1, true),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        panelConfig.setPreferredSize(new Dimension(300, 0));

        // --- Seleccion de tipo ---
        JPanel panelTipo = new JPanel(new BorderLayout(5, 5));
        panelTipo.setOpaque(false);
        JLabel lblTipo = new JLabel("Tipo de Reporte");
        lblTipo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTipo.setForeground(new Color(180, 180, 180));
        comboTipo = new JComboBox<>(columnasPorTipo.keySet().toArray(new String[0]));
        comboTipo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        comboTipo.setBackground(new Color(55, 55, 55));
        comboTipo.setForeground(Color.WHITE);
        comboTipo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(63, 63, 70)),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        comboTipo.addActionListener(e -> actualizarColumnas());
        panelTipo.add(lblTipo, BorderLayout.NORTH);
        panelTipo.add(comboTipo, BorderLayout.CENTER);
        panelConfig.add(panelTipo, BorderLayout.NORTH);

        // --- Panel scrollable de columnas y filtros ---
        JPanel panelScrollContent = new JPanel();
        panelScrollContent.setLayout(new BoxLayout(panelScrollContent, BoxLayout.Y_AXIS));
        panelScrollContent.setOpaque(false);

        // Columnas
        JLabel lblCols = new JLabel("Columnas a incluir");
        lblCols.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblCols.setForeground(new Color(180, 180, 180));
        lblCols.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblCols.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));
        panelScrollContent.add(lblCols);

        panelColumnas = new JPanel();
        panelColumnas.setLayout(new BoxLayout(panelColumnas, BoxLayout.Y_AXIS));
        panelColumnas.setOpaque(false);
        panelColumnas.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelScrollContent.add(panelColumnas);

        // Filtro especifico por tipo
        panelFiltroEspecifico = new JPanel();
        panelFiltroEspecifico.setLayout(new BoxLayout(panelFiltroEspecifico, BoxLayout.Y_AXIS));
        panelFiltroEspecifico.setOpaque(false);
        panelFiltroEspecifico.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelFiltroEspecifico.setVisible(false);
        panelScrollContent.add(panelFiltroEspecifico);

        // Filtros de fecha
        panelFiltros = new JPanel();
        panelFiltros.setLayout(new BoxLayout(panelFiltros, BoxLayout.Y_AXIS));
        panelFiltros.setOpaque(false);
        panelFiltros.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelFiltros.setVisible(false);
        panelScrollContent.add(panelFiltros);

        JScrollPane scrollConfig = new JScrollPane(panelScrollContent);
        scrollConfig.setBorder(BorderFactory.createEmptyBorder());
        scrollConfig.getViewport().setBackground(new Color(39, 39, 42));
        scrollConfig.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        panelConfig.add(scrollConfig, BorderLayout.CENTER);

        // --- Botones ---
        JPanel panelBotones = new JPanel(new GridLayout(2, 1, 5, 5));
        panelBotones.setOpaque(false);

        JButton btnGenerar = PanelProductos.crearBoton("📊 Generar Reporte", new Color(16, 185, 129));
        btnGenerar.addActionListener(e -> generarReporte());

        JButton btnExportar = PanelProductos.crearBoton("📤 Exportar a Excel", new Color(59, 130, 246));
        btnExportar.addActionListener(e -> exportarExcel());

        panelBotones.add(btnGenerar);
        panelBotones.add(btnExportar);
        panelConfig.add(panelBotones, BorderLayout.SOUTH);

        add(panelConfig, BorderLayout.WEST);

        // --- Tabla preview ---
        modeloPreview = new DefaultTableModel(new Object[0][0], new String[]{"Seleccione un tipo y genere un reporte"}) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaPreview = PanelProductos.crearTabla(modeloPreview);
        JScrollPane scrollTabla = new JScrollPane(tablaPreview);
        scrollTabla.setBorder(BorderFactory.createLineBorder(new Color(63, 63, 70), 1, true));
        scrollTabla.getViewport().setBackground(new Color(39, 39, 42));

        // Barra de busqueda/filtrado de texto
        txtBuscar = new JTextField(20);
        txtBuscar.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtBuscar.setBackground(new Color(39, 39, 42));
        txtBuscar.setForeground(Color.WHITE);
        txtBuscar.setCaretColor(new Color(16, 185, 129));
        txtBuscar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(63, 63, 70)),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        txtBuscar.putClientProperty("JTextField.placeholderText", "Filtrar resultados...");
        txtBuscar.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { aplicarFiltroTexto(); }
        });

        lblRegistros = new JLabel("0 registros");
        lblRegistros.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblRegistros.setForeground(new Color(140, 140, 140));
        lblRegistros.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));

        JPanel panelTablaTop = new JPanel(new BorderLayout(5, 0));
        panelTablaTop.setOpaque(false);
        panelTablaTop.add(txtBuscar, BorderLayout.CENTER);
        panelTablaTop.add(lblRegistros, BorderLayout.EAST);

        JPanel panelTabla = new JPanel(new BorderLayout(0, 5));
        panelTabla.setOpaque(false);
        panelTabla.add(panelTablaTop, BorderLayout.NORTH);
        panelTabla.add(scrollTabla, BorderLayout.CENTER);

        add(panelTabla, BorderLayout.CENTER);

        // Inicializar columnas para el primer tipo
        actualizarColumnas();
    }

    private void initColumnas() {
        columnasPorTipo.put("Productos", new String[]{"ID", "SKU", "Nombre", "Descripcion", "Categoria", "Precio Venta", "Costo Promedio", "Stock Minimo", "Stock Actual", "Estado"});
        columnasPorTipo.put("Categorias", new String[]{"ID", "Nombre", "Descripcion"});
        columnasPorTipo.put("Clientes", new String[]{"ID", "Cedula", "Nombre", "Correo", "Telefono"});
        columnasPorTipo.put("Proveedores", new String[]{"ID", "Nombre Empresa", "NIT/Cedula", "Telefono", "Direccion", "Correo", "Contacto"});
        columnasPorTipo.put("Movimientos de Inventario", new String[]{"ID", "Producto", "Proveedor", "Precio", "Precio Balance", "Cantidad", "Tipo Movimiento", "Fecha", "Motivo"});
        columnasPorTipo.put("Facturas", new String[]{"ID", "N. Factura", "Cliente", "Fecha", "Metodo Pago", "Subtotal", "Impuestos", "Total", "Estado"});
        columnasPorTipo.put("Usuarios", new String[]{"ID", "Nombre", "Email", "Rol"});

        for (String tipo : columnasPorTipo.keySet()) {
            columnasSeleccionadas.put(tipo, new LinkedHashSet<>(Arrays.asList(columnasPorTipo.get(tipo))));
        }

        filtrosPorTipo.put("Productos", new String[]{"Todos", "Activo", "Inactivo"});
        filtrosPorTipo.put("Movimientos de Inventario", new String[]{"Todos", "Entrada", "Salida", "Ajuste"});
        filtrosPorTipo.put("Facturas", new String[]{"Todos", "Pagada", "Pendiente", "Anulada"});
    }

    private void actualizarColumnas() {
        String tipo = (String) comboTipo.getSelectedItem();
        if (tipo == null) return;
        String[] cols = columnasPorTipo.get(tipo);
        Set<String> seleccionadas = columnasSeleccionadas.get(tipo);

        // Filtro especifico por tipo
        panelFiltroEspecifico.removeAll();
        if (filtrosPorTipo.containsKey(tipo)) {
            JLabel lblFiltro = new JLabel("Filtrar por");
            lblFiltro.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblFiltro.setForeground(new Color(180, 180, 180));
            lblFiltro.setAlignmentX(Component.LEFT_ALIGNMENT);
            lblFiltro.setBorder(BorderFactory.createEmptyBorder(15, 0, 5, 0));
            panelFiltroEspecifico.add(lblFiltro);

            comboFiltroEspecifico = new JComboBox<>(filtrosPorTipo.get(tipo));
            comboFiltroEspecifico.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            comboFiltroEspecifico.setBackground(new Color(55, 55, 55));
            comboFiltroEspecifico.setForeground(Color.WHITE);
            comboFiltroEspecifico.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(63, 63, 70)),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));
            comboFiltroEspecifico.setAlignmentX(Component.LEFT_ALIGNMENT);
            comboFiltroEspecifico.setMaximumSize(new Dimension(250, 30));
            comboFiltroEspecifico.addActionListener(e -> {
                aplicarFiltroEspecifico();
                aplicarFiltroTexto();
            });
            panelFiltroEspecifico.add(comboFiltroEspecifico);
            panelFiltroEspecifico.setVisible(true);
        } else {
            panelFiltroEspecifico.setVisible(false);
        }
        panelFiltroEspecifico.revalidate();
        panelFiltroEspecifico.repaint();

        panelColumnas.removeAll();
        for (String col : cols) {
            JCheckBox chk = new JCheckBox(col);
            chk.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            chk.setForeground(Color.WHITE);
            chk.setBackground(new Color(39, 39, 42));
            chk.setSelected(seleccionadas.contains(col));
            chk.setAlignmentX(Component.LEFT_ALIGNMENT);
            chk.addActionListener(e -> {
                if (chk.isSelected()) seleccionadas.add(col);
                else seleccionadas.remove(col);
            });
            panelColumnas.add(chk);
        }
        panelColumnas.revalidate();
        panelColumnas.repaint();

        // Mostrar/ocultar filtros de fecha
        boolean tieneFecha = "Movimientos de Inventario".equals(tipo) || "Facturas".equals(tipo);
        panelFiltros.removeAll();
        if (tieneFecha) {
            JLabel lblFiltro = new JLabel("Filtrar por fecha");
            lblFiltro.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblFiltro.setForeground(new Color(180, 180, 180));
            lblFiltro.setAlignmentX(Component.LEFT_ALIGNMENT);
            lblFiltro.setBorder(BorderFactory.createEmptyBorder(15, 0, 5, 0));
            panelFiltros.add(lblFiltro);

            chkFecha = new JCheckBox("Habilitar filtro de rango");
            chkFecha.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            chkFecha.setForeground(Color.WHITE);
            chkFecha.setBackground(new Color(39, 39, 42));
            chkFecha.setAlignmentX(Component.LEFT_ALIGNMENT);
            panelFiltros.add(chkFecha);

            SpinnerDateModel modelIni = new SpinnerDateModel();
            spFechaInicio = new JSpinner(modelIni);
            spFechaInicio.setEditor(new JSpinner.DateEditor(spFechaInicio, "yyyy-MM-dd"));
            spFechaInicio.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            spFechaInicio.setAlignmentX(Component.LEFT_ALIGNMENT);
            spFechaInicio.setMaximumSize(new Dimension(250, 30));

            SpinnerDateModel modelFin = new SpinnerDateModel();
            spFechaFin = new JSpinner(modelFin);
            spFechaFin.setEditor(new JSpinner.DateEditor(spFechaFin, "yyyy-MM-dd"));
            spFechaFin.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            spFechaFin.setAlignmentX(Component.LEFT_ALIGNMENT);
            spFechaFin.setMaximumSize(new Dimension(250, 30));

            JPanel panelRango = new JPanel();
            panelRango.setLayout(new BoxLayout(panelRango, BoxLayout.Y_AXIS));
            panelRango.setOpaque(false);
            panelRango.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel lblIni = new JLabel("Fecha inicio:");
            lblIni.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblIni.setForeground(new Color(160, 160, 160));
            lblIni.setAlignmentX(Component.LEFT_ALIGNMENT);
            panelRango.add(lblIni);
            panelRango.add(spFechaInicio);
            panelRango.add(Box.createRigidArea(new Dimension(0, 5)));
            JLabel lblFin = new JLabel("Fecha fin:");
            lblFin.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblFin.setForeground(new Color(160, 160, 160));
            lblFin.setAlignmentX(Component.LEFT_ALIGNMENT);
            panelRango.add(lblFin);
            panelRango.add(spFechaFin);

            chkFecha.addActionListener(e -> {
                spFechaInicio.setEnabled(chkFecha.isSelected());
                spFechaFin.setEnabled(chkFecha.isSelected());
            });
            spFechaInicio.setEnabled(false);
            spFechaFin.setEnabled(false);

            panelFiltros.add(panelRango);
            panelFiltros.setVisible(true);
        } else {
            panelFiltros.setVisible(false);
        }
        panelFiltros.revalidate();
        panelFiltros.repaint();
    }

    private void generarReporte() {
        String tipo = (String) comboTipo.getSelectedItem();
        if (tipo == null) return;
        Set<String> seleccionadas = columnasSeleccionadas.get(tipo);
        if (seleccionadas.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione al menos una columna.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        columnasActuales = new ArrayList<>(seleccionadas);
        datosCompletos = new ArrayList<>();
        datosActuales = new ArrayList<>();

        try {
            switch (tipo) {
                case "Productos":
                    cargarProductos(); break;
                case "Categorias":
                    cargarCategorias(); break;
                case "Clientes":
                    cargarClientes(); break;
                case "Proveedores":
                    cargarProveedores(); break;
                case "Movimientos de Inventario":
                    cargarMovimientos(); break;
                case "Facturas":
                    cargarFacturas(); break;
                case "Usuarios":
                    cargarUsuarios(); break;
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar datos:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            return;
        }

        // Aplicar filtro especifico (combo) y luego filtro de texto
        aplicarFiltroEspecifico();
        aplicarFiltroTexto();
    }

    private void aplicarFiltroEspecifico() {
        String tipo = (String) comboTipo.getSelectedItem();
        if (tipo == null || !filtrosPorTipo.containsKey(tipo) || comboFiltroEspecifico == null) {
            datosActuales = new ArrayList<>(datosCompletos);
            return;
        }
        String valorFiltro = (String) comboFiltroEspecifico.getSelectedItem();
        if ("Todos".equals(valorFiltro) || valorFiltro == null) {
            datosActuales = new ArrayList<>(datosCompletos);
            return;
        }
        datosActuales = new ArrayList<>();
        for (Object[] fila : datosCompletos) {
            for (Object val : fila) {
                if (val != null && val.toString().equalsIgnoreCase(valorFiltro)) {
                    datosActuales.add(fila);
                    break;
                }
            }
        }
    }

    private void aplicarFiltroTexto() {
        String texto = txtBuscar.getText().trim().toLowerCase();
        modeloPreview.setColumnIdentifiers(columnasActuales.toArray());
        modeloPreview.setRowCount(0);
        int count = 0;
        for (Object[] fila : datosActuales) {
            if (texto.isEmpty()) {
                modeloPreview.addRow(fila);
                count++;
            } else {
                boolean match = false;
                for (Object val : fila) {
                    if (val != null && val.toString().toLowerCase().contains(texto)) {
                        match = true;
                        break;
                    }
                }
                if (match) {
                    modeloPreview.addRow(fila);
                    count++;
                }
            }
        }
        lblRegistros.setText(count + " de " + datosActuales.size() + " registro(s)");
    }

    private Object[] extraerFila(Map<String, Object> valores, List<String> cols) {
        Object[] fila = new Object[cols.size()];
        for (int i = 0; i < cols.size(); i++) {
            fila[i] = valores.getOrDefault(cols.get(i), "");
        }
        return fila;
    }

    private void cargarProductos() throws SQLException {
        ProductoDAO dao = new ProductoDAO();
        List<Producto> lista = dao.listarTodosConCategoria();
        for (Producto p : lista) {
            Map<String, Object> v = new LinkedHashMap<>();
            v.put("ID", p.getId());
            v.put("SKU", p.getSku() != null ? p.getSku() : "");
            v.put("Nombre", p.getNombre());
            v.put("Descripcion", p.getDescripcion() != null ? p.getDescripcion() : "");
            v.put("Categoria", p.getCategoriaNombre() != null ? p.getCategoriaNombre() : "");
            v.put("Precio Venta", String.format("$%.2f", p.getPrecioVenta()));
            v.put("Costo Promedio", String.format("$%.2f", p.getCostoPromedio()));
            v.put("Stock Minimo", String.format("%.0f", p.getStockMinimo()));
            v.put("Stock Actual", String.format("%.0f", p.getStockActual()));
            v.put("Estado", p.isState() ? "Activo" : "Inactivo");
            datosCompletos.add(extraerFila(v, columnasActuales));
        }
    }

    private void cargarCategorias() throws SQLException {
        CategoriaDAO dao = new CategoriaDAO();
        List<Categoria> lista = dao.listarTodas();
        for (Categoria c : lista) {
            Map<String, Object> v = new LinkedHashMap<>();
            v.put("ID", c.getId());
            v.put("Nombre", c.getNombre());
            v.put("Descripcion", c.getDescripcion() != null ? c.getDescripcion() : "");
            datosCompletos.add(extraerFila(v, columnasActuales));
        }
    }

    private void cargarClientes() throws SQLException {
        ClienteDAO dao = new ClienteDAO();
        List<Cliente> lista = dao.listarTodos();
        for (Cliente c : lista) {
            Map<String, Object> v = new LinkedHashMap<>();
            v.put("ID", c.getId());
            v.put("Cedula", c.getCedula() != null ? c.getCedula() : "");
            v.put("Nombre", c.getNombre());
            v.put("Correo", c.getCorreo() != null ? c.getCorreo() : "");
            v.put("Telefono", c.getTelefono() != null ? c.getTelefono() : "");
            datosCompletos.add(extraerFila(v, columnasActuales));
        }
    }

    private void cargarProveedores() throws SQLException {
        ProveedorDAO dao = new ProveedorDAO();
        List<Proveedor> lista = dao.listarTodos();
        for (Proveedor p : lista) {
            Map<String, Object> v = new LinkedHashMap<>();
            v.put("ID", p.getId());
            v.put("Nombre Empresa", p.getNombreEmpresa());
            v.put("NIT/Cedula", p.getNitCedula() != null ? p.getNitCedula() : "");
            v.put("Telefono", p.getTelefono() != null ? p.getTelefono() : "");
            v.put("Direccion", p.getDireccion() != null ? p.getDireccion() : "");
            v.put("Correo", p.getCorreo() != null ? p.getCorreo() : "");
            v.put("Contacto", p.getNombreContacto() != null ? p.getNombreContacto() : "");
            datosCompletos.add(extraerFila(v, columnasActuales));
        }
    }

    private void cargarMovimientos() throws SQLException {
        InventarioDAO dao = new InventarioDAO();
        List<MovimientoInventario> lista = dao.listarMovimientosConNombres();
        Date fechaIni = chkFecha != null && chkFecha.isSelected() ? (Date) spFechaInicio.getValue() : null;
        Date fechaFin = chkFecha != null && chkFecha.isSelected() ? (Date) spFechaFin.getValue() : null;

        for (MovimientoInventario m : lista) {
            if (fechaIni != null && m.getFechaMovimiento() != null && m.getFechaMovimiento().before(fechaIni)) continue;
            if (fechaFin != null && m.getFechaMovimiento() != null && m.getFechaMovimiento().after(fechaFin)) continue;
            Map<String, Object> v = new LinkedHashMap<>();
            v.put("ID", m.getId());
            v.put("Producto", m.getProductoNombre() != null ? m.getProductoNombre() : "");
            v.put("Proveedor", m.getProveedorNombre() != null ? m.getProveedorNombre() : "");
            v.put("Precio", String.format("$%.2f", m.getPrecio()));
            v.put("Precio Balance", String.format("$%.2f", m.getPrecioBalance()));
            v.put("Cantidad", String.format("%.0f", m.getCantidad()));
            v.put("Tipo Movimiento", m.getTipoMovimiento() != null ? m.getTipoMovimiento() : "");
            v.put("Fecha", m.getFechaMovimiento() != null ? SDF.format(m.getFechaMovimiento()) : "");
            v.put("Motivo", m.getMotivo() != null ? m.getMotivo() : "");
            datosCompletos.add(extraerFila(v, columnasActuales));
        }
    }

    private void cargarFacturas() throws SQLException {
        FacturaDAO dao = new FacturaDAO();
        List<Factura> lista = dao.listarTodasConCliente();
        Date fechaIni = chkFecha != null && chkFecha.isSelected() ? (Date) spFechaInicio.getValue() : null;
        Date fechaFin = chkFecha != null && chkFecha.isSelected() ? (Date) spFechaFin.getValue() : null;

        for (Factura f : lista) {
            if (fechaIni != null && f.getFechaEmision() != null && f.getFechaEmision().before(fechaIni)) continue;
            if (fechaFin != null && f.getFechaEmision() != null && f.getFechaEmision().after(fechaFin)) continue;
            Map<String, Object> v = new LinkedHashMap<>();
            v.put("ID", f.getId());
            v.put("N. Factura", f.getNumeroFactura() != null ? f.getNumeroFactura() : "");
            v.put("Cliente", f.getClienteNombre() != null ? f.getClienteNombre() : "");
            v.put("Fecha", f.getFechaEmision() != null ? SDF.format(f.getFechaEmision()) : "");
            v.put("Metodo Pago", f.getMetodoPago() != null ? f.getMetodoPago() : "");
            v.put("Subtotal", String.format("$%.2f", f.getSubtotal()));
            v.put("Impuestos", String.format("$%.2f", f.getImpuestos()));
            v.put("Total", String.format("$%.2f", f.getTotal()));
            v.put("Estado", f.getEstado() != null ? f.getEstado() : "");
            datosCompletos.add(extraerFila(v, columnasActuales));
        }
    }

    private void cargarUsuarios() throws SQLException {
        UsuarioDAO dao = new UsuarioDAO();
        List<Usuario> lista = dao.listarTodos();
        for (Usuario u : lista) {
            Map<String, Object> v = new LinkedHashMap<>();
            v.put("ID", u.getId());
            v.put("Nombre", u.getNombre());
            v.put("Email", u.getEmail());
            v.put("Rol", u.getRol() != null ? u.getRol() : "");
            datosCompletos.add(extraerFila(v, columnasActuales));
        }
    }

    private void exportarExcel() {
        if (modeloPreview.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Genere un reporte primero.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String tipo = (String) comboTipo.getSelectedItem();
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar Reporte");
        fileChooser.setSelectedFile(new File("Reporte_" + tipo.replace(" ", "_") + ".xlsx"));

        if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File dest = fileChooser.getSelectedFile();
        if (!dest.getName().endsWith(".xlsx")) {
            dest = new File(dest.getAbsolutePath() + ".xlsx");
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(tipo.length() > 31 ? tipo.substring(0, 31) : tipo);

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            org.apache.poi.ss.usermodel.Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < modeloPreview.getColumnCount(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(modeloPreview.getColumnName(i));
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (int r = 0; r < modeloPreview.getRowCount(); r++) {
                Row row = sheet.createRow(rowNum++);
                for (int i = 0; i < modeloPreview.getColumnCount(); i++) {
                    Cell cell = row.createCell(i);
                    Object val = modeloPreview.getValueAt(r, i);
                    if (val != null) {
                        String sval = val.toString();
                        if (sval.startsWith("$")) {
                            try { cell.setCellValue(Double.parseDouble(sval.replace("$", "").replace(",", ""))); }
                            catch (NumberFormatException ex) { cell.setCellValue(sval); }
                        } else {
                            try { cell.setCellValue(Long.parseLong(sval)); }
                            catch (NumberFormatException ex) { cell.setCellValue(sval); }
                        }
                    }
                }
            }

            for (int i = 0; i < modeloPreview.getColumnCount(); i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream outputStream = new FileOutputStream(dest)) {
                workbook.write(outputStream);
            }
            JOptionPane.showMessageDialog(this, "Reporte exportado con exito a:\n" + dest.getAbsolutePath());
            try {
                Desktop.getDesktop().open(dest);
            } catch (Exception ignored) {}

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al exportar:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}
