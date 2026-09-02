package IG;

import Utils.Licencia;
import Utils.Config;
import Utils.UI;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Pantalla de bloqueo cuando la licencia está vencida.
 * Muestra el aviso con el correo del propietario y permite renovar
 * ingresando la clave del propietario y una nueva fecha de vencimiento.
 */
public class PanelLicenciaVencida extends JPanel {

    private final Runnable onLicenciaOK;

    public PanelLicenciaVencida(Runnable onLicenciaOK) {
        this.onLicenciaOK = onLicenciaOK;
        setBackground(new Color(24, 24, 27));
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 20, 8, 20);
        gc.gridx = 0;

        JLabel icono = new JLabel("⚠️", SwingConstants.CENTER);
        icono.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 56));
        gc.gridy = 0;
        add(icono, gc);

        JLabel titulo = new JLabel("LICENCIA VENCIDA", SwingConstants.CENTER);
        titulo.setFont(UI.TITULO);
        titulo.setForeground(new Color(239, 68, 68));
        gc.gridy = 1;
        add(titulo, gc);

        String correo = Config.getLicenciaCorreo();
        JLabel msg = new JLabel(
            "<html><div style='text-align:center'>El tiempo de uso del sistema ha finalizado.<br>" +
            "Para renovar su licencia, contacte a:<br><br>" +
            "<b style='color:#10b981'>" + correo + "</b></div></html>",
            SwingConstants.CENTER);
        msg.setFont(UI.TEXTO);
        msg.setForeground(new Color(200, 200, 200));
        gc.gridy = 2;
        add(msg, gc);

        JButton btnRenovar = PanelProductos.crearBoton("🔑 Renovar Licencia", new Color(16, 185, 129));
        btnRenovar.setFont(UI.BOTON);
        btnRenovar.setPreferredSize(new Dimension(230, 44));
        btnRenovar.addActionListener(e -> renovar());
        gc.gridy = 3;
        gc.insets = new Insets(18, 20, 6, 20);
        add(btnRenovar, gc);

        JButton btnSalir = new JButton("Cerrar Sesión");
        btnSalir.setFont(UI.NOTA);
        btnSalir.setForeground(new Color(150, 150, 150));
        btnSalir.setBorder(null);
        btnSalir.setContentAreaFilled(false);
        btnSalir.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSalir.addActionListener(e -> {
            CX.SesionUsuario.getInstancia().cerrarSesion();
            Window w = SwingUtilities.getWindowAncestor(this);
            if (w != null) w.dispose();
            new LOG().setVisible(true);
        });
        gc.gridy = 4;
        gc.insets = new Insets(4, 20, 0, 20);
        add(btnSalir, gc);
    }

    private void renovar() {
        JPasswordField pf = new JPasswordField();
        pf.setBackground(new Color(45, 45, 45));
        pf.setForeground(Color.WHITE);
        pf.setFont(UI.CAMPO);
        JOptionPane.showMessageDialog(this,
            new Object[]{ "Ingrese la clave del propietario para renovar:", pf },
            "Renovar Licencia", JOptionPane.PLAIN_MESSAGE);

        String clave = new String(pf.getPassword());
        if (clave.isEmpty()) return;

        if (!Licencia.verificarClavePropietario(clave)) {
            JOptionPane.showMessageDialog(this, "❌ Contraseña incorrecta.", "Renovación", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JTextField tfNueva = PanelProductos.crearCampo("", 10);
        JLabel lblFormato = new JLabel("Formato: dd/MM/yyyy");
        lblFormato.setForeground(new Color(150, 150, 150));
        lblFormato.setFont(UI.NOTA);
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(new Color(39, 39, 42));
        JLabel lInstr = new JLabel("Nueva fecha de vencimiento:");
        lInstr.setForeground(Color.WHITE);
        p.add(lInstr);
        p.add(Box.createRigidArea(new Dimension(0, 6)));
        p.add(tfNueva);
        p.add(Box.createRigidArea(new Dimension(0, 4)));
        p.add(lblFormato);

        int r = JOptionPane.showConfirmDialog(this, p, "Nueva Licencia", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r != JOptionPane.OK_OPTION) return;

        String fechaStr = tfNueva.getText().trim();
        if (fechaStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe ingresar una fecha de vencimiento.", "Renovación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        LocalDate fecha;
        try {
            fecha = LocalDate.parse(fechaStr, DateTimeFormatter.ofPattern("d/M/yyyy"));
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Formato de fecha inválido. Use dd/MM/yyyy.", "Renovación", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Long uid = CX.SesionUsuario.getInstancia().getUsuarioId();
        if (uid != null) {
            try {
                new DAO.UsuarioDAO().actualizarLicencia(uid, true, fecha.format(DateTimeFormatter.ISO_LOCAL_DATE));
            } catch (Exception ex) {
                System.err.println("Error actualizando licencia del usuario: " + ex.getMessage());
            }
        }
        try {
            new DAO.BitacoraDAO().registrar("Licencia", "Renovar",
                "Licencia renovada hasta: " + fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        } catch (Exception ex) {}

        JOptionPane.showMessageDialog(this, "✅ Licencia renovada hasta el " + fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ".");
        if (onLicenciaOK != null) onLicenciaOK.run();
    }
}