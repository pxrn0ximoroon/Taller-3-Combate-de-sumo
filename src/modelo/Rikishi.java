package modelo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Representa un luchador de sumo (Rikishi).
 *
 * <p>Es la entidad principal del sistema. Contiene: nombre, peso,
 * victorias, lista de kimarites que domina, referencia a su rival,
 * y estado (dentro o fuera del dohyō).</p>
 *
 * <p><b>Principio SRP:</b> solo gestiona datos del luchador.
 * Sin lógica de red ni de GUI.</p>
 *
 * @author Programación Avanzada - Universidad Distrital
 * @version 1.0
 */
public class Rikishi implements Serializable {

    /** UID de serialización. */
    private static final long serialVersionUID = 1L;

    /** Nombre del luchador. */
    private String nombre;

    /** Peso del luchador en kilogramos. */
    private double peso;

    /** Número de victorias acumuladas. */
    private int victorias;

    /**
     * Lista de técnicas que domina este luchador.
     * Cada luchador tiene su propio subconjunto de las 82 técnicas oficiales.
     */
    private List<Kimarite> kimarites;

    /**
     * Estado respecto al dohyō.
     * {@code volatile} porque dos hilos distintos lo leen y escriben.
     * true = dentro del ring; false = eliminado.
     */
    private volatile boolean dentroDohyo;

    /**
     * Referencia al rival en el combate actual.
     * {@code transient} para no serializarla (evita referencia circular).
     */
    private transient Rikishi rival;

    /**
     * Generador aleatorio para selección de técnicas.
     * {@code transient} porque Random no es útil serializar.
     * Se reinicializa en {@link #readObject} tras deserialización por socket.
     */
    private transient Random random;

    /**
     * Construye un luchador con sus datos básicos.
     * Inicia dentro del dohyō y sin técnicas asignadas aún.
     *
     * @param nombre    Nombre del rikishi.
     * @param peso      Peso en kilogramos.
     * @param victorias Victorias previas acumuladas.
     */
    public Rikishi(String nombre, double peso, int victorias) {
        this.nombre = nombre;
        this.peso = peso;
        this.victorias = victorias;
        this.kimarites = new ArrayList<>();
        this.dentroDohyo = true;
        this.random = new Random();
    }

    /**
     * Selecciona aleatoriamente una técnica del repertorio del luchador.
     *
     * <p>Genera un índice aleatorio entre 0 y {@code kimarites.size()-1}.
     * Esto asegura que no se usa siempre la misma técnica, como exige
     * el enunciado.</p>
     *
     * @return Un {@link Kimarite} aleatorio, o {@code null} si no hay técnicas.
     */
    public Kimarite seleccionarKimariteAleatorio() {
        if (kimarites == null || kimarites.isEmpty()) return null;
        return kimarites.get(random.nextInt(kimarites.size()));
    }

    /**
     * Agrega una técnica al repertorio del luchador.
     *
     * @param k La técnica a agregar. Se ignora si es null.
     */
    public void agregarKimarite(Kimarite k) {
        if (k != null) kimarites.add(k);
    }

    /** Incrementa en uno el contador de victorias. */
    public void incrementarVictorias() { victorias++; }

    // ── Getters y Setters ────────────────────────────────────────────────────

    /** @return Nombre del luchador. */
    public String getNombre() { return nombre; }

    /** @return Peso del luchador en kg. */
    public double getPeso() { return peso; }

    /** @return Número de victorias acumuladas. */
    public int getVictorias() { return victorias; }

    /** @return Lista de kimarites del luchador. */
    public List<Kimarite> getKimarites() { return kimarites; }

    /** @return true si el luchador está dentro del dohyō. */
    public boolean isDentroDohyo() { return dentroDohyo; }

    /**
     * Establece el estado del luchador.
     *
     * @param dentroDohyo true = dentro; false = eliminado.
     */
    public void setDentroDohyo(boolean dentroDohyo) { this.dentroDohyo = dentroDohyo; }

    /** @return El rival asignado para este combate. */
    public Rikishi getRival() { return rival; }

    /**
     * Asigna el rival para el combate actual.
     *
     * @param rival El luchador oponente.
     */
    public void setRival(Rikishi rival) { this.rival = rival; }

    /**
     * Reinicializa campos transient después de deserialización por socket.
     *
     * <p>Java llama este método automáticamente al reconstruir el objeto.
     * Sin esto, {@code random} queda {@code null} y
     * {@link #seleccionarKimariteAleatorio()} lanza NullPointerException
     * en el servidor.</p>
     *
     * @param ois Stream de deserialización.
     * @throws IOException            Si hay error de lectura.
     * @throws ClassNotFoundException Si falta alguna clase.
     */
    private void readObject(ObjectInputStream ois)
            throws IOException, ClassNotFoundException {
        ois.defaultReadObject();   // restaurar campos normales
        this.random = new Random(); // reinicializar transient
    }

    /**
     * Representación en texto del luchador para logs.
     *
     * @return Cadena con nombre, peso y victorias.
     */
    @Override
    public String toString() {
        return String.format("Rikishi[%s | %.1f kg | %d victorias]",
                nombre, peso, victorias);
    }
}
