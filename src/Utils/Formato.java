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

    /** "Bs 45,000.00" con separador de miles. */
    public static String bsMiles(double vUsd) {
        return String.format("Bs %,.2f", vUsd * Utils.Config.getTasaVES());
    }

    /** "$1,234.56 (Bs 55,555.20)" dólar + equivalente en Bs. */
    public static String usdBs(double vUsd) {
        return String.format("$%,.2f (Bs %,.2f)", vUsd, vUsd * Utils.Config.getTasaVES());
    }

    private Formato() {
    }
}
