package Utils;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.Toolkit;
import java.util.regex.Pattern;

public class ValidadorCampos extends DocumentFilter {
    private int limite;
    private boolean soloNumeros;
    private boolean permitirDecimal;
    private Pattern patron;

    public ValidadorCampos(int limite, boolean soloNumeros, boolean permitirDecimal) {
        this.limite = limite;
        this.soloNumeros = soloNumeros;
        this.permitirDecimal = permitirDecimal;
        
        if (soloNumeros) {
            if (permitirDecimal) {
                // Permite signo negativo opcional, números y un solo punto o coma decimal
                patron = Pattern.compile("^-?[0-9]*[.,]?[0-9]*$");
            } else {
                // Solo números enteros, permite signo negativo opcional
                patron = Pattern.compile("^-?[0-9]*$");
            }
        }
    }

    @Override
    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
        if (string == null) return;
        verificarYProcesar(fb, offset, 0, string, attr, true);
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
        if (text == null) return;
        verificarYProcesar(fb, offset, length, text, attrs, false);
    }

    private void verificarYProcesar(FilterBypass fb, int offset, int length, String text, AttributeSet attrs, boolean isInsert) throws BadLocationException {
        // 1. Verificar límite de caracteres
        int currentLength = fb.getDocument().getLength();
        if ((currentLength + text.length() - length) > limite) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }

        // 2. Verificar contenido si es numérico
        if (soloNumeros) {
            String currentContent = fb.getDocument().getText(0, currentLength);
            String before = currentContent.substring(0, offset);
            String after = currentContent.substring(offset + length);
            String result = before + text + after;

            if (!patron.matcher(result).matches()) {
                Toolkit.getDefaultToolkit().beep();
                return;
            }
        }

        if (isInsert) {
            super.insertString(fb, offset, text, attrs);
        } else {
            super.replace(fb, offset, length, text, attrs);
        }
    }
}
