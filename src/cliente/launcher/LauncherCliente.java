package cliente.launcher;

import cliente.controlador.ControladorCliente;
import cliente.vista.VentanaCliente;

import javax.swing.SwingUtilities;

/**
 * Punto de entrada del cliente de sumo.
 *
 * <p><b>Regla del enunciado (punto q):</b> sin lógica, sin objetos de modelo,
 * sin asignaciones. Solo inicia vista y controlador en el EDT de Swing.</p>
 *
 * <p>Para simular dos luchadores, iniciar dos instancias de este launcher.
 * Ejecutar: {@code java cliente.launcher.LauncherCliente}</p>
 *
 * @author Programación Avanzada - Universidad Distrital
 * @version 1.0
 */
public class LauncherCliente {

    /**
     * Inicia el cliente en el Event Dispatch Thread de Swing.
     *
     * @param args Argumentos de línea de comandos (no se usan).
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            VentanaCliente vista = new VentanaCliente();
            new ControladorCliente(vista);
            vista.setVisible(true);
        });
    }
}
