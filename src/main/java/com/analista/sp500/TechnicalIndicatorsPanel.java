package com.analista.sp500;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Window;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TechnicalIndicatorsPanel extends JPanel {
    private static final String KEY_LAST_CLOSE = "Ultimo fecho";
    private static final String KEY_SMA20 = "SMA 20";
    private static final String KEY_SMA50 = "SMA 50";
    private static final String KEY_SMA200 = "SMA 200";
    private static final String KEY_EMA20 = "EMA 20";
    private static final String KEY_RSI14 = "RSI 14";
    private static final String KEY_MACD = "MACD (12,26)";
    private static final String KEY_MACD_SIGNAL = "Sinal MACD (9)";
    private static final String KEY_MACD_HIST = "Histograma MACD";
    private static final String KEY_BOLL_UPPER = "Bollinger superior";
    private static final String KEY_BOLL_MIDDLE = "Bollinger media";
    private static final String KEY_BOLL_LOWER = "Bollinger inferior";
    private static final String KEY_HIGH_52 = "Max 52 semanas";
    private static final String KEY_LOW_52 = "Min 52 semanas";

    private static final Color SIGNAL_WARNING_BG = new Color(82, 66, 26);
    private static final Color SIGNAL_WARNING_FG = new Color(255, 205, 105);

    private static final Map<String, String> INDICATOR_EXPLANATIONS = Map.ofEntries(
            Map.entry("RSI", "<b>O que e:</b> RSI (Relative Strength Index) de 14 periodos.<br>"
                    + "<b>Calculo:</b><br>"
                    + "- Delta = Fecho(i) - Fecho(i-1)<br>"
                    + "- Ganho = max(Delta, 0), Perda = max(-Delta, 0)<br>"
                    + "- MediaGanho e MediaPerda suavizadas por Wilder:<br>"
                    + "&nbsp;&nbsp;MediaNova = ((MediaAnterior * (n-1)) + ValorAtual) / n<br>"
                    + "- RS = MediaGanho / MediaPerda<br>"
                    + "- RSI = 100 - (100 / (1 + RS))<br>"
                    + "<b>Escala:</b> 0 a 100. Tipicamente >70 sobrecompra, <30 sobrevenda.<br>"
                    + "<b>Nota:</b> Em tendencia forte pode permanecer bastante tempo em zonas extremas."),
            Map.entry("MACD", "<b>O que e:</b> Indicador de momentum e tendencia baseado em medias exponenciais.<br>"
                    + "<b>Calculo:</b><br>"
                    + "- EMA curta (12) e EMA longa (26)<br>"
                    + "- Linha MACD = EMA12 - EMA26<br>"
                    + "- Linha de sinal = EMA9 da linha MACD<br>"
                    + "- Histograma = MACD - Sinal<br>"
                    + "<b>Leitura:</b> Cruzamentos MACD/Sinal sugerem mudanca de ritmo; valor acima de zero indica predominio de alta."),
            Map.entry("Tendencia SMA 50", "<b>Definicao:</b> Compara o ultimo fecho com a SMA 50.<br>"
                    + "<b>Calculo da SMA:</b> SMA50 = soma dos ultimos 50 fechos / 50.<br>"
                    + "<b>Regra no painel:</b> Preco > SMA50 = Alta; Preco < SMA50 = Baixa; igual = Neutro.<br>"
                    + "<b>Uso:</b> filtro de tendencia de medio prazo."),
            Map.entry("Tendencia SMA 200", "<b>Definicao:</b> Compara o ultimo fecho com a SMA 200.<br>"
                    + "<b>Calculo da SMA:</b> SMA200 = soma dos ultimos 200 fechos / 200.<br>"
                    + "<b>Regra no painel:</b> Preco > SMA200 = Alta; Preco < SMA200 = Baixa; igual = Neutro.<br>"
                    + "<b>Uso:</b> referencia de tendencia estrutural/longo prazo."),
            Map.entry(KEY_LAST_CLOSE, "<b>Definicao:</b> Ultimo preco de fecho disponivel para o periodo atual.<br>"
                    + "<b>Origem:</b> campo Close do ultimo registo diario carregado.<br>"
                    + "<b>Uso:</b> base para comparar com medias, bandas e extremos."),
            Map.entry(KEY_SMA20, "<b>Calculo:</b> SMA20 = (C1 + C2 + ... + C20) / 20, onde C e o fecho diario.<br>"
                    + "<b>Uso:</b> tendencia de curto prazo e linha media de Bollinger."),
            Map.entry(KEY_SMA50, "<b>Calculo:</b> SMA50 = soma dos ultimos 50 fechos / 50.<br>"
                    + "<b>Uso:</b> tendencia de medio prazo e suporte/resistencia dinamico."),
            Map.entry(KEY_SMA200, "<b>Calculo:</b> SMA200 = soma dos ultimos 200 fechos / 200.<br>"
                    + "<b>Uso:</b> tendencia de longo prazo, muito usada como filtro macro."),
            Map.entry(KEY_EMA20, "<b>Calculo:</b><br>"
                    + "- Seed inicial = SMA20<br>"
                    + "- Multiplicador k = 2 / (20 + 1)<br>"
                    + "- EMA(i) = ((Preco(i) - EMA(i-1)) * k) + EMA(i-1)<br>"
                    + "<b>Uso:</b> semelhante a SMA20, mas com maior peso nos dados recentes."),
            Map.entry(KEY_RSI14, "<b>Indicador:</b> mesmo RSI descrito acima, com n=14.<br>"
                    + "<b>Formula final:</b> RSI = 100 - (100 / (1 + RS)).<br>"
                    + "<b>Interpretação comum:</b> >70 sobrecompra; <30 sobrevenda."),
            Map.entry(KEY_MACD, "<b>Linha MACD:</b> EMA12 - EMA26.<br>"
                    + "<b>Interpretação:</b> quanto mais positivo, maior aceleração de alta relativa; quanto mais negativo, maior pressão de baixa."),
            Map.entry(KEY_MACD_SIGNAL, "<b>Linha de sinal:</b> EMA9 aplicada sobre a série MACD.<br>"
                    + "<b>Interpretação:</b> usada para cruzamentos com a linha MACD."),
            Map.entry(KEY_MACD_HIST, "<b>Histograma:</b> MACD - Sinal.<br>"
                    + "<b>Interpretação:</b> positivo = momentum comprador dominante; negativo = vendedor dominante."),
            Map.entry(KEY_BOLL_UPPER, "<b>Banda superior:</b> SMA20 + (2 * desvio-padrao dos ultimos 20 fechos).<br>"
                    + "<b>Interpretação:</b> zona estatisticamente alta do preço recente."),
            Map.entry(KEY_BOLL_MIDDLE, "<b>Banda media:</b> SMA20.<br>"
                    + "<b>Interpretação:</b> linha de equilibrio da volatilidade recente."),
            Map.entry(KEY_BOLL_LOWER, "<b>Banda inferior:</b> SMA20 - (2 * desvio-padrao dos ultimos 20 fechos).<br>"
                    + "<b>Interpretação:</b> zona estatisticamente baixa do preço recente."),
            Map.entry(KEY_HIGH_52, "<b>Calculo:</b> maior fecho na janela de 252 sessoes (aprox. 52 semanas uteis).<br>"
                    + "<b>Interpretação:</b> referencia de breakout e força histórica."),
            Map.entry(KEY_LOW_52, "<b>Calculo:</b> menor fecho na janela de 252 sessoes.<br>"
                    + "<b>Interpretação:</b> referencia de fundo anual e risco de continuidade de queda.")
    );

    private final TechnicalIndicatorsCalculator calculator = new TechnicalIndicatorsCalculator();
    private final Map<String, JLabel> valueLabels = new LinkedHashMap<>();

    private final JLabel rsiSignalBadge = createSignalBadge();
    private final JLabel macdSignalBadge = createSignalBadge();
    private final JLabel trend50Badge = createSignalBadge();
    private final JLabel trend200Badge = createSignalBadge();

    public TechnicalIndicatorsPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);

        JPanel root = new JPanel(new BorderLayout(0, 8));
        root.setOpaque(false);
        root.add(buildTopFixedArea(), BorderLayout.NORTH);
        root.add(buildMetricsScrollPane(), BorderLayout.CENTER);

        add(root, BorderLayout.CENTER);
        setNoDataState();
    }

    public void updateIndicators(List<Sp500DataPoint> data) {
        if (data == null || data.isEmpty()) {
            setNoDataState();
            return;
        }

        TechnicalIndicators indicators = calculator.calculate(data);
        if (indicators == null) {
            setNoDataState();
            return;
        }

        String rsiSignal = describeRsi(indicators.rsi14());
        String macdSignal = describeMacd(indicators.macdHistogram());
        String trend50 = describeTrend(indicators.lastClose(), indicators.sma50());
        String trend200 = describeTrend(indicators.lastClose(), indicators.sma200());

        setValue(KEY_LAST_CLOSE, formatNumber(indicators.lastClose()));
        setValue(KEY_SMA20, formatNumber(indicators.sma20()));
        setValue(KEY_SMA50, formatNumber(indicators.sma50()));
        setValue(KEY_SMA200, formatNumber(indicators.sma200()));
        setValue(KEY_EMA20, formatNumber(indicators.ema20()));
        setValue(KEY_RSI14, formatNumber(indicators.rsi14()));
        setValue(KEY_MACD, formatNumber(indicators.macd()));
        setValue(KEY_MACD_SIGNAL, formatNumber(indicators.macdSignal()));
        setValue(KEY_MACD_HIST, formatSigned(indicators.macdHistogram()));
        setValue(KEY_BOLL_UPPER, formatNumber(indicators.bollingerUpper()));
        setValue(KEY_BOLL_MIDDLE, formatNumber(indicators.bollingerMiddle()));
        setValue(KEY_BOLL_LOWER, formatNumber(indicators.bollingerLower()));
        setValue(KEY_HIGH_52, formatNumber(indicators.high52Weeks()));
        setValue(KEY_LOW_52, formatNumber(indicators.low52Weeks()));

        applySignalStyle(rsiSignalBadge, rsiSignal);
        applySignalStyle(macdSignalBadge, macdSignal);
        applySignalStyle(trend50Badge, trend50);
        applySignalStyle(trend200Badge, trend200);
    }

    private JPanel buildTopFixedArea() {
        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        JPanel header = new JPanel(new BorderLayout(0, 2));
        header.setOpaque(false);
        header.add(UiTheme.createSectionLabel("Indicadores Tecnicos"), BorderLayout.NORTH);
        header.add(UiTheme.createSubtitleLabel("Leitura rapida de tendencia, momentum e volatilidade"), BorderLayout.SOUTH);

        top.add(header);
        top.add(Box.createVerticalStrut(6));
        top.add(buildSignalGrid());
        return top;
    }

    private JScrollPane buildMetricsScrollPane() {
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        body.add(buildSection("Preco e Medias", KEY_LAST_CLOSE, KEY_SMA20, KEY_SMA50, KEY_SMA200, KEY_EMA20));
        body.add(Box.createVerticalStrut(6));
        body.add(buildSection("Momentum", KEY_RSI14, KEY_MACD, KEY_MACD_SIGNAL, KEY_MACD_HIST));
        body.add(Box.createVerticalStrut(6));
        body.add(buildSection("Volatilidade", KEY_BOLL_UPPER, KEY_BOLL_MIDDLE, KEY_BOLL_LOWER));
        body.add(Box.createVerticalStrut(6));
        body.add(buildSection("Amplitude", KEY_HIGH_52, KEY_LOW_52));
        body.add(Box.createVerticalStrut(4));

        JScrollPane scrollPane = new JScrollPane(body);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);
        return scrollPane;
    }

    private JPanel buildSignalGrid() {
        JPanel signals = new JPanel(new GridLayout(2, 2, 6, 6));
        signals.setOpaque(false);
        signals.add(buildSignalCard("RSI", rsiSignalBadge));
        signals.add(buildSignalCard("MACD", macdSignalBadge));
        signals.add(buildSignalCard("Tendencia SMA 50", trend50Badge));
        signals.add(buildSignalCard("Tendencia SMA 200", trend200Badge));
        return signals;
    }

    private JPanel buildSignalCard(String title, JLabel badge) {
        JPanel card = new JPanel(new BorderLayout(0, 5));
        card.setOpaque(true);
        card.setBackground(UiTheme.SURFACE_ALT);
        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundedLineBorder(UiTheme.BORDER, 9, 1),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        titleRow.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UiTheme.FONT_BODY);
        titleLabel.setForeground(UiTheme.TEXT_SECONDARY);
        titleRow.add(titleLabel);
        titleRow.add(createInfoButton(title));

        JPanel badgeContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        badgeContainer.setOpaque(false);
        badgeContainer.add(badge);

        card.add(titleRow, BorderLayout.NORTH);
        card.add(badgeContainer, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildSection(String title, String... keys) {
        JPanel section = new JPanel(new BorderLayout(0, 6));
        section.setOpaque(true);
        section.setBackground(UiTheme.SURFACE_ALT);
        section.setBorder(BorderFactory.createCompoundBorder(
                new RoundedLineBorder(UiTheme.BORDER, 9, 1),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UiTheme.FONT_BODY_BOLD);
        titleLabel.setForeground(UiTheme.TEXT_PRIMARY);
        section.add(titleLabel, BorderLayout.NORTH);

        JPanel rows = new JPanel();
        rows.setOpaque(false);
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));

        for (int i = 0; i < keys.length; i++) {
            rows.add(buildMetricRow(keys[i]));
            if (i < keys.length - 1) {
                rows.add(Box.createVerticalStrut(4));
            }
        }

        section.add(rows, BorderLayout.CENTER);
        return section;
    }

    private JPanel buildMetricRow(String key) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
        left.setOpaque(false);

        JLabel keyLabel = new JLabel(key);
        keyLabel.setFont(UiTheme.FONT_BODY);
        keyLabel.setForeground(UiTheme.TEXT_SECONDARY);
        left.add(keyLabel);
        left.add(createInfoButton(key));

        JLabel valueLabel = new JLabel("n/d", SwingConstants.RIGHT);
        valueLabel.setFont(UiTheme.FONT_BODY_BOLD);
        valueLabel.setForeground(UiTheme.TEXT_PRIMARY);
        valueLabels.put(key, valueLabel);

        row.add(left, BorderLayout.WEST);
        row.add(valueLabel, BorderLayout.EAST);
        return row;
    }

    private JButton createInfoButton(String indicatorKey) {
        JButton button = new JButton("i") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color bg = getModel().isRollover() ? new Color(52, 64, 84) : new Color(37, 48, 66);
                Color border = getModel().isRollover() ? new Color(88, 108, 136) : new Color(70, 86, 112);

                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 9, 9);
                g2.setColor(border);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 9, 9);
                g2.dispose();

                super.paintComponent(g);
            }
        };
        button.setToolTipText("Explicar indicador");
        button.setFont(UiTheme.FONT_STATUS.deriveFont(10f));
        button.setForeground(new Color(172, 186, 208));
        button.setBackground(new Color(37, 48, 66));
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setBorderPainted(false);
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setVerticalAlignment(SwingConstants.CENTER);
        button.setFocusPainted(false);
        button.setFocusable(false);
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.setRolloverEnabled(true);
        button.setPreferredSize(new Dimension(16, 16));
        button.setMinimumSize(new Dimension(16, 16));
        button.setMaximumSize(new Dimension(16, 16));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addActionListener(e -> showIndicatorExplanation(indicatorKey));
        return button;
    }

    private void showIndicatorExplanation(String indicatorKey) {
        String message = INDICATOR_EXPLANATIONS.getOrDefault(
                indicatorKey,
                "<b>O que mede:</b> Indicador tecnico usado para apoiar a leitura de tendencia, momentum e volatilidade.<br>"
                        + "<b>Como interpretar:</b> Deve ser lido em conjunto com os restantes indicadores, nunca isoladamente."
        );

        Window owner = SwingUtilities.getWindowAncestor(this);
        IndicatorHelpDialog dialog = new IndicatorHelpDialog(owner, indicatorKey, message);
        dialog.setVisible(true);
    }

    private JLabel createSignalBadge() {
        JLabel label = new JLabel("N/D");
        label.setFont(UiTheme.FONT_BODY_BOLD);
        UiTheme.markAsBadge(label, UiTheme.PRIMARY_SOFT, UiTheme.TEXT_SECONDARY);
        return label;
    }

    private void setNoDataState() {
        for (Map.Entry<String, JLabel> entry : valueLabels.entrySet()) {
            entry.getValue().setText("n/d");
        }
        applySignalStyle(rsiSignalBadge, "n/d");
        applySignalStyle(macdSignalBadge, "n/d");
        applySignalStyle(trend50Badge, "n/d");
        applySignalStyle(trend200Badge, "n/d");
    }

    private void setValue(String key, String value) {
        JLabel label = valueLabels.get(key);
        if (label != null) {
            label.setText(value);
        }
    }

    private void applySignalStyle(JLabel badge, String signal) {
        String normalized = signal == null ? "n/d" : signal;
        switch (normalized) {
            case "Alta", "Sobrevendido" -> UiTheme.markAsBadge(badge, new Color(30, 76, 56), UiTheme.POSITIVE);
            case "Baixa" -> UiTheme.markAsBadge(badge, new Color(82, 40, 44), UiTheme.NEGATIVE);
            case "Sobrecomprado" -> UiTheme.markAsBadge(badge, SIGNAL_WARNING_BG, SIGNAL_WARNING_FG);
            case "Neutro" -> UiTheme.markAsBadge(badge, new Color(48, 56, 73), UiTheme.NEUTRAL);
            default -> UiTheme.markAsBadge(badge, UiTheme.PRIMARY_SOFT, UiTheme.TEXT_SECONDARY);
        }
        badge.setText(normalized.toUpperCase(Locale.ROOT));
    }

    private String formatNumber(Double value) {
        if (value == null) {
            return "n/d";
        }
        return String.format(Locale.US, "%.2f", value);
    }

    private String formatSigned(Double value) {
        if (value == null) {
            return "n/d";
        }
        return String.format(Locale.US, "%+.2f", value);
    }

    private String describeTrend(double lastClose, Double movingAverage) {
        if (movingAverage == null) {
            return "n/d";
        }
        if (lastClose > movingAverage) {
            return "Alta";
        }
        if (lastClose < movingAverage) {
            return "Baixa";
        }
        return "Neutro";
    }

    private String describeRsi(Double rsi) {
        if (rsi == null) {
            return "n/d";
        }
        if (rsi > 70.0) {
            return "Sobrecomprado";
        }
        if (rsi < 30.0) {
            return "Sobrevendido";
        }
        return "Neutro";
    }

    private String describeMacd(Double histogram) {
        if (histogram == null) {
            return "n/d";
        }
        if (histogram > 0.0) {
            return "Alta";
        }
        if (histogram < 0.0) {
            return "Baixa";
        }
        return "Neutro";
    }
}
