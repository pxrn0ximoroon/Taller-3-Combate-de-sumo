package red;

/**
 * Constantes del protocolo de comunicación cliente-servidor.
 *
 * <p>Define el puerto y los mensajes intercambiados. Al centralizar
 * aquí las constantes, si cambia el protocolo solo se modifica esta clase.</p>
 *
 * <p><b>Flujo de comunicación:</b></p>
 * <pre>
 * CLIENTE                              SERVIDOR
 *   | --[ObjectOutputStream: Rikishi]--> |  envía luchador serializado
 *   |                                    |  espera al segundo; combate
 *   | <--[String: "GANADOR"/"PERDEDOR"]- |  notifica resultado
 *   | <--[Object: Rikishi ganador]------- |  envía datos del ganador
 *   | --[String: "CLIENTE_LISTO"]------> |  cliente confirma y termina
 * </pre>
 *
 * <p><b>Principio OCP:</b> extender el protocolo = agregar constantes aquí,
 * sin tocar la lógica de los hilos.</p>
 *
 * @author Programación Avanzada - Universidad Distrital
 * @version 1.0
 */
public final class Protocolo {

    /** Puerto TCP en el que escucha el servidor. */
    public static final int PUERTO = 9090;

    /** Mensaje del servidor indicando que este cliente ganó. */
    public static final String MSG_GANADOR = "GANADOR";

    /** Mensaje del servidor indicando que este cliente perdió. */
    public static final String MSG_PERDEDOR = "PERDEDOR";

    /** Mensaje del cliente confirmando que recibió el resultado y va a cerrar. */
    public static final String MSG_CLIENTE_LISTO = "CLIENTE_LISTO";

    /** Constructor privado: clase de constantes, no instanciable. */
    private Protocolo() { }
}
