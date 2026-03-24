package pa.taller3.servidor.vista;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import pa.taller3.servidor.modelo.Rikishi;

/**
 * Ventana principal del servidor de sumo.
 *
 * <p>Muestra en tiempo real:</p>
 * <ul>
 *   <li>Log de eventos del servidor (conexiones, registros, combates).</li>
 *   <li>Contador de luchadores conectados.</li>
 *   <li>Estado actual del combate en el Dohyō.</li>
 * </ul>
 *
 * <p><b>Esta vista NO contiene lógica de negocio.</b>
 * Solo expone métodos para que el controlador y el Dohyo actualicen la UI.</p>
 *
 * @author Programación Avanzada - Universidad Distrital
 * @version 1.0
 */
public class VentanaServidor extends JFrame {

    // ── Paleta de colores ─────────────────────────────────────────────────────

    /** Rojo oscuro — color principal del servidor. */
    private static final Color ROJO_OSCURO = new Color(139, 0, 0);

    /** Fondo oscuro del log. */
    private static final Color FONDO_LOG   = new Color(20, 20, 20);

    /** Fondo general. */
    private static final Color FONDO       = new Color(30, 30, 30);

    /** Dorado para acentos. */
    private static final Color DORADO      = new Color(255, 215, 0);

    /** Verde para eventos positivos en el log. */
    private static final Color VERDE       = new Color(0, 200, 0);

    // ── Componentes ───────────────────────────────────────────────────────────

    /** Área de texto con el log de eventos del servidor. */
    private JTextArea areaLog;

    /** Etiqueta con el contador de luchadores conectados. */
    private JLabel lblContadorLuchadores;

    /** Etiqueta con el estado actual del combate. */
    private JLabel lblEstadoCombate;

    /** Etiqueta con los luchadores del combate actual. */
    private JLabel lblCombateActual;

    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Construye e inicializa la ventana del servidor.
     */
    public VentanaServidor() {
        configurarVentana();
        construirUI();
    }

    /**
     * Configura las propiedades básicas del {@link JFrame}.
     */
    private void configurarVentana() {
        setTitle("⚔️  Servidor Dohyō — Sumo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 600);
        setLocationRelativeTo(null);
        setResizable(true);
        getContentPane().setBackground(FONDO);
    }

    /**
     * Construye la interfaz completa ensamblando sus paneles.
     */
    private void construirUI() {
        JPanel contenedor = new JPanel(new BorderLayout(10, 10));
        contenedor.setBackground(FONDO);
        contenedor.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        contenedor.add(construirPanelEncabezado(), BorderLayout.NORTH);
        contenedor.add(construirPanelLog(),        BorderLayout.CENTER);
        contenedor.add(construirPanelEstado(),     BorderLayout.SOUTH);

        setContentPane(contenedor);
    }
    
    


    /**
     * Construye el encabezado con título y contador de luchadores.
     *
     * @return Panel encabezado.
     */
    private JPanel construirPanelEncabezado() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ROJO_OSCURO);
        panel.setBorder(new EmptyBorder(12, 15, 12, 15));

        JLabel titulo = new JLabel("相撲  SERVIDOR DOHYŌ", SwingConstants.CENTER);
        titulo.setFont(new Font("Serif", Font.BOLD, 26));
        titulo.setForeground(Color.WHITE);

        lblContadorLuchadores = new JLabel("Luchadores: 0 / 6", SwingConstants.CENTER);
        lblContadorLuchadores.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblContadorLuchadores.setForeground(DORADO);

        panel.add(titulo,                BorderLayout.CENTER);
        panel.add(lblContadorLuchadores, BorderLayout.SOUTH);
        return panel;
    }

    /**
     * Construye el panel central con el área de log de eventos.
     *
     * @return Panel de log.
     */
    private JPanel construirPanelLog() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(FONDO);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(ROJO_OSCURO),
            "Log de Eventos",
            0, 0,
            new Font("SansSerif", Font.BOLD, 12),
            DORADO
        ));

        areaLog = new JTextArea();
        areaLog.setEditable(false);
        areaLog.setBackground(FONDO_LOG);
        areaLog.setForeground(VERDE);
        areaLog.setFont(new Font("Monospaced", Font.PLAIN, 12));
        areaLog.setBorder(new EmptyBorder(5, 8, 5, 8));

        JScrollPane scroll = new JScrollPane(areaLog);
        scroll.setBorder(null);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Construye el panel inferior con el estado del combate actual.
     *
     * @return Panel de estado.
     */
    private JPanel construirPanelEstado() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 5, 5));
        panel.setBackground(FONDO);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(ROJO_OSCURO),
            "Combate Actual",
            0, 0,
            new Font("SansSerif", Font.BOLD, 12),
            DORADO
        ));

        lblCombateActual = new JLabel("Sin combate activo", SwingConstants.CENTER);
        lblCombateActual.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblCombateActual.setForeground(Color.WHITE);

        lblEstadoCombate = new JLabel("", SwingConstants.CENTER);
        lblEstadoCombate.setFont(new Font("SansSerif", Font.ITALIC, 12));
        lblEstadoCombate.setForeground(DORADO);

        panel.add(lblCombateActual);
        panel.add(lblEstadoCombate);
        return panel;
    }

    // ── Métodos de actualización — invocados por controlador y Dohyo ──────────

    /**
     * Agrega un mensaje al log de eventos.
     * Seguro para llamar desde cualquier hilo.
     *
     * @param evento Mensaje a agregar al log.
     */
    public void logEvento(String evento) {
        SwingUtilities.invokeLater(() -> {
            areaLog.append(evento + "\n");
            areaLog.setCaretPosition(areaLog.getDocument().getLength());
        });
    }

    /**
     * Actualiza el contador de luchadores en el encabezado.
     *
     * @param cantidad Número de luchadores actualmente registrados.
     */
    public void actualizarContadorLuchadores(int cantidad) {
        SwingUtilities.invokeLater(() ->
            lblContadorLuchadores.setText("Luchadores: " + cantidad + " / 6"));
    }

    /**
     * Actualiza el panel de combate con los nombres de los contendientes.
     *
     * @param nombreA Nombre del primer luchador.
     * @param nombreB Nombre del segundo luchador.
     */
    public void mostrarCombate(String nombreA, String nombreB) {
        SwingUtilities.invokeLater(() -> {
            lblCombateActual.setText("⚔️  " + nombreA + "  vs  " + nombreB);
            lblEstadoCombate.setText("Combate en curso...");
        });
    }

    /**
     * Muestra el ganador del combate actual en el panel de estado.
     *
     * @param nombreGanador Nombre del luchador ganador.
     */
    public void mostrarGanador(String nombreGanador) {
        SwingUtilities.invokeLater(() ->
            lblEstadoCombate.setText("🏆 Ganador: " + nombreGanador));
    }

    /**
     * Muestra un mensaje de error mediante {@link JOptionPane}.
     *
     * @param mensaje Mensaje de error.
     */
    public void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje,
            "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Muestra un mensaje informativo mediante {@link JOptionPane}.
     *
     * @param mensaje Mensaje a mostrar.
     */
    public void mostrarInfo(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje,
            "Información", JOptionPane.INFORMATION_MESSAGE);
    }
    
    
    
}