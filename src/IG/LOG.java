package IG;

import CX.SesionUsuario;
import DAO.UsuarioDAO;
import Modelo.Usuario;
import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.Random;
import Utils.SeguridadArgon2;
import Utils.UI;

public class LOG extends JFrame {

    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton;

    public LOG() {
        setTitle("Inventario Pro - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 520);
        setUndecorated(true);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new GridLayout(1, 2)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            }
        };
        mainPanel.setBackground(new Color(30, 30, 30));
        mainPanel.setDoubleBuffered(true);

        // ── Panel Izquierdo (Branding) ──
        JPanel brandPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(16, 185, 129), getWidth(), getHeight(), new Color(5, 150, 105));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                // Círculos decorativos
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
                g2d.setColor(Color.WHITE);
                g2d.fillOval(-50, -50, 250, 250);
                g2d.fillOval(getWidth()-150, getHeight()-150, 250, 250);
                g2d.fillOval(getWidth()/2-60, getHeight()/2+50, 180, 180);
                g2d.dispose();
            }
        };
        brandPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(5, 20, 5, 20);

        JLabel iconLabel = new JLabel("", SwingConstants.CENTER);
        try {
            com.formdev.flatlaf.extras.FlatSVGIcon boxIcon = new com.formdev.flatlaf.extras.FlatSVGIcon("IMG/box.svg", 72, 72);
            boxIcon.setColorFilter(new com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter(c -> Color.WHITE));
            iconLabel.setIcon(boxIcon);
        } catch (Exception e) {}
        gbc.gridy = 0;
        brandPanel.add(iconLabel, gbc);

        JLabel titleLabel = new JLabel("Inventario Pro");
        titleLabel.setFont(UI.TITULO);
        titleLabel.setForeground(Color.WHITE);
        gbc.gridy = 1;
        brandPanel.add(titleLabel, gbc);

        JLabel subtitleLabel = new JLabel("<html><center>Sistema Integral de Gestión<br>de Inventario</center></html>");
        subtitleLabel.setFont(UI.TEXTO);
        subtitleLabel.setForeground(new Color(255, 255, 255, 200));
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 2;
        gbc.insets = new Insets(10, 20, 20, 20);
        brandPanel.add(subtitleLabel, gbc);

        JLabel versionLabel = new JLabel("v1.0.0");
        versionLabel.setFont(UI.NOTA);
        versionLabel.setForeground(new Color(255, 255, 255, 150));
        gbc.gridy = 3;
        gbc.insets = new Insets(40, 20, 5, 20);
        brandPanel.add(versionLabel, gbc);

        brandPanel.setOpaque(false);

        // ── Panel Derecho (Login Form) ──
        JPanel loginPanel = new JPanel();
        loginPanel.setBackground(new Color(30, 30, 30));
        loginPanel.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(8, 40, 8, 40);

        // Botón cerrar
        JButton closeBtn = new JButton();
        try {
            com.formdev.flatlaf.extras.FlatSVGIcon xIcon = new com.formdev.flatlaf.extras.FlatSVGIcon("IMG/x.svg", 14, 14);
            xIcon.setColorFilter(new com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter(c -> new Color(150, 150, 150)));
            closeBtn.setIcon(xIcon);
        } catch (Exception e) { closeBtn.setText("✕"); }
        closeBtn.setBorder(null);
        closeBtn.setContentAreaFilled(false);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> System.exit(0));
        
        gc.gridy = 0;
        gc.anchor = GridBagConstraints.EAST;
        gc.fill = GridBagConstraints.NONE;
        gc.insets = new Insets(10, 0, 0, 15);
        loginPanel.add(closeBtn, gc);

        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.CENTER;
        gc.insets = new Insets(8, 50, 8, 50);

        JLabel loginTitle = new JLabel("Iniciar Sesión");
        loginTitle.setFont(UI.TITULO);
        loginTitle.setForeground(Color.WHITE);
        gc.gridy = 1;
        gc.insets = new Insets(15, 50, 5, 50);
        loginPanel.add(loginTitle, gc);

        JLabel loginSub = new JLabel("Ingrese sus credenciales para continuar");
        loginSub.setFont(UI.NOTA);
        loginSub.setForeground(new Color(150, 150, 150));
        gc.gridy = 2;
        gc.insets = new Insets(0, 50, 20, 50);
        loginPanel.add(loginSub, gc);

        // Email
        JLabel emailLbl = new JLabel("Correo electrónico");
        emailLbl.setForeground(new Color(180, 180, 180));
        gc.gridy = 3;
        gc.insets = new Insets(5, 50, 2, 50);
        loginPanel.add(emailLbl, gc);

        emailField = new JTextField();
        emailField.setPreferredSize(new Dimension(250, 40));
        emailField.setBackground(new Color(45, 45, 45));
        emailField.setForeground(Color.WHITE);
        emailField.setFont(UI.CAMPO);
        emailField.setCaretColor(new Color(16, 185, 129));
        emailField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 60), 1, true),
            BorderFactory.createEmptyBorder(5, 12, 5, 12)
        ));
        try {
            com.formdev.flatlaf.extras.FlatSVGIcon mailIcon = new com.formdev.flatlaf.extras.FlatSVGIcon("IMG/users.svg", 16, 16);
            mailIcon.setColorFilter(new com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter(c -> new Color(150, 150, 150)));
            emailField.putClientProperty("JTextField.leadingIcon", mailIcon);
        } catch (Exception e) {}
        
        gc.gridy = 4;
        gc.insets = new Insets(2, 50, 10, 50);
        loginPanel.add(emailField, gc);

        // Password
        JLabel passLbl = new JLabel("Contraseña");
        passLbl.setForeground(new Color(180, 180, 180));
        gc.gridy = 5;
        gc.insets = new Insets(5, 50, 2, 50);
        loginPanel.add(passLbl, gc);

        passwordField = new JPasswordField();
        passwordField.setPreferredSize(new Dimension(250, 40));
        passwordField.setBackground(new Color(45, 45, 45));
        passwordField.setForeground(Color.WHITE);
        passwordField.setFont(UI.CAMPO);
        passwordField.setCaretColor(new Color(16, 185, 129));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 60), 1, true),
            BorderFactory.createEmptyBorder(5, 12, 5, 12)
        ));
        try {
            com.formdev.flatlaf.extras.FlatSVGIcon lockIcon = new com.formdev.flatlaf.extras.FlatSVGIcon("IMG/key.svg", 16, 16);
            lockIcon.setColorFilter(new com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter(c -> new Color(150, 150, 150)));
            passwordField.putClientProperty("JTextField.leadingIcon", lockIcon);
        } catch (Exception e) {}

        gc.gridy = 6;
        gc.insets = new Insets(2, 50, 25, 50);
        loginPanel.add(passwordField, gc);

        // Login button
        loginButton = new JButton("INGRESAR") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2d.setPaint(new Color(5, 150, 105));
                } else if (getModel().isRollover()) {
                    g2d.setPaint(new Color(20, 200, 140));
                } else {
                    g2d.setPaint(new Color(16, 185, 129));
                }
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        loginButton.setFont(UI.BOTON);
        loginButton.setForeground(Color.WHITE);
        loginButton.setPreferredSize(new Dimension(250, 42));
        loginButton.setContentAreaFilled(false);
        loginButton.setBorderPainted(false);
        loginButton.setFocusPainted(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.addActionListener(e -> hacerLogin());
        
        gc.gridy = 7;
        gc.insets = new Insets(5, 50, 5, 50);
        loginPanel.add(loginButton, gc);

        // Recovery link
        JButton btnRecuperar = new JButton("¿Olvidaste tu contraseña?");
        btnRecuperar.setFont(UI.NOTA);
        btnRecuperar.setForeground(new Color(16, 185, 129));
        btnRecuperar.setBorder(null);
        btnRecuperar.setContentAreaFilled(false);
        btnRecuperar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRecuperar.addActionListener(e -> recuperarPassword());
        
        gc.gridy = 8;
        gc.insets = new Insets(0, 50, 15, 50);
        loginPanel.add(btnRecuperar, gc);

        // Drag support
        final Point[] dragPoint = {null};
        mainPanel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { dragPoint[0] = e.getPoint(); }
            public void mouseReleased(MouseEvent e) { dragPoint[0] = null; }
        });
        mainPanel.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (dragPoint[0] != null) {
                    Point p = getLocation();
                    setLocation(p.x + e.getX() - dragPoint[0].x, p.y + e.getY() - dragPoint[0].y);
                }
            }
        });

        mainPanel.add(brandPanel);
        mainPanel.add(loginPanel);
        setContentPane(mainPanel);
    }

    private void hacerLogin() {
        String email = emailField.getText().trim();
        String pass = new String(passwordField.getPassword()).trim();
        if (email.isEmpty() || pass.isEmpty()) return;

        try {
            UsuarioDAO dao = new UsuarioDAO();
            Usuario user = dao.login(email, pass);
            if (user != null) {
                SesionUsuario.getInstancia().iniciarSesion(user.getId(), user.getNombre(), user.getRol(),
                    user.getPermisos(), user.isLicenciaActiva(), user.getLicenciaVencimiento());
                new DAO.BitacoraDAO().registrar("Login", "Iniciar Sesión",
                    "Inicio de sesión: " + user.getNombre() + " [" + user.getEmail() + "] rol: " + user.getRol());
                Dashboard dash = new Dashboard();
                dash.setUndecorated(true);
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                GraphicsDevice gd = ge.getDefaultScreenDevice();
                if (gd.isFullScreenSupported()) {
                    gd.setFullScreenWindow(dash);
                } else {
                    dash.setExtendedState(JFrame.MAXIMIZED_BOTH);
                }
                dash.setVisible(true);
                SwingUtilities.invokeLater(dash::repaint);
                this.dispose();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Atención", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void recuperarPassword() {
        String email = JOptionPane.showInputDialog(this, "Ingrese su correo registrado:", "Recuperación de Contraseña", JOptionPane.QUESTION_MESSAGE);
        if (email == null || email.trim().isEmpty()) return;

        try {
            UsuarioDAO dao = new UsuarioDAO();
            Usuario user = dao.buscarPorEmail(email.trim());
            if (user == null) {
                JOptionPane.showMessageDialog(this, "El correo no existe en el sistema.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Flujo de Preguntas de Seguridad (Único método activo)
            java.util.Random rnd = new java.util.Random();
            int pNum = rnd.nextInt(4) + 1;
            String preg = "", respCorrecta = "";

            switch(pNum) {
                case 1: preg = user.getPregunta1(); respCorrecta = user.getRespuesta1(); break;
                case 2: preg = user.getPregunta2(); respCorrecta = user.getRespuesta2(); break;
                case 3: preg = user.getPregunta3(); respCorrecta = user.getRespuesta3(); break;
                case 4: preg = user.getPregunta4(); respCorrecta = user.getRespuesta4(); break;
            }

            if (preg == null || preg.isEmpty()) {
                JOptionPane.showMessageDialog(this, "El usuario no cuenta con preguntas de seguridad configuradas.", "Atención", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String respuesta = JOptionPane.showInputDialog(this, "Pregunta de Seguridad:\n\n" + preg);
            if (respuesta != null && respuesta.equalsIgnoreCase(respCorrecta)) {
                JPasswordField pf = new JPasswordField();
                JPasswordField pfConfirm = new JPasswordField();
                Object[] message = {
                    "✅ Identidad Verificada.\nIngrese su nueva contraseña:", pf,
                    "Confirmar contraseña:", pfConfirm
                };
                
                while (true) {
                    int opt = JOptionPane.showConfirmDialog(this, message, "Reset de Clave", JOptionPane.OK_CANCEL_OPTION);
                    if (opt != JOptionPane.OK_OPTION) break;

                    String pass = new String(pf.getPassword());
                    String passConfirm = new String(pfConfirm.getPassword());

                    if (!pass.equals(passConfirm)) {
                        JOptionPane.showMessageDialog(this, "Las contraseñas no coinciden.", "Error", JOptionPane.ERROR_MESSAGE);
                        pf.setText("");
                        pfConfirm.setText("");
                        continue;
                    }

                    if (!SeguridadArgon2.esSegura(pass)) {
                        JOptionPane.showMessageDialog(this, SeguridadArgon2.getRequisitosMensaje(), "Contraseña Débil", JOptionPane.WARNING_MESSAGE);
                        pf.setText("");
                        pfConfirm.setText("");
                        continue;
                    }

                    dao.cambiarPassword(user.getId(), pass);
                    JOptionPane.showMessageDialog(this, "✅ Operación exitosa. Contraseña actualizada correctamente.");
                    break;
                }
            } else if (respuesta != null) {
                JOptionPane.showMessageDialog(this, "❌ Respuesta incorrecta.", "Error de Validación", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error de Sistema: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(new FlatDarkLaf()); } catch (Exception e) {}
        CX.ConexionBD.inicializarBaseDatos();
        CX.ConexionBD.programarRespaldoAutomatico();
        actualizarTasaSincrona();
        // Construye el histórico de tasas de los días anteriores en segundo plano.
        new Thread(() -> {
            try {
                DAO.TasaCambioDAO tcd = new DAO.TasaCambioDAO();
                tcd.construirHistoricoPendiente();
                // Re-fija el Bs congelado de facturas viejas con la tasa real de su día.
                tcd.reconciliarFacturasConHistorico();
            } catch (Exception e) {
                System.out.println("No se pudo construir histórico de tasas: " + e.getMessage());
            }
        }, "historico-tasas").start();
        SwingUtilities.invokeLater(() -> {
            LOG login = new LOG();
            login.setVisible(true);
            login.repaint();
        });
    }

    private static void actualizarTasaSincrona() {
        try {
            // Intento 1: java.net.http.HttpClient
            java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofSeconds(5))
                .build();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create("https://ve.dolarapi.com/v1/dolares"))
                .header("Accept", "application/json")
                .timeout(java.time.Duration.ofSeconds(8))
                .GET()
                .build();
            java.net.http.HttpResponse<String> resp = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
            parsearYGuardarTasa(resp.body());
            return;
        } catch (Exception e) {
            System.out.println("HttpClient fallo, intentando con URLConnection: " + e.getMessage());
        }

        try {
            // Intento 2: URLConnection (fallback)
            java.net.URL url = new java.net.URL("https://ve.dolarapi.com/v1/dolares");
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(8000);
            conn.setRequestProperty("Accept", "application/json");
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();
            parsearYGuardarTasa(sb.toString());
        } catch (Exception e) {
            System.out.println("No se pudo actualizar tasa: " + e.getMessage());
        }
    }

    private static void parsearYGuardarTasa(String body) {
        int idx = body.indexOf("\"promedio\"");
        if (idx < 0) {
            System.out.println("Campo 'promedio' no encontrado en respuesta");
            return;
        }
        int colon = body.indexOf(':', idx);
        int end = body.indexOf(',', colon);
        if (end < 0) end = body.indexOf('}', colon);
        if (colon < 0 || end < 0) {
            System.out.println("Error parseando valor de promedio");
            return;
        }
        double tasa = Double.parseDouble(body.substring(colon + 1, end).trim());
        Utils.Config.setTasaVES(tasa);
        Utils.Config.setHoraUltimaActualizacion(java.time.LocalTime.now());
        // Registra la tasa de hoy en el historico por dia (tabla tasa_cambio).
        try {
            new DAO.TasaCambioDAO().guardarHoy(tasa);
        } catch (Exception e) {
            System.out.println("No se pudo registrar la tasa del dia en el historico: " + e.getMessage());
        }
        System.out.println("Tasa BCV actualizada: Bs " + tasa);
        actualizarEuro();
     }

    /** Obtiene tambien el precio del Euro oficial (BCV) y lo guarda en Config. */
    private static void actualizarEuro() {
        try {
            java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofSeconds(5))
                .build();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create("https://ve.dolarapi.com/v1/euros/oficial"))
                .header("Accept", "application/json")
                .timeout(java.time.Duration.ofSeconds(8))
                .GET()
                .build();
            java.net.http.HttpResponse<String> resp = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
            String body = resp.body();
            int idx = body.indexOf("\"promedio\"");
            if (idx < 0) return;
            int colon = body.indexOf(':', idx);
            int end = body.indexOf(',', colon);
            if (end < 0) end = body.indexOf('}', colon);
            if (colon < 0 || end < 0) return;
            double euro = Double.parseDouble(body.substring(colon + 1, end).trim());
            Utils.Config.setTasaEuroVES(euro);
            System.out.println("Tasa Euro BCV actualizada: Bs " + euro);
        } catch (Exception e) {
            System.out.println("No se pudo actualizar tasa euro: " + e.getMessage());
        }
    }
}
