package IG;

import DAO.ProductoDAO;
import DAO.ClienteDAO;
import Modelo.Producto;
import Utils.UI;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class PanelInicio extends JPanel {

    private final Color COLOR_FONDO = new Color(24, 24, 27);
    private final Color COLOR_CARD = new Color(39, 39, 42);

    public PanelInicio() {
        setBackground(COLOR_FONDO);
        setLayout(new BorderLayout());

        JPanel container = new JPanel(new BorderLayout(0, 20));
        container.setBackground(COLOR_FONDO);
        container.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        // Header
        JLabel titulo = new JLabel("Dashboard");
        titulo.setFont(Utils.UI.TITULO);
        titulo.setForeground(Color.WHITE);

        JLabel subtitulo = new JLabel("Resumen general del sistema de inventario");
        subtitulo.setFont(Utils.UI.NOTA);
        subtitulo.setForeground(new Color(140, 140, 140));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(titulo);
        header.add(Box.createRigidArea(new Dimension(0, 5)));
        header.add(subtitulo);
        container.add(header, BorderLayout.NORTH);

        // Cards
        JPanel cardsPanel = new JPanel(new GridLayout(1, 5, 12, 15));
        cardsPanel.setOpaque(false);

        int totalProductos = 0, totalClientes = 0;
        double ventasHoy = 0, prodHoy = 0, ganHoy = 0;

        try {
            ProductoDAO pDao = new ProductoDAO();
            ClienteDAO cDao = new ClienteDAO();
            DAO.DashboardDAO ddao = new DAO.DashboardDAO();
            totalProductos = pDao.contarTotal();
            totalClientes = cDao.contarTotal();
            ventasHoy = ddao.ventasHoy();
            prodHoy = ddao.productosVendidosHoy();
            ganHoy = ddao.gananciaHoy();
        } catch (SQLException e) {
            System.err.println("Error cargando stats: " + e.getMessage());
        }

        cardsPanel.add(crearTarjeta("box.svg", "Total Productos", String.valueOf(totalProductos), new Color(59, 130, 246)));
        cardsPanel.add(crearTarjeta("users.svg", "Clientes Atendidos", String.valueOf(totalClientes), new Color(139, 92, 246)));
        cardsPanel.add(crearTarjeta("dollar-sign.svg", "Ventas de Hoy", String.format("$%.2f", ventasHoy), new Color(16, 185, 129)));
        cardsPanel.add(crearTarjeta("box.svg", "Prod. Vendidos Hoy", String.format("%.0f", prodHoy), new Color(245, 158, 11)));
        cardsPanel.add(crearTarjeta("chart-bar.svg", "Ganancias de Hoy", Utils.Formato.usd(ganHoy), new Color(34, 197, 94)));

        // Selector moderno de rango de fechas (Desde / Hasta)
        JPanel fechaCard = new JPanel(new BorderLayout(0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(new Color(16, 185, 129));
                g2.fillRoundRect(0, 0, 4, getHeight(), 4, 4);
                g2.dispose();
            }
        };
        fechaCard.setOpaque(false);
        fechaCard.setBorder(BorderFactory.createEmptyBorder(14, 22, 14, 22));

        JLabel fechaTitulo = new JLabel("Período de consulta");
        fechaTitulo.setFont(Utils.UI.TEXTO_NEGRITA);
        fechaTitulo.setForeground(Color.WHITE);
        try {
            com.formdev.flatlaf.extras.FlatSVGIcon icon = new com.formdev.flatlaf.extras.FlatSVGIcon(getClass().getResource("/IMG/file-invoice.svg"));
            icon.setColorFilter(new com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter(c -> new Color(16, 185, 129)));
            fechaTitulo.setIcon(icon);
        } catch (Exception e) {}

        CalendarPicker cpDesde = new CalendarPicker(primerDiaMes());
        CalendarPicker cpHasta = new CalendarPicker(new java.util.Date());

        JPanel fechaFiltros = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        fechaFiltros.setOpaque(false);
        JLabel lbDesde = new JLabel("Desde:");
        lbDesde.setForeground(new Color(170, 170, 170));
        JLabel lbHasta = new JLabel("Hasta:");
        lbHasta.setForeground(new Color(170, 170, 170));
        fechaFiltros.add(lbDesde);
        fechaFiltros.add(cpDesde);
        fechaFiltros.add(lbHasta);
        fechaFiltros.add(cpHasta);

        JButton btnExcel = PanelProductos.crearBoton("Exportar Excel", new Color(139, 92, 246));
        try {
            com.formdev.flatlaf.extras.FlatSVGIcon fi = new com.formdev.flatlaf.extras.FlatSVGIcon(getClass().getResource("/IMG/download.svg")).derive(14, 14);
            fi.setColorFilter(new com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter(c -> Color.WHITE));
            btnExcel.setIcon(fi);
        } catch (Exception e) {}
        fechaFiltros.add(btnExcel);
        btnExcel.addActionListener(e -> mostrarVistaPreviaVentas(cpDesde, cpHasta));

        fechaCard.add(fechaTitulo, BorderLayout.WEST);
        fechaCard.add(fechaFiltros, BorderLayout.EAST);

        // Top content (Cards + Selector de fecha)
        JPanel topContent = new JPanel(new BorderLayout(0, 15));
        topContent.setOpaque(false);
        topContent.add(cardsPanel, BorderLayout.NORTH);
        topContent.add(fechaCard, BorderLayout.SOUTH);

        // Main Center Panel
        JPanel mainCenter = new JPanel(new BorderLayout(0, 15));
        mainCenter.setOpaque(false);
        mainCenter.add(topContent, BorderLayout.NORTH);

        // Bottom content (Graficas)
        JPanel filaGraficas1 = new JPanel(new GridLayout(1, 2, 15, 15));
        filaGraficas1.setOpaque(false);
        filaGraficas1.add(crearPanelVentasSemana());      // Qué día se vende más en la semana
        filaGraficas1.add(crearPanelPerdidaDolar());      // Pérdida por cambio del dólar

        JPanel filaGraficas2 = new JPanel(new GridLayout(1, 2, 15, 15));
        filaGraficas2.setOpaque(false);
        filaGraficas2.add(crearPanelProductoMasVendido()); // Producto más vendido
        filaGraficas2.add(crearPanelGrafica());            // Top productos por stock

        JPanel contentBottom = new JPanel(new GridLayout(2, 1, 0, 15));
        contentBottom.setOpaque(false);
        contentBottom.add(filaGraficas1);
        contentBottom.add(filaGraficas2);

        mainCenter.add(contentBottom, BorderLayout.CENTER); // Se estirará

        container.add(mainCenter, BorderLayout.CENTER);

        // ScrollPane Global
        JScrollPane mainScroll = new JScrollPane(container);
        mainScroll.setBorder(null);
        mainScroll.getViewport().setBackground(COLOR_FONDO);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);
        // Ocultar barra horizontal
        mainScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        add(mainScroll, BorderLayout.CENTER);
    }

    private JPanel crearTarjeta(String icono, String titulo, String valor, Color accentColor) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                // Accent line
                g2.setColor(accentColor);
                g2.fillRoundRect(0, 0, 4, getHeight(), 4, 4);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BorderLayout(15, 5));
        card.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 15));
        card.setPreferredSize(new Dimension(220, 110));

        JLabel emojiLabel = new JLabel();
        try {
            com.formdev.flatlaf.extras.FlatSVGIcon svgIcon = new com.formdev.flatlaf.extras.FlatSVGIcon("IMG/" + icono);
            svgIcon.setColorFilter(new com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter(color -> accentColor));
            emojiLabel.setIcon(svgIcon);
        } catch (Exception e) {
            System.err.println("No se pudo cargar el icono: " + icono);
        }

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel tituloLabel = new JLabel(titulo);
        tituloLabel.setFont(Utils.UI.NOTA);
        tituloLabel.setForeground(new Color(140, 140, 140));

        JLabel valorLabel = new JLabel(valor);
        valorLabel.setFont(Utils.UI.TITULO);
        valorLabel.setForeground(Color.WHITE);

        textPanel.add(tituloLabel);
        textPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        textPanel.add(valorLabel);

        card.add(emojiLabel, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);

        return card;
    }

    /** Selector de fecha con combo boxes de Día/Mes/Año + botón de calendario. */
    private class CalendarPicker extends JPanel {
        private final java.util.Calendar fecha = java.util.Calendar.getInstance();
        private final JComboBox<String> cbDia;
        private final JComboBox<String> cbMes;
        private final JComboBox<String> cbAnio;
        private final String[] meses = {"Enero","Febrero","Marzo","Abril","Mayo","Junio",
            "Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre"};
        private java.util.function.Consumer<java.util.Date> onCambio;
        private boolean construyendo = true;

        CalendarPicker(java.util.Date inicial) {
            setOpaque(false);
            setLayout(new FlowLayout(FlowLayout.LEFT, 4, 0));

            int anioHoy = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
            cbDia = new JComboBox<>();
            cbMes = new JComboBox<>(meses);
            cbAnio = new JComboBox<>();
            for (int a = 1980; a <= anioHoy + 20; a++) cbAnio.addItem(String.valueOf(a));
            for (int d = 1; d <= 31; d++) cbDia.addItem(String.format("%02d", d));

            estilizarCombo(cbDia);
            estilizarCombo(cbMes);
            estilizarCombo(cbAnio);
            cbDia.setPreferredSize(new Dimension(58, 26));
            cbMes.setPreferredSize(new Dimension(116, 26));
            cbAnio.setPreferredSize(new Dimension(80, 26));

            // Cuadro que agrupa los combos de Día/Mes/Año (estilo TitledBorder de la app)
            JPanel cajaFechas = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
            cajaFechas.setOpaque(false);
            cajaFechas.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(90, 90, 95)),
                    "Fecha", 0, 0, Utils.UI.NOTA, new Color(170, 170, 170)));
            cajaFechas.add(cbDia);
            cajaFechas.add(cbMes);
            cajaFechas.add(cbAnio);

            add(cajaFechas);

            JButton btnCal = new JButton();
            btnCal.setFocusable(false);
            btnCal.setPreferredSize(new Dimension(30, 28));
            btnCal.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btnCal.setToolTipText("Abrir calendario");
            try {
                com.formdev.flatlaf.extras.FlatSVGIcon icon = new com.formdev.flatlaf.extras.FlatSVGIcon(getClass().getResource("/IMG/file-invoice.svg")).derive(16, 16);
                icon.setColorFilter(new com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter(c -> new Color(16, 185, 129)));
                btnCal.setIcon(icon);
            } catch (Exception e) {}
            btnCal.addActionListener(e -> mostrarPopup());
            add(btnCal);

            setDate(inicial);
            construyendo = false;

            cbDia.addActionListener(e -> alCambiar());
            cbMes.addActionListener(e -> alCambiar());
            cbAnio.addActionListener(e -> alCambiar());
        }

        private void estilizarCombo(JComboBox<String> cb) {
            cb.setFont(Utils.UI.TEXTO);
            cb.setBackground(new Color(45, 45, 48));
            cb.setForeground(Color.WHITE);
            cb.setFocusable(false);
        }

        java.util.Date getDate() { return fecha.getTime(); }

        void setDate(java.util.Date d) {
            fecha.setTime(d);
            sincronizarCombos();
        }

        void onDateChange(java.util.function.Consumer<java.util.Date> cb) { this.onCambio = cb; }

        private void sincronizarCombos() {
            cbDia.setSelectedIndex(fecha.get(java.util.Calendar.DAY_OF_MONTH) - 1);
            cbMes.setSelectedIndex(fecha.get(java.util.Calendar.MONTH));
            cbAnio.setSelectedItem(String.valueOf(fecha.get(java.util.Calendar.YEAR)));
        }

        private void alCambiar() {
            if (construyendo) return;
            int dia = cbDia.getSelectedIndex() + 1;
            int m = cbMes.getSelectedIndex();
            int anio = Integer.parseInt((String) cbAnio.getSelectedItem());
            fecha.set(java.util.Calendar.YEAR, anio);
            fecha.set(java.util.Calendar.MONTH, m);
            int max = fecha.getActualMaximum(java.util.Calendar.DAY_OF_MONTH);
            if (dia > max) {
                cbDia.setSelectedIndex(max - 1);
                dia = max;
            }
            fecha.set(java.util.Calendar.DAY_OF_MONTH, dia);
            normalizar(fecha);
            if (onCambio != null) onCambio.accept(getDate());
        }

        private void seleccionar(java.util.Calendar nueva, JPopupMenu pop) {
            fecha.setTime(nueva.getTime());
            sincronizarCombos();
            if (onCambio != null) onCambio.accept(getDate());
            pop.setVisible(false);
        }

        /** Abre un popup con un mini calendario mensual (estilo Windows). */
        private void mostrarPopup() {
            JPopupMenu pop = new JPopupMenu();
            pop.setBackground(new Color(39, 39, 42));
            pop.setBorder(BorderFactory.createLineBorder(new Color(70, 70, 75)));

            JPanel calPanel = new JPanel(new BorderLayout(8, 8));
            calPanel.setBackground(new Color(39, 39, 42));
            calPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

            JLabel mesLbl = new JLabel("", SwingConstants.CENTER);
            mesLbl.setFont(Utils.UI.TEXTO_NEGRITA);
            mesLbl.setForeground(Color.WHITE);

            JButton prev = botonNav("‹");
            JButton next = botonNav("›");
            JPanel nav = new JPanel(new BorderLayout());
            nav.setOpaque(false);
            nav.add(prev, BorderLayout.WEST);
            nav.add(mesLbl, BorderLayout.CENTER);
            nav.add(next, BorderLayout.EAST);
            calPanel.add(nav, BorderLayout.NORTH);

            java.util.Calendar vis = (java.util.Calendar) fecha.clone();
            vis.set(java.util.Calendar.DAY_OF_MONTH, 1);
            normalizar(vis);

            JPanel gridHolder = new JPanel(new BorderLayout());
            gridHolder.setOpaque(false);
            calPanel.add(gridHolder, BorderLayout.CENTER);

            Runnable dibujarDias = () -> {
                gridHolder.removeAll();
                gridHolder.add(construirGrid(vis, pop), BorderLayout.CENTER);
                mesLbl.setText(tituloMes(vis));
                gridHolder.revalidate();
                gridHolder.repaint();
                pop.pack();
            };

            prev.addActionListener(e -> { vis.add(java.util.Calendar.MONTH, -1); dibujarDias.run(); });
            next.addActionListener(e -> { vis.add(java.util.Calendar.MONTH, 1); dibujarDias.run(); });

            JButton btnHoy = new JButton("Hoy");
            btnHoy.setFont(Utils.UI.NOTA);
            btnHoy.setFocusable(false);
            btnHoy.setBackground(new Color(16, 185, 129));
            btnHoy.setForeground(Color.WHITE);
            btnHoy.setOpaque(true);
            btnHoy.addActionListener(e -> {
                java.util.Calendar h = java.util.Calendar.getInstance();
                seleccionar(h, pop);
            });

            JPanel sur = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            sur.setOpaque(false);
            sur.add(btnHoy);
            calPanel.add(sur, BorderLayout.SOUTH);

            pop.add(calPanel);
            dibujarDias.run();
            pop.show(this, 0, getHeight() + 4);
        }

        /** Construye la cuadrícula de días del mes `vis`. */
        private JPanel construirGrid(java.util.Calendar vis, JPopupMenu pop) {
            JPanel g = new JPanel(new GridLayout(7, 7, 2, 2));
            g.setOpaque(false);
            for (String d : new String[]{"L","M","X","J","V","S","D"}) {
                JLabel c = new JLabel(d, SwingConstants.CENTER);
                c.setFont(Utils.UI.NOTA);
                c.setForeground(new Color(140, 140, 140));
                g.add(c);
            }
            int offset = (vis.get(java.util.Calendar.DAY_OF_WEEK) + 5) % 7; // 0=lunes
            int diasMes = vis.getActualMaximum(java.util.Calendar.DAY_OF_MONTH);
            boolean mismoMes = (vis.get(java.util.Calendar.MONTH) == fecha.get(java.util.Calendar.MONTH)
                             && vis.get(java.util.Calendar.YEAR) == fecha.get(java.util.Calendar.YEAR));
            int selDia = fecha.get(java.util.Calendar.DAY_OF_MONTH);
            for (int i = 0; i < offset; i++) g.add(new JLabel(""));
            for (int d = 1; d <= diasMes; d++) {
                final int dia = d;
                JButton b = new JButton(String.valueOf(dia));
                b.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                b.setFocusable(false);
                b.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
                boolean seleccionado = mismoMes && dia == selDia;
                b.setBackground(seleccionado ? new Color(16, 185, 129) : new Color(49, 49, 53));
                b.setForeground(Color.WHITE);
                b.setOpaque(true);
                b.setToolTipText(null);
                b.addActionListener(e -> {
                    java.util.Calendar nueva = (java.util.Calendar) vis.clone();
                    nueva.set(java.util.Calendar.DAY_OF_MONTH, dia);
                    seleccionar(nueva, pop);
                });
                g.add(b);
            }
            return g;
        }

        private String tituloMes(java.util.Calendar vis) {
            String s = new java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.forLanguageTag("es")).format(vis.getTime());
            return s.substring(0, 1).toUpperCase(java.util.Locale.ROOT) + s.substring(1);
        }

        private void normalizar(java.util.Calendar c) {
            c.set(java.util.Calendar.HOUR_OF_DAY, 0);
            c.set(java.util.Calendar.MINUTE, 0);
            c.set(java.util.Calendar.SECOND, 0);
            c.set(java.util.Calendar.MILLISECOND, 0);
        }

        private JButton botonNav(String txt) {
            JButton b = new JButton(txt);
            b.setFont(Utils.UI.TEXTO_NEGRITA);
            b.setFocusable(false);
            b.setPreferredSize(new Dimension(28, 26));
            b.setBackground(new Color(49, 49, 53));
            b.setForeground(Color.WHITE);
            b.setOpaque(true);
            return b;
        }
    }

    /** Devuelve el primer día del mes actual (como java.util.Date). */
    private java.util.Date primerDiaMes() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        return cal.getTime();
    }

    /** Abre una vista previa del resumen de ventas del rango y permite exportarlo a Excel. */
    private void mostrarVistaPreviaVentas(CalendarPicker desde, CalendarPicker hasta) {
        String d = new java.text.SimpleDateFormat("yyyy-MM-dd").format(desde.getDate());
        String h = new java.text.SimpleDateFormat("yyyy-MM-dd").format(hasta.getDate());
        java.util.List<Map<String, Object>> filas;
        Map<String, Double> resumen;
        try {
            DAO.DashboardDAO ddao = new DAO.DashboardDAO();
            resumen = ddao.resumenVentas(d, h);
            filas = ddao.resumenVentasExcel(d, h);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al consultar las ventas:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String[] cols = {"N° Factura", "Fecha", "Método Pago", "Estado", "Productos", "Total (USD)"};
        Object[][] datos = new Object[filas.size()][6];
        for (int i = 0; i < filas.size(); i++) {
            Map<String, Object> f = filas.get(i);
            datos[i][0] = String.valueOf(f.get("numero"));
            datos[i][1] = String.valueOf(f.get("fecha"));
            datos[i][2] = String.valueOf(f.get("metodo"));
            datos[i][3] = String.valueOf(f.get("estado"));
            datos[i][4] = ((Number) f.get("productos")).doubleValue();
            datos[i][5] = ((Number) f.get("total")).doubleValue();
        }

        JTable tabla = new JTable(datos, cols);
        tabla.setFillsViewportHeight(true);
        tabla.setRowHeight(24);
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.setGridColor(new Color(70, 70, 75));
        tabla.setBackground(new Color(45, 45, 48));
        tabla.setForeground(Color.WHITE);
        tabla.setSelectionBackground(new Color(139, 92, 246));
        tabla.setSelectionForeground(Color.WHITE);
        int[] anchos = {90, 130, 120, 100, 90, 100};
        for (int i = 0; i < anchos.length; i++) tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);

        JPanel resumenPanel = new JPanel(new GridLayout(2, 3, 12, 8));
        resumenPanel.setOpaque(false);
        String[][] pares = {
            {"Ventas totales", String.format("$%.2f", resumen.getOrDefault("total", 0.0))},
            {"Ganancia", String.format("$%.2f", resumen.getOrDefault("ganancia", 0.0))},
            {"Productos vendidos", String.format("%.0f", resumen.getOrDefault("productos", 0.0))},
            {"Facturas emitidas", String.format("%.0f", resumen.getOrDefault("facturas", 0.0))},
            {"Ticket promedio", String.format("$%.2f", resumen.getOrDefault("ticket", 0.0))},
            {"Período", new java.text.SimpleDateFormat("dd/MM/yyyy").format(desde.getDate()) + " a " + new java.text.SimpleDateFormat("dd/MM/yyyy").format(hasta.getDate())}
        };
        for (String[] p : pares) {
            JLabel l = new JLabel(p[0] + ":  " + p[1]);
            l.setForeground(Color.WHITE);
            l.setFont(Utils.UI.TEXTO_NEGRITA);
            resumenPanel.add(l);
        }

        JButton btnExportar = PanelProductos.crearBoton("Exportar a Excel", new Color(139, 92, 246));
        try {
            com.formdev.flatlaf.extras.FlatSVGIcon fi = new com.formdev.flatlaf.extras.FlatSVGIcon(getClass().getResource("/IMG/download.svg")).derive(14, 14);
            fi.setColorFilter(new com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter(c -> Color.WHITE));
            btnExportar.setIcon(fi);
        } catch (Exception e) {}

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        bottom.setOpaque(false);
        bottom.add(btnExportar);

        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 10, 14));
        panel.add(resumenPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);

        JDialog dialogo = new JDialog(SwingUtilities.getWindowAncestor(this));
        dialogo.setTitle("Vista previa – Resumen de ventas de " + new java.text.SimpleDateFormat("dd/MM/yyyy").format(desde.getDate())
            + " a " + new java.text.SimpleDateFormat("dd/MM/yyyy").format(hasta.getDate()));
        dialogo.setModal(true);
        dialogo.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialogo.setContentPane(panel);
        dialogo.setSize(780, 540);
        dialogo.setLocationRelativeTo(this);

        btnExportar.addActionListener(e -> {
            exportarExcelResumen(resumen, filas, desde, hasta);
            dialogo.dispose();
        });

        dialogo.setVisible(true);
    }

    /** Escribe el resumen de ventas a un archivo Excel (.xlsx). */
    private void exportarExcelResumen(Map<String, Double> resumen, java.util.List<Map<String, Object>> filas, CalendarPicker desde, CalendarPicker hasta) {
        try {
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Exportar Resumen de Ventas a Excel");
            fc.setSelectedFile(new java.io.File("Resumen_Ventas.xlsx"));
            if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

            java.io.File dest = fc.getSelectedFile();
            if (!dest.getName().endsWith(".xlsx")) {
                dest = new java.io.File(dest.getAbsolutePath() + ".xlsx");
            }

            try (org.apache.poi.xssf.usermodel.XSSFWorkbook wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
                org.apache.poi.ss.usermodel.Sheet sheet = wb.createSheet("Resumen de Ventas");

                org.apache.poi.ss.usermodel.CellStyle titulo = wb.createCellStyle();
                org.apache.poi.ss.usermodel.Font fTitulo = wb.createFont();
                fTitulo.setBold(true);
                fTitulo.setFontHeightInPoints((short) 14);
                titulo.setFont(fTitulo);

                org.apache.poi.ss.usermodel.CellStyle negrita = wb.createCellStyle();
                org.apache.poi.ss.usermodel.Font fNeg = wb.createFont();
                fNeg.setBold(true);
                negrita.setFont(fNeg);

                org.apache.poi.ss.usermodel.CellStyle encabezado = wb.createCellStyle();
                encabezado.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.GREY_25_PERCENT.getIndex());
                encabezado.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
                org.apache.poi.ss.usermodel.Font fEnc = wb.createFont();
                fEnc.setBold(true);
                encabezado.setFont(fEnc);

                int r = 0;
                org.apache.poi.ss.usermodel.Row rTit = sheet.createRow(r++);
                org.apache.poi.ss.usermodel.Cell cTit = rTit.createCell(0);
                cTit.setCellValue("RESUMEN DE VENTAS");
                cTit.setCellStyle(titulo);

                org.apache.poi.ss.usermodel.Row rRng = sheet.createRow(r++);
                rRng.createCell(0).setCellValue("Período: " + new java.text.SimpleDateFormat("dd/MM/yyyy").format(desde.getDate())
                    + "  al  " + new java.text.SimpleDateFormat("dd/MM/yyyy").format(hasta.getDate()));

                String[][] pares = {
                    {"Ventas totales (USD)", String.format("%.2f", resumen.getOrDefault("total", 0.0))},
                    {"Ganancia (USD)", String.format("%.2f", resumen.getOrDefault("ganancia", 0.0))},
                    {"Productos vendidos", String.format("%.0f", resumen.getOrDefault("productos", 0.0))},
                    {"Facturas emitidas", String.format("%.0f", resumen.getOrDefault("facturas", 0.0))},
                    {"Ticket promedio (USD)", String.format("%.2f", resumen.getOrDefault("ticket", 0.0))}
                };
                for (String[] p : pares) {
                    org.apache.poi.ss.usermodel.Row rr = sheet.createRow(r++);
                    rr.createCell(0).setCellValue(p[0]);
                    org.apache.poi.ss.usermodel.Cell vc = rr.createCell(1);
                    vc.setCellValue(p[1]);
                    vc.setCellStyle(negrita);
                }

                r += 1;
                String[] cols = {"N° Factura", "Fecha", "Método Pago", "Estado", "Productos", "Total (USD)"};
                org.apache.poi.ss.usermodel.Row hr = sheet.createRow(r++);
                for (int i = 0; i < cols.length; i++) {
                    org.apache.poi.ss.usermodel.Cell cell = hr.createCell(i);
                    cell.setCellValue(cols[i]);
                    cell.setCellStyle(encabezado);
                }
                for (Map<String, Object> f : filas) {
                    org.apache.poi.ss.usermodel.Row row = sheet.createRow(r++);
                    row.createCell(0).setCellValue(String.valueOf(f.get("numero")));
                    row.createCell(1).setCellValue(String.valueOf(f.get("fecha")));
                    row.createCell(2).setCellValue(String.valueOf(f.get("metodo")));
                    row.createCell(3).setCellValue(String.valueOf(f.get("estado")));
                    row.createCell(4).setCellValue(((Number) f.get("productos")).doubleValue());
                    row.createCell(5).setCellValue(((Number) f.get("total")).doubleValue());
                }

                for (int i = 0; i < cols.length; i++) sheet.autoSizeColumn(i);

                try (java.io.FileOutputStream fos = new java.io.FileOutputStream(dest)) {
                    wb.write(fos);
                }
                JOptionPane.showMessageDialog(this, "Resumen exportado con éxito a:\n" + dest.getAbsolutePath());
                try {
                    java.awt.Desktop.getDesktop().open(dest);
                } catch (Exception ignored) {}
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al exportar:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }


    /** Panel base con fondo redondeado y título con acento. */
    private JPanel crearPanelGrafico(String titulo, String svgIcon, Color acentColor) {
        JPanel panel = new JPanel(new BorderLayout(0, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel tit = new JLabel(titulo);
        try {
            com.formdev.flatlaf.extras.FlatSVGIcon icon = new com.formdev.flatlaf.extras.FlatSVGIcon(getClass().getResource("/IMG/" + svgIcon));
            icon.setColorFilter(new com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter(color -> acentColor));
            tit.setIcon(icon);
        } catch (Exception e) {}
        tit.setFont(Utils.UI.TEXTO_NEGRITA);
        tit.setForeground(acentColor);
        panel.add(tit, BorderLayout.NORTH);
        return panel;
    }

    /** Ventas por día con selector de mes/año, como gráfica de línea (trending). */
    private JPanel crearPanelVentasSemana() {
        JPanel panel = new JPanel(new BorderLayout(0, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        int anioActual = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
        JComboBox<String> cbMes = new JComboBox<>();
        cbMes.addItem("Todo el año");
        String[] mesesN = {"Enero","Febrero","Marzo","Abril","Mayo","Junio","Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre"};
        for (String m : mesesN) cbMes.addItem(m);
        JComboBox<String> cbAnio = new JComboBox<>();
        for (int a = anioActual - 10; a <= anioActual; a++) cbAnio.addItem(String.valueOf(a));
        cbAnio.setSelectedItem(String.valueOf(anioActual));

        JLabel tit = new JLabel("Ventas por Día");
        try {
            com.formdev.flatlaf.extras.FlatSVGIcon icon = new com.formdev.flatlaf.extras.FlatSVGIcon(getClass().getResource("/IMG/chart-bar.svg"));
            icon.setColorFilter(new com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter(c -> new Color(139, 92, 246)));
            tit.setIcon(icon);
        } catch (Exception e) {}
        tit.setFont(Utils.UI.TEXTO_NEGRITA);
        tit.setForeground(new Color(139, 92, 246));

        JPanel head = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        head.setOpaque(false);
        head.add(tit);
        cbMes.setFont(Utils.UI.TEXTO);
        cbMes.setBackground(new Color(45, 45, 48));
        cbMes.setForeground(Color.WHITE);
        cbMes.setFocusable(false);
        cbMes.setPreferredSize(new Dimension(120, 26));
        cbAnio.setFont(Utils.UI.TEXTO);
        cbAnio.setBackground(new Color(45, 45, 48));
        cbAnio.setForeground(Color.WHITE);
        cbAnio.setFocusable(false);
        cbAnio.setPreferredSize(new Dimension(80, 26));
        head.add(cbMes);
        head.add(cbAnio);
        panel.add(head, BorderLayout.NORTH);

        JPanel draw = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                try {
                    int anio = Integer.parseInt((String) cbAnio.getSelectedItem());
                    int mes = cbMes.getSelectedIndex(); // 0 = todo el año, 1-12 = mes
                    java.util.List<double[]> datos = new DAO.DashboardDAO().ventasSecuenciales(anio, mes);
                    int w = getWidth(), h = getHeight() - 25;
                    if (h < 40) h = 40;
                    int pad = 8;
                    double max = 1;
                    for (double[] d : datos) if (d[1] > max) max = d[1];
                    int n = datos.size();
                    if (n == 0) {
                        g2.setColor(new Color(150,150,150));
                        g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                        g2.drawString("Sin ventas en el período seleccionado", 15, 40);
                        g2.dispose();
                        return;
                    }
                    int maxDia = (mes == 0) ? 31 : diasDelMes(anio, mes);
                    int plotW = w - 2 * pad;
                    double escalaX = (double) plotW / Math.max(1, maxDia);
                    int baseY = h - 15;
                    double escalaY = (double)(h - 40) / (max > 0 ? max : 1);

                    int[] xs = new int[n + 2];
                    int[] ys = new int[n + 2];
                    for (int i = 0; i < n; i++) {
                        xs[i] = (int)(pad + (datos.get(i)[0] - 1) * escalaX);
                        ys[i] = (int)(baseY - datos.get(i)[1] * escalaY);
                    }
                    xs[n] = xs[n-1]; ys[n] = baseY;
                    xs[n+1] = xs[0]; ys[n+1] = baseY;
                    g2.setColor(new Color(139, 92, 246, 60));
                    g2.fillPolygon(xs, ys, n + 2);

                    g2.setColor(new Color(167, 139, 250));
                    g2.setStroke(new BasicStroke(2f));
                    for (int i = 1; i < n; i++) {
                        g2.drawLine(xs[i-1], ys[i-1], xs[i], ys[i]);
                    }
                    for (double[] d : datos) {
                        int px = (int)(pad + (d[0] - 1) * escalaX);
                        int py = (int)(baseY - d[1] * escalaY);
                        g2.setColor(new Color(139, 92, 246));
                        g2.fillOval(px - 3, py - 3, 6, 6);
                    }
                    g2.setColor(new Color(170, 170, 170));
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                    g2.drawString("Día " + maxDia, w - 40, h - 2);
                    for (double[] d : datos) {
                        if (d[1] >= max) {
                            g2.setColor(Color.WHITE);
                            g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                            g2.drawString(String.format("$%.0f", d[1]), (int)(pad + (d[0] - 1) * escalaX) - 15, (int)(baseY - d[1] * escalaY) - 5);
                            break;
                        }
                    }
                } catch (Exception ex) {}
                g2.dispose();
            }
        };
        draw.setOpaque(false);
        panel.add(draw, BorderLayout.CENTER);

        java.awt.event.ActionListener refrescar = e -> draw.repaint();
        cbMes.addActionListener(refrescar);
        cbAnio.addActionListener(refrescar);
        return panel;
    }

    private int diasDelMes(int anio, int mes) {
        switch (mes) {
            case 2: return (anio % 4 == 0 && (anio % 100 != 0 || anio % 400 == 0)) ? 29 : 28;
            case 4: case 6: case 9: case 11: return 30;
            default: return 31;
        }
    }

    /** Gráfica de pérdida (en Bs) por el cambio del precio del dólar, tipo línea (trending). */
    private JPanel crearPanelPerdidaDolar() {
        JPanel panel = crearPanelGrafico("Pérdida por Cambio del Dólar", "dollar-sign.svg", new Color(245, 158, 11));
        JPanel draw = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                try {
                    java.util.List<double[]> datos = new DAO.DashboardDAO().perdidaDolarPorDia(14);
                    int w = getWidth(), h = getHeight() - 30;
                    if (h < 40) h = 40;
                    int pad = 10;
                    int n = datos.size();
                    if (n == 0) {
                        g2.setColor(new Color(150,150,150));
                        g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                        g2.drawString("Sin ventas en los últimos 14 días", 15, 40);
                        g2.dispose();
                        return;
                    }
                    double maxUsd = 1, maxBs = 1;
                    for (double[] d : datos) { maxUsd = Math.max(maxUsd, d[0]); maxBs = Math.max(maxBs, d[1]); }
                    int baseY = h - 15;
                    int plotW = w - 2 * pad;
                    double escalaX = (n > 1) ? (double) plotW / (n - 1) : plotW;
                    double escUsd = (double)(h - 40) / (maxUsd > 0 ? maxUsd : 1);
                    double escBs  = (double)(h - 40) / (maxBs > 0 ? maxBs : 1);

                    double[] xp = new double[n];
                    for (int i = 0; i < n; i++) xp[i] = pad + i * escalaX;

                    int[] aUsdX = new int[n + 2], aUsdY = new int[n + 2];
                    int[] aBsX  = new int[n + 2], aBsY  = new int[n + 2];
                    for (int i = 0; i < n; i++) {
                        aUsdX[i] = (int) xp[i]; aUsdY[i] = (int)(baseY - datos.get(i)[0] * escUsd);
                        aBsX[i]  = (int) xp[i]; aBsY[i]  = (int)(baseY - datos.get(i)[1] * escBs);
                    }
                    aUsdX[n] = (int) xp[n-1]; aUsdY[n] = baseY;
                    aUsdX[n+1] = (int) xp[0]; aUsdY[n+1] = baseY;
                    aBsX[n] = (int) xp[n-1]; aBsY[n] = baseY;
                    aBsX[n+1] = (int) xp[0]; aBsY[n+1] = baseY;
                    g2.setColor(new Color(16, 185, 129, 50));
                    g2.fillPolygon(aUsdX, aUsdY, n + 2);
                    g2.setColor(new Color(239, 68, 68, 50));
                    g2.fillPolygon(aBsX, aBsY, n + 2);

                    g2.setColor(new Color(239, 68, 68));
                    g2.setStroke(new BasicStroke(2f));
                    for (int i = 1; i < n; i++)
                        g2.drawLine((int) xp[i-1], (int)(baseY - datos.get(i-1)[1] * escBs),
                                    (int) xp[i],   (int)(baseY - datos.get(i)[1] * escBs));
                    g2.setColor(new Color(16, 185, 129));
                    g2.setStroke(new BasicStroke(2f));
                    for (int i = 1; i < n; i++)
                        g2.drawLine((int) xp[i-1], (int)(baseY - datos.get(i-1)[0] * escUsd),
                                    (int) xp[i],   (int)(baseY - datos.get(i)[0] * escUsd));
                    for (int i = 0; i < n; i++) {
                        g2.setColor(new Color(16, 185, 129));
                        g2.fillOval((int) xp[i] - 3, (int)(baseY - datos.get(i)[0] * escUsd) - 3, 6, 6);
                        g2.setColor(new Color(239, 68, 68));
                        g2.fillOval((int) xp[i] - 3, (int)(baseY - datos.get(i)[1] * escBs) - 3, 6, 6);
                    }

                    g2.setColor(new Color(16, 185, 129));
                    g2.fillRect(12, 8, 10, 10);
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                    g2.drawString("Ganancia (USD)", 26, 17);
                    g2.setColor(new Color(239, 68, 68));
                    g2.fillRect(12, 22, 10, 10);
                    g2.setColor(Color.WHITE);
                    g2.drawString("Pérdida por dólar (Bs)", 26, 31);
                } catch (Exception ex) {}
                g2.dispose();
            }
        };
        draw.setOpaque(false);
        panel.add(draw, BorderLayout.CENTER);
        return panel;
    }

    /** Producto más vendido (últimos 30 días) como barras horizontales. */
    private JPanel crearPanelProductoMasVendido() {
        JPanel panel = crearPanelGrafico("Producto Más Vendido", "tags.svg", new Color(34, 197, 94));
        JPanel draw = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                try {
                    Map<String, Double> datos = new DAO.DashboardDAO().productosMasVendidos(30);
                    int limit = Math.min(6, datos.size());
                    if (limit == 0) {
                        g2.setColor(new Color(150,150,150));
                        g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                        g2.drawString("Sin ventas en los últimos 30 días", 15, 40);
                        g2.dispose();
                        return;
                    }
                    double max = datos.values().stream().mapToDouble(Double::doubleValue).max().orElse(1);
                    if (max <= 0) max = 1;
                    int w = getWidth(), h = getHeight();
                    int topPad = 10, bottomPad = 6;
                    int usableH = Math.max(1, h - topPad - bottomPad);
                    int rowH = Math.min(Math.max(24, usableH / limit), 38);
                    int totalH = rowH * limit;
                    int startY = topPad + Math.max(0, (usableH - totalH) / 2);
                    int nameW = Math.max(90, Math.min(130, w / 3));
                    int valueW = 40;
                    int barMaxW = w - nameW - valueW - 16;
                    java.util.List<Map.Entry<String, Double>> items = new java.util.ArrayList<>(datos.entrySet());
                    for (int i = 0; i < limit; i++) {
                        Map.Entry<String, Double> e = items.get(i);
                        int y = startY + i * rowH;
                        int rowMid = y + rowH / 2;
                        int bw = (int)((e.getValue() / max) * barMaxW);
                        if (bw < 4 && e.getValue() > 0) bw = 4;
                        g2.setColor(new Color(170, 170, 170));
                        g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                        String name = e.getKey();
                        if (g2.getFontMetrics().stringWidth(name) > nameW - 6) {
                            name = name.substring(0, Math.max(1, name.length() - 3)) + "…";
                            while (g2.getFontMetrics().stringWidth(name) > nameW - 6 && name.length() > 1) {
                                name = name.substring(0, name.length() - 2) + "…";
                            }
                        }
                        g2.drawString(name, 2, rowMid + 4);
                        int barX = nameW;
                        GradientPaint gp = new GradientPaint(barX, 0, new Color(16, 185, 129), barX + bw, 0, new Color(5, 150, 105));
                        g2.setPaint(gp);
                        g2.fillRoundRect(barX, y, bw, rowH - 6, 8, 8);
                        g2.setColor(Color.WHITE);
                        g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                        String val = String.format("%.0f", e.getValue());
                        g2.drawString(val, barX + bw + 6, rowMid + 4);
                    }
                } catch (Exception ex) {}
                g2.dispose();
            }
        };
        draw.setOpaque(false);
        panel.add(draw, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPanelGrafica() {
        JPanel panel = new JPanel(new BorderLayout(0, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titulo = new JLabel("Top Productos por Stock");
        try {
            com.formdev.flatlaf.extras.FlatSVGIcon icon = new com.formdev.flatlaf.extras.FlatSVGIcon(getClass().getResource("/IMG/chart-bar.svg"));
            icon.setColorFilter(new com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter(color -> new Color(59, 130, 246)));
            titulo.setIcon(icon);
        } catch (Exception e){}
        titulo.setFont(Utils.UI.TEXTO_NEGRITA);
        titulo.setForeground(new Color(59, 130, 246));
        panel.add(titulo, BorderLayout.NORTH);

        JPanel drawPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                try {
                    List<Producto> productos = new ProductoDAO().listarTodos();
                    productos.sort((p1, p2) -> Double.compare(p2.getStockActual(), p1.getStockActual()));
                    int limit = Math.min(5, productos.size());

                    int w = getWidth();
                    int h = getHeight() - 25; // espacio para label inferior
                    int barWidth = Math.max(20, (w - (limit + 1) * 10) / (limit == 0 ? 1 : limit));
                    
                    double maxStock = limit > 0 ? productos.get(0).getStockActual() : 1;
                    if(maxStock == 0) maxStock = 1;

                    for (int i = 0; i < limit; i++) {
                        Producto p = productos.get(i);
                        int barHeight = (int) ((p.getStockActual() / maxStock) * (h - 20));
                        int x = 10 + i * (barWidth + 10);
                        int y = h - barHeight;

                        // Gradiente de barra
                        GradientPaint gp = new GradientPaint(x, y, new Color(59, 130, 246), x, y + barHeight, new Color(29, 78, 216));
                        g2.setPaint(gp);
                        g2.fillRoundRect(x, y, barWidth, barHeight, 8, 8);

                        // Texto superior (valor)
                        g2.setColor(Color.WHITE);
                        g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                        String val = String.valueOf((int)p.getStockActual());
                        int textWidth = g2.getFontMetrics().stringWidth(val);
                        g2.drawString(val, x + (barWidth - textWidth) / 2, y - 5);

                        // Texto inferior (nombre)
                        g2.setColor(new Color(150, 150, 150));
                        g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                        String name = p.getNombre().length() > 9 ? p.getNombre().substring(0, 9) + ".." : p.getNombre();
                        int nameWidth = g2.getFontMetrics().stringWidth(name);
                        g2.drawString(name, x + (barWidth - nameWidth) / 2, h + 15);
                    }
                } catch (Exception e) {}
                g2.dispose();
            }
        };
        drawPanel.setOpaque(false);
        panel.add(drawPanel, BorderLayout.CENTER);

        return panel;
    }
}
