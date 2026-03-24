package pa.taller3.servidor.controlador;

import pa.taller3.servidor.dao.RikishiDAO;
import pa.taller3.servidor.hilo.HiloLuchador;
import pa.taller3.servidor.modelo.Kimarite;
import pa.taller3.servidor.modelo.Rikishi;
import pa.taller3.servidor.vista.VentanaServidor;

import java.io.*;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.*;

/**
 * Orquestador de los combates de sumo en el servidor.
 *
 * <p>El Dohyo es el ring sagrado donde se desarrollan los combates.
 * Corre en su propio hilo y coordina la secuencia de combates entre
 * los {@link HiloLuchador} registrados.</p>
 *
 * <p>Flujo de combates:</p>
 * <ol>
 *   <li>Selecciona aleatoriamente dos luchadores de la lista de pendientes.</li>
 *   <li>Sincroniza los turnos: cada luchador lanza un {@link Kimarite}
 *       y espera hasta 500ms al rival.</li>
 *   <li>Determina si el kimarite saca al rival del dohyō (baja probabilidad).</li>
 *   <li>Actualiza victorias y marca perdedor como combatido en BD.</li>
 *   <li>Guarda los datos de ambos contendientes en archivo de acceso
 *       aleatorio al finalizar CADA combate.</li>
 *   <li>Al terminar TODOS los combates notifica a todos los hilos.</li>
 * </ol>
 *
 * <p><b>Estructura del registro en archivo de acceso aleatorio (94 bytes):</b></p>
 * <pre>
 *   nombre    : 40 chars × 2 bytes = 80 bytes
 *   peso      : double             =  8 bytes
 *   victorias : int                =  4 bytes
 *   resultado : char               =  2 bytes ('G' = ganó, 'P' = perdió)
 * </pre>
 *
 * <p><b>Principio SRP:</b> solo orquesta combates y persiste resultados.</p>
 *
 * @author Programación Avanzada - Universidad Distrital
 * @version 1.0
 */
public class Dohyo extends Thread {

    /** Logger del Dohyo. */
    private static final Logger LOGGER =
        Logger.getLogger(Dohyo.class.getName());

    /** Tamaño fijo de cada registro en bytes. */
    private static final int TAMANO_REGISTRO = 94;

    /** Longitud fija del campo nombre en caracteres. */
    private static final int LONGITUD_NOMBRE = 40;

    /**
     * Probabilidad de que un kimarite saque al rival del dohyō.
     * 15% → la mayoría de veces no saca, eventualmente sí.
     */
    private static final double PROBABILIDAD_SAQUE = 0.15;

    /** Espera máxima entre turnos en milisegundos. */
    private static final int MAX_ESPERA_MS = 500;

    /** Ruta del archivo de acceso aleatorio de resultados. */
    private static final String RUTA_ARCHIVO = "./data/resultados.dat";

    /** Lista de hilos luchadores que participarán en los combates. */
    private final List<HiloLuchador> luchadores;

    /** DAO para actualizar victorias y marcar combatidos en BD. */
    private final RikishiDAO dao;

    /** Vista del servidor para actualizar la UI. */
    private final VentanaServidor vista;

    /** Generador de números aleatorios. */
    private final Random random;

    /**
     * Mapa que acumula el resultado final de cada hilo.
     * Se usa al final para notificar a todos los clientes.
     * Clave: HiloLuchador, Valor: 'G' o 'P'.
     */
    private final Map<HiloLuchador, Character> resultados;

    /**
     * Índice del próximo registro a escribir en el archivo.
     * Se incrementa de a dos por cada combate (ganador + perdedor).
     */
    private int indiceRegistro;

