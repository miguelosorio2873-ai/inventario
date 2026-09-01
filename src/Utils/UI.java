package Utils;

import java.awt.Font;

/**
 * Tipografia uniforme de todo el sistema.
 * Titulo 27px, cuerpo/campos/botones 16px, tablas 16px y encabezado 15px.
 */
public class UI {

    public static final Font TITULO = new Font("Segoe UI", Font.BOLD, 27);
    public static final Font TEXTO = new Font("Segoe UI", Font.PLAIN, 16);
    public static final Font TEXTO_NEGRITA = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font CAMPO = new Font("Segoe UI", Font.PLAIN, 16);
    public static final Font BOTON = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font TABLA = new Font("Segoe UI", Font.PLAIN, 16);
    public static final Font TABLA_ENCABEZADO = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font NOTA = new Font("Segoe UI", Font.PLAIN, 14);

    /** Alto de fila estándar de las tablas. */
    public static final int FILA_ALTO = 36;

    private UI() {
    }
}
