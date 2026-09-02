package Utils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.JWindow;
import javax.swing.border.LineBorder;

/**
 * Campo de fecha moderno tipo web (tema oscuro, acorde a FlatLaf):
 * una caja de texto con el dia/mes/ano junto a un boton de calendario
 * que despliega un selector visual de mes. El valor se mantiene en formato
 * ISO (yyyy-MM-dd) para persistencia; la caja muestra dd/MM/yyyy.
 */
public class DatePickerField extends JPanel {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter VISTA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final JTextField campo;
    private final JButton botonCal;
    private final Color fondo = new Color(30, 34, 42);
    private final Color borde = new Color(70, 74, 88);
    private final Color acento = new Color(16, 185, 129);
    private final Color texto = new Color(230, 230, 235);

    public DatePickerField() {
        setLayout(new BorderLayout(0, 0));
        setOpaque(false);

        campo = new JTextField();
        campo.setFont(UI.CAMPO);
        campo.setForeground(texto);
        campo.setBackground(fondo);
        campo.setCaretColor(texto);
        campo.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(borde, 1, true),
            BorderFactory.createEmptyBorder(7, 10, 7, 6)));
        campo.setEditable(true);
        campo.setHorizontalAlignment(JTextField.LEFT);

        botonCal = new JButton("📅");
        botonCal.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        botonCal.setFocusable(false);
        botonCal.setPreferredSize(new Dimension(42, campo.getPreferredSize().height + 10));
        botonCal.setBackground(borde);
        botonCal.setForeground(texto);
        botonCal.setBorder(BorderFactory.createEmptyBorder());
        botonCal.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        botonCal.addActionListener(e -> mostrarCalendario());

        add(campo, BorderLayout.CENTER);
        add(botonCal, BorderLayout.EAST);
    }

    public String getTexto() {
        return campo.getText().trim();
    }

    /** Devuelve la fecha en ISO (yyyy-MM-dd). Si el campo esta vacio devuelve la de hoy. */
    public String getTextoISO() {
        if (campo.getText().trim().isEmpty()) return LocalDate.now().format(ISO);
        try {
            return LocalDate.parse(campo.getText().trim(), VISTA).format(ISO);
        } catch (DateTimeParseException e) {
            try {
                return LocalDate.parse(campo.getText().trim(), ISO).format(ISO);
            } catch (DateTimeParseException e2) {
                return campo.getText().trim();
            }
        }
    }

    /** Establece una fecha en ISO (yyyy-MM-dd) o vacia si null. */
    public void setFechaISO(String iso) {
        if (iso == null || iso.trim().isEmpty()) {
            campo.setText("");
            return;
        }
        try {
            campo.setText(LocalDate.parse(iso.trim(), ISO).format(VISTA));
        } catch (Exception e) {
            campo.setText(iso.trim());
        }
    }

    @Override
    public void setEnabled(boolean en) {
        super.setEnabled(en);
        campo.setEnabled(en);
        botonCal.setEnabled(en);
    }

    public void onCambio(Runnable r) {
        campo.addActionListener(e -> r.run());
    }

    private JWindow ventanaCal;

    private void mostrarCalendario() {
        LocalDate seleccion = LocalDate.now();
        try {
            seleccion = LocalDate.parse(campo.getText().trim(), VISTA);
        } catch (Exception e) {
            try {
                seleccion = LocalDate.parse(campo.getText().trim(), ISO);
            } catch (Exception e2) {
                seleccion = LocalDate.now();
            }
        }
        ocultar();
        ventanaCal = crearVentana(seleccion);
        Point p = campo.getLocationOnScreen();
        ventanaCal.setLocation(p.x, p.y + campo.getHeight() + 4);
        ventanaCal.pack();
        ventanaCal.setVisible(true);
    }

    private void ocultar() {
        if (ventanaCal != null) {
            ventanaCal.dispose();
            ventanaCal = null;
        }
    }

    private JWindow crearVentana(LocalDate base) {
        LocalDate[] baseRef = { base };
        Window owner = SwingUtilities.getWindowAncestor(this);
        JWindow v = new JWindow(owner);
        v.setBackground(fondo);
        v.setAlwaysOnTop(true);

        JPanel cont = new JPanel(new BorderLayout(0, 10));
        cont.setBackground(fondo);
        cont.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(borde, 1, true),
            BorderFactory.createEmptyBorder(12, 14, 14, 14)));

        // Cabecera: combos de MES y ANO
        String[] meses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                          "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        JComboBox<String> cbMes = new JComboBox<>(meses);
        JComboBox<Integer> cbAno = new JComboBox<>();
        for (int a = actualAno() - 15; a <= actualAno() + 15; a++) cbAno.addItem(a);
        estiloCombo(cbMes);
        estiloCombo(cbAno);

        JPanel cab = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        cab.setOpaque(false);
        cab.add(cbMes);
        cab.add(cbAno);
        cont.add(cab, BorderLayout.NORTH);

        JPanel rejilla = new JPanel(new GridLayout(7, 7, 3, 3));
        rejilla.setOpaque(false);
        cont.add(rejilla, BorderLayout.CENTER);

        // Pie: boton "Hoy" y "Cerrar"
        JButton hoy = new JButton("Hoy");
        estiloPie(hoy);
        JButton cerrar = new JButton("Cerrar");
        estiloPie(cerrar);
        JPanel pie = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        pie.setOpaque(false);
        pie.add(hoy);
        pie.add(cerrar);
        cont.add(pie, BorderLayout.SOUTH);

        Runnable actualizar = () -> {
            int mes = cbMes.getSelectedIndex();
            int ano = (Integer) cbAno.getSelectedItem();
            YearMonth ym = YearMonth.of(ano, mes + 1);
            baseRef[0] = ym.atDay(Math.min(baseRef[0].getDayOfMonth(), ym.lengthOfMonth()));
            rebuildDias(rejilla, baseRef[0], this::aplicar, v);
        };

        cbMes.addActionListener(e -> actualizar.run());
        cbAno.addActionListener(e -> actualizar.run());
        hoy.addActionListener(e -> aplicar(LocalDate.now()));
        cerrar.addActionListener(e -> ocultar());

        // Seleccion inicial
        cbMes.setSelectedIndex(baseRef[0].getMonthValue() - 1);
        cbAno.setSelectedItem(baseRef[0].getYear());
        rebuildDias(rejilla, baseRef[0], this::aplicar, v);

        v.add(cont);
        return v;
    }

    private void estiloPie(JButton b) {
        b.setFont(UI.NOTA);
        b.setForeground(acento);
        b.setBackground(new Color(38, 44, 54));
        b.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        b.setFocusable(false);
    }

    private void estiloCombo(JComboBox<?> cb) {
        cb.setFont(UI.NOTA);
        cb.setForeground(texto);
        cb.setBackground(new Color(40, 46, 58));
        cb.setBorder(BorderFactory.createLineBorder(borde, 1, true));
    }

    private int actualAno() {
        return LocalDate.now().getYear();
    }

    private void rebuildDias(JPanel rejilla, LocalDate base, java.util.function.Consumer<LocalDate> seleccion, JWindow v) {
        rejilla.removeAll();
        String[] dias = {"L", "M", "X", "J", "V", "S", "D"};
        for (String d : dias) {
            JLabel h = new JLabel(d, SwingConstants.CENTER);
            h.setFont(UI.NOTA);
            h.setForeground(acento);
            rejilla.add(h);
        }
        YearMonth ym = YearMonth.of(base.getYear(), base.getMonth());
        LocalDate primero = ym.atDay(1);
        int offset = (primero.getDayOfWeek().getValue() + 6) % 7; // lunes=0
        int total = ym.lengthOfMonth();
        LocalDate hoy = LocalDate.now();
        for (int i = 0; i < offset; i++) rejilla.add(new JLabel(""));
        for (int d = 1; d <= total; d++) {
            LocalDate dia = ym.atDay(d);
            JLabel cell = new JLabel(String.valueOf(d), SwingConstants.CENTER);
            cell.setFont(UI.CAMPO);
            cell.setOpaque(true);
            cell.setBackground(fondo);
            cell.setForeground(texto);
            if (dia.equals(hoy)) {
                cell.setForeground(acento);
                cell.setFont(UI.TEXTO_NEGRITA);
            }
            final LocalDate f = dia;
            cell.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    considerarFondo(cell);
                    seleccion.accept(f);
                }
                public void mouseEntered(MouseEvent e) {
                    if (!f.equals(hoy)) cell.setBackground(new Color(50, 58, 72));
                }
                public void mouseExited(MouseEvent e) {
                    if (!f.equals(hoy)) cell.setBackground(fondo);
                }
            });
            rejilla.add(cell);
        }
        rejilla.revalidate();
        rejilla.repaint();
    }

    private void considerarFondo(JLabel cell) {
        cell.setBackground(new Color(50, 58, 72));
    }

    private void aplicar(LocalDate fecha) {
        campo.setText(fecha.format(VISTA));
        ocultar();
    }
}