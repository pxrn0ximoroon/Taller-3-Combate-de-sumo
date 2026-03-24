package pa.taller3.cliente.controlador;

import pa.taller3.cliente.vista.VentanaCliente;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Controlador principal del cliente de sumo.
 *
 * <p>Coordina la {@link VentanaCliente} con el {@link ControladorSocket},
 * manejando los eventos de la interfaz gráfica y el flujo de datos.</p>
 *
 * <p><b>Responsabilidades:</b></p>
 * <ul>
 *   <li>Registrar los listeners de los botones de la vista.</li>
 *   <li>Invocar {@code seleccionarArchivoProperties()} de la vista.</li>
 *   <li>Delegar la lectura del archivo al {@link ControladorSocket}.</li>
 *   <li>Validar datos antes de enviarlos.</li>
 *   <li>Coordinar el envío y recepción de datos por socket.</li>
 *   <li>Decidir si mostrar victoria o derrota en la vista.</li>
 *   <li>Mostrar el resultado final con JOptionPane y cerrar la app.</li>
 * </ul>
 *
 * <p><b>Principio SRP:</b> solo maneja el flujo de eventos del cliente.<br>
 * <b>Principio DIP:</b> depende de abstracciones (la vista y el socket),
 * no de implementaciones concretas del servidor.</p>
 *
 * @author Programación Avanzada - Universidad Distrital
 * @version 1.0
 */
public class ControladorCliente implements ActionListener {

    /** Vista principal del cliente. */
    private final VentanaCliente vista;

    /** Controlador de comunicación con el servidor. */
    private final ControladorSocket controladorSocket;

    /**
     * Construye el controlador, inicializa vista y socket, y registra listeners.
     */
    public ControladorCliente() {
        ControladorSocket socketTemp = null;
        VentanaCliente    vistaTemp  = null;

        try {
            socketTemp = new ControladorSocket();
            vistaTemp  = new VentanaCliente();
        } catch (IOException e) {
            javax.swing.JOptionPane.showMessageDialog(null,
                "No se pudo cargar config.properties:\n" + e.getMessage(),
                "Error de configuración",
                javax.swing.JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        this.controladorSocket = socketTemp;
        this.vista             = vistaTemp;

        registrarListeners();
        vista.setVisible(true);
    }

    /**
     * Registra los listeners de los botones usando los comandos
     * definidos en {@link VentanaCliente}.
     */
    private void registrarListeners() {
        vista.agregarListenerCargarProperties(this);
        vista.agregarListenerCombatir(this);
    }

    /**
     * Distribuye los eventos según el comando del botón presionado.
     *
     * @param e Evento de acción.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case VentanaCliente.CMD_CARGAR   -> cargarKimarites();
            case VentanaCliente.CMD_COMBATIR -> registrarYCombatir();
        }
    }

    /**
     * Invoca el {@link javax.swing.JFileChooser} de la vista,
     * lee el archivo seleccionado y carga los kimarites en la lista.
     */
    private void cargarKimarites() {
        File archivo = vista.seleccionarArchivoProperties();
        if (archivo == null) return;

        try {
            List<String> kimarites = controladorSocket.leerKimarites(archivo);
            if (kimarites.isEmpty()) {
                vista.mostrarError("El archivo no contiene kimarites válidos.");
                return;
            }
            vista.cargarKimaritesEnLista(kimarites);
            vista.setArchivoProperties(archivo.getName());
            vista.setEstado("✔ Técnicas cargadas. Selecciona las que domina tu rikishi.");
        } catch (IOException ex) {
            vista.mostrarError("No se pudo leer el archivo:\n" + ex.getMessage());
        }
    }

    /**
     * Valida los datos del formulario, conecta al servidor,
     * envía el luchador y espera el resultado final del combate.
     */
    private void registrarYCombatir() {
        String       nombre    = vista.getNombreLuchador();
        double       peso      = vista.getPesoLuchador();
        List<String> kimarites = vista.getKimaritesSeleccionados();

        if (nombre.isEmpty()) {
            vista.mostrarError("Debes ingresar el nombre del luchador.");
            return;
        }
        if (kimarites.isEmpty()) {
            vista.mostrarError("Debes seleccionar al menos una técnica Kimarite.");
            return;
        }

        vista.setBtnCombatirHabilitado(false);
        vista.setEstado("⏳ Conectando con el servidor...");

        // Hilo aparte para no bloquear la UI mientras espera al servidor
        new Thread(() -> ejecutarCombate(nombre, peso, kimarites)).start();
    }

    /**
     * Ejecuta la comunicación con el servidor en un hilo separado.
     * Actualiza la UI via {@code SwingUtilities.invokeLater}.
     *
     * @param nombre    Nombre del luchador.
     * @param peso      Peso del luchador.
     * @param kimarites Técnicas seleccionadas.
     */
    private void ejecutarCombate(String nombre, double peso, List<String> kimarites) {
        try {
            controladorSocket.conectar();
            javax.swing.SwingUtilities.invokeLater(() ->
                vista.setEstado("✔ Conectado. Enviando datos al servidor..."));

            controladorSocket.enviarLuchador(nombre, peso, kimarites);
            javax.swing.SwingUtilities.invokeLater(() ->
                vista.setEstado("⏳ Luchador registrado. Esperando resultado final..."));

            // Bloquea hasta que el servidor responda con el resultado final
            String resultado = controladorSocket.recibirResultado();
            procesarResultado(resultado);

        } catch (IOException ex) {
            javax.swing.SwingUtilities.invokeLater(() -> {
                vista.mostrarError("Error de conexión:\n" + ex.getMessage());
                vista.setBtnCombatirHabilitado(true);
                vista.setEstado("Error al conectar con el servidor.");
            });
        } finally {
            controladorSocket.desconectar();
        }
    }

    /**
     * Procesa el String de resultado recibido del servidor:
     * <ol>
     *   <li>Actualiza el panel de la ventana (victoria o derrota).</li>
     *   <li>Muestra el {@link javax.swing.JOptionPane} con el resultado final.</li>
     *   <li>Al aceptar, cierra la aplicación via {@code System.exit(0)}.</li>
     * </ol>
     *
     * <p>Formato esperado: {@code "GANASTE:detalle"} o
     * {@code "PERDISTE:detalle"}</p>
     *
     * @param resultado String recibido del servidor.
     */
    private void procesarResultado(String resultado) {
        String[] partes  = resultado.split(":", 2);
        boolean  gano    = partes[0].equalsIgnoreCase("GANASTE");
        String   detalle = partes.length > 1 ? partes[1] : "";

        javax.swing.SwingUtilities.invokeLater(() -> {
            // 1. Actualizar panel en la ventana
            if (gano) vista.mostrarVictoria(detalle);
            else      vista.mostrarDerrota(detalle);

            // 2. JOptionPane + cierre al aceptar
            vista.mostrarResultadoFinal(gano, detalle);
        });
    }
}