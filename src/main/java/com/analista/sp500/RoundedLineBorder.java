package com.analista.sp500;

import javax.swing.border.AbstractBorder;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;

public class RoundedLineBorder extends AbstractBorder {
    private final Color color;
    private final int radius;
    private final int thickness;

    public RoundedLineBorder(Color color, int radius, int thickness) {
        this.color = color;
        this.radius = radius;
        this.thickness = thickness;
    }

    @Override
    public Insets getBorderInsets(Component c) {
        int inset = Math.max(thickness, 1) + 3;
        return new Insets(inset, inset, inset, inset);
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        Insets computed = getBorderInsets(c);
        insets.top = computed.top;
        insets.left = computed.left;
        insets.bottom = computed.bottom;
        insets.right = computed.right;
        return insets;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);
        g2.setStroke(new BasicStroke(thickness));

        int offset = thickness / 2;
        int arc = radius * 2;
        g2.drawRoundRect(
                x + offset,
                y + offset,
                width - thickness,
                height - thickness,
                arc,
                arc
        );
        g2.dispose();
    }
}
