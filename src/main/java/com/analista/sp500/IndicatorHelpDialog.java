package com.analista.sp500;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.KeyEvent;

public class IndicatorHelpDialog extends JDialog {
    public IndicatorHelpDialog(Window owner, String indicatorName, String bodyHtml) {
        super(owner, "Ajuda do indicador", ModalityType.DOCUMENT_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(520, 380));
        setSize(580, 440);
        setLocationRelativeTo(owner);

        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBackground(UiTheme.SURFACE);
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setContentPane(root);

        JPanel header = new JPanel(new BorderLayout(0, 2));
        header.setOpaque(false);

        JLabel title = new JLabel(indicatorName);
        title.setFont(UiTheme.FONT_SECTION.deriveFont(17f));
        title.setForeground(UiTheme.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Guia de interpretacao do indicador tecnico");
        subtitle.setFont(UiTheme.FONT_BODY);
        subtitle.setForeground(UiTheme.TEXT_SECONDARY);

        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);

        JEditorPane content = new JEditorPane();
        content.setEditable(false);
        content.setFocusable(false);
        content.setOpaque(false);
        content.setContentType("text/html");
        content.setText(buildHtmlDocument(bodyHtml));
        content.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(true);
        scrollPane.getViewport().setBackground(UiTheme.SURFACE_ALT);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                new RoundedLineBorder(UiTheme.BORDER, 10, 1),
                BorderFactory.createEmptyBorder(6, 6, 6, 6)
        ));
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        footer.setOpaque(false);
        JButton closeButton = new JButton("Fechar");
        UiTheme.stylePrimaryButton(closeButton);
        closeButton.addActionListener(e -> dispose());
        footer.add(closeButton);

        root.add(header, BorderLayout.NORTH);
        root.add(scrollPane, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);

        root.registerKeyboardAction(
                e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JPanel.WHEN_IN_FOCUSED_WINDOW
        );
    }

    private String buildHtmlDocument(String bodyHtml) {
        return "<html><head><style>"
                + "body{font-family:'Segoe UI','Noto Sans',sans-serif;font-size:12px;color:#e4ebf6;line-height:1.48;}"
                + "b{color:#f5f8ff;}"
                + "ul{margin-top:4px;margin-bottom:6px;}"
                + "</style></head><body>"
                + bodyHtml
                + "<br><br><span style='color:#b6c3d9;'><b>Nota:</b> Isto e apoio analitico e nao recomendacao financeira.</span>"
                + "</body></html>";
    }
}
