package pa.taller3.cliente.vista;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

/**
 * Ventana principal del cliente de sumo.
 *
 * <p>Permite al usuario:</p>
 * <ol>
 *   <li>Cargar el archivo {@code .properties} con kimarites (via {@link JFileChooser}).</li>
 *   <li>Ingresar nombre y peso del luchador.</li>
 *   <li>Seleccionar las técnicas que domina (selección múltiple).</li>
 *   <li>Enviar el luchador al servidor para el combate.</li>
 *   <li>Ver el resultado del combate (victoria o derrota) en panel y JOptionPane.</li>
 * </ol>
 *
 * <p><b>Esta vista NO contiene lógica de negocio ni imports del servidor.</b>
 * Solo recoge datos y los expone al controlador mediante getters.</p>
 *
 * <p><b>Separación de eventos:</b> los listeners se registran desde el
 * controlador con {@link #agregarListenerCargarProperties(ActionListener)}
 * y {@link #agregarListenerCombatir(ActionListener)}.</p>
 *
 * @author Programación Avanzada - Universidad Distrital
 * @version 1.0
 */
public class VentanaCliente extends JFrame {

    // ── Paleta de colores ─────────────────────────────────────────────────────

    /** Morado oscuro principal. */
    private static final Color MORADO_OSCURO = new Color(75, 0, 130);

    /** Morado medio para botones secundarios. */
    private static final Color MORADO_MEDIO  = new Color(102, 51, 153);

    /** Morado claro para fondos de lista. */
    private static final Color MORADO_CLARO  = new Color(230, 220, 255);

    /** Fondo general de la ventana. */
    private static final Color FONDO         = new Color(245, 240, 255);

    /** Fondo de paneles internos. */
    private static final Color FONDO_PANEL   = new Color(255, 255, 255);

    /** Dorado para acentos en el encabezado. */
    private static final Color DORADO        = new Color(255, 215, 0);

    // ── Comandos de acción ────────────────────────────────────────────────────

    /** Comando del botón cargar properties. */
    public static final String CMD_CARGAR   = "cargar";

    /** Comando del botón combatir. */
    public static final String CMD_COMBATIR = "combatir";

    // ── Componentes del formulario ────────────────────────────────────────────

    /** Campo para el nombre del luchador. */
    private JTextField txtNombre;

    /** Spinner numérico para el peso. */
    private JSpinner spinnerPeso;

    /**
     * Lista visual de kimarites disponibles.
     * Trabaja con {@code String} en formato {@code "nombre|descripcion"}.
     */
    private JList<String> listaKimarites;

    /** Modelo de datos de la lista. */
    private DefaultListModel<String> modeloLista;

    /** Muestra el nombre del archivo .properties seleccionado. */
    private JLabel lblArchivoProperties;

    // ── Botones ───────────────────────────────────────────────────────────────

    /** Abre el JFileChooser para seleccionar el .properties. */
    private JButton btnCargarProperties;

    /** Envía el luchador al servidor e inicia el combate. */
    private JButton btnCombatir;

    // ── Panel de resultado ────────────────────────────────────────────────────

    /** Panel visible al terminar el combate con el resultado. */
    private JPanel panelResultado;

    /** Título grande del resultado. */
    private JLabel lblResultado;

    /** Detalles del resultado recibidos como String desde el servidor. */
    private JLabel lblDetalleGanador;

    /** Mensaje de estado en la parte inferior. */
    private JLabel lblEstado;

    // ─────────────────────────────────────────────────────────────────────────

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
        setSize(580, 700);
        setLocationRelativeTo(null);
        setResizable(false);
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
        contenedor.add(construirPanelFormulario(), BorderLayout.CENTER);
        contenedor.add(construirPanelAcciones(),   BorderLayout.SOUTH);

