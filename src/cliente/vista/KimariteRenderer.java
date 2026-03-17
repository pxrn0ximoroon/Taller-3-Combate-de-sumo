package cliente.vista;

import modelo.Kimarite;

import javax.swing.*;
import java.awt.*;

/**
 * Renderer personalizado para mostrar kimarites en la {@link JList}.
 *
 * <p>Cada celda muestra el nombre en negrita y la descripción
 * en texto secundario, con colores alternados por fila y
 * resaltado rojo para la selección.</p>
 *
 * <p><b>Principio SRP:</b> solo se encarga del renderizado visual
 * de cada celda de la lista.</p>
 *
 * @author Programación Avanzada - Universidad Distrital
 * @version 1.0
 */
public class KimariteRenderer extends DefaultListCellRenderer {

    /**
     * Personaliza la apariencia de cada elemento de la lista.
     *
     * @param list         La {@link JList} que contiene el elemento.
     * @param value        El elemento a renderizar (un {@link Kimarite}).
     * @param index        Índice del elemento en la lista.
     * @param isSelected   true si el elemento está seleccionado.
     * @param cellHasFocus true si la celda tiene el foco.
     * @return El componente configurado para renderizar la celda.
     */
    @Override
    public Component getListCellRendererComponent(
            JList<?> list, Object value, int index,
            boolean isSelected, boolean cellHasFocus) {

        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        if (value instanceof Kimarite k) {
            setText("<html><b>" + k.getNombre() + "</b>"
                + " <font color='#666'> — " + k.getDescripcion() + "</font></html>");
        }

        if (isSelected) {
            setBackground(new Color(139, 0, 0));
            setForeground(Color.WHITE);
        } else {
            setBackground(index % 2 == 0 ? Color.WHITE : new Color(255, 250, 240));
            setForeground(Color.BLACK);
        }

        setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        return this;
    }
}
