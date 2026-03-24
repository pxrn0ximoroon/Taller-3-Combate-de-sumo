package pa.taller3.servidor.modelo;

import java.util.List;
import java.util.ArrayList;

/**
 * Representa un luchador de sumo (Rikishi).
 *
 * <p>Cada rikishi tiene nombre, peso, victorias acumuladas,
 * un arreglo de técnicas ({@link Kimarite}) que domina,
 * una referencia a su rival actual y un estado que indica
 * si se encuentra dentro o fuera del dohyō.</p>
 *
 * <p><b>Principio SRP:</b> solo almacena y expone los datos
 * del luchador. La lógica del combate reside en {@code Dohyo}.</p>
 *
 * @author Programación Avanzada - Universidad Distrital
 * @version 1.0
 */
public class Rikishi {

    /** Nombre del luchador. */
    private String nombre;

    /** Peso del luchador en kilogramos. */
    private double peso;

    /** Número de combates ganados. */
    private int victorias;

    /**
     * Lista de kimarites que domina este luchador.
     * Se seleccionan aleatoriamente durante el combate.
     */
    private List<Kimarite> kimarites;

    /**
     * Referencia al luchador rival en el combate actual.
     * Es {@code null} si no está en combate.
     */
    private Rikishi rival;

    /**
     * Estado del luchador dentro del dohyō.
     * {@code true} = dentro del ring, {@code false} = fuera (perdió).
     */
    private boolean enDohyo;

    /**
     * Construye un Rikishi con sus datos básicos.
     * Por defecto está dentro del dohyō y sin victorias.
     *
     * @param nombre    Nombre del luchador.
     * @param peso      Peso en kilogramos.
     * @param kimarites Lista de técnicas que domina.
     */
    public Rikishi(String nombre, double peso, List<Kimarite> kimarites) {
        this.nombre     = nombre;
        this.peso       = peso;
        this.kimarites  = kimarites != null ? kimarites : new ArrayList<>();
        this.victorias  = 0;
        this.enDohyo    = true;
        this.rival      = null;
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    /**
     * @return Nombre del luchador.
     */
    public String getNombre() { return nombre; }

    /**
     * @return Peso del luchador en kg.
     */
    public double getPeso() { return peso; }

    /**
     * @return Número de victorias acumuladas.
     */
    public int getVictorias() { return victorias; }

    /**
     * @return Lista de kimarites que domina.
     */
    public List<Kimarite> getKimarites() { return kimarites; }

    /**
     * @return Rival actual, o {@code null} si no está en combate.
     */
    public Rikishi getRival() { return rival; }

    /**
     * @return {@code true} si el luchador está dentro del dohyō.
     */
    public boolean isEnDohyo() { return enDohyo; }

    // ── Setters ──────────────────────────────────────────────────────────────

    /**
     * @param nombre Nuevo nombre del luchador.
     */
    public void setNombre(String nombre) { this.nombre = nombre; }

    /**
     * @param peso Nuevo peso del luchador.
     */
    public void setPeso(double peso) { this.peso = peso; }

    /**
     * @param victorias Número de victorias a establecer.
     */
    public void setVictorias(int victorias) { this.victorias = victorias; }

    /**
     * @param kimarites Nueva lista de técnicas.
     */
    public void setKimarites(List<Kimarite> kimarites) { this.kimarites = kimarites; }

    /**
     * @param rival Luchador rival en el combate actual.
     */
    public void setRival(Rikishi rival) { this.rival = rival; }

    /**
     * @param enDohyo {@code true} si está dentro del ring.
     */
    public void setEnDohyo(boolean enDohyo) { this.enDohyo = enDohyo; }

    // ── Métodos de utilidad ───────────────────────────────────────────────────

    /**
     * Incrementa en uno el contador de victorias.
     */
    public void incrementarVictorias() { this.victorias++; }

    /**
     * Devuelve un kimarite aleatorio de la lista de técnicas del luchador.
     *
     * @return Un {@link Kimarite} aleatorio, o {@code null} si no tiene técnicas.
     */
    public Kimarite obtenerKimariteAleatorio() {
        if (kimarites == null || kimarites.isEmpty()) return null;
        int indice = (int) (Math.random() * kimarites.size());
        return kimarites.get(indice);
    }

    /**
     * Reinicia el estado del luchador para un nuevo combate.
     * Lo coloca dentro del dohyō y limpia el rival.
     */
    public void reiniciarEstado() {
        this.enDohyo = true;
        this.rival   = null;
    }

    /**
     * @return Representación en texto del luchador.
     */
    @Override
    public String toString() {
        return "Rikishi{nombre='" + nombre + "', peso=" + peso
             + ", victorias=" + victorias + ", enDohyo=" + enDohyo + "}";
    }
}