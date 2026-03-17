package util;

import modelo.Kimarite;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Utilidad para cargar kimarites desde un archivo {@code .properties}.
 *
 * <pre>
 *   kimarite.1.nombre=Yorikiri
 *   kimarite.1.descripcion=Empuje frontal sosteniendo el mawashi
 *   kimarite.2.nombre=Oshidashi
 *   kimarite.2.descripcion=Empuje directo sin agarre del mawashi
 * </pre>
 *
 * <p><b>Principio SRP:</b> esta clase solo lee y parsea el archivo.
 * No crea objetos de dominio más allá de {@link Kimarite}.</p>
 *
 * <p><b>Principio OCP:</b> si cambia el formato del archivo,
 * solo se modifica aquí.</p>
 *
 * @author Programación Avanzada - Universidad Distrital
 * @version 1.0
 */
public final class LectorKimarites {

    /** Constructor privado: clase utilitaria, no instanciable. */
    private LectorKimarites() { }

    /**
     * Carga la lista de kimarites desde un archivo {@code .properties}.
     *
     * <p>Lee entradas numeradas desde {@code kimarite.1} hasta que
     * no encuentre más. Si el archivo no existe o está vacío,
     * devuelve lista vacía.</p>
     *
     * @param rutaArchivo Ruta absoluta al archivo {@code .properties}.
     * @return Lista de {@link Kimarite} cargados. Vacía si hubo error.
     */
    public static List<Kimarite> cargarDesdeProperties(String rutaArchivo) {
        List<Kimarite> lista = new ArrayList<>();
        Properties props = new Properties();

        try (FileInputStream fis = new FileInputStream(rutaArchivo)) {
            props.load(fis);

            int i = 1;
            while (props.containsKey("kimarite." + i + ".nombre")) {
                String nombre = props.getProperty("kimarite." + i + ".nombre", "").trim();
                String desc   = props.getProperty("kimarite." + i + ".descripcion", "").trim();
                if (!nombre.isEmpty()) {
                    lista.add(new Kimarite(nombre, desc));
                }
                i++;
            }
        } catch (IOException e) {
            // El error se propaga como lista vacía; el controlador
            // lo detectará y mostrará el mensaje apropiado en la GUI.
            lista.clear();
        }

        return lista;
    }
}
