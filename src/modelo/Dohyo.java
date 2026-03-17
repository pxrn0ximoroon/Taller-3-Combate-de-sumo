package modelo;

import java.util.Random;

/**
 * Representa el dohyō (ring de sumo) donde se ejecuta el combate.
 *
 * <p>Esta es la clase más crítica del sistema en términos de concurrencia.
 * Actúa como <b>monitor Java</b>: sincroniza los dos hilos (luchadores)
 * para que cada uno espere el turno del otro.</p>
 *
 * <h2>Flujo del combate dentro del dohyō:</h2>
 * <ol>
 *   <li>Los dos hilos llaman {@link #subirAlDohyo(Rikishi)}.
 *       El primero en llegar hace {@code wait()} hasta que llegue el segundo.</li>
 *   <li>Cuando llegan los dos, se asignan como rivales y se sortea quién ataca.</li>
 *   <li>Cada hilo llama {@link #ejecutarTurno(Rikishi)} en bucle.</li>
 *   <li>Si no es su turno, el hilo espera con {@code wait(500ms)}.</li>
 *   <li>Cuando le toca, selecciona kimarite aleatorio y evalúa el saque (15% prob.).</li>
 *   <li>Cambia el turno y llama {@code notifyAll()} para despertar al rival.</li>
 *   <li>El combate termina cuando un luchador queda {@code dentroDohyo = false}.</li>
 * </ol>
 *
 * <p><b>Por qué synchronized + wait/notifyAll:</b><br>
 * Sin sincronización, ambos hilos podrían ejecutar {@code turnoActual == miIndice}
 * al mismo tiempo, causando que los dos ataquen simultáneamente (condición de carrera).
 * El monitor garantiza exclusión mutua.</p>
 *
 * @author Programación Avanzada - Universidad Distrital
 * @version 1.0
 */
public class Dohyo {

    /**
     * Probabilidad de sacar al oponente con un kimarite.
     * 15% → la mayoría de veces el rival se mantiene,
     * pero eventualmente es sacado (combate dinámico).
     */
    private static final double PROBABILIDAD_SAQUE = 0.15;

    /**
     * Espera máxima en ms para que el oponente lance su kimarite.
     * El enunciado fija este tope en 500 ms.
     */
    private static final int MAX_ESPERA_MS = 500;

    /** Primer luchador que llega al dohyō. */
    private Rikishi luchador1;

    /** Segundo luchador que llega al dohyō. */
    private Rikishi luchador2;

    /** Cuántos luchadores han llegado ya. El combate espera hasta que sean 2. */
    private int luchadoresllegados;

    /**
     * Índice del luchador con turno de ataque.
     * 0 = luchador1 ataca; 1 = luchador2 ataca.
     */
    private int turnoActual;

    /** true cuando uno de los dos luchadores queda fuera del dohyō. */
    private volatile boolean combateTerminado;

    /** El ganador del combate. null mientras el combate siga. */
    private Rikishi ganador;

    /** Generador de números aleatorios para sorteo de turno y evaluación de saque. */
    private final Random random;

    /**
     * Construye un dohyō vacío listo para recibir dos luchadores.
     */
    public Dohyo() {
        this.luchadoresllegados = 0;
        this.combateTerminado = false;
        this.random = new Random();
    }

    // ── Sincronización de llegada ─────────────────────────────────────────────

    /**
     * Registra la llegada de un luchador al dohyō y sincroniza el inicio.
     *
     * <p>El primer luchador en llegar hace {@code wait()} hasta que el segundo
     * llegue y lo despierte con {@code notifyAll()}. En ese momento se asignan
     * rivales mutuamente y se sortea el turno inicial.</p>
     *
     * <p>El método es {@code synchronized} para que la operación de contar
     * luchadores sea atómica entre los dos hilos.</p>
     *
     * @param luchador El luchador que llega al dohyō.
     * @throws InterruptedException Si el hilo es interrumpido mientras espera.
     */
    public synchronized void subirAlDohyo(Rikishi luchador) throws InterruptedException {
        luchadoresllegados++;

        if (luchadoresllegados == 1) {
            luchador1 = luchador;
            // El primer hilo espera al segundo
            while (luchadoresllegados < 2) {
                wait();
            }
        } else {
            luchador2 = luchador;
            // Configurar rivalidad mutua
            luchador1.setRival(luchador2);
            luchador2.setRival(luchador1);
            // Sortear quién ataca primero (0 o 1 aleatoriamente)
            turnoActual = random.nextInt(2);
            // Despertar al hilo 1 que estaba esperando
            notifyAll();
        }
    }

    // ── Ejecución de turno ────────────────────────────────────────────────────

    /**
     * Ejecuta el turno de un luchador: espera su turno, aplica kimarite,
     * evalúa el saque y cede el turno al rival.
     *
     * <h3>Detalle del mecanismo de espera:</h3>
     * <p>El hilo llama {@code wait(500)} mientras no sea su turno.
     * {@code wait(500)} es diferente a {@code wait()}: libera el monitor
     * y espera como máximo 500ms antes de reevaluar la condición,
     * cumpliendo el requerimiento del enunciado.</p>
     *
     * <h3>Evaluación de saque:</h3>
     * <p>Se genera un {@code double} entre 0.0 y 1.0.
     * Solo si es menor a {@link #PROBABILIDAD_SAQUE} (15%) el rival es sacado.
     * Esto hace que la mayoría de turnos el rival se mantenga en pie,
     * pero eventualmente sea eliminado.</p>
     *
     * @param luchador El luchador que intenta ejecutar su turno.
     * @return {@link ResultadoTurno} con la técnica usada y si sacó al rival.
     * @throws InterruptedException Si el hilo es interrumpido durante la espera.
     */
    public synchronized ResultadoTurno ejecutarTurno(Rikishi luchador)
            throws InterruptedException {

        int miIndice = (luchador == luchador1) ? 0 : 1;

        // Esperar turno (máximo 500ms por iteración)
        while (turnoActual != miIndice && !combateTerminado) {
            wait(MAX_ESPERA_MS);
        }

        // Si el combate terminó mientras esperaba, salir sin atacar
        if (combateTerminado) {
            return new ResultadoTurno(null, false);
        }

        // Seleccionar técnica aleatoria del repertorio
        Kimarite tecnica = luchador.seleccionarKimariteAleatorio();

        // Evaluar si la técnica saca al oponente (15% de probabilidad)
        boolean sacaAlOponente = random.nextDouble() < PROBABILIDAD_SAQUE;

        if (sacaAlOponente) {
            luchador.getRival().setDentroDohyo(false);
            combateTerminado = true;
            ganador = luchador;
            ganador.incrementarVictorias();
        }

        // Ceder turno al rival y notificar
        turnoActual = (miIndice == 0) ? 1 : 0;
        notifyAll();

        return new ResultadoTurno(tecnica, sacaAlOponente);
    }

    // ── Getters de estado ─────────────────────────────────────────────────────

    /** @return true si el combate ya tiene un ganador. */
    public boolean isCombateTerminado() { return combateTerminado; }

    /** @return El ganador, o null si el combate continúa. */
    public Rikishi getGanador() { return ganador; }

    /** @return Primer luchador registrado. */
    public Rikishi getLuchador1() { return luchador1; }

    /** @return Segundo luchador registrado. */
    public Rikishi getLuchador2() { return luchador2; }
}
