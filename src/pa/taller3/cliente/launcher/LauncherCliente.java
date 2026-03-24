package pa.taller3.cliente.launcher;

import pa.taller3.cliente.controlador.ControladorCliente;
import javax.swing.SwingUtilities;

/**
 * Punto de entrada de la aplicación cliente de sumo.
 *
 * <p>Solo instancia el {@link ControladorCliente} principal
 * dentro del hilo de eventos de Swing.</p>
 *
 * <p><b>Principio SRP:</b> no contiene lógica, objetos del modelo,
 * ni creación de interfaces.</p>
 *
 * @author Programación Avanzada - Universidad Distrital
 * @version 1.0
 */
public class LauncherCliente {

    /**
     * Método principal. Inicia la aplicación cliente.
     *
     * @param args Argumentos de línea de comandos (no utilizados).
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(ControladorCliente::new);
    }
}