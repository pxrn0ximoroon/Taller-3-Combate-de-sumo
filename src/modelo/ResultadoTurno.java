package modelo;

/**
 * Objeto de transferencia que encapsula el resultado de un turno.
 *
 * <p>Cuando un luchador ejecuta su turno en el {@link Dohyo}, se
 * produce un resultado: la técnica usada y si logró sacar al rival.
 * Esta clase transporta esos dos datos de vuelta al hilo que ejecutó
 * el turno.</p>
 *
 * <p><b>Principio SRP:</b> solo transporta datos, no tiene lógica.</p>
 *
 * @author Programación Avanzada - Universidad Distrital
 * @version 1.0
 */
public class ResultadoTurno {

    /** La técnica aplicada en este turno. */
    private final Kimarite tecnicaUsada;

    /** Indica si la técnica logró sacar al oponente del dohyō. */
    private final boolean sacoAlOponente;

    /**
     * Construye el resultado de un turno.
     *
     * @param tecnicaUsada   El kimarite que fue aplicado.
     * @param sacoAlOponente true si el oponente quedó fuera del dohyō.
     */
    public ResultadoTurno(Kimarite tecnicaUsada, boolean sacoAlOponente) {
        this.tecnicaUsada = tecnicaUsada;
        this.sacoAlOponente = sacoAlOponente;
    }

    /**
     * Obtiene la técnica usada en el turno.
     *
     * @return El kimarite aplicado.
     */
    public Kimarite getTecnicaUsada() { return tecnicaUsada; }

    /**
     * Indica si el oponente fue sacado del ring.
     *
     * @return true si la técnica fue efectiva para sacar al rival.
     */
    public boolean isSacoAlOponente() { return sacoAlOponente; }
}
