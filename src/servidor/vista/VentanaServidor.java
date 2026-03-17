package servidor.vista;

import modelo.ResultadoTurno;
import modelo.Rikishi;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Ventana principal del servidor.
 *
 * <p>Muestra visualmente el dohyō, los dos luchadores que llegan,
 * el desarrollo del combate turno a turno en un log, y el resultado
 * final con el ganador destacado.</p>
 *
 * <p>Esta clase <b>no tiene lógica de negocio</b>. Solo recibe datos
 * del {@code ControladorServidor} y los muestra. El controlador llama
 * los métodos públicos de esta clase desde {@code SwingUtilities.invokeLater()}
 * para garantizar thread-safety con Swing.</p>
 *
 * <p><b>Separación de paneles:</b> encabezado (norte), luchadores+ring (centro),
 * log del combate (sur). Cada sección se construye en su propio método.</p>
 *
 * @author Programación Avanzada - Universidad Distrital
 * @version 1.0
 */
public class VentanaServidor extends JFrame {

    // ── Componentes de la GUI ─────────────────────────────────────────────────

    /** Etiqueta de estado general del servidor. */
    private JLabel lblEstado;

    /** Panel izquierdo: muestra datos del luchador 1. */
    private PanelLuchador panelLuchador1;

    /** Panel derecho: muestra datos del luchador 2. */
    private PanelLuchador panelLuchador2;

    /** Área de texto con el log del combate turno a turno. */
    private JTextArea areaLog;

    /** Etiqueta central que representa visualmente el dohyō. */
    private JLabel lblDohyo;

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
        setTitle("🏯 Servidor Dohyō — Combate de Sumo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(920, 700);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(new Color(30, 15, 5));
    }

    /**
     * Construye la interfaz completa organizando los paneles en BorderLayout.
     */
    private void construirUI() {
        JPanel contenedor = new JPanel(new BorderLayout(10, 10));
        contenedor.setBackground(new Color(30, 15, 5));
        contenedor.setBorder(new EmptyBorder(10, 10, 10, 10));

        contenedor.add(construirPanelEncabezado(), BorderLayout.NORTH);
        contenedor.add(construirPanelCentral(),    BorderLayout.CENTER);
        contenedor.add(construirPanelLog(),         BorderLayout.SOUTH);

        setContentPane(contenedor);
    }

