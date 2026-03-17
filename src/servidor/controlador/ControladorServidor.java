package servidor.controlador;

import modelo.Dohyo;
import modelo.ResultadoTurno;
import modelo.Rikishi;
import servidor.red.EventoCombateListener;
import servidor.red.ServidorSumo;
import servidor.vista.VentanaServidor;

import javax.swing.SwingUtilities;

/**
 * Controlador principal del servidor de sumo.
 *
 * <p>Implementa {@link EventoCombateListener} para recibir eventos
 * desde los hilos de red y actualizarlos <b>de forma segura</b> en la
 * GUI usando {@code SwingUtilities.invokeLater()}.</p>
 *
 * <ul>
 *   <li>Crea el {@link Dohyo} (modelo)</li>
 *   <li>Crea el {@link ServidorSumo} (red/thread)</li>
 *   <li>Actualiza la {@link VentanaServidor} (vista) con cada evento</li>
 * </ul>
 *
 *
 * <p><b>Principio DIP:</b> depende de {@link EventoCombateListener}
 * (abstracción), no de los hilos concretos.</p>
 *
 * @author Programación Avanzada - Universidad Distrital
 * @version 1.0
 */
public class ControladorServidor implements EventoCombateListener {

    /** Vista del servidor. */
    private final VentanaServidor vista;

    /** El dohyō donde ocurre el combate. */
    private final Dohyo dohyo;

    /** El servidor de sockets. */
    private final ServidorSumo servidor;

    /**
     * Construye el controlador, crea el dohyō y arranca el servidor.
     *
     * @param vista La ventana del servidor ya creada e inicializada.
     */
    public ControladorServidor(VentanaServidor vista) {
        this.vista   = vista;
        this.dohyo   = new Dohyo();
        this.servidor = new ServidorSumo(dohyo, this);
        servidor.setDaemon(false);
        servidor.start();
    }

    // ── Implementación EventoCombateListener ─────────────────────────────────

    /**
     * Recibe el evento de llegada de un luchador y lo pasa a la vista.
     * Siempre en el EDT de Swing vía {@code invokeLater}.
     *
     * @param luchador El luchador que llegó.
     */
    @Override
    public void onLuchadorLlego(Rikishi luchador) {
        SwingUtilities.invokeLater(() -> vista.mostrarLuchadorLlego(luchador));
    }

    /**
     * Recibe el evento de inicio de combate.
     *
     * @param luchador1 Primer luchador.
     * @param luchador2 Segundo luchador.
     */
    @Override
    public void onCombateInicia(Rikishi luchador1, Rikishi luchador2) {
        SwingUtilities.invokeLater(() -> vista.mostrarInicioCombate(luchador1, luchador2));
    }

    /**
     * Recibe el evento de ejecución de un turno.
     *
     * @param luchador El luchador que atacó.
     * @param resultado Resultado del turno.
     */
    @Override
    public void onTurnoEjecutado(Rikishi luchador, ResultadoTurno resultado) {
        SwingUtilities.invokeLater(() -> vista.mostrarTurno(luchador, resultado));
    }

    /**
     * Recibe el evento de fin de combate.
     *
     * @param ganador El luchador ganador.
     */
    @Override
    public void onCombateTermino(Rikishi ganador) {
        SwingUtilities.invokeLater(() -> vista.mostrarGanador(ganador));
    }

    /**
     * Recibe la confirmación de un cliente.
     *
     * @param luchador El luchador cuyo cliente confirmó.
     */
    @Override
    public void onClienteConfirmo(Rikishi luchador) {
        SwingUtilities.invokeLater(() -> vista.mostrarClienteDesconectado(luchador));
    }

    /**
     * Recibe un error de la red.
     *
     * @param mensaje Descripción del error.
     */
    @Override
    public void onError(String mensaje) {
        SwingUtilities.invokeLater(() -> vista.mostrarError(mensaje));
    }
}
