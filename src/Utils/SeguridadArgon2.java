package Utils;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

/**
 * Utilidad de criptografía avanzada usando Argon2id.
 * Implementa seguridad de grado empresarial siguiendo recomendaciones de OWASP.
 */
public class SeguridadArgon2 {

    // Parámetros recomendados: 
    // iteraciones: 10, memoria: 64MB (65536 KB), paralelismo: 1
    private static final int ITERACIONES = 10;
    private static final int MEMORIA_KB = 65536;
    private static final int PARALELISMO = 1;

    private static final Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);

    /**
     * Genera un hash seguro de la contraseña usando Argon2id.
     * @param password La contraseña en texto plano.
     * @return El hash codificado.
     */
    public static String generarHash(String password) {
        char[] passwordChars = password.toCharArray();
        try {
            return argon2.hash(ITERACIONES, MEMORIA_KB, PARALELISMO, passwordChars);
        } finally {
            // Limpieza inmediata de memoria sensible
            argon2.wipeArray(passwordChars);
        }
    }

    /**
     * Verifica la contraseña ingresada contra el hash almacenado.
     * @param hash El hash de la base de datos.
     * @param password La contraseña ingresada por el usuario.
     * @return true si coincide, false en caso contrario.
     */
    public static boolean verificar(String hash, String password) {
        char[] passwordChars = password.toCharArray();
        try {
            return argon2.verify(hash, passwordChars);
        } finally {
            argon2.wipeArray(passwordChars);
        }
    }
    /**
     * Verifica si una contraseña cumple con los requisitos de seguridad:
     * - Mínimo 8 caracteres
     * - Al menos una mayúscula
     * - Al menos una minúscula
     * - Al menos un número
     * - Al menos un carácter especial (@#$%^&+=!)
     * @param password La contraseña a validar.
     * @return true si es segura, false si no.
     */
    public static boolean esSegura(String password) {
        if (password == null || password.length() < 8) return false;
        
        boolean tieneMayuscula = false;
        boolean tieneMinuscula = false;
        boolean tieneNumero = false;
        boolean tieneEspecial = false;
        String especiales = "@#$%^&+=!._-";

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) tieneMayuscula = true;
            else if (Character.isLowerCase(c)) tieneMinuscula = true;
            else if (Character.isDigit(c)) tieneNumero = true;
            else if (especiales.indexOf(c) != -1) tieneEspecial = true;
        }

        return tieneMayuscula && tieneMinuscula && tieneNumero && tieneEspecial;
    }

    public static String getRequisitosMensaje() {
        return "La contraseña debe tener al menos:\n" +
               "- 8 caracteres\n" +
               "- Una letra mayúscula\n" +
               "- Una letra minúscula\n" +
               "- Un número\n" +
               "- Un carácter especial (@#$%^&+=!._-)";
    }
}
