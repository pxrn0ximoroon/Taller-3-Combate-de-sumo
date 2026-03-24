package pa.taller3.servidor.dao;

import pa.taller3.servidor.modelo.ConexionDB;
import pa.taller3.servidor.modelo.Kimarite;
import pa.taller3.servidor.modelo.Rikishi;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementación JDBC del {@link RikishiDAO}.
 *
 * <p>Realiza todas las operaciones de persistencia sobre las tablas
 * {@code luchadores} y {@code kimarites_luchador} de {@code sumo_db}.</p>
 *
 * <p>Usa {@link ConexionDB} (Singleton) para obtener la conexión.</p>
 *
 * <p><b>Principio SRP:</b> solo gestiona el acceso a datos de luchadores.<br>
 * <b>Principio OCP:</b> nuevas operaciones se agregan implementando
 * métodos en {@link RikishiDAO} sin modificar esta clase.</p>
 *
 * @author Programación Avanzada - Universidad Distrital
 * @version 1.0
 */
public class RikishiDAOImpl implements RikishiDAO {

    /** Logger de la clase. */
    private static final Logger LOGGER =
        Logger.getLogger(RikishiDAOImpl.class.getName());

    // ── Consultas SQL ─────────────────────────────────────────────────────────

    private static final String SQL_INSERTAR_LUCHADOR =
        "INSERT INTO luchadores (nombre, peso, victorias, ha_combatido) " +
        "VALUES (?, ?, 0, 0)";

    private static final String SQL_INSERTAR_KIMARITE =
        "INSERT INTO kimarites_luchador (id_luchador, nombre_kimarite, descripcion) " +
        "VALUES (?, ?, ?)";

    private static final String SQL_BUSCAR_POR_ID =
        "SELECT id, nombre, peso, victorias, ha_combatido " +
        "FROM luchadores WHERE id = ?";

    private static final String SQL_OBTENER_TODOS =
        "SELECT id, nombre, peso, victorias, ha_combatido FROM luchadores";

    private static final String SQL_OBTENER_DISPONIBLES =
        "SELECT id, nombre, peso, victorias, ha_combatido " +
        "FROM luchadores WHERE ha_combatido = 0";

    private static final String SQL_KIMARITES_POR_LUCHADOR =
        "SELECT nombre_kimarite, descripcion " +
        "FROM kimarites_luchador WHERE id_luchador = ?";

    private static final String SQL_ACTUALIZAR_VICTORIAS =
        "UPDATE luchadores SET victorias = ? WHERE id = ?";

    private static final String SQL_MARCAR_COMBATIDO =
        "UPDATE luchadores SET ha_combatido = 1 WHERE id = ?";

    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Obtiene la conexión activa desde el Singleton {@link ConexionDB}.
     *
     * @return La {@link Connection} activa.
     * @throws SQLException Si no se puede obtener la conexión.
     */
    private Connection getConexion() throws SQLException {
        try {
            return ConexionDB.getInstancia().getConexion();
        } catch (IOException ex) {
            throw new SQLException("Error al leer configuración de BD.", ex);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Inserta el luchador en {@code luchadores}, obtiene el id generado
     * por la BD y luego inserta sus kimarites en {@code kimarites_luchador}.</p>
     *
     * @return El id generado por la base de datos, o {@code -1} si falló.
     */
    @Override
    public int insertar(Rikishi rikishi) throws SQLException {
        Connection conn     = getConexion();
        int        idGenerado = -1;

        try (PreparedStatement ps = conn.prepareStatement(
                SQL_INSERTAR_LUCHADOR, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, rikishi.getNombre());
            ps.setDouble(2, rikishi.getPeso());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    idGenerado = rs.getInt(1);
                }
            }
        }

        if (idGenerado != -1) {
            insertarKimarites(idGenerado, rikishi.getKimarites());
            LOGGER.info("Rikishi '" + rikishi.getNombre()
                + "' insertado con id: " + idGenerado);
        }

        return idGenerado;
    }

