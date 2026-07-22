package IG;

import DAO.UsuarioDAO;
import Modelo.Usuario;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import Utils.SeguridadArgon2;
import java.util.List;
import java.util.Random;

public class PanelUsuarios extends JPanel {

    private JTable tabla;
    private DefaultTableModel modelo;
    private UsuarioDAO dao = new UsuarioDAO();

    public PanelUsuarios() {
        setBackground(new Color(24, 24, 27));
        setLayout(new BorderLayout(0, 15));
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        // Header
        JPanel header = new JPanel(new BorderLayout(15, 0));
        header.setOpaque(false);

        JLabel titulo = new JLabel("Usuarios del Sistema");
        try {
            com.formdev.flatlaf.extras.FlatSVGIcon icon = new com.formdev.flatlaf.extras.FlatSVGIcon(getClass().getResource("/IMG/users.svg")).derive(24, 24);
            icon.setColorFilter(new com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter(c -> Color.WHITE));
            titulo.setIcon(icon);
            titulo.setIconTextGap(10);
        } catch (Exception e) {}
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titulo.setForeground(Color.WHITE);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        acciones.setOpaque(false);

        JButton btnNuevo = PanelProductos.crearBoton("➕ Nuevo", new Color(16, 185, 129));
        btnNuevo.addActionListener(e -> dialogoUsuario(null));

        JButton btnEditar = PanelProductos.crearBoton("✏️ Editar", new Color(59, 130, 246));
        btnEditar.addActionListener(e -> editarSeleccionado());

        JButton btnPass = PanelProductos.crearBoton("🔑 Cambiar Clave", new Color(245, 158, 11));
        btnPass.addActionListener(e -> cambiarPassword());

        JButton btnEliminar = PanelProductos.crearBoton("🗑️ Eliminar", new Color(239, 68, 68));
        btnEliminar.addActionListener(e -> eliminarSeleccionado());

        // Aplicar permisos
        CX.SesionUsuario sesion = CX.SesionUsuario.getInstancia();
        btnNuevo.setEnabled(sesion.tienePermiso("Usuarios", "Crear"));
        btnEditar.setEnabled(sesion.tienePermiso("Usuarios", "Editar"));
        btnPass.setEnabled(sesion.tienePermiso("Usuarios", "Editar"));
        btnEliminar.setEnabled(sesion.tienePermiso("Usuarios", "Eliminar"));

        acciones.add(btnNuevo);
        acciones.add(btnEditar);
        acciones.add(btnPass);
        acciones.add(btnEliminar);

        header.add(titulo, BorderLayout.WEST);
        header.add(acciones, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Tabla
        String[] cols = {"ID", "Nombre", "Email", "Rol", "Estado"};
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
            for (Usuario u : dao.listarTodos()) {
                modelo.addRow(new Object[]{
                    u.getId(),
                    u.getNombre(),
                    u.getEmail(),
                    u.getRol() != null ? u.getRol() : "Estándar",
                    "Activo"
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void dialogoUsuario(Usuario usr) {
        boolean ed = usr != null;

        JTextField tfNombre = PanelProductos.crearCampo(ed ? usr.getNombre() : "", 100);
        JTextField tfEmail = PanelProductos.crearCampo(ed ? usr.getEmail() : "", 100);
        JPasswordField tfPass = new JPasswordField();
        tfPass.setBackground(new Color(45, 45, 45));
        tfPass.setForeground(Color.WHITE);
        tfPass.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tfPass.setCaretColor(new Color(16, 185, 129));
        tfPass.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(63, 63, 70)),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)));

        JPasswordField tfPassConfirm = new JPasswordField();
        tfPassConfirm.setBackground(new Color(45, 45, 45));
        tfPassConfirm.setForeground(Color.WHITE);
        tfPassConfirm.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tfPassConfirm.setCaretColor(new Color(16, 185, 129));
        tfPassConfirm.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(63, 63, 70)),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)));

        JComboBox<String> cbRol = new JComboBox<>(new String[]{"Estándar", "Admin"});
        cbRol.setBackground(new Color(45, 45, 45));
        cbRol.setForeground(Color.WHITE);
        cbRol.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbRol.setEditable(true);
        if (ed && usr.getRol() != null) cbRol.setSelectedItem(usr.getRol());

        // Panel de Permisos Detallado
        String[] modulos = {"Productos", "Categorias", "Clientes", "Proveedores", "Inventario", "Facturas", "Usuarios", "Reportes", "Configuracion"};
        String[] acciones = {"Crear", "Editar", "Eliminar", "Exportar"};
        char[] codigos = {'C', 'E', 'D', 'X'};
        
        JCheckBox[][] matrix = new JCheckBox[modulos.length][acciones.length];
        JPanel pPermisos = new JPanel();
        pPermisos.setLayout(new BoxLayout(pPermisos, BoxLayout.Y_AXIS));
        pPermisos.setBackground(new Color(45, 45, 45));
        pPermisos.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(70, 70, 70)), "Permisos Detallados", 0, 0, null, Color.WHITE));

        for (int i = 0; i < modulos.length; i++) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
            row.setOpaque(false);
            JLabel lbl = new JLabel(modulos[i]);
            lbl.setForeground(Color.WHITE);
            lbl.setPreferredSize(new Dimension(100, 20));
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            row.add(lbl);
            
            for (int j = 0; j < acciones.length; j++) {
                String etiqueta = obtenerEtiquetaAccion(modulos[i], codigos[j]);
                matrix[i][j] = new JCheckBox(etiqueta);
                matrix[i][j].setFont(new Font("Segoe UI", Font.PLAIN, 11));
                matrix[i][j].setForeground(new Color(200, 200, 200));
                matrix[i][j].setOpaque(false);
                
                if (!esAccionAplicable(modulos[i], codigos[j])) {
                    matrix[i][j].setVisible(false);
                }
                
                row.add(matrix[i][j]);
                
                if (ed && usr.getPermisos() != null) {
                    String p = normalizar(usr.getPermisos());
                    String m = normalizar(modulos[i]);
                    int idx = p.indexOf(m + ":");
                    if (idx != -1) {
                        int endIdx = p.indexOf(",", idx);
                        String sub = (endIdx == -1) ? p.substring(idx) : p.substring(idx, endIdx);
                        String actionsPart = sub.substring(sub.indexOf(":") + 1);
                        if (actionsPart.contains(String.valueOf(codigos[j]))) matrix[i][j].setSelected(true);
                    } else if (p.contains(m)) {
                        // Comportamiento por defecto para formato antiguo si se desea
                    }
                }
            }
            
            pPermisos.add(row);
        }

        JPanel pBtnPerm = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pBtnPerm.setOpaque(false);
        JButton btnAll = new JButton("Seleccionar Todo");
        JButton btnNone = new JButton("Limpiar");
        btnAll.addActionListener(e -> {
            for(JCheckBox[] r : matrix) for(JCheckBox c : r) {
                if (c.isVisible()) c.setSelected(true);
            }
        });
        btnNone.addActionListener(e -> {
            for(JCheckBox[] r : matrix) for(JCheckBox c : r) {
                if (c.isVisible()) c.setSelected(false);
            }
        });
        pBtnPerm.add(btnAll); pBtnPerm.add(btnNone);
        pPermisos.add(pBtnPerm);

        cbRol.addActionListener(e -> {
            boolean isAdmin = "Admin".equalsIgnoreCase(cbRol.getSelectedItem().toString());
            // Ya no forzamos la selección, solo permitimos editar o no según se prefiera.
            // Para permitir "Admins Restringidos", dejaremos las casillas habilitadas siempre.
        });
        
        // Disparar evento inicial (opcional ahora)
        // No forzamos selección inicial

        // Preguntas 1-4
        JTextField tfP1 = PanelProductos.crearCampo(ed ? usr.getPregunta1() : "Nombre de tu primera mascota?", 200);
        JTextField tfR1 = PanelProductos.crearCampo(ed ? usr.getRespuesta1() : "", 200);
        JTextField tfP2 = PanelProductos.crearCampo(ed ? usr.getPregunta2() : "Ciudad de nacimiento de tu madre?", 200);
        JTextField tfR2 = PanelProductos.crearCampo(ed ? usr.getRespuesta2() : "", 200);
        JTextField tfP3 = PanelProductos.crearCampo(ed ? usr.getPregunta3() : "Nombre de tu escuela primaria?", 200);
        JTextField tfR3 = PanelProductos.crearCampo(ed ? usr.getRespuesta3() : "", 200);
        JTextField tfP4 = PanelProductos.crearCampo(ed ? usr.getPregunta4() : "Marca de tu primer coche?", 200);
        JTextField tfR4 = PanelProductos.crearCampo(ed ? usr.getRespuesta4() : "", 200);

        // --- Construcción de la Interfaz ---
        JPanel pMain = new JPanel();
        pMain.setLayout(new BoxLayout(pMain, BoxLayout.Y_AXIS));
        pMain.setBackground(new Color(39, 39, 42));
        pMain.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Sección 1: Datos Básicos
        JPanel pBasico = new JPanel(new GridLayout(0, 2, 10, 10));
        pBasico.setOpaque(false);
        pBasico.add(new JLabel("Nombre:") {{ setForeground(Color.WHITE); }}); pBasico.add(tfNombre);
        pBasico.add(new JLabel("Email:") {{ setForeground(Color.WHITE); }}); pBasico.add(tfEmail);
        pBasico.add(new JLabel("Rol:") {{ setForeground(Color.WHITE); }}); pBasico.add(cbRol);
        pMain.add(pBasico);
        pMain.add(Box.createRigidArea(new Dimension(0, 15)));

        // Sección 2: Contraseñas (solo nuevo)
        if (!ed) {
            JPanel pPass = new JPanel(new GridLayout(0, 2, 10, 10));
            pPass.setOpaque(false);
            pPass.add(new JLabel("Contraseña:") {{ setForeground(Color.WHITE); }}); pPass.add(tfPass);
            pPass.add(new JLabel("Confirmar:") {{ setForeground(Color.WHITE); }}); pPass.add(tfPassConfirm);
            pMain.add(pPass);
            pMain.add(Box.createRigidArea(new Dimension(0, 15)));
        }

        // Sección 3: Permisos
        pMain.add(new JLabel("Configuración de Acceso") {{ setForeground(new Color(16, 185, 129)); setFont(getFont().deriveFont(Font.BOLD)); }});
        pMain.add(Box.createRigidArea(new Dimension(0, 5)));
        pMain.add(pPermisos);
        pMain.add(Box.createRigidArea(new Dimension(0, 15)));

        // Sección 4: Seguridad
        pMain.add(new JLabel("Preguntas de Seguridad") {{ setForeground(new Color(16, 185, 129)); setFont(getFont().deriveFont(Font.BOLD)); }});
        pMain.add(Box.createRigidArea(new Dimension(0, 5)));
        JPanel pSeguridad = new JPanel(new GridLayout(0, 2, 10, 8));
        pSeguridad.setOpaque(false);
        pSeguridad.add(new JLabel("Pregunta 1:") {{ setForeground(Color.WHITE); }}); pSeguridad.add(tfP1);
        pSeguridad.add(new JLabel("Respuesta 1:") {{ setForeground(Color.WHITE); }}); pSeguridad.add(tfR1);
        pSeguridad.add(new JLabel("Pregunta 2:") {{ setForeground(Color.WHITE); }}); pSeguridad.add(tfP2);
        pSeguridad.add(new JLabel("Respuesta 2:") {{ setForeground(Color.WHITE); }}); pSeguridad.add(tfR2);
        pSeguridad.add(new JLabel("Pregunta 3:") {{ setForeground(Color.WHITE); }}); pSeguridad.add(tfP3);
        pSeguridad.add(new JLabel("Respuesta 3:") {{ setForeground(Color.WHITE); }}); pSeguridad.add(tfR3);
        pSeguridad.add(new JLabel("Pregunta 4:") {{ setForeground(Color.WHITE); }}); pSeguridad.add(tfP4);
        pSeguridad.add(new JLabel("Respuesta 4:") {{ setForeground(Color.WHITE); }}); pSeguridad.add(tfR4);
        pMain.add(pSeguridad);

        while (true) {
            int r = JOptionPane.showConfirmDialog(this, new JScrollPane(pMain) {{ 
                setPreferredSize(new Dimension(580, 550));
                setBorder(null);
            }}, ed ? "Editar Usuario" : "Nuevo Usuario", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            
            if (r != JOptionPane.OK_OPTION) break;

            try {
                Usuario u = ed ? usr : new Usuario();
                u.setNombre(tfNombre.getText().trim());
                u.setEmail(tfEmail.getText().trim());
                u.setRol(cbRol.getSelectedItem().toString());
                
                // Construir cadena de permisos detallada
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < modulos.length; i++) {
                    StringBuilder modSb = new StringBuilder();
                    for (int j = 0; j < acciones.length; j++) {
                        if (matrix[i][j].isSelected()) modSb.append(codigos[j]);
                    }
                    if (sb.length() > 0) sb.append(",");
                    sb.append(normalizar(modulos[i])).append(":").append(modSb.toString());
                }
                u.setPermisos(sb.toString());

                u.setPregunta1(tfP1.getText()); u.setRespuesta1(tfR1.getText());
                u.setPregunta2(tfP2.getText()); u.setRespuesta2(tfR2.getText());
                u.setPregunta3(tfP3.getText()); u.setRespuesta3(tfR3.getText());
                u.setPregunta4(tfP4.getText()); u.setRespuesta4(tfR4.getText());

                if (ed) {
                    dao.actualizar(u);
                    // Actualizar sesión en vivo si el usuario editado es el actual
                    CX.SesionUsuario sesionActual = CX.SesionUsuario.getInstancia();
                    if (u.getId() == sesionActual.getUsuarioId()) {
                        sesionActual.setPermisos(u.getPermisos());
                        if (Dashboard.getInstancia() != null) {
                            Dashboard.getInstancia().refrescarMenu();
                            if (sesionActual.tienePermiso("Usuarios", "Ver")) {
                                Dashboard.getInstancia().mostrarPanel(new PanelUsuarios());
                            } else {
                                Dashboard.getInstancia().mostrarPanel(new PanelInicio());
                            }
                            JOptionPane.showMessageDialog(this, "Tus permisos se han actualizado y el menú ha sido refrescado.", "Actualización Exitosa", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                } else {
                    String pass = new String(tfPass.getPassword());
                    String passConfirm = new String(tfPassConfirm.getPassword());

                    if (!pass.equals(passConfirm)) {
                        JOptionPane.showMessageDialog(this, "Las contraseñas no coinciden.", "Error", JOptionPane.ERROR_MESSAGE);
                        tfPass.setText("");
                        tfPassConfirm.setText("");
                        continue;
                    }

                    if (!SeguridadArgon2.esSegura(pass)) {
                        JOptionPane.showMessageDialog(this, SeguridadArgon2.getRequisitosMensaje(), "Contraseña Débil", JOptionPane.WARNING_MESSAGE);
                        tfPass.setText("");
                        tfPassConfirm.setText("");
                        continue;
                    }

                    u.setPassword(pass);
                    dao.insertar(u);
                }
                cargar();
                break;
            } catch (SQLException e) { 
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage()); 
                break;
            }
        }
    }

    private void editarSeleccionado() {
        int row = tabla.getSelectedRow();
        if (row < 0) return;
        try {
            long id = (long) modelo.getValueAt(row, 0);
            Usuario u = dao.buscarPorId(id);
            if (u != null) dialogoUsuario(u);
        } catch (SQLException e) { JOptionPane.showMessageDialog(this, e.getMessage()); }
    }

    private void cambiarPassword() {
        int row = tabla.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Seleccione un usuario."); return; }
        long id = (long) modelo.getValueAt(row, 0);

        try {
            Usuario u = dao.buscarPorId(id);
            if (u == null) return;

            // Desafío de Seguridad Aleatorio
            Random rnd = new Random();
            int pNum = rnd.nextInt(4) + 1;
            String preg = "";
            String respCorrecta = "";

            switch(pNum) {
                case 1: preg = u.getPregunta1(); respCorrecta = u.getRespuesta1(); break;
                case 2: preg = u.getPregunta2(); respCorrecta = u.getRespuesta2(); break;
                case 3: preg = u.getPregunta3(); respCorrecta = u.getRespuesta3(); break;
                case 4: preg = u.getPregunta4(); respCorrecta = u.getRespuesta4(); break;
            }

            if (preg == null || preg.isEmpty()) {
                JOptionPane.showMessageDialog(this, "El usuario no tiene preguntas configuradas.");
                return;
            }

            String inputResp = JOptionPane.showInputDialog(this, "Validación de Seguridad:\n\n" + preg, "Seguridad", JOptionPane.QUESTION_MESSAGE);
            
            if (inputResp != null && inputResp.equalsIgnoreCase(respCorrecta)) {
                JPasswordField pf = new JPasswordField();
                JPasswordField pfConfirm = new JPasswordField();
                Object[] message = {
                    "Respuesta Correcta ✅\nNueva contraseña:", pf,
                    "Confirmar contraseña:", pfConfirm
                };
                
                while (true) {
                    int opt = JOptionPane.showConfirmDialog(this, message, "Nueva Clave", JOptionPane.OK_CANCEL_OPTION);
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

                    dao.cambiarPassword(id, pass);
                    JOptionPane.showMessageDialog(this, "✅ Contraseña actualizada.");
                    break;
                }
            } else if (inputResp != null) {
                JOptionPane.showMessageDialog(this, "❌ Respuesta incorrecta.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException e) { JOptionPane.showMessageDialog(this, e.getMessage()); }
    }

    private void eliminarSeleccionado() {
        int row = tabla.getSelectedRow();
        if (row < 0) return;
        int r = JOptionPane.showConfirmDialog(this, "¿Eliminar usuario?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) {
            try {
                dao.eliminar((long) modelo.getValueAt(row, 0));
                cargar();
            } catch (SQLException e) { JOptionPane.showMessageDialog(this, e.getMessage()); }
        }
    }
    private String normalizar(String s) {
        if (s == null) return "";
        return s.toUpperCase()
                .replace("Á", "A").replace("É", "E").replace("Í", "I").replace("Ó", "O").replace("Ú", "U")
                .replaceAll("[^A-Z0-9:,]", "");
    }

    private String obtenerEtiquetaAccion(String modulo, char codigo) {
        if (codigo == 'X') return "Exportar";
        
        switch (modulo) {
            case "Inventario":
                if (codigo == 'C') return "Entrada";
                if (codigo == 'E') return "Ajuste";
                if (codigo == 'D') return "Salida";
                break;
            case "Facturas":
                if (codigo == 'C') return "Emitir";
                if (codigo == 'E') return "Pagar";
                if (codigo == 'D') return "Anular";
                break;
            case "Clientes":
            case "Proveedores":
            case "Usuarios":
                if (codigo == 'C') return "Registrar";
                break;
            case "Configuracion":
                if (codigo == 'E') return "Modificar";
                break;
        }
        
        switch (codigo) {
            case 'C': return "Crear";
            case 'E': return "Editar";
            case 'D': return "Eliminar";
        }
        return "";
    }

    private boolean esAccionAplicable(String modulo, char codigo) {
        switch (modulo) {
            case "Reportes":
                return codigo == 'X'; // Solo Exportar
            case "Configuracion":
                return codigo == 'E'; // Solo Editar/Modificar
            case "Facturas":
                return codigo == 'C' || codigo == 'E' || codigo == 'D';
            case "Inventario":
                return codigo == 'C' || codigo == 'E' || codigo == 'D';
            case "Productos":
                return codigo == 'C' || codigo == 'E' || codigo == 'D';
            case "Categorias":
            case "Clientes":
            case "Proveedores":
            case "Usuarios":
                return codigo == 'C' || codigo == 'E' || codigo == 'D'; // Crear, Editar, Eliminar
        }
        return true;
    }
}
