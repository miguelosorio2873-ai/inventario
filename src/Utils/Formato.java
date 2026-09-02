package Utils;

/**
 * Formateo de montos en dólares (USD) con su equivalente en bolívares (Bs),
 * usando la tasa definida en Utils.Config.getTasaVES().
 */
public class Formato {

    /** "$1,234.56" con separador de miles. */
    public static String usd(double v) {
        return String.format("$%,.2f", v);
    }

    /** "Bs45.00" equivalente de un monto en dólares. */
    public static String bs(double vUsd) {
        return String.format("Bs%.2f", vUsd * Utils.Config.getTasaVES());
    }

    /** "Bs45.00" de un monto YA expresado en Bs (congelado, sin recalcular por la tasa). */
    public static String bsFixed(double vBs) {
        return String.format("Bs%.2f", vBs);
    }

    /** "Bs 45,000.00" de un monto YA expresado en Bs (congelado, sin recalcular por la tasa). */
    public static String bsMilesFixed(double vBs) {
        return String.format("Bs %,.2f", vBs);
    }

    /** "Bs 45,000.00" con separador de miles. */
    public static String bsMiles(double vUsd) {
        return String.format("Bs %,.2f", vUsd * Utils.Config.getTasaVES());
    }

    /** "$1,234.56 (Bs 55,555.20)" dólar + equivalente en Bs. */
    public static String usdBs(double vUsd) {
        return String.format("$%,.2f (Bs %,.2f)", vUsd, vUsd * Utils.Config.getTasaVES());
    }

    /** "$1,234.56 (Bs 55,555.20)" dólar + equivalente en Bs CONGELADO (sin recalcular). */
    public static String usdBsFixed(double vUsd, double vBsFrozen) {
        return String.format("$%,.2f (Bs %,.2f)", vUsd, vBsFrozen);
    }

    /** "$1,234.56 (Bs 55,555.20)" dólar + equivalente en Bs usando la tasa del DÍA indicado
     *  (desde el historial tasa_cambio; si no hay registro, usa la vigente). */
    public static String usdBsConFecha(double vUsd, java.util.Date fecha) {
        double t = new DAO.TasaCambioDAO().obtenerPorFecha(fecha);
        return String.format("$%,.2f (Bs %,.2f)", vUsd, vUsd * t);
    }

    private Formato() {
    }
}
