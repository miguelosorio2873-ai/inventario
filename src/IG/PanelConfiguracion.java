package IG;

import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import Utils.UI;

public class PanelConfiguracion extends JPanel {

    public PanelConfiguracion() {
        CX.SesionUsuario sesion = CX.SesionUsuario.getInstancia();
        boolean puedeEditar = sesion.tienePermiso("Configuracion", "Editar");
        
        setBackground(new Color(24, 24, 27));
        setLayout(new BorderLayout(0, 15));
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel titulo = new JLabel("Configuración del Sistema");
        try {
            com.formdev.flatlaf.extras.FlatSVGIcon icon = new com.formdev.flatlaf.extras.FlatSVGIcon("IMG/cog.svg").derive(32, 32);
            icon.setColorFilter(new com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter(c -> Color.WHITE));
            titulo.setIcon(icon);
            titulo.setIconTextGap(10);
        } catch (Exception e) {}
        titulo.setFont(UI.TITULO);
        titulo.setForeground(Color.WHITE);
        header.add(titulo, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(10, 10, 10, 10);
        gc.weightx = 1.0;

        // --- Tarjeta: Moneda ---
        JPanel cardMoneda = crearTarjeta("Tasa de Cambio (Bolívares / USD)");
        JPanel formMoneda = new JPanel(new GridBagLayout());
        formMoneda.setOpaque(false);
        formMoneda.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        GridBagConstraints gcm = new GridBagConstraints();
        gcm.fill = GridBagConstraints.HORIZONTAL;
        gcm.insets = new Insets(8, 5, 8, 5);
        
        JTextField tfTasa = PanelProductos.crearCampo(String.valueOf(Utils.Config.getTasaVES()), 15);
        addFormRow(formMoneda, gcm, 0, "Tasa Actual (Bs por 1$):", tfTasa);
        
        JLabel lblEstadoApi = new JLabel(" ");
        lblEstadoApi.setFont(UI.NOTA);
        lblEstadoApi.setForeground(new Color(156, 163, 175));

        JPanel botonesTasa = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        botonesTasa.setOpaque(false);

        JButton btnGuardarTasa = PanelProductos.crearBoton("💾 Guardar", new Color(16, 185, 129));
        btnGuardarTasa.setEnabled(puedeEditar);
        btnGuardarTasa.addActionListener(e -> {
            try {
                double nt = Double.parseDouble(tfTasa.getText().replace(",", "."));
                Utils.Config.setTasaVES(nt);
                new DAO.BitacoraDAO().registrar("Configuracion", "Editar", "Tasa de cambio actualizada a: Bs " + nt);
                JOptionPane.showMessageDialog(this, "Tasa actualizada a: Bs " + nt);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "El formato de la tasa es inválido. Use punto como decimal.");
            }
        });

        JButton btnConsultarApi = PanelProductos.crearBoton("🔄 Consultar API", new Color(59, 130, 246));
        btnConsultarApi.setEnabled(puedeEditar);
        btnConsultarApi.addActionListener(e -> {
            btnConsultarApi.setEnabled(false);
            btnConsultarApi.setText("Consultando...");
            lblEstadoApi.setText("Obteniendo tasa del BCV...");
            lblEstadoApi.setForeground(new Color(245, 158, 11));

            new SwingWorker<Double, Void>() {
                @Override
                protected Double doInBackground() throws Exception {
                    HttpClient client = HttpClient.newHttpClient();
                    HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://ve.dolarapi.com/v1/dolares"))
                        .header("Accept", "application/json")
                        .GET()
                        .build();
                    HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString());
                    String body = resp.body();
                    // Primer elemento del array: {"promedio": 787.51, ...}
                    int idx = body.indexOf("\"promedio\"");
                    if (idx < 0) throw new Exception("Formato inesperado");
                    int colon = body.indexOf(':', idx);
                    int end = body.indexOf(',', colon);
                    if (end < 0) end = body.indexOf('}', colon);
                    return Double.parseDouble(body.substring(colon + 1, end).trim());
                }

                @Override
                protected void done() {
                    btnConsultarApi.setEnabled(true);
                    btnConsultarApi.setText("🔄 Consultar API");
                    try {
                        Double tasa = get();
                        tfTasa.setText(String.valueOf(tasa));
                        lblEstadoApi.setText("Tasa obtenida: Bs " + tasa + " (BCV)");
                        lblEstadoApi.setForeground(new Color(16, 185, 129));
                    } catch (Exception ex) {
                        lblEstadoApi.setText("Error: " + ex.getMessage());
                        lblEstadoApi.setForeground(new Color(239, 68, 68));
                    }
                }
            }.execute();
        });