    /**
     * Inserta los kimarites de un luchador en {@code kimarites_luchador}.
     *
     * @param idLuchador Id del luchador al que pertenecen.
     * @param kimarites  Lista de kimarites a insertar.
     * @throws SQLException Si ocurre un error en la inserción.
     */
    private void insertarKimarites(int idLuchador,
                                    List<Kimarite> kimarites) throws SQLException {
        if (kimarites == null || kimarites.isEmpty()) return;

        try (PreparedStatement ps = getConexion()
                .prepareStatement(SQL_INSERTAR_KIMARITE)) {
            for (Kimarite k : kimarites) {
                ps.setInt(1, idLuchador);
                ps.setString(2, k.getNombre());
                ps.setString(3, k.getDescripcion());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Rikishi buscarPorId(int id) throws SQLException {
        try (PreparedStatement ps = getConexion()
                .prepareStatement(SQL_BUSCAR_POR_ID)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return construirRikishi(rs);
                }
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Rikishi> obtenerTodos() throws SQLException {
        List<Rikishi> lista = new ArrayList<>();
        try (PreparedStatement ps = getConexion()
                .prepareStatement(SQL_OBTENER_TODOS);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(construirRikishi(rs));
            }
        }
        return lista;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Rikishi> obtenerDisponibles() throws SQLException {
        List<Rikishi> lista = new ArrayList<>();
        try (PreparedStatement ps = getConexion()
                .prepareStatement(SQL_OBTENER_DISPONIBLES);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(construirRikishi(rs));
            }
        }
        return lista;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void actualizarVictorias(int id, int victorias) throws SQLException {
        try (PreparedStatement ps = getConexion()
                .prepareStatement(SQL_ACTUALIZAR_VICTORIAS)) {
            ps.setInt(1, victorias);
            ps.setInt(2, id);
            ps.executeUpdate();
            LOGGER.info("Victorias actualizadas — id: " + id
                + ", victorias: " + victorias);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void marcarComoCombatido(int id) throws SQLException {
        try (PreparedStatement ps = getConexion()
                .prepareStatement(SQL_MARCAR_COMBATIDO)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            LOGGER.info("Luchador id " + id + " marcado como combatido.");
        }
    }

    // ── Métodos auxiliares ────────────────────────────────────────────────────

    /**
     * Construye un {@link Rikishi} a partir de un {@link ResultSet}
     * de la tabla {@code luchadores}, cargando también sus kimarites.
     *
     * @param rs {@link ResultSet} posicionado en la fila del luchador.
     * @return El {@link Rikishi} construido con sus kimarites.
     * @throws SQLException Si ocurre un error al leer el ResultSet.
     */
    private Rikishi construirRikishi(ResultSet rs) throws SQLException {
        int    id        = rs.getInt("id");
        String nombre    = rs.getString("nombre");
        double peso      = rs.getDouble("peso");
        int    victorias = rs.getInt("victorias");

        List<Kimarite> kimarites = obtenerKimaritesDe(id);
        Rikishi rikishi = new Rikishi(nombre, peso, kimarites);
        rikishi.setVictorias(victorias);
        return rikishi;
    }

    /**
     * Obtiene los kimarites asociados a un luchador desde la BD.
     *
     * @param idLuchador Id del luchador.
     * @return Lista de {@link Kimarite} del luchador.
     * @throws SQLException Si ocurre un error en la consulta.
     */
    private List<Kimarite> obtenerKimaritesDe(int idLuchador) throws SQLException {
        List<Kimarite> kimarites = new ArrayList<>();
        try (PreparedStatement ps = getConexion()
                .prepareStatement(SQL_KIMARITES_POR_LUCHADOR)) {
            ps.setInt(1, idLuchador);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    kimarites.add(new Kimarite(
                        rs.getString("nombre_kimarite"),
                        rs.getString("descripcion")
                    ));
                }
            }
        }
        return kimarites;
    }
}