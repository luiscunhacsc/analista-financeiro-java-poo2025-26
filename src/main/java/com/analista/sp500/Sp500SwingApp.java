package com.analista.sp500;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Sp500SwingApp {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Se falhar, mantem look and feel padrao.
        }

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