        botonesTasa.add(btnGuardarTasa);
        botonesTasa.add(btnConsultarApi);
        
        gcm.gridy = 1; gcm.gridx = 0; gcm.gridwidth = 2;
        formMoneda.add(botonesTasa, gcm);

        gcm.gridy = 2; gcm.gridx = 0; gcm.gridwidth = 2;
        formMoneda.add(lblEstadoApi, gcm);

        cardMoneda.add(formMoneda, BorderLayout.CENTER);

        // --- Tarjeta: Respaldo Manual y Restauración ---
        JPanel cardBackup = crearTarjeta("Respaldo Manual y Restauración");
        JPanel backupContent = new JPanel(new BorderLayout(0, 10));
        backupContent.setOpaque(false);
        backupContent.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

        JLabel lblInfoBackup = new JLabel("Base de datos actual: " + CX.ConexionBD.getRutaBaseDatos());
        lblInfoBackup.setFont(UI.NOTA);
        lblInfoBackup.setForeground(new Color(156, 163, 175));

        JLabel lblEstadoBackup = new JLabel(" ");
        lblEstadoBackup.setFont(UI.NOTA);
        lblEstadoBackup.setForeground(new Color(156, 163, 175));

        JPanel botonesBackup = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        botonesBackup.setOpaque(false);

        JButton btnRespaldo = PanelProductos.crearBoton("⬆️ Hacer Respaldo", new Color(16, 185, 129));
        btnRespaldo.setEnabled(puedeEditar);
        btnRespaldo.addActionListener(e -> hacerRespaldo(lblEstadoBackup));

        JButton btnRestaurar = PanelProductos.crearBoton("⬇️ Restaurar Respaldo", new Color(245, 158, 11));
        btnRestaurar.setEnabled(puedeEditar);
        btnRestaurar.addActionListener(e -> restaurarRespaldo(lblEstadoBackup));

        botonesBackup.add(btnRespaldo);
        botonesBackup.add(btnRestaurar);

        backupContent.add(lblInfoBackup, BorderLayout.NORTH);
        backupContent.add(botonesBackup, BorderLayout.CENTER);
        backupContent.add(lblEstadoBackup, BorderLayout.SOUTH);
        cardBackup.add(backupContent, BorderLayout.CENTER);

        // --- Tarjeta: Respaldo Automático ---
        JPanel cardAuto = crearTarjeta("Respaldo Automático");
        JPanel autoForm = new JPanel(new GridBagLayout());
        autoForm.setOpaque(false);
        autoForm.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        GridBagConstraints gca = new GridBagConstraints();
        gca.fill = GridBagConstraints.HORIZONTAL;
        gca.insets = new Insets(6, 5, 6, 5);

        JTextField tfAutoFrecuencia = PanelProductos.crearCampo(String.valueOf(Utils.Config.getRespaldoFrecuencia()), 8);
        String unidadActual = Utils.Config.getRespaldoUnidad();
        JComboBox<String> cbAutoUnidad = new JComboBox<>(new String[]{"MINUTOS", "HORAS", "DIAS"});
        cbAutoUnidad.setSelectedItem(unidadActual);

