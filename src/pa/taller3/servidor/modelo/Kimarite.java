package pa.taller3.servidor.modelo;

import java.io.Serializable;

/**
 * Representa una técnica de combate en el sumo (Kimarite).
 *
 * <p>Un Kimarite tiene nombre y descripción. Las técnicas son cargadas
 * desde un archivo {@code .properties} y asignadas al luchador según
 * su especialidad. Implementa {@link Serializable} para enviarse
 * por socket entre cliente y servidor.</p>
 *
 * <p><b>Principio SRP:</b> solo gestiona los datos de una técnica.</p>
 *
 * @author Programación Avanzada - Universidad Distrital
 * @version 1.0
 */
public class Kimarite implements Serializable {

    /** UID de serialización para compatibilidad entre JVM. */
    private static final long serialVersionUID = 1L;

    /** Nombre de la técnica, ej: "Yorikiri". */
    private final String nombre;

    /** Descripción de cómo se aplica la técnica. */
    private final String descripcion;

    /**
     * Crea un Kimarite con nombre y descripción.
     *
     * @param nombre      Nombre de la técnica de sumo.
     * @param descripcion Descripción breve de la técnica.
     */
    public Kimarite(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    /**
     * Obtiene el nombre de la técnica.
     *
     * @return Nombre del kimarite.
     */
    public String getNombre() { return nombre; }

    /**
     * Obtiene la descripción de la técnica.
     *
     * @return Descripción del kimarite.
     */
    public String getDescripcion() { return descripcion; }

    /**
     * Representación de texto del kimarite mostrando su nombre.
     *
     * @return Nombre de la técnica.
     */
    @Override
    public String toString() { return nombre; }
}
