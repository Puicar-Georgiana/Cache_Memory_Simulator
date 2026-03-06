package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class CacheCellRenderer extends DefaultTableCellRenderer {

    private int lastOperationLine = -1;
    private String lastOperationType = "";

    public void updateHighlight(int line, String type) {
        this.lastOperationLine = line;
        this.lastOperationType = type;
    }

    @Override
    public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column) {

        Component c = super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);

        if (row == lastOperationLine) {
            if ("hit".equals(lastOperationType)) {
                c.setBackground(new Color(144, 238, 144));
            } else if ("miss".equals(lastOperationType)) {
                c.setBackground(new Color(255, 182, 193));
            }
        } else {
            c.setBackground(Color.WHITE);
        }

        setHorizontalAlignment(SwingConstants.CENTER);
        return c;
    }
}
