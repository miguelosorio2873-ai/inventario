package IG;

import DAO.ProductoDAO;
import DAO.ClienteDAO;
import DAO.InventarioDAO;
import Modelo.Producto;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

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
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titulo.setForeground(Color.WHITE);

        JLabel subtitulo = new JLabel("Resumen general del sistema de inventario");
        subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitulo.setForeground(new Color(140, 140, 140));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(titulo);
        header.add(Box.createRigidArea(new Dimension(0, 5)));
        header.add(subtitulo);
        container.add(header, BorderLayout.NORTH);

        // Cards
        JPanel cardsPanel = new JPanel(new GridLayout(1, 4, 15, 15));
        cardsPanel.setOpaque(false);

        int totalProductos = 0, stockBajo = 0, totalClientes = 0;
        double ventasMes = 0;

        try {
            ProductoDAO pDao = new ProductoDAO();
            ClienteDAO cDao = new ClienteDAO();
            InventarioDAO iDao = new InventarioDAO();
            totalProductos = pDao.contarTotal();
            stockBajo = pDao.contarStockBajo();
            totalClientes = cDao.contarTotal();
            ventasMes = iDao.obtenerVentasMes();
        } catch (SQLException e) {
            System.err.println("Error cargando stats: " + e.getMessage());
        }

        cardsPanel.add(crearTarjeta("box.svg", "Total Productos", String.valueOf(totalProductos), new Color(59, 130, 246)));
        cardsPanel.add(crearTarjeta("alert-triangle.svg", "Stock Bajo", String.valueOf(stockBajo), new Color(245, 158, 11)));
        cardsPanel.add(crearTarjeta("users.svg", "Clientes", String.valueOf(totalClientes), new Color(139, 92, 246)));
        cardsPanel.add(crearTarjeta("dollar-sign.svg", "Ventas del Mes", String.format("$%.2f", ventasMes), new Color(16, 185, 129)));

        // Contenedor de Textos de Bienvenida y Botón (Estilo antiguo Dashboard)
        JPanel welcomeCard = new JPanel(new BorderLayout(15, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
            }
        };
        welcomeCard.setOpaque(false);
        welcomeCard.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JLabel welcomeTitle = new JLabel("Bienvenido a Inventario Pro");
        welcomeTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        welcomeTitle.setForeground(new Color(16, 185, 129));

        JLabel welcomeText = new JLabel("<html><p>Este sistema le permite administrar de forma eficiente todos los procesos del negocio.</p><br>" +
            "<p>Funcionalidades principales:</p>" +
            "<ul><li>Gestión automatizada de Productos y Stock.</li>" +
            "<li>Facturación y registro de salidas/entradas.</li>" +
            "<li>Administración de Clientes, Proveedores y Usuarios.</li>" +
            "<li>Gráficos dinámicos, métricas y reportes en Excel.</li></ul>" +
            "<p>Si necesita ayuda, puede descargar el manual de usuario completo.</p></html>");
        welcomeText.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        welcomeText.setForeground(new Color(180, 180, 180));

        JButton btnManual = PanelProductos.crearBoton("📥 Ver Manual de Usuario", new Color(16, 185, 129));
        try {
            com.formdev.flatlaf.extras.FlatSVGIcon mIcon = new com.formdev.flatlaf.extras.FlatSVGIcon(getClass().getResource("/IMG/download.svg")).derive(16, 16);
            mIcon.setColorFilter(new com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter(c -> Color.WHITE));
            btnManual.setIcon(mIcon);
        } catch(Exception e){}
        btnManual.addActionListener(e -> {
            try {
                java.io.File manualFile = new java.io.File("manual/manual.pdf");
                if (manualFile.exists() && java.awt.Desktop.isDesktopSupported()) {
                    java.awt.Desktop.getDesktop().open(manualFile);
                } else {
                    JOptionPane.showMessageDialog(this, "No se encontró el archivo del manual (manual/manual.pdf).", "Archivo no encontrado", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al abrir el manual.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel topWelcome = new JPanel(new BorderLayout());
        topWelcome.setOpaque(false);
        topWelcome.add(welcomeTitle, BorderLayout.NORTH);
        topWelcome.add(welcomeText, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false);
        btnPanel.add(btnManual);

        welcomeCard.add(topWelcome, BorderLayout.CENTER);
        welcomeCard.add(btnPanel, BorderLayout.SOUTH);

        // Top content (Cards + Welcome)
        JPanel topContent = new JPanel(new BorderLayout(0, 15));
        topContent.setOpaque(false);
        topContent.add(cardsPanel, BorderLayout.NORTH);
        topContent.add(welcomeCard, BorderLayout.SOUTH);

        // Main Center Panel
        JPanel mainCenter = new JPanel(new BorderLayout(0, 15));
        mainCenter.setOpaque(false);
        mainCenter.add(topContent, BorderLayout.NORTH);

        // Bottom content (Table + Chart)
        JPanel contentBottom = new JPanel(new GridLayout(1, 2, 15, 0));
        contentBottom.setOpaque(false);
        contentBottom.add(crearPanelStockBajo());
        contentBottom.add(crearPanelGrafica());
        contentBottom.setPreferredSize(new Dimension(0, 360)); // Forzar altura amigable

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
            com.formdev.flatlaf.extras.FlatSVGIcon svgIcon = new com.formdev.flatlaf.extras.FlatSVGIcon(getClass().getResource("/IMG/" + icono));
            svgIcon.setColorFilter(new com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter(color -> accentColor));
            emojiLabel.setIcon(svgIcon);
        } catch (Exception e) {
            System.err.println("No se pudo cargar el icono: " + icono);
        }

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel tituloLabel = new JLabel(titulo);
        tituloLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tituloLabel.setForeground(new Color(140, 140, 140));

        JLabel valorLabel = new JLabel(valor);
        valorLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        valorLabel.setForeground(Color.WHITE);

        textPanel.add(tituloLabel);
        textPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        textPanel.add(valorLabel);

        card.add(emojiLabel, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);

        return card;
    }

    private JPanel crearPanelStockBajo() {
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

        JLabel titulo = new JLabel("Productos con Stock Bajo");
        try {
            com.formdev.flatlaf.extras.FlatSVGIcon icon = new com.formdev.flatlaf.extras.FlatSVGIcon(getClass().getResource("/IMG/alert-triangle.svg"));
            icon.setColorFilter(new com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter(color -> new Color(245, 158, 11)));
            titulo.setIcon(icon);
        } catch (Exception e){}
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titulo.setForeground(new Color(245, 158, 11));
        panel.add(titulo, BorderLayout.NORTH);

        String[] cols = {"SKU", "Producto", "Categoría", "Stock Actual", "Stock Mínimo"};
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        try {
            List<Producto> productos = new ProductoDAO().listarStockBajo();
            for (Producto p : productos) {
                model.addRow(new Object[]{p.getSku(), p.getNombre(), p.getCategoriaNombre(),
                    String.format("%.0f", p.getStockActual()), String.format("%.0f", p.getStockMinimo())});
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }

        JTable tabla = new JTable(model);
        tabla.setBackground(new Color(39, 39, 42));
        tabla.setForeground(Color.WHITE);
        tabla.setGridColor(new Color(55, 55, 55));
        tabla.setSelectionBackground(new Color(16, 185, 129, 50));
        tabla.setSelectionForeground(Color.WHITE);
        tabla.setRowHeight(32);
        tabla.getTableHeader().setBackground(new Color(30, 30, 30));
        tabla.getTableHeader().setForeground(new Color(180, 180, 180));
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(new Color(39, 39, 42));
        panel.add(scroll, BorderLayout.CENTER);

        if (model.getRowCount() == 0) {
            JLabel noData = new JLabel("No hay productos con stock bajo", SwingConstants.CENTER);
            try {
                com.formdev.flatlaf.extras.FlatSVGIcon icon = new com.formdev.flatlaf.extras.FlatSVGIcon(getClass().getResource("/IMG/check-circle.svg"));
                icon.setColorFilter(new com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter(color -> new Color(16, 185, 129)));
                noData.setIcon(icon);
            } catch (Exception e){}
            noData.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            noData.setForeground(new Color(16, 185, 129));
            panel.add(noData, BorderLayout.CENTER);
        }

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
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
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
