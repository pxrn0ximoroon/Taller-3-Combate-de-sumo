package pa.taller3.servidor.vista;

import pa.taller3.servidor.modelo.Rikishi;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Panel reutilizable que muestra los datos de un luchador en la GUI del servidor.
 *
 * <p>Se instancia dos veces: uno para el luchador del lado izquierdo
 * y otro para el lado derecho del dohyō. Permite actualizar visualmente
 * el estado del luchador durante el combate.</p>
 *
 * <p><b>Principio DRY:</b> evita duplicar código de dos paneles idénticos
 * creando una clase reutilizable parametrizable por color.</p>
 *
 * @author Programación Avanzada - Universidad Distrital
 * @version 1.0
 */
public class PanelLuchador extends JPanel {

    /** Color de acento del panel para distinguir visualmente a cada luchador. */
    private final Color colorAcento;

    /** Luchador actualmente mostrado en este panel. null si está vacío. */
    private Rikishi luchadorActual;

    // ── Componentes visuales ──────────────────────────────────────────────────

    /** Etiqueta con el nombre del luchador. */
    private JLabel lblNombre;

    /** Etiqueta con el peso. */
    private JLabel lblPeso;

    /** Etiqueta con el número de victorias. */
    private JLabel lblVictorias;

    /** Etiqueta con el estado actual (en ring, ganador, perdedor). */
    private JLabel lblEstado;
    /** Etiqueta con la imagen predetermianda del rakishi */
    private JLabel lblImagen;

    /**
     * Construye el panel con título y color de acento.
     *
     * @param titulo      Título descriptivo ("Luchador 1" / "Luchador 2").
     * @param colorAcento Color del borde y encabezado del panel.
     */
    public PanelLuchador(String titulo, Color colorAcento) {
        this.colorAcento = colorAcento;
        inicializar(titulo);
    }

    /**
     * Inicializa todos los componentes del panel.
     *
     * @param titulo Título del panel.
     */
    private void inicializar(String titulo) {
        setLayout(new GridLayout(6, 1, 4, 4));
        setBackground(new Color(20, 10, 5));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(colorAcento, 2),
            new EmptyBorder(8, 10, 8, 10)
        ));
        setPreferredSize(new Dimension(195, 240));

        JLabel lblTitulo = new JLabel(titulo, SwingConstants.CENTER);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblTitulo.setForeground(colorAcento);
        
        lblNombre    = crearLabel("Esperando...");
        lblPeso      = crearLabel("Peso: -");
        lblVictorias = crearLabel("Victorias: -");
        lblEstado    = crearLabel("⏳ En espera");
        
        lblImagen = new JLabel();
        lblImagen.setHorizontalAlignment(SwingConstants.CENTER);

        lblImagen = new JLabel();
        lblImagen.setHorizontalAlignment(SwingConstants.CENTER);

        ImageIcon icon = new ImageIcon("data/img/rikishi.gif");
        Image img = icon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
        lblImagen.setIcon(new ImageIcon(img));

        add(lblImagen);
        add(lblTitulo);
        add(lblNombre);
        add(lblPeso);
        add(lblVictorias);
        add(lblEstado);
        add(new JLabel(""));
    }

    /**
     * Crea una etiqueta con el estilo estándar del panel.
     *
     * @param texto Texto inicial de la etiqueta.
     * @return {@link JLabel} configurado.
     */
    private JLabel crearLabel(String texto) {
        JLabel lbl = new JLabel(texto, SwingConstants.CENTER);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lbl.setForeground(Color.WHITE);
        return lbl;
    }

    // ── Métodos de actualización ──────────────────────────────────────────────

    /**
     * Carga y muestra los datos de un luchador en el panel.
     *
     * @param luchador El luchador a mostrar.
     */
    public void setLuchador(Rikishi luchador) {
        this.luchadorActual = luchador;
        lblNombre.setText("🥋 " + luchador.getNombre());
        lblPeso.setText("Peso: " + luchador.getPeso() + " kg");
        lblVictorias.setText("Victorias: " + luchador.getVictorias());
        lblEstado.setText("✅ En el Dohyō");
        lblEstado.setForeground(new Color(144, 238, 144));
        repaint();
    }

    /**
     * Resalta visualmente el panel cuando este luchador acaba de atacar.
     *
     * @param fueExitoso true si la técnica sacó al rival.
     */
    public void resaltarAtaque(boolean fueExitoso) {
        setBackground(fueExitoso ? new Color(50, 100, 50) : new Color(20, 10, 5));
        repaint();
    }

    /** Marca el panel con estilo de ganador. */
    public void marcarGanador() {
        setBackground(new Color(184, 134, 11));
        lblEstado.setText("🏆 ¡GANADOR!");
        lblEstado.setForeground(Color.WHITE);
        setBorder(BorderFactory.createLineBorder(new Color(255, 215, 0), 4));
        if (luchadorActual != null) {
            lblVictorias.setText("Victorias: " + luchadorActual.getVictorias());
        }
        repaint();
    }

    /** Marca el panel con estilo de perdedor. */
    public void marcarPerdedor() {
        setBackground(new Color(60, 20, 20));
        lblEstado.setText("💀 Fuera del Dohyō");
        lblEstado.setForeground(new Color(255, 100, 100));
        repaint();
    }

    /**
     * Indica si este panel aún no tiene un luchador asignado.
     *
     * @return true si está vacío.
     */
    public boolean estaVacio() { return luchadorActual == null; }

    /**
     * Indica si este panel pertenece al luchador dado.
     *
     * @param luchador Luchador a comparar (por referencia).
     * @return true si es el mismo objeto.
     */
    public boolean correspondeA(Rikishi luchador) {
        return luchadorActual != null && luchadorActual == luchador;
    }
    
    public void mostrarAtaque() {
        lblImagen.setIcon(new ImageIcon("data/img/ataque.gif"));
    }

    public void mostrarNormal() {
        lblImagen.setIcon(new ImageIcon("data/img/rikishi.gif"));
    }
    
    
}
