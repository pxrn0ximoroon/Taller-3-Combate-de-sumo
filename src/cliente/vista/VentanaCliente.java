package cliente.vista;

import modelo.Kimarite;
import modelo.Rikishi;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Ventana principal del cliente.
 *
 * <p>Permite al usuario:</p>
 * <ol>
 *   <li>Cargar el archivo {@code .properties} con kimarites (via {@link JFileChooser}).</li>
 *   <li>Ingresar nombre y peso del luchador.</li>
 *   <li>Seleccionar las técnicas que domina (selección múltiple).</li>
 *   <li>Enviar el luchador al servidor.</li>
 *   <li>Ver el resultado del combate (victoria o derrota).</li>
 * </ol>
 *
 * <p><b>Esta vista NO contiene lógica de negocio.</b> Solo recoge datos
 * y los expone al controlador mediante getters.</p>
 *
 * <p><b>Separación de eventos (requerimiento del enunciado):</b><br>
 * Los listeners se registran desde el controlador con
 * {@link #agregarListenerCargarProperties(ActionListener)} y
 * {@link #agregarListenerCombatir(ActionListener)}.
 * La vista no conoce qué hace cada listener.</p>
 *
 * @author Programación Avanzada - Universidad Distrital
 * @version 1.0
 */
public class VentanaCliente extends JFrame {

    // ── Componentes del formulario ────────────────────────────────────────────

    /** Campo para el nombre del luchador. */
    private JTextField txtNombre;

    /** Spinner numérico para el peso. */
    private JSpinner spinnerPeso;

    /** Lista visual de kimarites disponibles. */
    private JList<Kimarite> listaKimarites;

    /** Modelo de datos de la lista. */
    private DefaultListModel<Kimarite> modeloLista;

    /** Muestra la ruta del archivo .properties seleccionado. */
    private JLabel lblArchivoProperties;

    // ── Botones ───────────────────────────────────────────────────────────────

    /** Abre el JFileChooser para seleccionar el .properties. */
    private JButton btnCargarProperties;

    /** Envía el luchador al servidor e inicia el combate. */
    private JButton btnCombatir;

    // ── Panel de resultado ────────────────────────────────────────────────────

    /** Panel visible al terminar el combate con el resultado. */
    private JPanel panelResultado;

    /** Título grande del resultado (¡VICTORIA! / DERROTA). */
    private JLabel lblResultado;

    /** Detalles del ganador. */
    private JLabel lblDetalleGanador;

    /** Estado general en la parte inferior. */
    private JLabel lblEstado;

    /**
     * Construye e inicializa la ventana del cliente.
     */
    public VentanaCliente() {
        configurarVentana();
        construirUI();
    }

    /**
     * Configura las propiedades básicas del {@link JFrame}.
     */
    private void configurarVentana() {
        setTitle("🥋 Rikishi — Registrar Luchador de Sumo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(560, 680);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(new Color(240, 230, 210));
    }

    /**
     * Construye la interfaz completa con sus paneles.
     */
    private void construirUI() {
        JPanel contenedor = new JPanel(new BorderLayout(10, 10));
        contenedor.setBackground(new Color(240, 230, 210));
        contenedor.setBorder(new EmptyBorder(15, 15, 15, 15));

        contenedor.add(construirPanelEncabezado(), BorderLayout.NORTH);
        contenedor.add(construirPanelFormulario(), BorderLayout.CENTER);
        contenedor.add(construirPanelAcciones(),   BorderLayout.SOUTH);

        setContentPane(contenedor);
    }

    /**
     * Construye el encabezado decorativo.
     *
     * @return Panel encabezado.
     */
    private JPanel construirPanelEncabezado() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(139, 0, 0));
        panel.setBorder(new EmptyBorder(12, 15, 12, 15));

        JLabel titulo = new JLabel("相撲  REGISTRAR RIKISHI", SwingConstants.CENTER);
        titulo.setFont(new Font("Serif", Font.BOLD, 24));
        titulo.setForeground(Color.WHITE);

        JLabel subtitulo = new JLabel("Prepara tu luchador para el Dohyō", SwingConstants.CENTER);
        subtitulo.setFont(new Font("SansSerif", Font.ITALIC, 12));
        subtitulo.setForeground(new Color(255, 215, 0));

        panel.add(titulo, BorderLayout.CENTER);
        panel.add(subtitulo, BorderLayout.SOUTH);
        return panel;
    }

    /**
     * Construye el formulario de datos: nombre, peso y lista de kimarites.
     *
     * @return Panel formulario.
     */
    private JPanel construirPanelFormulario() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(240, 230, 210));

        // ── Sub-panel datos básicos ──
        JPanel panelDatos = new JPanel(new GridBagLayout());
        panelDatos.setBackground(new Color(250, 245, 235));
        panelDatos.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(139, 0, 0)),
            "Datos del Luchador",
            0, 0, new Font("SansSerif", Font.BOLD, 12), new Color(139, 0, 0)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Nombre
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        panelDatos.add(new JLabel("Nombre del Rikishi:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        txtNombre = new JTextField(20);
        txtNombre.setFont(new Font("SansSerif", Font.PLAIN, 14));
        panelDatos.add(txtNombre, gbc);

        // Peso
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        panelDatos.add(new JLabel("Peso (kg):"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        spinnerPeso = new JSpinner(new SpinnerNumberModel(80.0, 50.0, 300.0, 0.5));
        spinnerPeso.setFont(new Font("SansSerif", Font.PLAIN, 14));
        panelDatos.add(spinnerPeso, gbc);

        // ── Sub-panel kimarites ──
        JPanel panelKimarites = new JPanel(new BorderLayout(5, 5));
        panelKimarites.setBackground(new Color(250, 245, 235));
        panelKimarites.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(139, 0, 0)),
            "Técnicas Kimarite — selecciona en las que es experto tu rikishi",
            0, 0, new Font("SansSerif", Font.BOLD, 12), new Color(139, 0, 0)
        ));

        // Botón + label del archivo
        JPanel panelArchivo = new JPanel(new BorderLayout(5, 0));
        panelArchivo.setBackground(new Color(250, 245, 235));

        btnCargarProperties = new JButton("📂 Cargar técnicas (.properties)");
        btnCargarProperties.setBackground(new Color(70, 130, 180));
        btnCargarProperties.setForeground(Color.WHITE);
        btnCargarProperties.setFont(new Font("SansSerif", Font.BOLD, 12));

        lblArchivoProperties = new JLabel("Ningún archivo seleccionado");
        lblArchivoProperties.setFont(new Font("SansSerif", Font.ITALIC, 11));
        lblArchivoProperties.setForeground(Color.GRAY);

        panelArchivo.add(btnCargarProperties, BorderLayout.WEST);
        panelArchivo.add(lblArchivoProperties, BorderLayout.CENTER);

        // Lista de kimarites con selección múltiple
        modeloLista  = new DefaultListModel<>();
        listaKimarites = new JList<>(modeloLista);
        listaKimarites.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        listaKimarites.setBackground(new Color(255, 250, 240));
        listaKimarites.setFont(new Font("SansSerif", Font.PLAIN, 13));
        listaKimarites.setCellRenderer(new KimariteRenderer());

        JScrollPane scroll = new JScrollPane(listaKimarites);
        scroll.setPreferredSize(new Dimension(400, 190));

        JLabel tip = new JLabel("💡 Ctrl+Click para seleccionar múltiples técnicas");
        tip.setFont(new Font("SansSerif", Font.ITALIC, 11));
        tip.setForeground(new Color(100, 100, 100));

        panelKimarites.add(panelArchivo, BorderLayout.NORTH);
        panelKimarites.add(scroll,       BorderLayout.CENTER);
        panelKimarites.add(tip,          BorderLayout.SOUTH);

        panel.add(panelDatos,     BorderLayout.NORTH);
        panel.add(panelKimarites, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Construye el panel inferior con el botón de combatir, estado y resultado.
     *
     * @return Panel de acciones.
     */
    private JPanel construirPanelAcciones() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(new Color(240, 230, 210));

        btnCombatir = new JButton("⚔️  ¡ENTRAR AL DOHYŌ!");
        btnCombatir.setFont(new Font("SansSerif", Font.BOLD, 16));
        btnCombatir.setBackground(new Color(139, 0, 0));
        btnCombatir.setForeground(Color.WHITE);
        btnCombatir.setPreferredSize(new Dimension(300, 50));

        lblEstado = new JLabel("Registra tu luchador y entra al combate.", SwingConstants.CENTER);
        lblEstado.setFont(new Font("SansSerif", Font.ITALIC, 12));
        lblEstado.setForeground(new Color(80, 80, 80));

        // Panel de resultado (oculto hasta que termine el combate)
        panelResultado = new JPanel(new BorderLayout());
        panelResultado.setBackground(new Color(240, 230, 210));
        panelResultado.setVisible(false);

        lblResultado = new JLabel("", SwingConstants.CENTER);
        lblResultado.setFont(new Font("Serif", Font.BOLD, 34));

        lblDetalleGanador = new JLabel("", SwingConstants.CENTER);
        lblDetalleGanador.setFont(new Font("SansSerif", Font.PLAIN, 14));

        panelResultado.add(lblResultado,     BorderLayout.CENTER);
        panelResultado.add(lblDetalleGanador, BorderLayout.SOUTH);

        panel.add(btnCombatir,    BorderLayout.NORTH);
        panel.add(lblEstado,      BorderLayout.CENTER);
        panel.add(panelResultado, BorderLayout.SOUTH);
        return panel;
    }

    // ── Métodos para registrar listeners (desacoplamiento MVC) ───────────────

    /**
     * Registra el listener del botón "Cargar Properties".
     * El controlador llama este método; la vista solo guarda el listener.
     *
     * @param listener El {@link ActionListener} a registrar.
     */
    public void agregarListenerCargarProperties(ActionListener listener) {
        btnCargarProperties.addActionListener(listener);
    }

    /**
     * Registra el listener del botón "Combatir".
     *
     * @param listener El {@link ActionListener} a registrar.
     */
    public void agregarListenerCombatir(ActionListener listener) {
        btnCombatir.addActionListener(listener);
    }

    // ── Getters de datos ingresados ───────────────────────────────────────────

    /**
     * @return Nombre ingresado en el campo de texto, sin espacios.
     */
    public String getNombreLuchador() { return txtNombre.getText().trim(); }

    /**
     * @return Peso seleccionado en el spinner.
     */
    public double getPesoLuchador() { return (Double) spinnerPeso.getValue(); }

    /**
     * @return Lista de kimarites seleccionados por el usuario.
     */
    public List<Kimarite> getKimaritesSeleccionados() {
        return listaKimarites.getSelectedValuesList();
    }

    // ── Métodos de actualización de la vista ─────────────────────────────────

    /**
     * Carga kimarites en la lista visual, reemplazando los anteriores.
     *
     * @param kimarites Lista de kimarites a mostrar.
     */
    public void cargarKimaritesEnLista(List<Kimarite> kimarites) {
        modeloLista.clear();
        kimarites.forEach(modeloLista::addElement);
    }

    /**
     * Actualiza la etiqueta del archivo .properties seleccionado.
     *
     * @param nombreArchivo Nombre (no ruta completa) del archivo.
     */
    public void setArchivoProperties(String nombreArchivo) {
        lblArchivoProperties.setText(nombreArchivo);
        lblArchivoProperties.setForeground(new Color(0, 100, 0));
    }

    /**
     * Actualiza el mensaje de estado en la parte inferior.
     *
     * @param mensaje El mensaje a mostrar.
     */
    public void setEstado(String mensaje) { lblEstado.setText(mensaje); }

    /**
     * Muestra el resultado final del combate.
     *
     * @param gano    true si este cliente fue el ganador.
     * @param ganador El objeto {@link Rikishi} del ganador (para mostrar datos).
     */
    public void mostrarResultado(boolean gano, Rikishi ganador) {
        btnCombatir.setEnabled(false);
        panelResultado.setVisible(true);

        if (gano) {
            lblResultado.setText("🏆 ¡VICTORIA!");
            lblResultado.setForeground(new Color(184, 134, 11));
            panelResultado.setBackground(new Color(255, 250, 200));
        } else {
            lblResultado.setText("💀 DERROTA");
            lblResultado.setForeground(new Color(139, 0, 0));
            panelResultado.setBackground(new Color(255, 220, 220));
        }

        if (ganador != null) {
            lblDetalleGanador.setText("Ganador: " + ganador.getNombre()
                + "  |  Victorias totales: " + ganador.getVictorias());
        }
        revalidate();
        repaint();
    }

    /**
     * Habilita o deshabilita el botón de combatir.
     *
     * @param habilitado true para habilitar.
     */
    public void setBtnCombatirHabilitado(boolean habilitado) {
        btnCombatir.setEnabled(habilitado);
    }
}
