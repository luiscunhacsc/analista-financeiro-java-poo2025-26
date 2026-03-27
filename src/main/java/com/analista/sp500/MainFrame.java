package com.analista.sp500;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class MainFrame extends JFrame {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter UPDATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final PerformanceChartPanel chartPanel = new PerformanceChartPanel();
    private final TechnicalIndicatorsPanel indicatorsPanel = new TechnicalIndicatorsPanel();
    private final JLabel statusLabel = new JLabel("A carregar dados...", SwingConstants.LEFT);
    private final JLabel sourceLabel = UiTheme.createSubtitleLabel("Fonte: Stooq (^SPX, fecho diario)");
    private final JLabel updateLabel = UiTheme.createSubtitleLabel("Ultima atualizacao: n/d");
    private final Sp500DataService dataService = new Sp500DataService();

    private JToggleButton oneDayButton;
    private JToggleButton fiveDaysButton;
    private JToggleButton oneMonthButton;
    private JToggleButton oneYearButton;
    private JToggleButton threeYearsButton;
    private JToggleButton fiveYearsButton;
    private JToggleButton allButton;

    private LocalDateTime lastUpdate;

    public MainFrame() {
        super("S&P 500 Analytics");
        UiTheme.applyGlobalDefaults();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1180, 700));
        setSize(1320, 810);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBackground(UiTheme.BACKGROUND);
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setContentPane(root);

        root.add(buildHeaderCard(), BorderLayout.NORTH);
        root.add(buildCenterSplitLayout(), BorderLayout.CENTER);
        root.add(buildStatusCard(), BorderLayout.SOUTH);

        UiTheme.applyStatusLabelStyle(statusLabel);
        UiTheme.applyStatusLabelStyle(sourceLabel);
        UiTheme.applyStatusLabelStyle(updateLabel);
        loadData();
    }

    private JPanel buildHeaderCard() {
        JPanel card = UiTheme.createCardPanel();
        card.setLayout(new BorderLayout(0, 6));

        JPanel firstLine = new JPanel(new BorderLayout(10, 0));
        firstLine.setOpaque(false);
        firstLine.add(UiTheme.createTitleLabel("S&P 500 Market Dashboard"), BorderLayout.WEST);
        firstLine.add(buildHeaderControls(), BorderLayout.EAST);

        JPanel secondLine = new JPanel(new BorderLayout(10, 0));
        secondLine.setOpaque(false);
        secondLine.add(sourceLabel, BorderLayout.WEST);
        secondLine.add(updateLabel, BorderLayout.EAST);

        card.add(firstLine, BorderLayout.NORTH);
        card.add(secondLine, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildHeaderControls() {
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controls.setOpaque(false);

        JPanel rangePanel = UiTheme.createMutedContainer();
        rangePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 6, 2));

        oneDayButton = new JToggleButton(TimeRange.ONE_DAY.getLabel());
        fiveDaysButton = new JToggleButton(TimeRange.FIVE_DAYS.getLabel());
        oneMonthButton = new JToggleButton(TimeRange.ONE_MONTH.getLabel());
        oneYearButton = new JToggleButton(TimeRange.ONE_YEAR.getLabel());
        threeYearsButton = new JToggleButton(TimeRange.THREE_YEARS.getLabel());
        fiveYearsButton = new JToggleButton(TimeRange.FIVE_YEARS.getLabel());
        allButton = new JToggleButton(TimeRange.ALL.getLabel());
        UiTheme.styleRangeToggle(oneDayButton);
        UiTheme.styleRangeToggle(fiveDaysButton);
        UiTheme.styleRangeToggle(oneMonthButton);
        UiTheme.styleRangeToggle(oneYearButton);
        UiTheme.styleRangeToggle(threeYearsButton);
        UiTheme.styleRangeToggle(fiveYearsButton);
        UiTheme.styleRangeToggle(allButton);

        ButtonGroup group = new ButtonGroup();
        group.add(oneDayButton);
        group.add(fiveDaysButton);
        group.add(oneMonthButton);
        group.add(oneYearButton);
        group.add(threeYearsButton);
        group.add(fiveYearsButton);
        group.add(allButton);
        oneYearButton.setSelected(true);

        oneDayButton.addActionListener(e -> setRange(TimeRange.ONE_DAY));
        fiveDaysButton.addActionListener(e -> setRange(TimeRange.FIVE_DAYS));
        oneMonthButton.addActionListener(e -> setRange(TimeRange.ONE_MONTH));
        oneYearButton.addActionListener(e -> setRange(TimeRange.ONE_YEAR));
        threeYearsButton.addActionListener(e -> setRange(TimeRange.THREE_YEARS));
        fiveYearsButton.addActionListener(e -> setRange(TimeRange.FIVE_YEARS));
        allButton.addActionListener(e -> setRange(TimeRange.ALL));

        rangePanel.add(oneDayButton);
        rangePanel.add(fiveDaysButton);
        rangePanel.add(oneMonthButton);
        rangePanel.add(oneYearButton);
        rangePanel.add(threeYearsButton);
        rangePanel.add(fiveYearsButton);
        rangePanel.add(allButton);

        JButton refreshButton = new JButton("Atualizar dados");
        UiTheme.styleSecondaryButton(refreshButton);
        refreshButton.setPreferredSize(new Dimension(148, 36));
        refreshButton.addActionListener(e -> loadData());

        controls.add(rangePanel);
        controls.add(refreshButton);
        return controls;
    }

    private JSplitPane buildCenterSplitLayout() {
        JPanel chartCard = UiTheme.createCardPanel();
        chartCard.setLayout(new BorderLayout());
        chartCard.setMinimumSize(new Dimension(760, 420));
        chartCard.add(chartPanel, BorderLayout.CENTER);

        JPanel indicatorsCard = UiTheme.createCardPanel();
        indicatorsCard.setLayout(new BorderLayout());
        indicatorsCard.setMinimumSize(new Dimension(320, 420));
        indicatorsCard.setPreferredSize(new Dimension(360, 420));
        indicatorsCard.add(indicatorsPanel, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, chartCard, indicatorsCard);
        splitPane.setResizeWeight(0.72);
        splitPane.setContinuousLayout(true);
        splitPane.setDividerSize(9);
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        splitPane.setOpaque(false);

        SwingUtilities.invokeLater(() -> splitPane.setDividerLocation(0.72));
        return splitPane;
    }

    private JPanel buildStatusCard() {
        JPanel card = UiTheme.createCardPanel();
        card.setLayout(new BorderLayout());
        card.add(statusLabel, BorderLayout.CENTER);
        return card;
    }

    private void setRange(TimeRange range) {
        chartPanel.setSelectedRange(range);
        refreshSummaryPanels();
    }

    private void loadData() {
        statusLabel.setText("A carregar dados do S&P 500...");
        updateLabel.setText("Ultima atualizacao: a carregar...");

        SwingWorker<List<Sp500DataPoint>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Sp500DataPoint> doInBackground() throws Exception {
                return dataService.fetchDailyCloseData();
            }

            @Override
            protected void done() {
                try {
                    List<Sp500DataPoint> data = get();
                    chartPanel.setData(data);
                    lastUpdate = LocalDateTime.now();
                    refreshSummaryPanels();
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    showLoadError("Carregamento interrompido.");
                } catch (ExecutionException executionException) {
                    String message = executionException.getCause() != null
                            ? executionException.getCause().getMessage()
                            : executionException.getMessage();
                    showLoadError(message);
                }
            }
        };

        worker.execute();
    }

    private void refreshSummaryPanels() {
        updateStatusLabel();
        indicatorsPanel.updateIndicators(chartPanel.getFilteredData());
        refreshLastUpdateLabel();
    }

    private void updateStatusLabel() {
        List<Sp500DataPoint> filtered = chartPanel.getFilteredData();
        if (filtered.size() < 2) {
            statusLabel.setText("Sem dados para o periodo selecionado.");
            return;
        }

        Sp500DataPoint first = filtered.get(0);
        Sp500DataPoint last = filtered.get(filtered.size() - 1);
        double returnPct = (last.close() / first.close() - 1.0) * 100.0;

        String status = String.format(
                Locale.US,
                "Periodo: %s | Inicio: %s (%.2f) | Fim: %s (%.2f) | Retorno: %+.2f%% | Pontos: %d",
                chartPanel.getSelectedRange().getLabel(),
                first.date().format(DATE_FORMATTER),
                first.close(),
                last.date().format(DATE_FORMATTER),
                last.close(),
                returnPct,
                filtered.size()
        );
        statusLabel.setText(status);
    }

    private void refreshLastUpdateLabel() {
        if (lastUpdate == null) {
            updateLabel.setText("Ultima atualizacao: n/d");
            return;
        }
        updateLabel.setText("Ultima atualizacao: " + lastUpdate.format(UPDATE_TIME_FORMATTER));
    }

    private void showLoadError(String message) {
        statusLabel.setText("Erro ao carregar dados.");
        refreshLastUpdateLabel();
        JOptionPane.showMessageDialog(
                this,
                "Nao foi possivel obter os dados do S&P 500.\n\nDetalhe: " + message,
                "Erro",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
