package IG;

import CX.SesionUsuario;
import Utils.UI;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Dashboard extends JFrame {

    private JPanel panelContenido;
    private JPanel sidebarPanel;
    private static Dashboard instancia;
    private JButton btnActivo = null;
    private final Color COLOR_SIDEBAR = new Color(17, 24, 39);
    private final Color COLOR_SIDEBAR_HOVER = new Color(31, 41, 55);
    private final Color COLOR_ACTIVO = new Color(16, 185, 129);
    private final Color COLOR_FONDO = new Color(24, 24, 27);

    public Dashboard() {
        instancia = this;
        setTitle("Inventario Pro - Dashboard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 780);
        setMinimumSize(new Dimension(1100, 700));
        setLocationRelativeTo(null);
        setUndecorated(true);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(COLOR_FONDO);
        mainPanel.setDoubleBuffered(true);

        // ── Top Bar ──
        JPanel topBar = crearTopBar();

        // ── Sidebar ──
        JPanel sidebar = crearSidebar();

        // ── Contenido ──
        panelContenido = new JPanel(new BorderLayout());
        panelContenido.setBackground(COLOR_FONDO);

        mainPanel.add(topBar, BorderLayout.NORTH);
        mainPanel.add(sidebar, BorderLayout.WEST);
        mainPanel.add(panelContenido, BorderLayout.CENTER);

        setContentPane(mainPanel);

        // Mostrar Dashboard por defecto
        mostrarPanel(new PanelInicio());

        // Drag support
        final Point[] dragPoint = {null};
        topBar.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { dragPoint[0] = e.getPoint(); }
            public void mouseReleased(MouseEvent e) { dragPoint[0] = null; }
        });
        topBar.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (dragPoint[0] != null) {
                    Point p = getLocation();
                    setLocation(p.x + e.getX() - dragPoint[0].x, p.y + e.getY() - dragPoint[0].y);
                }
            }
        });
    }

    private JPanel crearTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(17, 24, 39));
        topBar.setPreferredSize(new Dimension(0, 50));
        topBar.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 15));

        JLabel logo = new JLabel("Inventario Pro");
        try {
            com.formdev.flatlaf.extras.FlatSVGIcon boxIcon = new com.formdev.flatlaf.extras.FlatSVGIcon("IMG/box.svg").derive(32, 32);
            boxIcon.setColorFilter(new com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter(c -> new Color(16, 185, 129)));
            logo.setIcon(boxIcon);
            logo.setIconTextGap(8);
        } catch (Exception e) {}
        logo.setFont(Utils.UI.TITULO);
        logo.setForeground(new Color(16, 185, 129));

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        rightPanel.setOpaque(false);

        SesionUsuario sesion = SesionUsuario.getInstancia();
        JLabel userLabel = new JLabel(" " + (sesion.getNombreUsuario() != null ? sesion.getNombreUsuario() : "Admin"));
        try {
            com.formdev.flatlaf.extras.FlatSVGIcon userIcon = new com.formdev.flatlaf.extras.FlatSVGIcon("IMG/users.svg");
            userIcon.setColorFilter(new com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter(color -> new Color(180, 180, 180)));
            userLabel.setIcon(userIcon);
        } catch(Exception ex){}
        userLabel.setFont(Utils.UI.TEXTO);
        userLabel.setForeground(new Color(180, 180, 180));

        JLabel clockLabel = new JLabel();
        clockLabel.setFont(Utils.UI.TEXTO_NEGRITA);
        clockLabel.setForeground(new Color(16, 185, 129));
        javax.swing.Timer timer = new javax.swing.Timer(1000, e -> {
            clockLabel.setText(new java.text.SimpleDateFormat("dd/MM/yyyy hh:mm:ss a").format(new java.util.Date()));
        });
        timer.start();
        clockLabel.setText(new java.text.SimpleDateFormat("dd/MM/yyyy hh:mm:ss a").format(new java.util.Date()));

        JButton btnMin = crearBotonControl("minus.svg", new Color(250, 204, 21));
        btnMin.addActionListener(e -> setState(Frame.ICONIFIED));

        JButton btnMax = crearBotonControl("square.svg", new Color(52, 211, 153));
        btnMax.addActionListener(e -> {
            if (getExtendedState() == JFrame.MAXIMIZED_BOTH) {
                setExtendedState(JFrame.NORMAL);
            } else {
                setExtendedState(JFrame.MAXIMIZED_BOTH);
            }
        });

        JButton btnClose = crearBotonControl("x.svg", new Color(239, 68, 68));
        btnClose.addActionListener(e -> {
            int r = JOptionPane.showConfirmDialog(this, "¿Desea salir del sistema?", "Confirmar", JOptionPane.YES_NO_OPTION);
            if (r == JOptionPane.YES_OPTION) System.exit(0);
        });

        // Tasa BCV
        JLabel tasaLabel = new JLabel();
        tasaLabel.setFont(Utils.UI.BOTON);
        tasaLabel.setForeground(new Color(245, 158, 11));
        actualizarTasaLabel(tasaLabel);

        rightPanel.add(tasaLabel);
        rightPanel.add(Box.createRigidArea(new Dimension(15, 0)));
        rightPanel.add(clockLabel);
        rightPanel.add(Box.createRigidArea(new Dimension(15, 0)));
        rightPanel.add(userLabel);
        rightPanel.add(Box.createRigidArea(new Dimension(15, 0)));
        rightPanel.add(btnMin);
        rightPanel.add(btnMax);
        rightPanel.add(btnClose);

        topBar.add(logo, BorderLayout.WEST);
        topBar.add(rightPanel, BorderLayout.EAST);
        return topBar;
    }

    private JButton crearBotonControl(String iconName, Color hoverColor) {
        JButton btn = new JButton();
        try {
            com.formdev.flatlaf.extras.FlatSVGIcon icon = new com.formdev.flatlaf.extras.FlatSVGIcon("IMG/" + iconName).derive(14, 14);
            icon.setColorFilter(new com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter(c -> new Color(150, 150, 150)));
            btn.setIcon(icon);
        } catch (Exception e) {}
        btn.setFont(Utils.UI.BOTON);
        btn.setForeground(new Color(150, 150, 150));
        btn.setBackground(new Color(17, 24, 39));
        btn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { 
                try {
                    com.formdev.flatlaf.extras.FlatSVGIcon icon = new com.formdev.flatlaf.extras.FlatSVGIcon("IMG/" + iconName).derive(14, 14);
                    icon.setColorFilter(new com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter(c -> hoverColor));
                    btn.setIcon(icon);
                } catch (Exception ex) {}
                btn.setBackground(new Color(31, 41, 55));
            }
            public void mouseExited(MouseEvent e) { 
                try {
                    com.formdev.flatlaf.extras.FlatSVGIcon icon = new com.formdev.flatlaf.extras.FlatSVGIcon("IMG/" + iconName).derive(14, 14);
                    icon.setColorFilter(new com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter(c -> new Color(150, 150, 150)));
                    btn.setIcon(icon);
                } catch (Exception ex) {}
                btn.setBackground(new Color(17, 24, 39));
            }
        });
        return btn;
    }

    public static Dashboard getInstancia() {
        return instancia;
    }

    public void refrescarMenu() {
        if (sidebarPanel != null) {
            sidebarPanel.removeAll();
            JPanel nuevoMenu = crearSidebarContenido();
            sidebarPanel.add(nuevoMenu, BorderLayout.CENTER);
            sidebarPanel.revalidate();
            sidebarPanel.repaint();
        }
    }

    private JPanel crearSidebar() {
        sidebarPanel = new JPanel(new BorderLayout());
        sidebarPanel.setBackground(COLOR_SIDEBAR);
        sidebarPanel.setPreferredSize(new Dimension(220, 0));
        sidebarPanel.add(crearSidebarContenido());
        return sidebarPanel;
    }

    private JPanel crearSidebarContenido() {
        JPanel sidebar = new JPanel();
        sidebar.setOpaque(false);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));

        JLabel menuTitle = new JLabel("  MENÚ PRINCIPAL");
        menuTitle.setFont(Utils.UI.NOTA);
        menuTitle.setForeground(new Color(100, 100, 100));
        menuTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        menuTitle.setMaximumSize(new Dimension(220, 30));
        menuTitle.setBorder(BorderFactory.createEmptyBorder(5, 15, 10, 0));
        sidebar.add(menuTitle);

        String[][] items = {
            {"home.svg", "Dashboard", ""},
            {"file-invoice.svg", "Ventas", "Ventas"},
            {"box.svg", "Productos", "Productos"},
            {"chart-bar.svg", "Inventario", "Inventario"},
            {"users.svg", "Usuarios", "Usuarios"},
            {"chart-pie.svg", "Reportes", "Reportes"},
            {"scroll.svg", "Bitacora", "Bitacora"},
            {"cog.svg", "Configuracion", "Configuracion"}
        };

        SesionUsuario sesion = SesionUsuario.getInstancia();
        boolean isAdmin = "Admin".equalsIgnoreCase(sesion.getRol()) || "Administrador".equalsIgnoreCase(sesion.getRol());

        for (String[] item : items) {
            String modulo = item[1];
            String permiso = item[2];
            
            boolean tienePerm = permiso.isEmpty() || sesion.tienePermiso(permiso);
            
            JButton btn = crearBotonMenu(item[1], item[0]);
            
            btn.setVisible(tienePerm);
            
            btn.addActionListener(e -> {
                setBotonActivo(btn);
                switch (item[1]) {
                    case "Dashboard": mostrarPanel(new PanelInicio()); break;
                    case "Ventas": mostrarPanel(new PanelFacturas()); break;
                    case "Productos": mostrarPanel(new PanelProductos()); break;
                    case "Inventario": mostrarPanel(new PanelInventario()); break;
                    case "Usuarios": mostrarPanel(new PanelUsuarios()); break;
                    case "Reportes": mostrarPanel(new PanelReportes()); break;
                    case "Bitacora": mostrarPanel(new PanelBitacora()); break;
                    case "Configuracion": mostrarPanel(new PanelConfiguracion()); break;
                }
            });
            sidebar.add(btn);
        }

        sidebar.add(Box.createVerticalGlue());

        // Separator
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(190, 1));
        sep.setForeground(new Color(55, 65, 81));
        sidebar.add(sep);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));

        // Cerrar sesión
        JButton btnLogout = crearBotonMenu("Cerrar Sesión", "sign-out.svg");
        btnLogout.setForeground(new Color(239, 68, 68));
        btnLogout.addActionListener(e -> {
            int r = JOptionPane.showConfirmDialog(this, "¿Cerrar sesión?", "Confirmar", JOptionPane.YES_NO_OPTION);
            if (r == JOptionPane.YES_OPTION) {
                SesionUsuario su = SesionUsuario.getInstancia();
                if (su.getNombreUsuario() != null) {
                    new DAO.BitacoraDAO().registrar("Login", "Cerrar Sesión",
                        "Cierre de sesión: " + su.getNombreUsuario());
                }
                su.cerrarSesion();
                new LOG().setVisible(true);
                dispose();
            }
        });
        sidebar.add(btnLogout);

        return sidebar;
    }

    private JButton crearBotonMenu(String texto, String iconName) {
        JButton btn = new JButton("  " + texto);
        try {
            com.formdev.flatlaf.extras.FlatSVGIcon icon = new com.formdev.flatlaf.extras.FlatSVGIcon("IMG/" + iconName);
            if (icon != null) {
                icon = icon.derive(20, 20);
                icon.setColorFilter(new com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter(color -> new Color(180, 180, 180)));
                btn.setIcon(icon);
            }
        } catch (Exception ex) {
            System.err.println("No se encontro icono: " + iconName);
        }
        btn.setFont(Utils.UI.BOTON);
        btn.setForeground(new Color(180, 180, 180));
        btn.setBackground(COLOR_SIDEBAR);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(220, 42));
        btn.setPreferredSize(new Dimension(220, 42));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 10));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (btn != btnActivo) {
                    btn.setBackground(COLOR_SIDEBAR_HOVER);
                    btn.setForeground(Color.WHITE);
                    if (btn.getIcon() instanceof com.formdev.flatlaf.extras.FlatSVGIcon) {
                        ((com.formdev.flatlaf.extras.FlatSVGIcon) btn.getIcon()).setColorFilter(
                            new com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter(color -> Color.WHITE));
                    }
                }
            }
            public void mouseExited(MouseEvent e) {
                if (btn != btnActivo) {
                    btn.setBackground(COLOR_SIDEBAR);
                    Color baseColor = btn.getText().contains("Cerrar") ? new Color(239, 68, 68) : new Color(180, 180, 180);
                    btn.setForeground(baseColor);
                    if (btn.getIcon() instanceof com.formdev.flatlaf.extras.FlatSVGIcon) {
                        ((com.formdev.flatlaf.extras.FlatSVGIcon) btn.getIcon()).setColorFilter(
                            new com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter(color -> baseColor));
                    }
                }
            }
        });

        return btn;
    }

    private void setBotonActivo(JButton btn) {
        if (btnActivo != null) {
            btnActivo.setBackground(COLOR_SIDEBAR);
            Color baseColor = btnActivo.getText().contains("Cerrar") ? new Color(239, 68, 68) : new Color(180, 180, 180);
            btnActivo.setForeground(baseColor);
            if (btnActivo.getIcon() instanceof com.formdev.flatlaf.extras.FlatSVGIcon) {
                ((com.formdev.flatlaf.extras.FlatSVGIcon) btnActivo.getIcon()).setColorFilter(
                    new com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter(color -> baseColor));
            }
        }
        btn.setBackground(new Color(16, 185, 129, 30));
        btn.setForeground(COLOR_ACTIVO);
        if (btn.getIcon() instanceof com.formdev.flatlaf.extras.FlatSVGIcon) {
            ((com.formdev.flatlaf.extras.FlatSVGIcon) btn.getIcon()).setColorFilter(
                new com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter(color -> COLOR_ACTIVO));
        }
        btnActivo = btn;
    }

    public void mostrarPanel(JPanel panel) {
        panelContenido.removeAll();
        panelContenido.add(panel, BorderLayout.CENTER);
        panelContenido.revalidate();
        panelContenido.repaint();
    }

    private void actualizarTasaLabel(JLabel label) {
        double dolar = Utils.Config.getTasaVES();
        double euro = Utils.Config.getTasaEuroVES();
        String euroStr = euro > 0 ? String.format("Bs %.2f", euro) : "Bs -- ";
        label.setText("BCV | $ " + String.format("%.2f", dolar) + " | € " + euroStr);
    }

    public void setDatosUsuario(String nombre, String tipo, String cedula) {
        // Compatibilidad
    }
}
