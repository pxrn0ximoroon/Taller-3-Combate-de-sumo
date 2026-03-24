package pa.taller3.servidor.hilo;

import pa.taller3.servidor.modelo.Kimarite;
import pa.taller3.servidor.modelo.Rikishi;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.*;

/**
 * Hilo de ejecución asignado a cada cliente (luchador) que se conecta al servidor.
 *
 * <p>Ciclo de vida:</p>
 * <ol>
 *   <li>Recibe los datos del luchador via {@code readUTF()}.</li>
 *   <li>Construye el objeto {@link Rikishi} y lo expone al controlador.</li>
 *   <li>Queda en espera ({@code wait()}) hasta que el {@code Dohyo} lo despierte.</li>
 *   <li>Envía el resultado al cliente via {@code writeUTF()} y termina.</li>
 * </ol>
 *
 * <p><b>Formato de recepción:</b> {@code "nombre|peso|kim1,kim2,kim3"}</p>
 * <p><b>Formato de respuesta:</b> {@code "GANASTE:detalle"} o
 * {@code "PERDISTE:detalle"}</p>
 *
 * <p>El campo {@code idBD} es asignado por el {@code ControladorServidor}
 * tras insertar el luchador en la base de datos. El hilo no conoce
 * el DAO ni la BD — solo expone su {@link Rikishi} y su {@code idBD}.</p>
 *
 * <p><b>Principio SRP:</b> solo gestiona la comunicación con un cliente
 * y la espera sincronizada para el combate.</p>
 *
 * @author Programación Avanzada - Universidad Distrital
 * @version 1.0
 */
public class HiloLuchador extends Thread {

    /** Logger para registrar eventos del hilo. */
    private static final Logger LOGGER =
        Logger.getLogger(HiloLuchador.class.getName());

    /** Socket de conexión con el cliente. */
    private final Socket socket;

    /** Stream de entrada para recibir datos del cliente. */
    private DataInputStream dis;

    /** Stream de salida para enviar datos al cliente. */
    private DataOutputStream dos;

    /** Identificador único de sesión asignado por el servidor. */
    private final int idSesion;

    /** Luchador construido a partir de los datos recibidos del cliente. */
    private Rikishi rikishi;

    /**
     * Id de la base de datos asignado por el {@code ControladorServidor}
     * tras insertar el luchador en la BD.
     */
    private int idBD;

    /** Resultado del combate a enviar al cliente. Seteado por el Dohyo. */
    private String resultado;

    /** Indica si el combate terminó y hay resultado disponible. */
    private boolean combateTerminado;

    /**
     * Construye el hilo inicializando los streams de comunicación.
     *
     * @param socket   Socket de conexión con el cliente.
     * @param idSesion Identificador único de sesión.
     */
    public HiloLuchador(Socket socket, int idSesion) {
        this.socket           = socket;
        this.idSesion         = idSesion;
        this.combateTerminado = false;
        this.idBD             = -1;

        try {
            dis = new DataInputStream(
                    new BufferedInputStream(socket.getInputStream()));
            dos = new DataOutputStream(
                    new BufferedOutputStream(socket.getOutputStream()));
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE,
                "Error al inicializar streams del hilo " + idSesion, ex);
        }
    }

    /**
     * Ciclo principal del hilo:
     * recibe datos → construye Rikishi → espera combate → envía resultado.
     */
    @Override
    public void run() {
        try {
            // 1. Recibir datos del cliente
            String mensaje = dis.readUTF();
            LOGGER.info("Hilo " + idSesion + " recibió: " + mensaje);

            // 2. Parsear y construir el Rikishi
            rikishi = parsearRikishi(mensaje);
            LOGGER.info("Rikishi parseado: " + rikishi.getNombre());

            // 3. Esperar a que el Dohyo despierte este hilo con el resultado
            synchronized (this) {
                while (!combateTerminado) {
                    wait();
                }
            }

            // 4. Enviar resultado al cliente
            dos.writeUTF(resultado);
            dos.flush();
            LOGGER.info("Hilo " + idSesion + " envió resultado: " + resultado);

        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE,
                "Error de comunicación en hilo " + idSesion, ex);
        } catch (InterruptedException ex) {
            LOGGER.log(Level.WARNING,
                "Hilo " + idSesion + " interrumpido", ex);
            Thread.currentThread().interrupt();
        } finally {
            desconectar();
        }
    }

    /**
     * Parsea el mensaje recibido del cliente y construye un {@link Rikishi}.
     *
     * <p>Formato esperado: {@code "nombre|peso|kim1,kim2,kim3"}</p>
     *
     * @param mensaje String recibido del cliente.
     * @return El {@link Rikishi} construido.
     */
    private Rikishi parsearRikishi(String mensaje) {
        String[] partes = mensaje.split("\\|");
        String   nombre = partes[0];
        double   peso   = Double.parseDouble(partes[1]);

        List<Kimarite> kimarites = new ArrayList<>();
        if (partes.length > 2) {
            for (String k : partes[2].split(",")) {
                kimarites.add(new Kimarite(k.trim(), ""));
            }
        }
        return new Rikishi(nombre, peso, kimarites);
    }

    /**
     * Llamado por el {@code Dohyo} para notificar el resultado del combate
     * y despertar el hilo para que envíe la respuesta al cliente.
     *
     * @param resultado String con el resultado ({@code "GANASTE:detalle"} o
     *                  {@code "PERDISTE:detalle"}).
     */
    public synchronized void notificarResultado(String resultado) {
        this.resultado        = resultado;
        this.combateTerminado = true;
        notify();
    }

    /**
     * Cierra el socket liberando los recursos de red.
     */
    public void desconectar() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING,
                "Error al cerrar socket del hilo " + idSesion, ex);
        }
    }

    // ── Getters y setters ─────────────────────────────────────────────────────

    /**
     * @return El {@link Rikishi} construido a partir de los datos del cliente,
     *         o {@code null} si aún no se ha recibido.
     */
    public Rikishi getRikishi() { return rikishi; }

    /**
     * @return Identificador de sesión de este hilo.
     */
    public int getIdSesion() { return idSesion; }

    /**
     * @return Id de la base de datos asignado por el {@code ControladorServidor},
     *         o {@code -1} si aún no ha sido registrado en BD.
     */
    public int getIdBD() { return idBD; }

    /**
     * Asignado por el {@code ControladorServidor} tras insertar el luchador en BD.
     *
     * @param idBD Id generado por la base de datos.
     */
    public void setIdBD(int idBD) { this.idBD = idBD; }
}