        setContentPane(contenedor);
    }

    /**
     * Construye el encabezado decorativo con título y subtítulo.
     *
     * @return Panel encabezado.
     */
    private JPanel construirPanelEncabezado() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(MORADO_OSCURO);
        panel.setBorder(new EmptyBorder(12, 15, 12, 15));

        JLabel titulo = new JLabel("相撲  REGISTRAR RIKISHI", SwingConstants.CENTER);
        titulo.setFont(new Font("Serif", Font.BOLD, 24));
        titulo.setForeground(Color.WHITE);

        JLabel subtitulo = new JLabel("Prepara tu luchador para el Dohyō", SwingConstants.CENTER);
        subtitulo.setFont(new Font("SansSerif", Font.ITALIC, 12));
        subtitulo.setForeground(DORADO);

        panel.add(titulo,    BorderLayout.CENTER);
        panel.add(subtitulo, BorderLayout.SOUTH);
        return panel;
    }

    /**
     * Construye el formulario con datos básicos y selección de kimarites.
     *
     * @return Panel formulario.
     */
    private JPanel construirPanelFormulario() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(FONDO);
        panel.add(construirPanelDatos(),     BorderLayout.NORTH);
        panel.add(construirPanelKimarites(), BorderLayout.CENTER);
        return panel;
    }

    /**
     * Construye el sub-panel con nombre y peso del luchador.
     *
     * @return Panel de datos básicos.
     */
    private JPanel construirPanelDatos() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(FONDO_PANEL);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(MORADO_OSCURO),
            "Datos del Luchador",
            0, 0,
            new Font("SansSerif", Font.BOLD, 12),
            MORADO_OSCURO
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        // Nombre
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.35;
        JLabel lblNombre = new JLabel("Nombre del Rikishi:");
        lblNombre.setForeground(MORADO_OSCURO);
        lblNombre.setFont(new Font("SansSerif", Font.BOLD, 12));
        panel.add(lblNombre, gbc);

        gbc.gridx = 1; gbc.weightx = 0.65;
        txtNombre = new JTextField(20);
        txtNombre.setFont(new Font("SansSerif", Font.PLAIN, 14));
        txtNombre.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(MORADO_MEDIO),
            BorderFactory.createEmptyBorder(3, 6, 3, 6)
        ));
        panel.add(txtNombre, gbc);

        // Peso
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.35;
        JLabel lblPeso = new JLabel("Peso (kg):");
        lblPeso.setForeground(MORADO_OSCURO);
        lblPeso.setFont(new Font("SansSerif", Font.BOLD, 12));
        panel.add(lblPeso, gbc);

        gbc.gridx = 1; gbc.weightx = 0.65;
        spinnerPeso = new JSpinner(new SpinnerNumberModel(80.0, 50.0, 300.0, 0.5));
        spinnerPeso.setFont(new Font("SansSerif", Font.PLAIN, 14));
        panel.add(spinnerPeso, gbc);

        return panel;
    }

    /**
     * Construye el sub-panel de selección de técnicas kimarite.
     *
     * @return Panel de kimarites.
     */
    private JPanel construirPanelKimarites() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(FONDO_PANEL);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(MORADO_OSCURO),
            "Técnicas Kimarite — selecciona en las que es experto tu rikishi",
            0, 0,
            new Font("SansSerif", Font.BOLD, 12),
            MORADO_OSCURO
        ));

        // Fila superior: botón + nombre del archivo
        JPanel panelArchivo = new JPanel(new BorderLayout(8, 0));
        panelArchivo.setBackground(FONDO_PANEL);
        panelArchivo.setBorder(new EmptyBorder(4, 0, 4, 0));

        btnCargarProperties = new JButton("📂 Cargar técnicas (.properties)");
        btnCargarProperties.setActionCommand(CMD_CARGAR);
        btnCargarProperties.setBackground(MORADO_MEDIO);
        btnCargarProperties.setForeground(Color.WHITE);
        btnCargarProperties.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnCargarProperties.setFocusPainted(false);

        lblArchivoProperties = new JLabel("Ningún archivo seleccionado");
        lblArchivoProperties.setFont(new Font("SansSerif", Font.ITALIC, 11));
        lblArchivoProperties.setForeground(Color.GRAY);

        panelArchivo.add(btnCargarProperties,  BorderLayout.WEST);
        panelArchivo.add(lblArchivoProperties, BorderLayout.CENTER);

        // Lista de kimarites
        modeloLista    = new DefaultListModel<>();
        listaKimarites = new JList<>(modeloLista);
        listaKimarites.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        listaKimarites.setBackground(FONDO);
        listaKimarites.setFont(new Font("SansSerif", Font.PLAIN, 13));
        listaKimarites.setCellRenderer(new KimariteRenderer());

        JScrollPane scroll = new JScrollPane(listaKimarites);
        scroll.setPreferredSize(new Dimension(400, 200));
        scroll.setBorder(BorderFactory.createLineBorder(MORADO_CLARO));

        JLabel tip = new JLabel("💡 Ctrl+Click para seleccionar múltiples técnicas");
        tip.setFont(new Font("SansSerif", Font.ITALIC, 11));
        tip.setForeground(MORADO_MEDIO);

        panel.add(panelArchivo, BorderLayout.NORTH);
        panel.add(scroll,       BorderLayout.CENTER);
        panel.add(tip,          BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Construye el panel inferior con botón, estado y resultado.
     *
     * @return Panel de acciones.
     */
    private JPanel construirPanelAcciones() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(FONDO);

        btnCombatir = new JButton("⚔️  ¡ENTRAR AL DOHYŌ!");
        btnCombatir.setActionCommand(CMD_COMBATIR);
        btnCombatir.setFont(new Font("SansSerif", Font.BOLD, 16));
        btnCombatir.setBackground(MORADO_OSCURO);
        btnCombatir.setForeground(Color.WHITE);
        btnCombatir.setPreferredSize(new Dimension(300, 50));
        btnCombatir.setFocusPainted(false);

        lblEstado = new JLabel("Registra tu luchador y entra al combate.", SwingConstants.CENTER);
        lblEstado.setFont(new Font("SansSerif", Font.ITALIC, 12));
        lblEstado.setForeground(MORADO_MEDIO);

        // Panel resultado — oculto hasta terminar el combate
        panelResultado = new JPanel(new BorderLayout());
        panelResultado.setBackground(FONDO);
        panelResultado.setVisible(false);

        lblResultado = new JLabel("", SwingConstants.CENTER);
        lblResultado.setFont(new Font("Serif", Font.BOLD, 34));

        lblDetalleGanador = new JLabel("", SwingConstants.CENTER);
        lblDetalleGanador.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblDetalleGanador.setForeground(MORADO_OSCURO);

        panelResultado.add(lblResultado,      BorderLayout.CENTER);
        panelResultado.add(lblDetalleGanador, BorderLayout.SOUTH);

        panel.add(btnCombatir,    BorderLayout.NORTH);
        panel.add(lblEstado,      BorderLayout.CENTER);
        panel.add(panelResultado, BorderLayout.SOUTH);

        return panel;
    }

    // ── Registro de listeners (desacoplamiento MVC) ───────────────────────────

    /**
     * Registra el listener del botón "Cargar Properties".
     *
     * @param listener El {@link ActionListener} proporcionado por el controlador.
     */
    public void agregarListenerCargarProperties(ActionListener listener) {
        btnCargarProperties.addActionListener(listener);
    }

    /**
     * Registra el listener del botón "Combatir".
     *
     * @param listener El {@link ActionListener} proporcionado por el controlador.
     */
    public void agregarListenerCombatir(ActionListener listener) {
        btnCombatir.addActionListener(listener);
    }

    // ── Métodos invocados por el controlador ──────────────────────────────────

    /**
     * Abre un {@link JFileChooser} para seleccionar el archivo {@code .properties}.
     * Invocado por el controlador, no por la vista misma.
     *
     * @return El {@link File} seleccionado, o {@code null} si se canceló.
     */
    public File seleccionarArchivoProperties() {
        JFileChooser fc = new JFileChooser("./data");
        fc.setDialogTitle("Seleccionar kimarites (.properties)");
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "Archivos de propiedades (*.properties)", "properties"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            return fc.getSelectedFile();
        }
        return null;
    }

    /**
     * Muestra un mensaje de error mediante {@link JOptionPane}.
     * Invocado por el controlador.
     *
     * @param mensaje Mensaje de error a mostrar.
     */
    public void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje,
            "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Muestra un mensaje informativo mediante {@link JOptionPane}.
     * Invocado por el controlador.
     *
     * @param mensaje Mensaje a mostrar.
     */
    public void mostrarInfo(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje,
            "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Muestra el resultado final mediante {@link JOptionPane} y cierra
     * la aplicación al aceptar. Invocado por el controlador.
     *
     * @param gano    {@code true} si el luchador ganó.
     * @param detalle Texto con el detalle del resultado.
     */
    public void mostrarResultadoFinal(boolean gano, String detalle) {
        String mensaje = gano
            ? "🏆 ¡VICTORIA!\n\n" + detalle
            : "💀 DERROTA\n\n" + detalle;
        String titulo = gano ? "¡Ganaste!" : "Has perdido";
        int tipo = gano
            ? JOptionPane.INFORMATION_MESSAGE
            : JOptionPane.WARNING_MESSAGE;

        JOptionPane.showMessageDialog(this, mensaje, titulo, tipo);
        System.exit(0);
    }

    // ── Getters de datos ingresados ───────────────────────────────────────────

    /**
     * @return Nombre ingresado, sin espacios extremos.
     */
    public String getNombreLuchador() { return txtNombre.getText().trim(); }

    /**
     * @return Peso seleccionado en el spinner.
     */
    public double getPesoLuchador() { return (Double) spinnerPeso.getValue(); }

    /**
     * @return Lista de Strings seleccionados en formato {@code "nombre|descripcion"}.
     */
    public List<String> getKimaritesSeleccionados() {
        return listaKimarites.getSelectedValuesList();
    }

    // ── Métodos de actualización ──────────────────────────────────────────────

    /**
     * Carga la lista de kimarites en la vista.
     * Los Strings deben tener formato {@code "nombre|descripcion"}.
     *
     * @param kimarites Lista de Strings a mostrar.
     */
    public void cargarKimaritesEnLista(List<String> kimarites) {
        modeloLista.clear();
        kimarites.forEach(modeloLista::addElement);
    }

    /**
     * Actualiza la etiqueta con el nombre del archivo seleccionado.
     *
     * @param nombreArchivo Nombre del archivo (no la ruta completa).
     */
    public void setArchivoProperties(String nombreArchivo) {
        lblArchivoProperties.setText("✔ " + nombreArchivo);
        lblArchivoProperties.setForeground(new Color(0, 128, 0));
    }

    /**
     * Actualiza el mensaje de estado.
     *
     * @param mensaje Mensaje a mostrar.
     */
    public void setEstado(String mensaje) { lblEstado.setText(mensaje); }

    /**
     * Muestra el panel de victoria en la ventana principal.
     * El controlador decide cuándo invocar este método.
     *
     * @param detalle Texto con el detalle del resultado enviado por el servidor.
     */
    public void mostrarVictoria(String detalle) {
        btnCombatir.setEnabled(false);
        panelResultado.setVisible(true);
        lblResultado.setText("🏆 ¡VICTORIA!");
        lblResultado.setForeground(new Color(184, 134, 11));
        panelResultado.setBackground(new Color(240, 230, 255));
        lblDetalleGanador.setText(detalle);
        revalidate();
        repaint();
    }

    /**
     * Muestra el panel de derrota en la ventana principal.
     * El controlador decide cuándo invocar este método.
     *
     * @param detalle Texto con el detalle del resultado enviado por el servidor.
     */
    public void mostrarDerrota(String detalle) {
        btnCombatir.setEnabled(false);
        panelResultado.setVisible(true);
        lblResultado.setText("💀 DERROTA");
        lblResultado.setForeground(MORADO_OSCURO);
        panelResultado.setBackground(new Color(220, 210, 240));
        lblDetalleGanador.setText(detalle);
        revalidate();
        repaint();
    }

    /**
     * Habilita o deshabilita el botón de combatir.
     *
     * @param habilitado {@code true} para habilitar.
     */
    public void setBtnCombatirHabilitado(boolean habilitado) {
        btnCombatir.setEnabled(habilitado);
    }
}