        JTextField tfAutoRuta = PanelProductos.crearCampo(Utils.Config.getRespaldoDirectorio(), 300);
        JButton btnAutoRuta = PanelProductos.crearBoton("Elegir", new Color(59, 130, 246));
        btnAutoRuta.addActionListener(e -> {
            JFileChooser ch = new JFileChooser();
            ch.setDialogTitle("Carpeta para Respaldos Automáticos");
            ch.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (ch.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                tfAutoRuta.setText(ch.getSelectedFile().getAbsolutePath());
            }
        });

        JPanel rowAutoRuta = new JPanel(new BorderLayout(6, 0));
        rowAutoRuta.setOpaque(false);
        rowAutoRuta.add(tfAutoRuta, BorderLayout.CENTER);
        rowAutoRuta.add(btnAutoRuta, BorderLayout.EAST);

        JLabel lblAutoEstado = new JLabel(" ");
        lblAutoEstado.setFont(UI.NOTA);
        lblAutoEstado.setForeground(new Color(156, 163, 175));

        addFormRow(autoForm, gca, 0, "Cada:", tfAutoFrecuencia);
        addFormRow(autoForm, gca, 1, "Unidad:", cbAutoUnidad);
        addFormRow(autoForm, gca, 2, "Carpeta destino:", rowAutoRuta);