    /**
     * Construye el Dohyo con la lista de hilos, el DAO y la vista.
     *
     * @param luchadores Lista de {@link HiloLuchador} registrados.
     * @param dao        DAO para operaciones de BD.
     * @param vista      Vista del servidor para mostrar eventos.
     */
    public Dohyo(List<HiloLuchador> luchadores, RikishiDAO dao,
                 VentanaServidor vista) {
        this.luchadores     = new ArrayList<>(luchadores);
        this.dao            = dao;
        this.vista          = vista;
        this.random         = new Random();
        this.resultados     = new LinkedHashMap<>();
        this.indiceRegistro = 0;

        // Inicializar todos como perdedores por defecto
        for (HiloLuchador h : luchadores) {
            resultados.put(h, 'P');
        }
    }

    /**
     * Ciclo principal del Dohyo.
     * Orquesta todos los combates y al final notifica a todos los hilos.
     */
    @Override
    public void run() {
        vista.logEvento("🏯 ¡El Dohyō está listo! Iniciando combates...");

        List<HiloLuchador> pendientes = new ArrayList<>(luchadores);

        HiloLuchador ganadorActual = extraerAleatorio(pendientes);
        HiloLuchador retador       = extraerAleatorio(pendientes);

        while (retador != null) {
            ganadorActual = ejecutarCombate(ganadorActual, retador);
            retador = pendientes.isEmpty() ? null : extraerAleatorio(pendientes);
        }

        // Marcar ganador final en el mapa
        resultados.put(ganadorActual, 'G');
        vista.logEvento("🥇 ¡CAMPEÓN FINAL: "
            + ganadorActual.getRikishi().getNombre() + "!");

        // Mostrar contenido completo del archivo acumulado
        vista.logEvento("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        vista.logEvento("📋 RESULTADOS FINALES (archivo acceso aleatorio):");
        mostrarArchivoResultados();

        // Notificar a todos los clientes ahora que terminaron todos los combates
        notificarATodos(ganadorActual.getRikishi().getNombre());

        // Marcar campeón como combatido en BD
        marcarCombatidoBD(ganadorActual);

        vista.logEvento("🏁 Todos los combates han terminado.");
    }

    /**
     * Ejecuta un combate completo entre dos hilos luchadores.
     *
     * <p>Al terminar el combate:</p>
     * <ul>
     *   <li>Actualiza victorias del ganador en BD.</li>
     *   <li>Marca al perdedor como combatido en BD.</li>
     *   <li>Guarda los datos de ambos contendientes en el archivo
     *       de acceso aleatorio (datos frescos desde BD).</li>
     * </ul>
     *
     * @param hiloA Primer luchador.
     * @param hiloB Segundo luchador.
     * @return El {@link HiloLuchador} ganador del combate.
     */
    private HiloLuchador ejecutarCombate(HiloLuchador hiloA, HiloLuchador hiloB) {
        Rikishi rikishiA = hiloA.getRikishi();
        Rikishi rikishiB = hiloB.getRikishi();

        rikishiA.setRival(rikishiB);
        rikishiB.setRival(rikishiA);
        rikishiA.setEnDohyo(true);
        rikishiB.setEnDohyo(true);

        vista.logEvento("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        vista.logEvento("⚔️  COMBATE: " + rikishiA.getNombre()
            + " vs " + rikishiB.getNombre());
        vista.mostrarCombate(rikishiA.getNombre(), rikishiB.getNombre());

        // Turnos alternados hasta que uno salga del dohyō
        boolean turnoA = true;
        while (rikishiA.isEnDohyo() && rikishiB.isEnDohyo()) {
            if (turnoA) ejecutarTurno(rikishiA, rikishiB);
            else        ejecutarTurno(rikishiB, rikishiA);
            turnoA = !turnoA;
        }

        HiloLuchador ganador  = rikishiA.isEnDohyo() ? hiloA : hiloB;
        HiloLuchador perdedor = rikishiA.isEnDohyo() ? hiloB : hiloA;

        ganador.getRikishi().incrementarVictorias();

        vista.logEvento("🏆 Ganador del combate: "
            + ganador.getRikishi().getNombre()
            + " (victorias: " + ganador.getRikishi().getVictorias() + ")");
        vista.mostrarGanador(ganador.getRikishi().getNombre());

        // 1. Actualizar victorias del ganador en BD
        actualizarVictoriasBD(ganador);

        // 2. Marcar perdedor como combatido en BD
        marcarCombatidoBD(perdedor);
        resultados.put(perdedor, 'P');

        // 3. Guardar ambos contendientes en archivo al final de ESTE combate
        //    Los datos vienen expresamente de la BD via consulta
        guardarCombateEnArchivo(ganador, 'G');
        guardarCombateEnArchivo(perdedor, 'P');

        return ganador;
    }

    /**
     * Ejecuta el turno de un luchador: selecciona un kimarite aleatorio,
     * espera un tiempo aleatorio (máx 500ms) y determina si saca al rival.
     *
     * @param atacante Luchador que ejecuta el kimarite.
     * @param defensor Luchador que recibe el kimarite.
     */
    private void ejecutarTurno(Rikishi atacante, Rikishi defensor) {
        try {
            Thread.sleep(random.nextInt(MAX_ESPERA_MS));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Kimarite kimarite  = atacante.obtenerKimariteAleatorio();
        String   nombreKim = kimarite != null ? kimarite.getNombre() : "Empujón";

        vista.logEvento("  → " + atacante.getNombre()
            + " ejecuta: " + nombreKim);

        boolean saca = random.nextDouble() < PROBABILIDAD_SAQUE;
        if (saca) {
            defensor.setEnDohyo(false);
            vista.logEvento("  💥 ¡" + defensor.getNombre()
                + " fue sacado del Dohyō!");
        } else {
            vista.logEvento("  🔄 " + defensor.getNombre()
                + " resiste dentro del Dohyō.");
        }
    }

    /**
     * Guarda los datos de un contendiente en el archivo de acceso aleatorio.
     * Los datos se obtienen expresamente de la BD via {@link RikishiDAO}.
     *
     * <p>Estructura del registro (94 bytes):</p>
     * <ul>
     *   <li>nombre    : 40 chars × 2 bytes = 80 bytes</li>
     *   <li>peso      : double             =  8 bytes</li>
     *   <li>victorias : int                =  4 bytes</li>
     *   <li>resultado : char               =  2 bytes</li>
     * </ul>
     *
     * @param hilo      Hilo del luchador a guardar.
     * @param resultado 'G' si ganó este combate, 'P' si perdió.
     */
    private void guardarCombateEnArchivo(HiloLuchador hilo, char resultado) {
        // Traer datos frescos de la BD
        Rikishi rikishi = hilo.getRikishi();
        if (hilo.getIdBD() != -1) {
            try {
                Rikishi deBD = dao.buscarPorId(hilo.getIdBD());
                if (deBD != null) rikishi = deBD;
            } catch (SQLException ex) {
                LOGGER.log(Level.WARNING,
                    "No se pudo leer de BD, usando datos en memoria.", ex);
            }
        }

        try (RandomAccessFile raf = new RandomAccessFile(RUTA_ARCHIVO, "rw")) {
            raf.seek((long) indiceRegistro * TAMANO_REGISTRO);

            // Nombre con padding hasta 40 chars
            String nombre = rikishi.getNombre();
            if (nombre.length() > LONGITUD_NOMBRE) {
                nombre = nombre.substring(0, LONGITUD_NOMBRE);
            }
            StringBuilder sb = new StringBuilder(nombre);
            while (sb.length() < LONGITUD_NOMBRE) sb.append(' ');
            for (char c : sb.toString().toCharArray()) raf.writeChar(c);

            raf.writeDouble(rikishi.getPeso());
            raf.writeInt(rikishi.getVictorias());
            raf.writeChar(resultado);

            indiceRegistro++;
            LOGGER.info("Registro guardado — posición "
                + (indiceRegistro - 1) + ": " + rikishi.getNombre()
                + " | " + resultado);

        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Error al escribir en archivo.", ex);
            vista.logEvento("❌ Error al guardar en archivo: "
                + ex.getMessage());
        }
    }

    /**
     * Notifica a TODOS los hilos su resultado final una vez terminados
     * todos los combates.
     *
     * @param nombreCampeon Nombre del campeón final.
     */
    private void notificarATodos(String nombreCampeon) {
        for (Map.Entry<HiloLuchador, Character> entrada : resultados.entrySet()) {
            HiloLuchador hilo      = entrada.getKey();
            char         resultado = entrada.getValue();
            Rikishi      rikishi   = hilo.getRikishi();

            String detalle = "Campeón: " + nombreCampeon
                + " | " + rikishi.getNombre()
                + " | Victorias: " + rikishi.getVictorias();

            if (resultado == 'G') {
                hilo.notificarResultado("GANASTE:" + detalle);
            } else {
                hilo.notificarResultado("PERDISTE:" + detalle);
            }

            vista.logEvento("📨 Notificado: " + rikishi.getNombre()
                + " → " + (resultado == 'G' ? "GANASTE" : "PERDISTE"));
        }
    }

    /**
     * Lee y muestra en consola y en la vista todos los registros
     * acumulados en el archivo de acceso aleatorio.
     */
    private void mostrarArchivoResultados() {
        try (RandomAccessFile raf = new RandomAccessFile(RUTA_ARCHIVO, "r")) {
            int total = (int) (raf.length() / TAMANO_REGISTRO);

            for (int i = 0; i < total; i++) {
                raf.seek((long) i * TAMANO_REGISTRO);

                StringBuilder nombre = new StringBuilder();
                for (int j = 0; j < LONGITUD_NOMBRE; j++) {
                    nombre.append(raf.readChar());
                }
                double peso      = raf.readDouble();
                int    victorias = raf.readInt();
                char   resultado = raf.readChar();

                String linea = String.format(
                    "[%d] %-40s | Peso: %6.1f kg | Victorias: %d | %s",
                    i,
                    nombre.toString().trim(),
                    peso,
                    victorias,
                    resultado == 'G' ? "GANÓ 🏆" : "PERDIÓ 💀"
                );
                System.out.println(linea);
                vista.logEvento(linea);
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Error al leer archivo de resultados", ex);
            vista.logEvento("❌ Error al leer archivo de resultados.");
        }
    }

    /**
     * Actualiza las victorias del ganador en la base de datos.
     *
     * @param hilo Hilo del luchador ganador.
     */
    private void actualizarVictoriasBD(HiloLuchador hilo) {
        if (hilo.getIdBD() == -1) return;
        try {
            dao.actualizarVictorias(hilo.getIdBD(),
                hilo.getRikishi().getVictorias());
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al actualizar victorias en BD", ex);
            vista.logEvento("❌ Error al actualizar victorias en BD.");
        }
    }

    /**
     * Marca un luchador como combatido en la base de datos.
     *
     * @param hilo Hilo del luchador a marcar.
     */
    private void marcarCombatidoBD(HiloLuchador hilo) {
        if (hilo.getIdBD() == -1) return;
        try {
            dao.marcarComoCombatido(hilo.getIdBD());
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al marcar combatido en BD", ex);
            vista.logEvento("❌ Error al marcar combatido en BD.");
        }
    }

    /**
     * Extrae un {@link HiloLuchador} aleatorio de la lista,
     * removiéndolo de ella.
     *
     * @param lista Lista de hilos disponibles.
     * @return Un hilo aleatorio, o {@code null} si la lista está vacía.
     */
    private HiloLuchador extraerAleatorio(List<HiloLuchador> lista) {
        if (lista.isEmpty()) return null;
        return lista.remove(random.nextInt(lista.size()));
    }
}