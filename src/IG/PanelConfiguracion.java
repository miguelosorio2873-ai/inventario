package IG;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.FlatLaf;
import javax.swing.*;
import java.awt.*;

public class PanelConfiguracion extends JPanel {

    public PanelConfiguracion() {
        setBackground(new Color(24, 24, 27));
        setLayout(new BorderLayout(0, 15));
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel titulo = new JLabel("Configuración del Sistema");
        try {
            com.formdev.flatlaf.extras.FlatSVGIcon icon = new com.formdev.flatlaf.extras.FlatSVGIcon(getClass().getResource("/IMG/cog.svg")).derive(24, 24);
            icon.setColorFilter(new com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter(c -> Color.WHITE));
            titulo.setIcon(icon);
            titulo.setIconTextGap(10);
        } catch (Exception e) {}
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titulo.setForeground(Color.WHITE);
        header.add(titulo, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // Body
        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(10, 10, 10, 10);
        gc.weightx = 1.0;

        // --- Tarjeta: Apariencia ---
        JPanel cardTema = crearTarjeta("Apariencia y Tema");
        JPanel temaContent = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        temaContent.setOpaque(false);
        
        JButton btnOscuro = PanelProductos.crearBoton("🌙 Modo Oscuro", new Color(31, 41, 55));
        btnOscuro.addActionListener(e -> cambiarTema(true));
        
        JButton btnClaro = PanelProductos.crearBoton("☀️ Modo Claro", new Color(229, 231, 235));
        btnClaro.setForeground(Color.BLACK);
        btnClaro.addActionListener(e -> cambiarTema(false));

        temaContent.add(btnOscuro);
        temaContent.add(btnClaro);
        cardTema.add(temaContent, BorderLayout.CENTER);

        // --- Tarjeta: Datos de la Empresa ---
        JPanel cardEmpresa = crearTarjeta("Datos de la Empresa (Facturación)");
        JPanel formEmpresa = new JPanel(new GridBagLayout());
        formEmpresa.setOpaque(false);
        formEmpresa.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        GridBagConstraints gcf = new GridBagConstraints();
        gcf.fill = GridBagConstraints.HORIZONTAL;
        gcf.insets = new Insets(8, 5, 8, 5);
        
        int row = 0;
        JTextField tfNombre = PanelProductos.crearCampo("Inventario Pro S.A.", 100);
        addFormRow(formEmpresa, gcf, row++, "Nombre Empresa:", tfNombre);
        
        JTextField tfNit = PanelProductos.crearCampo("J-12345678-9", 20);
        addFormRow(formEmpresa, gcf, row++, "NIT / RIF:", tfNit);
        
        JTextField tfTel = PanelProductos.crearCampo("0212-5550000", 20);
        addFormRow(formEmpresa, gcf, row++, "Teléfono:", tfTel);
        
        JTextField tfDir = PanelProductos.crearCampo("Av. Principal, Edificio Central", 200);
        addFormRow(formEmpresa, gcf, row++, "Dirección:", tfDir);

        JButton btnGuardar = PanelProductos.crearBoton("💾 Guardar Cambios", new Color(16, 185, 129));
        btnGuardar.addActionListener(e -> JOptionPane.showMessageDialog(this, "Datos de la empresa guardados (Simulado)."));
        
        gcf.gridy = row;
        gcf.gridx = 0;
        gcf.gridwidth = 2;
        gcf.insets = new Insets(20, 5, 5, 5);
        formEmpresa.add(btnGuardar, gcf);
        cardEmpresa.add(formEmpresa, BorderLayout.CENTER);

        // --- Tarjeta: Configuración de Moneda ---
        JPanel cardMoneda = crearTarjeta("Tasa de Cambio (Bolívares / USD)");
        JPanel formMoneda = new JPanel(new GridBagLayout());
        formMoneda.setOpaque(false);
        formMoneda.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        GridBagConstraints gcm = new GridBagConstraints();
        gcm.fill = GridBagConstraints.HORIZONTAL;
        gcm.insets = new Insets(8, 5, 8, 5);
        
        JTextField tfTasa = PanelProductos.crearCampo(String.valueOf(Utils.Config.getTasaVES()), 15);
        addFormRow(formMoneda, gcm, 0, "Tasa Actual (Bs por 1$):", tfTasa);
        
        JButton btnGuardarTasa = PanelProductos.crearBoton("💾 Actualizar Tasa", new Color(16, 185, 129));
        btnGuardarTasa.addActionListener(e -> {
            try {
                double nt = Double.parseDouble(tfTasa.getText().replace(",", "."));
                Utils.Config.setTasaVES(nt);
                JOptionPane.showMessageDialog(this, "✅ Tasa actualizada a: Bs " + nt);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "El formato de la tasa es inválido. Use punto como decimal.");
            }
        });
        
        gcm.gridy = 1;
        gcm.gridx = 0;
        gcm.gridwidth = 2;
        gcm.insets = new Insets(20, 5, 5, 5);
        formMoneda.add(btnGuardarTasa, gcm);
        cardMoneda.add(formMoneda, BorderLayout.CENTER);

        // Añadir tarjetas al body
        gc.gridy = 0; body.add(cardMoneda, gc);
        gc.gridy = 1; body.add(cardTema, gc);
        gc.gridy = 2; body.add(cardEmpresa, gc);
        
        // Espaciador final
        gc.gridy = 3; gc.weighty = 1.0;
        body.add(Box.createGlue(), gc);

        add(new JScrollPane(body) {{
            setBorder(null);
            getViewport().setOpaque(false);
            setOpaque(false);
        }}, BorderLayout.CENTER);
    }

    private void cambiarTema(boolean oscuro) {
        try {
            if (oscuro) {
                UIManager.setLookAndFeel(new FlatDarkLaf());
                UIManager.put("Panel.background", new Color(24, 24, 27)); // Mantener colores base oscuros
            } else {
                UIManager.setLookAndFeel(new FlatLightLaf());
                UIManager.put("Panel.background", new Color(243, 244, 246));
            }
            
            // Re-aplicar propiedades globales comunes
            UIManager.put("Button.arc", 15);
            UIManager.put("Component.arc", 15);
            UIManager.put("TextComponent.arc", 15);
            UIManager.put("Component.focusWidth", 2);
            UIManager.put("Component.innerFocusWidth", 1);
            UIManager.put("ScrollBar.thumbArc", 999);
            
            FlatLaf.updateUI();
            
            // Forzar actualización del frame completo
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window != null) {
                SwingUtilities.updateComponentTreeUI(window);
            }
            
        } catch (Exception ex) {
            System.err.println("Fallo al inicializar LaF");
        }
    }

    private JPanel crearTarjeta(String titulo) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(39, 39, 42));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(63, 63, 70), 1, true),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        
        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setForeground(new Color(16, 185, 129));
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        card.add(lblTitulo, BorderLayout.NORTH);
        return card;
    }

    private void addFormRow(JPanel form, GridBagConstraints gc, int row, String label, JComponent field) {
        gc.gridy = row;
        gc.gridx = 0;
        gc.weightx = 0.3;
        gc.gridwidth = 1;
        JLabel lbl = new JLabel(label);
        lbl.setForeground(new Color(180, 180, 180));
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        form.add(lbl, gc);
        
        gc.gridx = 1;
        gc.weightx = 0.7;
        form.add(field, gc);
    }
}
