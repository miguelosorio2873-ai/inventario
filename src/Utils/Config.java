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
        // Modo desarrollo: src/application.properties junto al proyecto
        Path p = PROJECT_DIR.resolve(FILE_PATH);
        if (Files.exists(p)) return p;
        // fallback desde la ubicacion de las clases (../../src)
        try {
            Path classDir = Paths.get(Config.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            p = classDir.resolve("../../" + FILE_PATH).normalize();
            if (Files.exists(p)) return p;
            // Modo instalado: application.properties junto al .class/jar
            p = classDir.resolve("application.properties").normalize();
            if (Files.exists(p)) return p;
        } catch (Exception e) {}
        // Modo instalado: application.properties en el directorio de trabajo
        Path instalado = PROJECT_DIR.resolve("application.properties");
        if (Files.exists(instalado)) return instalado;
        return Paths.get("application.properties");
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

    // ---- Licencia ----

    /** Correo del propietario (usuario 'miguel') que puede renovar la licencia. */
    public static String getLicenciaCorreo() {
        String c = getProp("licencia.correo", "miguelosorio2873@gmail.com");
        return (c == null || c.trim().isEmpty()) ? "miguelosorio2873@gmail.com" : c.trim();
    }

    public static void setLicenciaCorreo(String correo) {
        setProp("licencia.correo", correo == null ? "" : correo.trim());
    }

    /** Fecha de vencimiento de la licencia en formato ISO (yyyy-MM-dd); vacio = sin vencimiento. */
    public static String getLicenciaVencimiento() {
        return getProp("licencia.vencimiento", "");
    }

    public static void setLicenciaVencimiento(String fechaIso) {
        setProp("licencia.vencimiento", fechaIso == null ? "" : fechaIso.trim());
    }

    /** ¿La licencia tiene un vencimiento configurado? */
    public static boolean hayLicencia() {
        return !getLicenciaVencimiento().isEmpty();
    }

    /** Nombre visible del propietario / empresa para el mensaje de renovación. */
    public static String getLicenciaTitular() {
        String t = getProp("licencia.titular", "Miguel Osorio");
        return (t == null || t.trim().isEmpty()) ? "Miguel Osorio" : t.trim();
    }

    public static void setLicenciaTitular(String titular) {
        setProp("licencia.titular", titular == null ? "" : titular.trim());
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
