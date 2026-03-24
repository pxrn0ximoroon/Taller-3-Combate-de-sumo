package pa.taller3.servidor.modelo;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gestiona la conexión JDBC a la base de datos MySQL.
 *
 * <p>Implementa el patrón <b>Singleton</b> para garantizar que solo
 * exista una instancia de conexión durante toda la ejecución del servidor.</p>
 *
 * <p>Los parámetros de conexión ({@code db.url}, {@code db.usuario},
 * {@code db.password}) se cargan desde {@code ./data/config.properties}.</p>
 *
 * <p><b>Principio SRP:</b> solo gestiona la conexión a la base de datos.</p>
 *
 * @author Programación Avanzada - Universidad Distrital
 * @version 1.0
 */
public class ConexionDB {

    /** Logger de la clase. */
    private static final Logger LOGGER =
        Logger.getLogger(ConexionDB.class.getName());

    /** Ruta del archivo de propiedades de configuración. */
    private static final String RUTA_CONFIG = "./data/config.properties";

    /** Instancia única del Singleton. */
    private static ConexionDB instancia;

    /** Conexión JDBC activa. */
    private Connection conexion;

    /**
     * Constructor privado — carga la configuración y establece la conexión.
     *
     * @throws SQLException Si no se puede conectar a la base de datos.
     * @throws IOException  Si no se puede leer el archivo de propiedades.
     */
    private ConexionDB() throws SQLException, IOException {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(RUTA_CONFIG)) {
            props.load(fis);
        }

        String url      = props.getProperty("db.url");
        String usuario  = props.getProperty("db.usuario");
        String password = props.getProperty("db.password");

        conexion = DriverManager.getConnection(url, usuario, password);
        LOGGER.info("Conexión a la base de datos establecida.");
    }

    /**
     * Devuelve la instancia única del Singleton.Si no existe o la conexión está cerrada, la crea.
     *
     * @return La instancia única de {@link ConexionDB}.
     * @throws SQLException Si no se puede conectar.
     * @throws IOException  Si no se puede leer la configuración.
     */
    public static ConexionDB getInstancia() throws SQLException, IOException {
        if (instancia == null || instancia.conexion.isClosed()) {
            instancia = new ConexionDB();
        }
        return instancia;
    }

    /**
     * Devuelve la conexión JDBC activa.
     *
     * @return La {@link Connection} activa.
     */
    public Connection getConexion() {
        return conexion;
    }

    /**
     * Cierra la conexión JDBC y limpia la instancia del Singleton.
     */
    public void cerrar() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
                instancia = null;
                LOGGER.info("Conexión a la base de datos cerrada.");
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Error al cerrar la conexión.", ex);
        }
    }
}