        JButton btnAutoGuardar = PanelProductos.crearBoton("💾 Aplicar Respaldo Automático", new Color(16, 185, 129));
        btnAutoGuardar.setEnabled(puedeEditar);
        btnAutoGuardar.addActionListener(e -> {
            try {
                double frec = Double.parseDouble(tfAutoFrecuencia.getText().replace(",", ".").trim());
                if (frec <= 0) {
                    Utils.Config.setRespaldoFrecuencia(0);
                    CX.ConexionBD.detenerRespaldoAutomatico();
                    lblAutoEstado.setText("Respaldo automático desactivado.");
                    lblAutoEstado.setForeground(new Color(156, 163, 175));
                    JOptionPane.showMessageDialog(this, "Respaldo automático desactivado.");
                    return;
                }
                String ruta = tfAutoRuta.getText().trim();
                if (ruta.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Seleccione la carpeta destino del respaldo.");
                    return;
                }
                Utils.Config.setRespaldoFrecuencia(frec);
                Utils.Config.setRespaldoUnidad((String) cbAutoUnidad.getSelectedItem());
                Utils.Config.setRespaldoDirectorio(ruta);
                CX.ConexionBD.programarRespaldoAutomatico();
                new DAO.BitacoraDAO().registrar("Configuracion", "RespaldoAutomatico",
                    "Respaldo automatico activado cada " + frec + " " + cbAutoUnidad.getSelectedItem());
                lblAutoEstado.setText("Respaldos automáticos: cada " + frec + " " + cbAutoUnidad.getSelectedItem());
                lblAutoEstado.setForeground(new Color(16, 185, 129));
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "El valor de frecuencia es inválido.");
            }
        });

        gca.gridy = 3; gca.gridx = 0; gca.gridwidth = 2;
        gca.insets = new Insets(14, 5, 4, 5);
        autoForm.add(btnAutoGuardar, gca);
        gca.gridy = 4; gca.insets = new Insets(2, 5, 2, 5);
        autoForm.add(lblAutoEstado, gca);
        cardAuto.add(autoForm, BorderLayout.CENTER);

        // --- Tarjeta: Base de Datos Replicada (Espejo) ---
        JPanel cardReplica = crearTarjeta("Base de Datos Replicada (Espejo)");
        JPanel replicaForm = new JPanel(new GridBagLayout());
        replicaForm.setOpaque(false);
        replicaForm.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        GridBagConstraints gcr = new GridBagConstraints();
        gcr.fill = GridBagConstraints.HORIZONTAL;
        gcr.insets = new Insets(6, 5, 6, 5);

        String rutaReplicaActual = Utils.Config.getRutaReplica();
        boolean replicaActiva = rutaReplicaActual != null && !rutaReplicaActual.trim().isEmpty();
        JTextField tfReplicaRuta = PanelProductos.crearCampo(rutaReplicaActual, 300);
        JButton btnReplicaRuta = PanelProductos.crearBoton("Elegir", new Color(59, 130, 246));
        btnReplicaRuta.addActionListener(e -> {
            JFileChooser ch = new JFileChooser();
            ch.setDialogTitle("Seleccionar base de datos replica (.db)");
            ch.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Base de datos SQLite (*.db)", "db"));
            if (ch.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                tfReplicaRuta.setText(ch.getSelectedFile().getAbsolutePath());
            }
        });

        JPanel rowReplicaRuta = new JPanel(new BorderLayout(6, 0));
        rowReplicaRuta.setOpaque(false);
        rowReplicaRuta.add(tfReplicaRuta, BorderLayout.CENTER);
        rowReplicaRuta.add(btnReplicaRuta, BorderLayout.EAST);

        JLabel lblReplicaEstado = new JLabel(replicaActiva ? "Replicación activa" : "Replicación desactivada");
        lblReplicaEstado.setFont(UI.NOTA);
        lblReplicaEstado.setForeground(replicaActiva ? new Color(16, 185, 129) : new Color(156, 163, 175));

        addFormRow(replicaForm, gcr, 0, "Archivo replica:", rowReplicaRuta);

        JPanel botonesReplica = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        botonesReplica.setOpaque(false);

        JButton btnReplicaActivar = PanelProductos.crearBoton("✅ Activar Replica", new Color(16, 185, 129));
        btnReplicaActivar.setEnabled(puedeEditar);
        btnReplicaActivar.addActionListener(e -> {
            String ruta = tfReplicaRuta.getText().trim();
            if (ruta.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Seleccione el archivo de base de datos replica.");
                return;
            }
            Utils.Config.setRutaReplica(ruta);
            CX.ConexionBD.sincronizarReplica();
            new DAO.BitacoraDAO().registrar("Configuracion", "Replica", "Replicacion activada hacia: " + ruta);
            lblReplicaEstado.setText("Replicación activa");
            lblReplicaEstado.setForeground(new Color(16, 185, 129));
            JOptionPane.showMessageDialog(this, "Replicación activada.\nA partir de ahora cada guardado sincronizará la réplica.");
        });

        JButton btnReplicaSincronizar = PanelProductos.crearBoton("🔄 Sincronizar ahora", new Color(59, 130, 246));
        btnReplicaSincronizar.addActionListener(e -> {
            CX.ConexionBD.sincronizarReplica();
            lblReplicaEstado.setText("Sincronizado");
            lblReplicaEstado.setForeground(new Color(16, 185, 129));
            new DAO.BitacoraDAO().registrar("Configuracion", "Replica", "Replica sincronizada manualmente");
            JOptionPane.showMessageDialog(this, "Réplica sincronizada.");
        });

        JButton btnReplicaDesactivar = PanelProductos.crearBoton("Desactivar", new Color(239, 68, 68));
        btnReplicaDesactivar.setEnabled(puedeEditar);
        btnReplicaDesactivar.addActionListener(e -> {
            Utils.Config.setRutaReplica("");
            new DAO.BitacoraDAO().registrar("Configuracion", "Replica", "Replicacion desactivada");
            lblReplicaEstado.setText("Replicación desactivada");
            lblReplicaEstado.setForeground(new Color(156, 163, 175));
            tfReplicaRuta.setText("");
            JOptionPane.showMessageDialog(this, "Replicación desactivada.");
        });

        botonesReplica.add(btnReplicaActivar);
        botonesReplica.add(btnReplicaSincronizar);
        botonesReplica.add(btnReplicaDesactivar);

        gcr.gridy = 1; gcr.gridx = 0; gcr.gridwidth = 2;
        gcr.insets = new Insets(10, 5, 4, 5);
        replicaForm.add(botonesReplica, gcr);
        gcr.gridy = 2; gcr.insets = new Insets(2, 5, 2, 5);
        replicaForm.add(lblReplicaEstado, gcr);
        cardReplica.add(replicaForm, BorderLayout.CENTER);

        // Añadir tarjetas
        gc.gridy = 0; body.add(cardMoneda, gc);
        gc.gridy = 1; body.add(cardAuto, gc);
        gc.gridy = 2; body.add(cardReplica, gc);
        gc.gridy = 3; body.add(cardBackup, gc);
        gc.gridy = 4; gc.weighty = 1.0; body.add(Box.createGlue(), gc);

        add(new JScrollPane(body) {{
            setBorder(null);
            getViewport().setOpaque(false);
            setOpaque(false);
        }}, BorderLayout.CENTER);
    }

    private void hacerRespaldo(JLabel estado) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Guardar Respaldo de Base de Datos");
        chooser.setSelectedFile(new java.io.File("respaldo_inventario_" + fechaHora() + ".db"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        java.io.File dest = chooser.getSelectedFile();
        if (!dest.getName().toLowerCase().endsWith(".db")) {
            dest = new java.io.File(dest.getAbsolutePath() + ".db");
        }
        try {
            estado.setText("Creando respaldo...");
            estado.setForeground(new Color(245, 158, 11));
            CX.ConexionBD.respaldarBaseDatos(dest);
            new DAO.BitacoraDAO().registrar("Configuracion", "Respaldo", "Respaldo de BD creado en: " + dest.getAbsolutePath());
            estado.setText("Respaldo creado: " + dest.getAbsolutePath());
            estado.setForeground(new Color(16, 185, 129));
            JOptionPane.showMessageDialog(this, "Respaldo guardado correctamente:\n" + dest.getAbsolutePath());
        } catch (Exception ex) {
            estado.setText("Error al respaldar: " + ex.getMessage());
            estado.setForeground(new Color(239, 68, 68));
        }
    }

    private void restaurarRespaldo(JLabel estado) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Seleccionar Respaldo a Restaurar");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Base de datos SQLite (*.db)", "db"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        java.io.File origen = chooser.getSelectedFile();

        int confirm = JOptionPane.showConfirmDialog(this,
                "Se reemplazará la base de datos actual con el respaldo seleccionado.\n"
                + "Se recomienda hacer un respaldo previo.\n\n¿Desea continuar?",
                "Restaurar Base de Datos", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            estado.setText("Restaurando...");
            estado.setForeground(new Color(245, 158, 11));
            CX.ConexionBD.restaurarBaseDatos(origen);
            new DAO.BitacoraDAO().registrar("Configuracion", "Restauracion", "BD restaurada desde: " + origen.getAbsolutePath());
            estado.setText("Base de datos restaurada correctamente.");
            estado.setForeground(new Color(16, 185, 129));
            JOptionPane.showMessageDialog(this,
                    "Base de datos restaurada correctamente.\n"
                    + "Reinicie la aplicación para aplicar los cambios.");
        } catch (IllegalArgumentException ex) {
            estado.setText(ex.getMessage());
            estado.setForeground(new Color(239, 68, 68));
        } catch (Exception ex) {
            estado.setText("Error al restaurar: " + ex.getMessage());
            estado.setForeground(new Color(239, 68, 68));
        }
    }

    private String fechaHora() {
        return new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
    }

    private JPanel crearTarjeta(String titulo) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(39, 39, 42));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(63, 63, 70), 1, true),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(UI.TEXTO_NEGRITA);
        lblTitulo.setForeground(new Color(16, 185, 129));
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        card.add(lblTitulo, BorderLayout.NORTH);
        return card;
    }

    private void addFormRow(JPanel form, GridBagConstraints gc, int row, String label, JComponent field) {
        gc.gridy = row; gc.gridx = 0; gc.weightx = 0.3; gc.gridwidth = 1;
        JLabel lbl = new JLabel(label);
        lbl.setForeground(new Color(180, 180, 180));
        lbl.setFont(UI.NOTA);
        form.add(lbl, gc);
        gc.gridx = 1; gc.weightx = 0.7;
        form.add(field, gc);
    }
}