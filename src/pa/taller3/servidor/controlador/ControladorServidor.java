package pa.taller3.servidor.controlador;

import pa.taller3.servidor.dao.RikishiDAO;
import pa.taller3.servidor.dao.RikishiDAOImpl;
import pa.taller3.servidor.hilo.HiloLuchador;
import pa.taller3.servidor.vista.VentanaServidor;

import java.io.*;
import java.net.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.*;

/**
 * Controlador principal del servidor de sumo.
 *
 * <p>Responsabilidades:</p>
 * <ul>
 *   <li>Leer {@code config.properties} para obtener el puerto.</li>
 *   <li>Abrir el {@link ServerSocket} y aceptar conexiones en un hilo aparte.</li>
 *   <li>Generar un {@link HiloLuchador} por cada cliente conectado.</li>
 *   <li>Llamar al {@link RikishiDAO} para insertar el luchador en BD
 *       y asignar el {@code idBD} al hilo.</li>
 *   <li>Mantener el registro de todos los hilos conectados.</li>
 *   <li>Cuando hay al menos 6 luchadores registrados, arrancar el {@link Dohyo}.</li>
 *   <li>Actualizar la {@link VentanaServidor} con los eventos del servidor.</li>
 * </ul>
 *
 * <p><b>Principio SRP:</b> solo gestiona la aceptación de conexiones,
 * el registro en BD y la coordinación con el Dohyo.</p>
 *
 * @author Programación Avanzada - Universidad Distrital
 * @version 1.0
 */
public class ControladorServidor {

    /** Logger del servidor. */
    private static final Logger LOGGER =
        Logger.getLogger(ControladorServidor.class.getName());

    /** Mínimo de luchadores requeridos para iniciar los combates. */
    private static final int MIN_LUCHADORES = 6;

    /** Puerto del servidor leído desde config.properties. */
    private int puerto;

    /** Socket principal del servidor. */
    private ServerSocket serverSocket;

    /** Lista de hilos de luchadores conectados y registrados en BD. */
    private final List<HiloLuchador> hilosLuchadores;

    /** Vista del servidor para mostrar eventos. */
    private final VentanaServidor vista;

    /** DAO para persistencia de luchadores. */
    private final RikishiDAO dao;

    /** Dohyo — orquestador de los combates. */
    private Dohyo dohyo;

    /** Contador de sesiones para ids únicos de hilos. */
    private int contadorSesion;

    /**
     * Construye el controlador, carga la configuración,
     * inicializa el DAO y la vista.
     */
    public ControladorServidor() {
        this.hilosLuchadores = new ArrayList<>();
        this.contadorSesion  = 0;
        this.dao             = new RikishiDAOImpl();
        this.vista           = new VentanaServidor();

        try {
            cargarConfiguracion();
        } catch (IOException e) {
            vista.mostrarError("No se pudo cargar config.properties:\n"
                + e.getMessage());
            return;
        }

        vista.setVisible(true);
        iniciarAceptacionConexiones();
    }

    /**
     * Lee {@code ./data/config.properties} y extrae el puerto del servidor.
     *
     * @throws IOException Si el archivo no existe o no se puede leer.
     */
    private void cargarConfiguracion() throws IOException {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("./data/config.properties")) {
            props.load(fis);
        }
        this.puerto = Integer.parseInt(props.getProperty("server.port", "5000"));
    }

    /**
     * Inicia un hilo dedicado a aceptar conexiones entrantes.
     * No bloquea la UI del servidor.
     */
    private void iniciarAceptacionConexiones() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(puerto);
                LOGGER.info("Servidor escuchando en puerto " + puerto);
                vista.logEvento("✔ Servidor iniciado en puerto " + puerto);
                vista.logEvento("⏳ Esperando luchadores... (mínimo "
                    + MIN_LUCHADORES + ")");

                while (true) {
                    Socket socket = serverSocket.accept();
                    LOGGER.info("Nueva conexión: " + socket);
                    vista.logEvento("🔌 Nueva conexión: "
                        + socket.getInetAddress());

                    HiloLuchador hilo = new HiloLuchador(socket, contadorSesion++);
                    hilo.start();

                    // Esperar a que el hilo parsee el Rikishi
                    esperarRikishi(hilo);

                    // Insertar en BD y asignar idBD al hilo
                    registrarEnBD(hilo);

                    synchronized (hilosLuchadores) {
                        hilosLuchadores.add(hilo);
                    }

                    vista.logEvento("🥋 Luchador registrado: "
                        + hilo.getRikishi().getNombre()
                        + " | idBD: " + hilo.getIdBD()
                        + " (total: " + hilosLuchadores.size() + ")");
                    vista.actualizarContadorLuchadores(hilosLuchadores.size());

                    // Arrancar Dohyo cuando hay suficientes luchadores
                    if (hilosLuchadores.size() >= MIN_LUCHADORES
                            && dohyo == null) {
                        vista.logEvento("⚔️  ¡Suficientes luchadores! "
                            + "Iniciando combates...");
                        arrancarDohyo();
                    }
                }
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Error en el servidor", ex);
                vista.logEvento("❌ Error: " + ex.getMessage());
            }
        }).start();
    }

    /**
     * Espera en polling corto hasta que el hilo haya parseado su Rikishi.
     *
     * @param hilo El {@link HiloLuchador} a esperar.
     */
    private void esperarRikishi(HiloLuchador hilo) {
        while (hilo.getRikishi() == null) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Inserta el luchador en la base de datos via el {@link RikishiDAO}
     * y asigna el id generado al hilo.
     *
     * @param hilo El {@link HiloLuchador} cuyo Rikishi se insertará.
     */
    private void registrarEnBD(HiloLuchador hilo) {
        try {
            int idBD = dao.insertar(hilo.getRikishi());
            hilo.setIdBD(idBD);
            LOGGER.info("Rikishi guardado en BD con id: " + idBD);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al insertar en BD", ex);
            vista.logEvento("❌ Error al guardar luchador en BD: "
                + ex.getMessage());
        }
    }

    /**
     * Crea y arranca el {@link Dohyo} pasándole la lista de hilos,
     * el DAO y la vista.
     */
    private void arrancarDohyo() {
        List<HiloLuchador> copia;
        synchronized (hilosLuchadores) {
            copia = new ArrayList<>(hilosLuchadores);
        }
        dohyo = new Dohyo(copia, dao, vista);
        dohyo.start();
    }

    /**
     * Cierra el servidor liberando el {@link ServerSocket}.
     */
    public void cerrar() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Error al cerrar el servidor", ex);
        }
    }
}