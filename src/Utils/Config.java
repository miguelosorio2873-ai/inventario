package Utils;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

public class Config {
    private static final Path PROJECT_DIR = Paths.get(System.getProperty("user.dir"));
    private static final String FILE_PATH = "src" + File.separator + "application.properties";
    private static java.time.LocalTime horaUltimaActualizacion = null;

    public static java.time.LocalTime getHoraUltimaActualizacion() {
        return horaUltimaActualizacion;
    }

    public static void setHoraUltimaActualizacion(java.time.LocalTime hora) {
        horaUltimaActualizacion = hora;
    }

    private static Path resolvePath() {
        Path p = PROJECT_DIR.resolve(FILE_PATH);
        if (Files.exists(p)) return p;
        // fallback: try from class location
        try {
            Path classDir = Paths.get(Config.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            p = classDir.resolve("../../" + FILE_PATH).normalize();
            if (Files.exists(p)) return p;
        } catch (Exception e) {}
        return Paths.get(FILE_PATH);
    }

    public static double getTasaVES() {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(resolvePath().toFile())) {
            props.load(in);
            return Double.parseDouble(props.getProperty("tasa.ves", "45.0"));
        } catch (Exception e) {
            return 45.0;
        }
    }

    public static void setTasaVES(double tasa) {
        Path path = resolvePath();
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(path.toFile())) {
            props.load(in);
        } catch (Exception e) {}
        props.setProperty("tasa.ves", String.valueOf(tasa));
        try (FileOutputStream out = new FileOutputStream(path.toFile())) {
            props.store(out, "Configuraciones Globales de Inventario");
        } catch (Exception e) {
            System.err.println("Error guardando tasa: " + e.getMessage());
        }
    }

    /** Tasa del Euro (BCV) en bolívares. */
    public static double getTasaEuroVES() {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(resolvePath().toFile())) {
            props.load(in);
            return Double.parseDouble(props.getProperty("tasa.eur", "0.0"));
        } catch (Exception e) {
            return 0.0;
        }
    }

    public static void setTasaEuroVES(double tasa) {
        Path path = resolvePath();
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(path.toFile())) {
            props.load(in);
        } catch (Exception e) {}
        props.setProperty("tasa.eur", String.valueOf(tasa));
        try (FileOutputStream out = new FileOutputStream(path.toFile())) {
            props.store(out, "Configuraciones Globales de Inventario");
        } catch (Exception e) {
            System.err.println("Error guardando tasa euro: " + e.getMessage());
        }
    }

    // ---- Base de datos replicada (espejo) ----

    /** Ruta de la base de datos replica ("" si esta desactivada). */
    public static String getRutaReplica() {
        return getProp("replica.ruta", "");
    }

    public static void setRutaReplica(String ruta) {
        setProp("replica.ruta", ruta);
    }

    // ---- Respaldo automatico ----

    /** Frecuencia de respaldo (0 = desactivado). */
    public static double getRespaldoFrecuencia() {
        try { return Double.parseDouble(getProp("auto.frecuencia", "0")); }
        catch (Exception e) { return 0; }
    }

    public static void setRespaldoFrecuencia(double valor) {
        setProp("auto.frecuencia", String.valueOf(valor));
    }

    /** Unidad de respaldo: MINUTOS / HORAS / DIAS. */
    public static String getRespaldoUnidad() {
        return getProp("auto.unidad", "MINUTOS");
    }

    public static void setRespaldoUnidad(String unidad) {
        setProp("auto.unidad", unidad);
    }

    /** Directorio donde se guarda el respaldo automatico (sobrescribe respaldo_inventario.db). */
    public static String getRespaldoDirectorio() {
        return getProp("auto.ruta", "");
    }

    public static void setRespaldoDirectorio(String ruta) {
        setProp("auto.ruta", ruta);
    }

    private static String getProp(String clave, String def) {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(resolvePath().toFile())) {
            props.load(in);
        } catch (Exception e) {}
        return props.getProperty(clave, def) == null ? def : props.getProperty(clave, def);
    }

    private static void setProp(String clave, String valor) {
        Path path = resolvePath();
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(path.toFile())) {
            props.load(in);
        } catch (Exception e) {}
        props.setProperty(clave, valor);
        try (FileOutputStream out = new FileOutputStream(path.toFile())) {
            props.store(out, "Configuraciones Globales de Inventario");
        } catch (Exception e) {
            System.err.println("Error guardando config: " + e.getMessage());
        }
    }
}
