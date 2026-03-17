package cliente.controlador;

import cliente.vista.VentanaCliente;
import modelo.Kimarite;
import modelo.Rikishi;
import red.Protocolo;
import util.LectorKimarites;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.util.List;

/**
 * Controlador principal del cliente de sumo.
 *
 * <p>Gestiona la interacción entre {@link VentanaCliente} (vista),
 * el modelo ({@link Rikishi}/{@link Kimarite}) y la red (sockets).</p>
 *
 * <h2>Separación de eventos:</h2>
 * <ul>
 *   <li>{@link #registrarListeners()} — vincula la vista con el controlador</li>
 *   <li>{@link ListenerCargarProperties} / {@link ListenerCombatir} — implementan
 *       {@link ActionListener} (el listener)</li>
 *   <li>{@link #accionCargarProperties()} / {@link #accionCombatir()} — contienen
 *       la lógica real (el performed)</li>
 * </ul>
 *
 *
 * @author Programación Avanzada - Universidad Distrital
 * @version 1.0
 */
public class ControladorCliente {

    /** Dirección del servidor. */
    private static final String HOST = "localhost";

    /** Vista del cliente. */
    private final VentanaCliente vista;

    /** Ruta del archivo .properties seleccionado. */
    private String rutaProperties;

    /**
     * Construye el controlador y registra los listeners.
     *
     * @param vista La ventana del cliente ya creada.
     */
    public ControladorCliente(VentanaCliente vista) {
        this.vista = vista;
        registrarListeners();
    }

    /**
     * Registra los listeners en los botones de la vista.
     * Punto de separación evento/listener del enunciado.
     */
    private void registrarListeners() {
        vista.agregarListenerCargarProperties(new ListenerCargarProperties());
        vista.agregarListenerCombatir(new ListenerCombatir());
    }

    // ── Lógica de acciones ────────────────────────────────────────────────────

    /**
     * Abre JFileChooser para seleccionar el .properties y carga kimarites.
     */
    private void accionCargarProperties() {
        JFileChooser chooser = new JFileChooser("./data");
        chooser.setDialogTitle("Seleccionar archivo de kimarites (.properties)");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "Archivos de propiedades (*.properties)", "properties"
        ));

        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            rutaProperties = chooser.getSelectedFile().getAbsolutePath();
            vista.setArchivoProperties(chooser.getSelectedFile().getName());

            List<Kimarite> kimarites = LectorKimarites.cargarDesdeProperties(rutaProperties);
            if (kimarites.isEmpty()) {

                JOptionPane.showMessageDialog(null,
                    "No se encontraron kimarites en el archivo.",
                    "Archivo inválido", JOptionPane.WARNING_MESSAGE);
            } else {
                vista.cargarKimaritesEnLista(kimarites);
                vista.setEstado(kimarites.size() + " técnicas cargadas. Selecciona las que domina tu rikishi.");
            }
        }
    }

    /**
     * Valida datos, construye el Rikishi y lo envía al servidor en hilo separado.
     */
    private void accionCombatir() {
        String nombre = vista.getNombreLuchador();
        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Ingresa el nombre del luchador.",
                "Datos incompletos", JOptionPane.WARNING_MESSAGE);
            return;
        }
        List<Kimarite> tecnicas = vista.getKimaritesSeleccionados();
        if (tecnicas.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Selecciona al menos una técnica.",
                "Datos incompletos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Rikishi luchador = new Rikishi(nombre, vista.getPesoLuchador(), 0);
        tecnicas.forEach(luchador::agregarKimarite);

        vista.setBtnCombatirHabilitado(false);
        vista.setEstado("⏳ Conectando al servidor...");

        Thread hiloRed = new Thread(() -> enviarAlServidor(luchador));
        hiloRed.setDaemon(true);
        hiloRed.start();
    }

    /**
     * Maneja la comunicación completa con el servidor.
     *
     * <p><b>Orden de streams:</b> se crea el {@link ObjectOutputStream} primero
     * y se hace {@code flush()} inmediato para enviar el header al servidor.
     * Solo después se crea el {@link ObjectInputStream}, que lee el header
     * que el servidor ya envió. Esto previene el deadlock de inicialización.</p>
     *
     * @param luchador El luchador a enviar.
     */
    private void enviarAlServidor(Rikishi luchador) {
        ObjectOutputStream salida = null;
        ObjectInputStream  entrada = null;
        Socket socket = null;
        try {
            socket  = new Socket(HOST, Protocolo.PUERTO);

            // CRÍTICO: OOS primero + flush, luego OIS
            salida  = new ObjectOutputStream(socket.getOutputStream());
            salida.flush(); // envía header al servidor para que pueda crear su OIS

            entrada = new ObjectInputStream(socket.getInputStream());

            // Enviar luchador
            salida.writeObject(luchador);
            salida.flush();

            final String estadoMsg = "🥋 " + luchador.getNombre() + " en el dohyō. ¡Combate en curso!";
            SwingUtilities.invokeLater(() -> vista.setEstado(estadoMsg));

            // Esperar resultado (bloqueante)
            String resultado = (String) entrada.readObject();
            Rikishi ganador  = (Rikishi) entrada.readObject();

            boolean esGanador = Protocolo.MSG_GANADOR.equals(resultado);
            SwingUtilities.invokeLater(() -> vista.mostrarResultado(esGanador, ganador));

            // Confirmar al servidor
            salida.writeObject(Protocolo.MSG_CLIENTE_LISTO);
            salida.flush();

        } catch (IOException | ClassNotFoundException e) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(null,
                    "Error de conexión con el servidor:\n" + e.getMessage()
                    + "\n\nVerifica que el servidor esté activo en el puerto " + Protocolo.PUERTO,
                    "Error de red", JOptionPane.ERROR_MESSAGE);
                vista.setBtnCombatirHabilitado(true);
                vista.setEstado("Error al conectar. Verifica el servidor.");
            });
        } finally {
            try { if (salida  != null) salida.close();  } catch (IOException ignored) { }
            try { if (entrada != null) entrada.close();  } catch (IOException ignored) { }
            try { if (socket  != null) socket.close();   } catch (IOException ignored) { }
        }
    }

    // ── Listeners internos ────────────────────────────────────────────────────

    /**
     * Listener del botón "Cargar Properties".
     * Delega al método {@link #accionCargarProperties()}.
     */
    private class ListenerCargarProperties implements ActionListener {
        /** @param e Evento de clic. */
        @Override
        public void actionPerformed(ActionEvent e) {
            accionCargarProperties();
        }
    }

    /**
     * Listener del botón "Combatir".
     * Delega al método {@link #accionCombatir()}.
     */
    private class ListenerCombatir implements ActionListener {
        /** @param e Evento de clic. */
        @Override
        public void actionPerformed(ActionEvent e) {
            accionCombatir();
        }
    }
}
