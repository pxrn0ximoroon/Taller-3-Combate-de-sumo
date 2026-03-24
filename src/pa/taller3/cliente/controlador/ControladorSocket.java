package pa.taller3.cliente.controlador;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Properties;

/**
 * Maneja toda la comunicación con el servidor vía sockets.
 *
 * <p>Responsabilidades:</p>
 * <ul>
 *   <li>Leer {@code config.properties} para obtener host y puerto.</li>
 *   <li>Leer el archivo {@code .properties} de kimarites.</li>
 *   <li>Abrir y cerrar la conexión con el servidor.</li>
 *   <li>Enviar los datos del luchador al servidor.</li>
 *   <li>Recibir el resultado del combate.</li>
 * </ul>
 *
 * <p><b>Principio SRP:</b> solo gestiona la comunicación en red
 * y la lectura de archivos de propiedades.</p>
 *
 * <p><b>Protocolo:</b> intercambio de cadenas UTF-8 mediante
 * {@link DataInputStream} y {@link DataOutputStream}.</p>
 *
 * <p><b>Formato de envío:</b> {@code "nombre|peso|kim1,kim2,kim3"}</p>
 * <p><b>Formato de respuesta:</b> {@code "GANASTE:detalle"} o
 * {@code "PERDISTE:detalle"}</p>
 *
 * @author Programación Avanzada - Universidad Distrital
 * @version 1.0
 */
public class ControladorSocket  {

    /** Host del servidor leído desde config.properties. */
    private String host;

    /** Puerto del servidor leído desde config.properties. */
    private int puerto;

    /** Socket de conexión con el servidor. */
    private Socket socket;

    /** Stream de entrada para recibir datos del servidor. */
    private DataInputStream entrada;

    /** Stream de salida para enviar datos al servidor. */
    private DataOutputStream salida;

    /**
     * Construye el controlador cargando host y puerto desde
     * {@code ./data/config.properties}.
     *
     * @throws IOException Si no se puede leer el archivo de configuración.
     */
    public ControladorSocket() throws IOException {
        cargarConfiguracion();
    }

    /**
     * Lee {@code ./data/config.properties} y extrae host y puerto.
     *
     * @throws IOException Si el archivo no existe o no se puede leer.
     */
    private void cargarConfiguracion() throws IOException {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("./data/config.properties")) {
            props.load(fis);
        }
        this.host   = props.getProperty("server.host", "localhost");
        this.puerto = Integer.parseInt(props.getProperty("server.port", "5000"));
    }

    /**
     * Lee un archivo {@code .properties} de kimarites y devuelve
     * una lista de Strings en formato {@code "nombre|descripcion"}.
     *
     * @param archivo Archivo {@code .properties} seleccionado por el usuario.
     * @return Lista de Strings con los kimarites disponibles.
     * @throws IOException Si el archivo no se puede leer.
     */
    public List<String> leerKimarites(File archivo) throws IOException {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(archivo)) {
            props.load(fis);
        }

        List<String> kimarites = new java.util.ArrayList<>();
        int i = 1;
        while (props.getProperty("kimarite." + i + ".nombre") != null) {
            String nombre = props.getProperty("kimarite." + i + ".nombre");
            String desc   = props.getProperty("kimarite." + i + ".descripcion", "");
            kimarites.add(nombre + "|" + desc);
            i++;
        }
        return kimarites;
    }

    /**
     * Abre la conexión con el servidor e inicializa los streams.
     *
     * @throws IOException Si no se puede conectar al servidor.
     */
    public void conectar() throws IOException {
        socket  = new Socket(host, puerto);
        entrada = new DataInputStream(
                    new BufferedInputStream(socket.getInputStream()));
        salida  = new DataOutputStream(
                    new BufferedOutputStream(socket.getOutputStream()));
    }

    /**
     * Envía los datos del luchador al servidor.
     *
     * <p>Formato: {@code "nombre|peso|kim1,kim2,kim3"}</p>
     *
     * @param nombre    Nombre del luchador.
     * @param peso      Peso del luchador.
     * @param kimarites Lista de Strings seleccionados en formato
     *                  {@code "nombre|descripcion"} — se envía solo el nombre.
     * @throws IOException Si ocurre un error al enviar.
     */
    public void enviarLuchador(String nombre, double peso,
                                List<String> kimarites) throws IOException {
        // Extraer solo el nombre de cada kimarite (antes del '|')
        List<String> nombres = new java.util.ArrayList<>();
        for (String k : kimarites) {
            nombres.add(k.split("\\|")[0]);
        }
        String mensaje = nombre + "|" + peso + "|" + String.join(",", nombres);
        salida.writeUTF(mensaje);
        salida.flush();
    }

    /**
     * Espera y recibe el resultado del combate desde el servidor.
     *
     * <p>Formato esperado: {@code "GANASTE:detalle"} o
     * {@code "PERDISTE:detalle"}</p>
     *
     * @return String con el resultado enviado por el servidor.
     * @throws IOException Si ocurre un error al recibir.
     */
    public String recibirResultado() throws IOException {
        return entrada.readUTF();
    }

    /**
     * Cierra la conexión con el servidor liberando todos los recursos.
     */
    public void desconectar() {
        try {
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.err.println("Error al cerrar socket: " + e.getMessage());
        }
    }

    /**
     * @return {@code true} si el socket está conectado y abierto.
     */
    public boolean estaConectado() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }
}
