package com.analista.sp500;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class UiTheme {
    public static final Color BACKGROUND = new Color(12, 16, 23);
    public static final Color SURFACE = new Color(20, 26, 35);
    public static final Color SURFACE_ALT = new Color(27, 34, 46);
    public static final Color BORDER = new Color(52, 63, 81);
    public static final Color TEXT_PRIMARY = new Color(236, 241, 249);
    public static final Color TEXT_SECONDARY = new Color(163, 174, 196);
    public static final Color PRIMARY = new Color(67, 148, 255);
    public static final Color PRIMARY_SOFT = new Color(34, 52, 77);
    public static final Color POSITIVE = new Color(88, 214, 141);
    public static final Color NEGATIVE = new Color(255, 116, 116);
    public static final Color NEUTRAL = new Color(155, 168, 191);
    private static final Color PRIMARY_BUTTON_BG = new Color(59, 132, 237);
    private static final Color PRIMARY_BUTTON_HOVER = new Color(73, 147, 252);
    private static final Color PRIMARY_BUTTON_PRESSED = new Color(47, 111, 201);
    private static final Color PRIMARY_BUTTON_BORDER = new Color(90, 166, 255);
    private static final Color PRIMARY_BUTTON_DISABLED_BG = new Color(37, 45, 60);
    private static final Color PRIMARY_BUTTON_DISABLED_FG = new Color(123, 136, 158);

    public static final Font FONT_TITLE = resolveFont(20, Font.BOLD);
    public static final Font FONT_SUBTITLE = resolveFont(12, Font.PLAIN);
    public static final Font FONT_SECTION = resolveFont(13, Font.BOLD);
    public static final Font FONT_BODY = resolveFont(12, Font.PLAIN);
    public static final Font FONT_BODY_BOLD = resolveFont(12, Font.BOLD);
    public static final Font FONT_STATUS = resolveFont(11, Font.PLAIN);

    private static final Border CARD_BORDER = new CompoundBorder(
            new RoundedLineBorder(BORDER, 14, 1),
            new EmptyBorder(10, 10, 10, 10)
    );

    private UiTheme() {
    }

    public static void applyGlobalDefaults() {
        UIManager.put("Panel.background", BACKGROUND);
        UIManager.put("Label.foreground", TEXT_PRIMARY);
        UIManager.put("ToolTip.background", SURFACE);
        UIManager.put("ToolTip.foreground", TEXT_PRIMARY);
        UIManager.put("ToolTip.border", BorderFactory.createLineBorder(BORDER));
        UIManager.put("OptionPane.background", SURFACE);
        UIManager.put("OptionPane.foreground", TEXT_PRIMARY);
        UIManager.put("OptionPane.messageForeground", TEXT_PRIMARY);
        UIManager.put("OptionPane.messageFont", FONT_BODY);
        UIManager.put("OptionPane.buttonFont", FONT_BODY_BOLD);
    }

    public static JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(true);
        panel.setBackground(SURFACE);
        panel.setBorder(CARD_BORDER);
        return panel;
    }

    public static JLabel createTitleLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_TITLE);
        label.setForeground(TEXT_PRIMARY);
        return label;
    }

    public static JLabel createSubtitleLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_SUBTITLE);
        label.setForeground(TEXT_SECONDARY);
        return label;
    }

    public static JLabel createSectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_SECTION);
        label.setForeground(TEXT_PRIMARY);
        return label;
    }

    public static void stylePrimaryButton(JButton button) {
        button.setUI(new BasicButtonUI());
        button.setFont(FONT_BODY_BOLD);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setRolloverEnabled(true);
        button.setBorderPainted(true);
        button.setBorder(new CompoundBorder(
                new RoundedLineBorder(PRIMARY_BUTTON_BORDER, 10, 1),
                new EmptyBorder(6, 12, 6, 12)
        ));
        button.setBackground(PRIMARY_BUTTON_BG);
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.addChangeListener(e -> refreshPrimaryButtonStyle(button));
        refreshPrimaryButtonStyle(button);
    }

    public static void styleRangeToggle(JToggleButton button) {
        button.setFont(FONT_BODY_BOLD);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setBorder(new EmptyBorder(6, 12, 6, 12));

        button.addChangeListener(e -> refreshToggleStyle(button));
        refreshToggleStyle(button);
    }

    public static JPanel createMutedContainer() {
        JPanel panel = new JPanel();
        panel.setOpaque(true);
        panel.setBackground(SURFACE_ALT);
        panel.setBorder(new CompoundBorder(
                new RoundedLineBorder(BORDER, 12, 1),
                new EmptyBorder(3, 5, 3, 5)
        ));
        return panel;
    }

    public static void applyBodyLabelStyle(JLabel label) {
        label.setFont(FONT_BODY);
        label.setForeground(TEXT_PRIMARY);
    }

    public static void applySecondaryLabelStyle(JLabel label) {
        label.setFont(FONT_BODY);
        label.setForeground(TEXT_SECONDARY);
    }

    public static void applyStatusLabelStyle(JLabel label) {
        label.setFont(FONT_STATUS);
        label.setForeground(TEXT_SECONDARY);
        label.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
    }

    public static void markAsBadge(JComponent component, Color background, Color foreground) {
        component.setOpaque(true);
        component.setBackground(background);
        component.setForeground(foreground);
        component.setBorder(new CompoundBorder(
                new RoundedLineBorder(background.darker(), 10, 1),
                new EmptyBorder(3, 10, 3, 10)
        ));
    }

    private static void refreshPrimaryButtonStyle(JButton button) {
        if (!button.isEnabled()) {
            button.setBackground(PRIMARY_BUTTON_DISABLED_BG);
            button.setForeground(PRIMARY_BUTTON_DISABLED_FG);
            button.setBorder(new CompoundBorder(
                    new RoundedLineBorder(BORDER, 10, 1),
                    new EmptyBorder(6, 12, 6, 12)
            ));
            return;
        }

        if (button.getModel().isPressed()) {
            button.setBackground(PRIMARY_BUTTON_PRESSED);
        } else if (button.getModel().isRollover()) {
            button.setBackground(PRIMARY_BUTTON_HOVER);
        } else {
            button.setBackground(PRIMARY_BUTTON_BG);
        }
        button.setForeground(Color.WHITE);
        button.setBorder(new CompoundBorder(
                new RoundedLineBorder(PRIMARY_BUTTON_BORDER, 10, 1),
                new EmptyBorder(6, 12, 6, 12)
        ));
    }

    private static void refreshToggleStyle(JToggleButton button) {
        if (button.isSelected()) {
            button.setBackground(PRIMARY);
            button.setForeground(Color.WHITE);
            button.setBorder(new CompoundBorder(
                    new RoundedLineBorder(PRIMARY, 10, 1),
                    new EmptyBorder(6, 12, 6, 12)
            ));
        } else {
            button.setBackground(SURFACE_ALT);
            button.setForeground(TEXT_SECONDARY);
            button.setBorder(new CompoundBorder(
                    new RoundedLineBorder(BORDER, 10, 1),
                    new EmptyBorder(6, 12, 6, 12)
            ));
        }
    }

    private static Font resolveFont(int size, int style) {
        String[] candidates = {
                "Segoe UI Variable",
                "Segoe UI",
                "SF Pro Text",
                "Helvetica Neue",
                "Noto Sans",
                "Dialog"
        };

        Set<String> available = new HashSet<>(Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()
        ));

        for (String candidate : candidates) {
            if (available.contains(candidate)) {
                return new Font(candidate, style, size);
            }
        }
        return new Font("Dialog", style, size);
    }
}
