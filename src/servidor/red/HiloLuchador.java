package servidor.red;

import modelo.Dohyo;
import modelo.ResultadoTurno;
import modelo.Rikishi;
import red.Protocolo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Hilo del servidor que atiende la comunicación con un cliente.
 *
 * <p>El servidor crea un {@code HiloLuchador} por cada cliente que
 * se conecta. Este hilo se encarga de todo el ciclo de vida:</p>
 * <ol>
 *   <li>Recibir el {@link Rikishi} serializado desde el cliente.</li>
 *   <li>Subirlo al {@link Dohyo} (puede esperar al segundo luchador).</li>
 *   <li>Ejecutar turnos en bucle hasta que el combate termine.</li>
 *   <li>Notificar el resultado al cliente vía socket.</li>
 *   <li>Esperar confirmación del cliente y cerrar la conexión.</li>
 * </ol>
 *
 * <p><b>Nota crítica de streams:</b> {@link ObjectOutputStream} escribe
 * un header de serialización al construirse. Si el servidor no hace
 * {@code flush()} de ese header antes de que el cliente cree su
 * {@link ObjectInputStream}, el cliente se bloquea esperando el header
 * del servidor indefinidamente (deadlock de inicialización). Por eso
 * se crea el OOS, se hace flush inmediato, y luego se crea el OIS.</p>
 *
 * @author Programación Avanzada - Universidad Distrital
 * @version 1.0
 */
public class HiloLuchador extends Thread {

    /** Socket de la conexión con el cliente. */
    private final Socket socket;

    /** Dohyō compartido entre los dos hilos. */
    private final Dohyo dohyo;

    /** Listener para notificar eventos a la GUI del servidor. */
    private final EventoCombateListener listener;

    /**
     * Construye el hilo para un cliente.
     *
     * @param socket   Socket TCP del cliente.
     * @param dohyo    Dohyō compartido.
     * @param listener Observador de eventos para la GUI.
     */
    public HiloLuchador(Socket socket, Dohyo dohyo, EventoCombateListener listener) {
        this.socket   = socket;
        this.dohyo    = dohyo;
        this.listener = listener;
    }

    /**
     * Ciclo completo: recibir → combatir → notificar → confirmar.
     *
     * <p>Se crea primero el {@link ObjectOutputStream} y se hace
     * {@code flush()} inmediato para enviar el header al cliente.
     * Solo después se crea el {@link ObjectInputStream}, que a su
     * vez lee el header que el cliente ya envió. Esto evita el
     * deadlock de inicialización de streams.</p>
     */
    @Override
    public void run() {
        ObjectOutputStream salida = null;
        ObjectInputStream  entrada = null;
        try {
            // ── CRÍTICO: OOS primero + flush para enviar header al cliente ──
            salida = new ObjectOutputStream(socket.getOutputStream());
            salida.flush(); // el cliente espera este header para poder crear su OIS

            // Ahora sí el cliente puede crear su OIS y nosotros leer el suyo
            entrada = new ObjectInputStream(socket.getInputStream());

            // ── 1. Recibir el luchador serializado ──────────────────────────
            Rikishi luchador = (Rikishi) entrada.readObject();
            listener.onLuchadorLlego(luchador);

            // ── 2. Subir al dohyō (el primero espera al segundo) ────────────
            dohyo.subirAlDohyo(luchador);

            // Solo luchador1 dispara el evento de inicio (evitar duplicado)
            if (dohyo.getLuchador1() == luchador) {
                listener.onCombateInicia(dohyo.getLuchador1(), dohyo.getLuchador2());
            }

            // ── 3. Bucle del combate ─────────────────────────────────────────
            while (!dohyo.isCombateTerminado()) {
                ResultadoTurno resultado = dohyo.ejecutarTurno(luchador);
                if (resultado.getTecnicaUsada() != null) {
                    listener.onTurnoEjecutado(luchador, resultado);
                }
                Thread.sleep(250);
            }

            // ── 4. Notificar resultado al cliente ────────────────────────────
            boolean esGanador = (dohyo.getGanador() == luchador);
            salida.writeObject(esGanador ? Protocolo.MSG_GANADOR : Protocolo.MSG_PERDEDOR);
            salida.writeObject(dohyo.getGanador());
            salida.flush();
            listener.onCombateTermino(dohyo.getGanador());

            // ── 5. Esperar confirmación del cliente ──────────────────────────
            String confirmacion = (String) entrada.readObject();
            if (Protocolo.MSG_CLIENTE_LISTO.equals(confirmacion)) {
                listener.onClienteConfirmo(luchador);
            }

        } catch (IOException | ClassNotFoundException e) {
            listener.onError("Error de red: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            listener.onError("Hilo interrumpido: " + e.getMessage());
        } finally {
            cerrarRecursos(salida, entrada);
        }
    }

    /**
     * Cierra streams y socket de forma segura.
     *
     * @param salida Stream de salida a cerrar.
     * @param entrada Stream de entrada a cerrar.
     */
    private void cerrarRecursos(ObjectOutputStream salida, ObjectInputStream entrada) {
        try { if (salida  != null) salida.close();  } catch (IOException ignored) { }
        try { if (entrada != null) entrada.close();  } catch (IOException ignored) { }
        try { if (socket  != null && !socket.isClosed()) socket.close(); } catch (IOException ignored) { }
    }
}
