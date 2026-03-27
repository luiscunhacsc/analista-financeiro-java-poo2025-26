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
import java.awt.GridLayout;
import java.awt.Insets;
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
            Map.entry("RSI", "<b>O que mede:</b> O RSI mede momentum (forca dos movimentos) numa escala de 0 a 100, normalmente em 14 periodos.<br>"
                    + "<b>Como interpretar:</b> Acima de 70 costuma ser lido como sobrecompra; abaixo de 30 como sobrevenda.<br>"
                    + "<b>Boas praticas:</b> Use junto com tendencia e suporte/resistencia. Em tendencias fortes, RSI pode ficar muito tempo em zonas extremas."),
            Map.entry("MACD", "<b>O que mede:</b> O MACD compara duas medias moveis exponenciais para captar aceleracao/desaceleracao do preco.<br>"
                    + "<b>Como interpretar:</b> Cruzamento da linha MACD com a linha de sinal sugere mudanca de momentum. Acima de zero tende a favorecer alta, abaixo de zero baixa.<br>"
                    + "<b>Boas praticas:</b> Evite usar isolado em mercados laterais, onde pode gerar falsos sinais."),
            Map.entry("Tendencia SMA 50", "<b>O que mede:</b> Relacao entre preco atual e media movel simples de 50 periodos.<br>"
                    + "<b>Como interpretar:</b> Preco acima da SMA 50 indica vies de medio prazo mais positivo; abaixo, vies mais fraco.<br>"
                    + "<b>Boas praticas:</b> Funciona melhor como filtro de tendencia do que como gatilho unico de entrada/saida."),
            Map.entry("Tendencia SMA 200", "<b>O que mede:</b> Relacao entre preco atual e media movel simples de 200 periodos.<br>"
                    + "<b>Como interpretar:</b> Acima da SMA 200 costuma indicar tendencia estrutural mais forte; abaixo, ambiente mais defensivo.<br>"
                    + "<b>Boas praticas:</b> Indicador lento; confirma direcao de longo prazo, mas reage tarde em viragens abruptas."),
            Map.entry(KEY_LAST_CLOSE, "<b>O que mede:</b> Ultimo preco de fecho disponivel no periodo selecionado.<br>"
                    + "<b>Como interpretar:</b> Serve de referencia base para comparar com medias, bandas e extremos do periodo.<br>"
                    + "<b>Boas praticas:</b> Em intraday, pode divergir do preco corrente porque este valor e de fecho."),
            Map.entry(KEY_SMA20, "<b>O que mede:</b> Media movel simples dos ultimos 20 fechos.<br>"
                    + "<b>Como interpretar:</b> Mostra tendencia de curto prazo de forma suavizada.<br>"
                    + "<b>Boas praticas:</b> Boa para leitura rapida, mas sensivel a ruido em fases de baixa direcionalidade."),
            Map.entry(KEY_SMA50, "<b>O que mede:</b> Media movel simples dos ultimos 50 fechos.<br>"
                    + "<b>Como interpretar:</b> Referencia classica de medio prazo para direcao do mercado.<br>"
                    + "<b>Boas praticas:</b> Cruzamentos com o preco ajudam contexto, mas podem atrasar sinais."),
            Map.entry(KEY_SMA200, "<b>O que mede:</b> Media movel simples dos ultimos 200 fechos.<br>"
                    + "<b>Como interpretar:</b> Linha de longo prazo muito usada para separar ciclos de alta/baixa.<br>"
                    + "<b>Boas praticas:</b> Excelente para contexto macro, pouco util para timing muito curto."),
            Map.entry(KEY_EMA20, "<b>O que mede:</b> Media movel exponencial de 20 periodos, com maior peso nos dados recentes.<br>"
                    + "<b>Como interpretar:</b> Reage mais rapido que SMA 20 a mudancas de direcao.<br>"
                    + "<b>Boas praticas:</b> Melhor para ritmo de curto prazo, mas tambem gera mais sinais falsos."),
            Map.entry(KEY_RSI14, "<b>O que mede:</b> RSI calculado em 14 periodos.<br>"
                    + "<b>Como interpretar:</b> Zonas >70 e <30 sinalizam extremos de momentum, nao necessariamente reversao imediata.<br>"
                    + "<b>Boas praticas:</b> Combine com estrutura de preco e tendencia dominante."),
            Map.entry(KEY_MACD, "<b>O que mede:</b> Linha MACD = EMA curta - EMA longa (tipicamente 12 - 26).<br>"
                    + "<b>Como interpretar:</b> Valores crescentes indicam reforco de momentum; decrescentes, perda de forca.<br>"
                    + "<b>Boas praticas:</b> Observe em conjunto com linha de sinal e histograma."),
            Map.entry(KEY_MACD_SIGNAL, "<b>O que mede:</b> Media exponencial da linha MACD (tipicamente 9 periodos).<br>"
                    + "<b>Como interpretar:</b> Cruzamentos MACD/sinal sao usados como alertas de mudanca de ritmo.<br>"
                    + "<b>Boas praticas:</b> Em consolidacao, pode alternar frequentemente sem direcao clara."),
            Map.entry(KEY_MACD_HIST, "<b>O que mede:</b> Diferenca entre MACD e linha de sinal.<br>"
                    + "<b>Como interpretar:</b> Positivo sugere predominio de alta; negativo, predominio de baixa. A variacao mostra aceleracao/desaceleracao.<br>"
                    + "<b>Boas praticas:</b> Excelente para momentum, mas nao substitui analise de tendencia."),
            Map.entry(KEY_BOLL_UPPER, "<b>O que mede:</b> Banda superior de Bollinger (SMA 20 + 2 desvios padrao, na configuracao comum).<br>"
                    + "<b>Como interpretar:</b> Aproximacao ou toque pode indicar esticamento de curto prazo.<br>"
                    + "<b>Boas praticas:</b> Toque na banda superior nao e sinal automatico de venda em tendencia forte."),
            Map.entry(KEY_BOLL_MIDDLE, "<b>O que mede:</b> Banda media de Bollinger (normalmente SMA 20).<br>"
                    + "<b>Como interpretar:</b> Atua como linha de equilibrio do movimento recente.<br>"
                    + "<b>Boas praticas:</b> Perda/recuperacao desta linha pode ajudar leitura de continuidade ou enfraquecimento."),
            Map.entry(KEY_BOLL_LOWER, "<b>O que mede:</b> Banda inferior de Bollinger (SMA 20 - 2 desvios padrao, na configuracao comum).<br>"
                    + "<b>Como interpretar:</b> Aproximacao ou toque pode indicar pressao vendedora estendida.<br>"
                    + "<b>Boas praticas:</b> Em baixa forte, preco pode permanecer perto da banda por bastante tempo."),
            Map.entry(KEY_HIGH_52, "<b>O que mede:</b> Maximo observado nas ultimas 52 semanas (ou janela equivalente disponivel).<br>"
                    + "<b>Como interpretar:</b> Funciona como referencia de breakout e forca relativa historica.<br>"
                    + "<b>Boas praticas:</b> Ruptura ganha qualidade quando acompanhada por volume e contexto de tendencia."),
            Map.entry(KEY_LOW_52, "<b>O que mede:</b> Minimo observado nas ultimas 52 semanas (ou janela equivalente disponivel).<br>"
                    + "<b>Como interpretar:</b> Referencia importante de risco e possivel ruptura de suporte historico.<br>"
                    + "<b>Boas praticas:</b> Quebra de minimo anual tende a sinalizar aumento de fragilidade estrutural.")
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

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
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
        JButton button = new JButton("?");
        button.setToolTipText("Explicar indicador");
        button.setFont(UiTheme.FONT_BODY_BOLD);
        button.setForeground(UiTheme.TEXT_SECONDARY);
        button.setBackground(UiTheme.SURFACE_ALT);
        button.setBorder(new RoundedLineBorder(UiTheme.BORDER, 9, 1));
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setVerticalAlignment(SwingConstants.CENTER);
        button.setFocusPainted(false);
        button.setFocusable(false);
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.setPreferredSize(new Dimension(22, 22));
        button.setMinimumSize(new Dimension(22, 22));
        button.setMaximumSize(new Dimension(22, 22));
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
