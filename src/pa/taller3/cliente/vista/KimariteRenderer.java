package pa.taller3.cliente.vista;

import javax.swing.*;
import java.awt.*;

/**
 * Renderer personalizado para mostrar kimarites en la {@link JList}.
 *
 * <p>Cada celda muestra el nombre en negrita y la descripción
 * en texto secundario, con colores alternados por fila y
 * resaltado morado para la selección.</p>
 *
 * <p>Trabaja con {@code String} en formato {@code "nombre|descripcion"},
 * sin dependencia del modelo del servidor.</p>
 *
 * <p><b>Principio SRP:</b> solo se encarga del renderizado visual
 * de cada celda de la lista.</p>
 *
 * @author Programación Avanzada - Universidad Distrital
 * @version 1.0
 */
public class KimariteRenderer extends DefaultListCellRenderer {

    /** Color principal morado oscuro para selección. */
    private static final Color MORADO_SELECCION = new Color(75, 0, 130);

    /** Color de fila par. */
    private static final Color FILA_PAR = new Color(255, 255, 255);

    /** Color de fila impar. */
    private static final Color FILA_IMPAR = new Color(245, 240, 255);

    /**
     * Personaliza la apariencia de cada elemento de la lista.
     *
     * @param list         La {@link JList} que contiene el elemento.
     * @param value        El elemento a renderizar (un {@code String} "nombre|descripcion").
     * @param index        Índice del elemento en la lista.
     * @param isSelected   {@code true} si el elemento está seleccionado.
     * @param cellHasFocus {@code true} si la celda tiene el foco.
     * @return El componente configurado para renderizar la celda.
     */
    @Override
    public Component getListCellRendererComponent(
            JList<?> list, Object value, int index,
            boolean isSelected, boolean cellHasFocus) {

        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        if (value instanceof String raw) {
            String[] partes = raw.split("\\|");
            String nombre = partes[0];
            String desc   = partes.length > 1 ? partes[1] : "";
            setText("<html><b>" + nombre + "</b>"
                + " <font color='#666'> — " + desc + "</font></html>");
        }

        if (isSelected) {
            setBackground(MORADO_SELECCION);
            setForeground(Color.WHITE);
        } else {
            setBackground(index % 2 == 0 ? FILA_PAR : FILA_IMPAR);
            setForeground(Color.BLACK);
        }

        setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        return this;
    }
}