package pa.taller3.servidor.dao;

import pa.taller3.servidor.modelo.Rikishi;

import java.sql.SQLException;
import java.util.List;

/**
 * Interfaz DAO para operaciones de persistencia de {@link Rikishi}.
 *
 * <p>Define el contrato de acceso a datos para los luchadores,
 * desacoplando la lógica de negocio de la implementación concreta
 * de base de datos.</p>
 *
 * <p><b>Principio ISP:</b> solo declara las operaciones necesarias
 * para el flujo del combate de sumo.</p>
 *
 * <p><b>Principio DIP:</b> el {@code ControladorServidor} y el {@code Dohyo}
 * dependen de esta interfaz, no de {@code RikishiDAOImpl}.</p>
 *
 * @author Programación Avanzada - Universidad Distrital
 * @version 1.0
 */
public interface RikishiDAO {

    /**
     * Inserta un nuevo luchador en la base de datos junto con sus kimarites.
     *
     * @param rikishi El luchador a insertar.
     * @return El id generado por la base de datos, o {@code -1} si falló.
     * @throws SQLException Si ocurre un error en la base de datos.
     */
    int insertar(Rikishi rikishi) throws SQLException;

    /**
     * Busca un luchador por su id de base de datos.
     *
     * @param id Id del luchador en la BD.
     * @return El {@link Rikishi} encontrado, o {@code null} si no existe.
     * @throws SQLException Si ocurre un error en la base de datos.
     */
    Rikishi buscarPorId(int id) throws SQLException;

    /**
     * Retorna todos los luchadores almacenados, incluyendo sus kimarites.
     *
     * @return Lista de todos los {@link Rikishi} registrados.
     * @throws SQLException Si ocurre un error en la base de datos.
     */
    List<Rikishi> obtenerTodos() throws SQLException;

    /**
     * Retorna luchadores que aún no han combatido ({@code ha_combatido = 0}).
     *
     * @return Lista de {@link Rikishi} disponibles para combatir.
     * @throws SQLException Si ocurre un error en la base de datos.
     */
    List<Rikishi> obtenerDisponibles() throws SQLException;

    /**
     * Actualiza las victorias de un luchador en la base de datos.
     *
     * @param id        Id del luchador en la BD.
     * @param victorias Nuevo número de victorias.
     * @throws SQLException Si ocurre un error en la base de datos.
     */
    void actualizarVictorias(int id, int victorias) throws SQLException;

    /**
     * Marca un luchador como ya combatido ({@code ha_combatido = 1}).
     *
     * @param id Id del luchador en la BD.
     * @throws SQLException Si ocurre un error en la base de datos.
     */
    void marcarComoCombatido(int id) throws SQLException;
}