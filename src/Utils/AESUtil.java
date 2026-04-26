package Utils;

import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * Utilidad para encriptación simétrica AES-128.
 * Permite guardar datos que pueden ser recuperados (desencriptados).
 */
public class AESUtil {
    
    // Llave maestra de 16 caracteres para AES-128
    private static final String SECRET_KEY = "AntigravityKey26"; // En producción, esto debe estar en una variable de entorno/config segura
    private static final String ALGORITHM = "AES";

    /**
     * Encripta un texto plano.
     */
    public static String encriptar(String textoPlano) {
        if (textoPlano == null || textoPlano.isEmpty()) return textoPlano;
        try {
            SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] bytesEncriptados = cipher.doFinal(textoPlano.getBytes());
            return Base64.getEncoder().encodeToString(bytesEncriptados);
        } catch (Exception e) {
            System.err.println("Error al encriptar: " + e.getMessage());
            return textoPlano;
        }
    }

    /**
     * Desencripta un texto encriptado en Base64.
     */
    public static String desencriptar(String textoEncriptado) {
        if (textoEncriptado == null || textoEncriptado.isEmpty()) return textoEncriptado;
        try {
            SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] bytesDesencriptados = cipher.doFinal(Base64.getDecoder().decode(textoEncriptado));
            return new String(bytesDesencriptados);
        } catch (Exception e) {
            // Si no puede desencriptar, probablemente ya era texto plano
            return textoEncriptado;
        }
    }
}
