package Utils;

import java.io.*;
import java.util.Properties;

public class Config {
    // Apuntamos al archivo local properties en la raíz de src
    private static final String FILE_PATH = "src/application.properties";

    public static double getTasaVES() {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(FILE_PATH)) {
            props.load(in);
            return Double.parseDouble(props.getProperty("tasa.ves", "45.0"));
        } catch (Exception e) {
            return 45.0; // Valor por defecto si hay un error o no está parametrizado
        }
    }

    public static void setTasaVES(double tasa) {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(FILE_PATH)) {
            props.load(in);
        } catch (Exception e) {
        } // Ignore if doesn't exist

        props.setProperty("tasa.ves", String.valueOf(tasa));

        try (FileOutputStream out = new FileOutputStream(FILE_PATH)) {
            props.store(out, "Configuraciones Globales de Inventario");
        } catch (Exception e) {
            System.err.println("Error guardando tasa: " + e.getMessage());
        }
    }
}