    /**
     * Construye el encabezado con título y etiqueta de estado.
     *
     * @return Panel encabezado configurado.
     */
    private JPanel construirPanelEncabezado() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(139, 0, 0));
        panel.setBorder(new EmptyBorder(10, 15, 10, 15));

        JLabel titulo = new JLabel("相撲  COMBATE DE SUMO", SwingConstants.CENTER);
        titulo.setFont(new Font("Serif", Font.BOLD, 28));
        titulo.setForeground(Color.WHITE);

        lblEstado = new JLabel("⏳ Esperando luchadores...", SwingConstants.CENTER);
        lblEstado.setFont(new Font("SansSerif", Font.ITALIC, 13));
        lblEstado.setForeground(new Color(255, 215, 0));

        panel.add(titulo, BorderLayout.CENTER);
        panel.add(lblEstado, BorderLayout.SOUTH);
        return panel;
    }

    /**
     * Construye el panel central con los paneles de ambos luchadores
     * y el ring visual en el centro.
     *
     * @return Panel central configurado.
     */
    private JPanel construirPanelCentral() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(new Color(30, 15, 5));

        panelLuchador1 = new PanelLuchador("Luchador 1", new Color(70, 130, 180));
        panelLuchador2 = new PanelLuchador("Luchador 2", new Color(178, 34, 34));

        lblDohyo = new JLabel();
        lblDohyo.setHorizontalAlignment(SwingConstants.CENTER);
        lblDohyo.setPreferredSize(new Dimension(300, 240));
        lblDohyo.setText("<html><center>"
            + "<font size='8' color='#D2B48C'>⊙</font><br>"
            + "<font size='4' color='#FFD700'>DOHYŌ</font><br>"
            + "<font size='3' color='#C0C0C0'>Esperando luchadores...</font>"
            + "</center></html>");
        lblDohyo.setBackground(new Color(210, 180, 140));
        lblDohyo.setOpaque(true);
        lblDohyo.setBorder(BorderFactory.createLineBorder(new Color(139, 69, 19), 4));

        panel.add(panelLuchador1, BorderLayout.WEST);
        panel.add(lblDohyo,       BorderLayout.CENTER);
        panel.add(panelLuchador2, BorderLayout.EAST);
        return panel;
    }

    /**
     * Construye el panel inferior con el log textual del combate.
     *
     * @return Panel log configurado.
     */
    private JPanel construirPanelLog() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(30, 15, 5));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(255, 215, 0)),
            "📋 Log del Combate",
            0, 0,
            new Font("SansSerif", Font.BOLD, 12),
            new Color(255, 215, 0)
        ));

        areaLog = new JTextArea(8, 70);
        areaLog.setEditable(false);
        areaLog.setBackground(new Color(10, 5, 2));
        areaLog.setForeground(new Color(144, 238, 144));
        areaLog.setFont(new Font("Monospaced", Font.PLAIN, 12));
        areaLog.setLineWrap(true);

        JScrollPane scroll = new JScrollPane(areaLog);
        scroll.setBorder(null);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // ── Métodos públicos llamados por el controlador ──────────────────────────

    /**
     * Muestra la llegada de un luchador en el panel correspondiente.
     * Llamado por el controlador via {@code SwingUtilities.invokeLater()}.
     *
     * @param luchador El luchador que llegó.
     */
    public void mostrarLuchadorLlego(Rikishi luchador) {
        if (panelLuchador1.estaVacio()) {
            panelLuchador1.setLuchador(luchador);
        } else {
            panelLuchador2.setLuchador(luchador);
        }
        lblEstado.setText("⏳ " + luchador.getNombre() + " conectado. Esperando rival...");
        agregarLog("🥋 " + luchador.getNombre() + " llegó al dohyō.");
    }

    /**
     * Muestra el inicio del combate con ambos luchadores.
     *
     * @param l1 Primer luchador.
     * @param l2 Segundo luchador.
     */
    public void mostrarInicioCombate(Rikishi l1, Rikishi l2) {
        lblEstado.setText("⚔️  ¡TACHI-AI! El combate ha comenzado");
        lblDohyo.setText("<html><center>"
            + "<font size='6' color='red'>⚔️</font><br>"
            + "<font size='4' color='#FFD700'>COMBATE</font><br>"
            + "<font size='3' color='white'>EN CURSO</font>"
            + "</center></html>");
        agregarLog("━━━━ ¡TACHI-AI! " + l1.getNombre() + " VS " + l2.getNombre() + " ━━━━");
    }

    /**
     * Muestra el resultado de un turno en el log y actualiza el panel del atacante.
     *
     * @param luchador El luchador que atacó.
     * @param resultado Resultado del turno.
     */
    public void mostrarTurno(Rikishi luchador, ResultadoTurno resultado) {
        String tecnica = resultado.getTecnicaUsada() != null
            ? resultado.getTecnicaUsada().getNombre() : "???";
        String msg = resultado.isSacoAlOponente()
            ? "💥 " + luchador.getNombre() + " usa [" + tecnica + "] → ¡SACA AL RIVAL!"
            : "🔄 " + luchador.getNombre() + " usa [" + tecnica + "] → rival se mantiene";
        agregarLog(msg);

        if (panelLuchador1.correspondeA(luchador)) {
            panelLuchador1.resaltarAtaque(resultado.isSacoAlOponente());
        } else {
            panelLuchador2.resaltarAtaque(resultado.isSacoAlOponente());
        }
    }

    /**
     * Muestra al ganador con estilo destacado.
     *
     * @param ganador El luchador ganador.
     */
    public void mostrarGanador(Rikishi ganador) {
        lblEstado.setText("🏆 ¡" + ganador.getNombre() + " GANA! Victorias: " + ganador.getVictorias());
        lblEstado.setForeground(new Color(255, 215, 0));
        agregarLog("🏆 ━━ GANADOR: " + ganador.getNombre()
            + " | Victorias totales: " + ganador.getVictorias() + " ━━");

        if (panelLuchador1.correspondeA(ganador)) {
            panelLuchador1.marcarGanador();
            panelLuchador2.marcarPerdedor();
        } else {
            panelLuchador2.marcarGanador();
            panelLuchador1.marcarPerdedor();
        }
    }

    /**
     * Registra que un cliente confirmó la recepción del resultado.
     *
     * @param luchador El luchador cuyo cliente confirmó.
     */
    public void mostrarClienteDesconectado(Rikishi luchador) {
        agregarLog("✅ " + luchador.getNombre() + " recibió resultado y se desconectó.");
    }

    /**
     * Muestra un mensaje de error en el log.
     *
     * @param mensaje Descripción del error.
     */
    public void mostrarError(String mensaje) {
        agregarLog("❌ ERROR: " + mensaje);
    }

    /**
     * Agrega texto al área de log y hace scroll automático al final.
     *
     * @param texto Línea a agregar.
     */
    private void agregarLog(String texto) {
        areaLog.append(texto + "\n");
        areaLog.setCaretPosition(areaLog.getDocument().getLength());
    }
}
