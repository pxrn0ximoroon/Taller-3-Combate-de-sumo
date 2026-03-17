package servidor.red;

import modelo.ResultadoTurno;
import modelo.Rikishi;

/**
 * Interfaz observadora (patrón Observer) para eventos del combate.
 *
 *
 * <p><b>Principio ISP:</b> cada método es específico a un evento.
 * Ninguna clase implementadora necesita métodos que no usa.</p>
 *
 * <p><b>Principio DIP:</b> {@link HiloLuchador} depende de esta
 * abstracción, no de la clase concreta del controlador.</p>
 *
 * @author Programación Avanzada - Universidad Distrital
 * @version 1.0
 */
public interface EventoCombateListener {

    /**
     * Se dispara cuando un luchador llega y se conecta al servidor.
     *
     * @param luchador El luchador que acaba de conectarse.
     */
    void onLuchadorLlego(Rikishi luchador);

    /**
     * Se dispara cuando los dos luchadores están en el dohyō
     * y el combate está por comenzar.
     *
     * @param luchador1 Primer luchador.
     * @param luchador2 Segundo luchador.
     */
    void onCombateInicia(Rikishi luchador1, Rikishi luchador2);

    /**
     * Se dispara cada vez que un luchador ejecuta una técnica.
     *
     * @param luchador El luchador que atacó en este turno.
     * @param resultado Técnica usada y si fue efectiva.
     */
    void onTurnoEjecutado(Rikishi luchador, ResultadoTurno resultado);

    /**
     * Se dispara cuando el combate termina y hay un ganador.
     *
     * @param ganador El luchador que ganó el combate.
     */
    void onCombateTermino(Rikishi ganador);

    /**
     * Se dispara cuando un cliente confirma haber recibido el resultado.
     *
     * @param luchador El luchador cuyo cliente confirmó.
     */
    void onClienteConfirmo(Rikishi luchador);

    /**
     * Se dispara cuando ocurre un error en la comunicación.
     *
     * @param mensaje Descripción del error.
     */
    void onError(String mensaje);
}
