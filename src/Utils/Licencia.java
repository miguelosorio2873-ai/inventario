package Utils;

import DAO.UsuarioDAO;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Manejo de la licencia del sistema: fecha de vencimiento y expiración.
 * La lógica es puramente utilitaria; la renovación pide la clave del propietario.
 */
public class Licencia {

    /** Fecha de vencimiento configurada como LocalDate, o null si no hay licencia. */
    public static LocalDate getVencimiento() {
        String iso = Config.getLicenciaVencimiento();
        if (iso == null || iso.trim().isEmpty()) return null;
        try {
            return LocalDate.parse(iso.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /** ¿Hay una licencia configurada (con fecha de vencimiento)? */
    public static boolean hayLicencia() {
        return getVencimiento() != null;
    }

    /** ¿La licencia está vencida? (solo si hay licencia y hoy supera el vencimiento). */
    public static boolean estaVencida() {
        LocalDate v = getVencimiento();
        if (v == null) return false;
        return LocalDate.now().isAfter(v);
    }

    /** Días restantes de licencia (negativo si ya venció). Si no hay licencia devuelve null. */
    public static Long diasRestantes() {
        LocalDate v = getVencimiento();
        if (v == null) return null;
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), v);
    }

    /**
     * Verifica la contraseña del propietario (usuario 'miguel') para renovar la licencia.
     */
    public static boolean verificarClavePropietario(String password) {
        if (password == null || password.trim().isEmpty()) return false;
        try {
            return new UsuarioDAO().verificarClavePropietario(Config.getLicenciaCorreo(), password);
        } catch (Exception e) {
            return false;
        }
    }
}