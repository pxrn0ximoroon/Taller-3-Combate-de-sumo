package pa.taller3.servidor;

import pa.taller3.servidor.controlador.ControladorServidor;
import javax.swing.SwingUtilities;

/**
 * Punto de entrada de la aplicación servidor de sumo.
 *
 * <p>Solo instancia el {@link ControladorServidor} principal
 * dentro del hilo de eventos de Swing.</p>
 *
 * <p><b>Principio SRP:</b> no contiene lógica, objetos del modelo,
 * ni creación de interfaces.</p>
 *
 * @author Programación Avanzada - Universidad Distrital
 * @version 1.0
 */
public class LauncherServidor {

    /**
     * Método principal. Inicia la aplicación servidor.
     *
     * @param args Argumentos de línea de comandos (no utilizados).
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(ControladorServidor::new);
    }
}