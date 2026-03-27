package com.analista.sp500;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Path2D;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PerformanceChartPanel extends JPanel {
    private static final int LEFT_MARGIN = 66;
    private static final int RIGHT_MARGIN = 18;
    private static final int TOP_MARGIN = 14;
    private static final int BOTTOM_MARGIN = 42;

    private static final int SIDE_LEGEND_WIDTH = 184;
    private static final int SIDE_LEGEND_GAP = 12;
    private static final int MIN_PLOT_WIDTH_WITH_SIDE_LEGEND = 560;

    private static final int BOTTOM_LEGEND_HEIGHT = 54;
    private static final int BOTTOM_LEGEND_GAP = 8;

    private static final int PANEL_GAP = 12;
    private static final int PANEL_HEADER_HEIGHT = 16;

    private static final Color PRICE_COLOR = new Color(88, 178, 255);
    private static final Color PRICE_FILL_TOP = new Color(88, 178, 255, 70);
    private static final Color PRICE_FILL_BOTTOM = new Color(88, 178, 255, 10);
    private static final Color SMA50_COLOR = new Color(242, 179, 93);
    private static final Color SMA200_COLOR = new Color(240, 127, 127);
    private static final Color BOLLINGER_UPPER_COLOR = new Color(173, 182, 196);
    private static final Color BOLLINGER_MIDDLE_COLOR = new Color(149, 160, 177);
    private static final Color BOLLINGER_LOWER_COLOR = new Color(136, 145, 160);
    private static final Color GRID_COLOR = new Color(45, 56, 73);
    private static final Color AXIS_COLOR = new Color(82, 97, 120);
    private static final Color PLOT_BG_TOP = new Color(22, 30, 43);
    private static final Color PLOT_BG_BOTTOM = new Color(17, 24, 36);
    private static final Color MACD_COLOR = new Color(100, 196, 255);
    private static final Color MACD_SIGNAL_COLOR = new Color(255, 181, 101);
    private static final Color MACD_HIST_POSITIVE = new Color(57, 199, 127, 120);
    private static final Color MACD_HIST_NEGATIVE = new Color(255, 122, 122, 120);
    private static final Color RSI_COLOR = new Color(114, 184, 255);
    private static final Color RSI_OVERBOUGHT_BG = new Color(108, 47, 47, 95);
    private static final Color RSI_OVERSOLD_BG = new Color(40, 91, 58, 95);

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final List<Sp500DataPoint> allData = new ArrayList<>();

    private TimeRange selectedRange = TimeRange.ONE_YEAR;

    public PerformanceChartPanel() {
        setOpaque(false);
        setBackground(UiTheme.SURFACE);
    }

    public void setData(List<Sp500DataPoint> newData) {
        allData.clear();
        allData.addAll(newData);
        repaint();
    }

    public void setSelectedRange(TimeRange range) {
        this.selectedRange = range;
        repaint();
    }

    public TimeRange getSelectedRange() {
        return selectedRange;
    }

    public List<Sp500DataPoint> getFilteredData() {
        if (allData.isEmpty()) {
            return List.of();
        }

        if (selectedRange == TimeRange.ALL) {
            return List.copyOf(allData);
        }

        LocalDate latestDate = allData.get(allData.size() - 1).date();
        LocalDate startDate = selectedRange.getStartDate(latestDate);
        List<Sp500DataPoint> filtered = allData.stream()
                .filter(point -> !point.date().isBefore(startDate))
                .toList();

        if (filtered.isEmpty()) {
            return List.copyOf(allData);
        }
        if (filtered.size() < 2 && allData.size() >= 2) {
            return List.copyOf(allData.subList(allData.size() - 2, allData.size()));
        }
        return filtered;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g2 = (Graphics2D) graphics.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        List<Sp500DataPoint> data = getFilteredData();
        if (data.size() < 2) {
            String emptyMessage = allData.isEmpty()
                    ? "A aguardar dados..."
                    : "Sem dados suficientes para desenhar o grafico.";
            drawCenteredMessage(g2, emptyMessage);
            g2.dispose();
            return;
        }

        int chartX = LEFT_MARGIN;
        int chartY = TOP_MARGIN;
        int availableWidth = Math.max(100, getWidth() - LEFT_MARGIN - RIGHT_MARGIN);
        boolean useSideLegend = availableWidth >= (SIDE_LEGEND_WIDTH + SIDE_LEGEND_GAP + MIN_PLOT_WIDTH_WITH_SIDE_LEGEND);
        int chartWidth = useSideLegend
                ? Math.max(100, availableWidth - SIDE_LEGEND_WIDTH - SIDE_LEGEND_GAP)
                : availableWidth;

        int extraLegendBottom = useSideLegend ? 0 : BOTTOM_LEGEND_HEIGHT + BOTTOM_LEGEND_GAP;
        int totalHeight = Math.max(210, getHeight() - TOP_MARGIN - BOTTOM_MARGIN - extraLegendBottom);
        PanelHeights panelHeights = computePanelHeights(totalHeight);

        int mainY = chartY;
        int macdY = mainY + panelHeights.mainHeight + PANEL_GAP;
        int rsiY = macdY + panelHeights.macdHeight + PANEL_GAP;

        double base = data.get(0).close();
        double[] perf = new double[data.size()];
        for (int i = 0; i < data.size(); i++) {
            perf[i] = (data.get(i).close() / base - 1.0) * 100.0;
        }

        Double[] sma50 = movingAveragePerformance(data, 50, base);
        Double[] sma200 = movingAveragePerformance(data, 200, base);
        BollingerSeries bollinger = bollingerPerformance(data, 20, 2.0, base);
        MacdSeries macd = macdSeries(data, 12, 26, 9);
        Double[] rsi = rsiSeries(data, 14);

        drawMainPanel(g2, chartX, mainY, chartWidth, panelHeights.mainHeight, perf, sma50, sma200, bollinger);
        drawMacdPanel(g2, chartX, macdY, chartWidth, panelHeights.macdHeight, macd);
        drawRsiPanel(g2, chartX, rsiY, chartWidth, panelHeights.rsiHeight, rsi);
        drawXAxisLabels(g2, data, chartX, rsiY, chartWidth, panelHeights.rsiHeight);

        if (useSideLegend) {
            int dividerX = chartX + chartWidth + (SIDE_LEGEND_GAP / 2);
            drawLegendDivider(g2, dividerX, mainY, panelHeights.mainHeight);
            drawLegendSide(g2, chartX + chartWidth + SIDE_LEGEND_GAP, mainY + 8, SIDE_LEGEND_WIDTH);
        } else {
            int bottomLegendY = rsiY + panelHeights.rsiHeight + BOTTOM_LEGEND_GAP;
            drawLegendBottom(g2, chartX, bottomLegendY, chartWidth, BOTTOM_LEGEND_HEIGHT);
        }

        g2.dispose();
    }

    private PanelHeights computePanelHeights(int totalHeight) {
        int usable = Math.max(120, totalHeight - PANEL_GAP * 2);
        int main = (int) Math.round(usable * 0.58);
        int macd = (int) Math.round(usable * 0.21);
        int rsi = usable - main - macd;

        if (main < 120 || macd < 60 || rsi < 60) {
            main = Math.max(110, usable / 2);
            int remain = usable - main;
            macd = Math.max(55, remain / 2);
            rsi = Math.max(55, remain - macd);
        }

        return new PanelHeights(main, macd, rsi);
    }

    private void drawMainPanel(
            Graphics2D g2,
            int x,
            int y,
            int width,
            int height,
            double[] perf,
            Double[] sma50,
            Double[] sma200,
            BollingerSeries bollinger
    ) {
        int plotY = y + PANEL_HEADER_HEIGHT;
        int plotHeight = Math.max(60, height - PANEL_HEADER_HEIGHT);

        double min = perf[0];
        double max = perf[0];
        for (double value : perf) {
            min = Math.min(min, value);
            max = Math.max(max, value);
        }
        min = includeNullableSeriesMin(min, sma50);
        max = includeNullableSeriesMax(max, sma50);
        min = includeNullableSeriesMin(min, sma200);
        max = includeNullableSeriesMax(max, sma200);
        min = includeNullableSeriesMin(min, bollinger.upper());
        max = includeNullableSeriesMax(max, bollinger.upper());
        min = includeNullableSeriesMin(min, bollinger.middle());
        max = includeNullableSeriesMax(max, bollinger.middle());
        min = includeNullableSeriesMin(min, bollinger.lower());
        max = includeNullableSeriesMax(max, bollinger.lower());

        min = Math.min(min, 0.0);
        max = Math.max(max, 0.0);
        double padding = Math.max((max - min) * 0.08, 1.2);
        max += padding;
        min -= padding;

        drawGridAndAxes(g2, x, plotY, width, plotHeight, min, max, 6, "%.1f%%");
        drawPanelLabel(g2, "Preco e Medias", x, y);
        drawAreaUnderPrice(g2, x, plotY, width, plotHeight, perf, min, max);
        drawNullableLine(g2, x, plotY, width, plotHeight, bollinger.upper(), min, max, BOLLINGER_UPPER_COLOR, 1.2f, true);
        drawNullableLine(g2, x, plotY, width, plotHeight, bollinger.middle(), min, max, BOLLINGER_MIDDLE_COLOR, 1.1f, false);
        drawNullableLine(g2, x, plotY, width, plotHeight, bollinger.lower(), min, max, BOLLINGER_LOWER_COLOR, 1.2f, true);
        drawNullableLine(g2, x, plotY, width, plotHeight, sma200, min, max, SMA200_COLOR, 1.8f, false);
        drawNullableLine(g2, x, plotY, width, plotHeight, sma50, min, max, SMA50_COLOR, 1.8f, false);
        drawLine(g2, x, plotY, width, plotHeight, perf, min, max, PRICE_COLOR, 2.4f);
        drawLatestPoint(g2, x, plotY, width, plotHeight, perf, min, max);
    }

    private void drawMacdPanel(Graphics2D g2, int x, int y, int width, int height, MacdSeries macd) {
        int plotY = y + PANEL_HEADER_HEIGHT;
        int plotHeight = Math.max(40, height - PANEL_HEADER_HEIGHT);

        double min = 0.0;
        double max = 0.0;
        min = includeNullableSeriesMin(min, macd.macd());
        max = includeNullableSeriesMax(max, macd.macd());
        min = includeNullableSeriesMin(min, macd.signal());
        max = includeNullableSeriesMax(max, macd.signal());
        min = includeNullableSeriesMin(min, macd.histogram());
        max = includeNullableSeriesMax(max, macd.histogram());

        double padding = Math.max((max - min) * 0.15, 0.3);
        max += padding;
        min -= padding;

        drawGridAndAxes(g2, x, plotY, width, plotHeight, min, max, 4, "%.2f");
        drawPanelLabel(g2, "MACD (12,26,9)", x, y);
        drawMacdHistogram(g2, x, plotY, width, plotHeight, macd.histogram(), min, max);
        drawNullableLine(g2, x, plotY, width, plotHeight, macd.macd(), min, max, MACD_COLOR, 1.9f, false);
        drawNullableLine(g2, x, plotY, width, plotHeight, macd.signal(), min, max, MACD_SIGNAL_COLOR, 1.7f, false);
    }

    private void drawRsiPanel(Graphics2D g2, int x, int y, int width, int height, Double[] rsi) {
        int plotY = y + PANEL_HEADER_HEIGHT;
        int plotHeight = Math.max(40, height - PANEL_HEADER_HEIGHT);

        double min = 0.0;
        double max = 100.0;

        drawGridAndAxes(g2, x, plotY, width, plotHeight, min, max, 4, "%.0f");
        drawPanelLabel(g2, "RSI (14)", x, y);

        int y70 = toY(plotY, plotHeight, min, max, 70.0);
        int y30 = toY(plotY, plotHeight, min, max, 30.0);
        int y100 = toY(plotY, plotHeight, min, max, 100.0);
        int y0 = toY(plotY, plotHeight, min, max, 0.0);

        g2.setColor(RSI_OVERBOUGHT_BG);
        g2.fillRect(x + 1, y100 + 1, width - 1, Math.max(1, y70 - y100));
        g2.setColor(RSI_OVERSOLD_BG);
        g2.fillRect(x + 1, y30, width - 1, Math.max(1, y0 - y30));

        Stroke originalStroke = g2.getStroke();
        g2.setStroke(new BasicStroke(1.0f));
        g2.setColor(new Color(220, 125, 125));
        g2.drawLine(x, y70, x + width, y70);
        g2.setColor(new Color(113, 191, 132));
        g2.drawLine(x, y30, x + width, y30);
        g2.setStroke(originalStroke);

        drawNullableLine(g2, x, plotY, width, plotHeight, rsi, min, max, RSI_COLOR, 1.9f, false);
    }

    private void drawGridAndAxes(
            Graphics2D g2,
            int x,
            int y,
            int width,
            int height,
            double min,
            double max,
            int steps,
            String axisFormat
    ) {
        g2.setPaint(new GradientPaint(0, y, PLOT_BG_TOP, 0, y + height, PLOT_BG_BOTTOM));
        g2.fillRoundRect(x, y, width, height, 12, 12);

        g2.setFont(UiTheme.FONT_BODY);
        for (int i = 0; i <= steps; i++) {
            int yLine = y + (int) ((double) i / steps * height);
            g2.setColor(GRID_COLOR);
            g2.drawLine(x, yLine, x + width, yLine);

            double value = max - (max - min) * i / steps;
            g2.setColor(UiTheme.TEXT_SECONDARY);
            g2.drawString(String.format(Locale.US, axisFormat, value), 10, yLine + 4);
        }

        g2.setColor(AXIS_COLOR);
        g2.drawRoundRect(x, y, width, height, 12, 12);

        if (min <= 0 && max >= 0) {
            Stroke originalStroke = g2.getStroke();
            int zeroY = toY(y, height, min, max, 0.0);
            g2.setColor(new Color(103, 121, 149));
            g2.setStroke(new BasicStroke(1.1f));
            g2.drawLine(x, zeroY, x + width, zeroY);
            g2.setStroke(originalStroke);
        }
    }

    private void drawPanelLabel(Graphics2D g2, String text, int x, int y) {
        g2.setFont(UiTheme.FONT_STATUS);
        g2.setColor(UiTheme.TEXT_SECONDARY);
        g2.drawString(text, x + 8, y + 12);
    }

    private void drawAreaUnderPrice(
            Graphics2D g2,
            int x,
            int y,
            int width,
            int height,
            double[] values,
            double min,
            double max
    ) {
        Path2D path = new Path2D.Double();
        int baseY = y + height;

        int firstX = x;
        int firstY = toY(y, height, min, max, values[0]);
        path.moveTo(firstX, firstY);

        for (int i = 1; i < values.length; i++) {
            int px = toX(x, width, values.length, i);
            int py = toY(y, height, min, max, values[i]);
            path.lineTo(px, py);
        }

        path.lineTo(x + width, baseY);
        path.lineTo(firstX, baseY);
        path.closePath();

        g2.setPaint(new GradientPaint(0, y, PRICE_FILL_TOP, 0, y + height, PRICE_FILL_BOTTOM));
        g2.fill(path);
    }

    private void drawLine(
            Graphics2D g2,
            int x,
            int y,
            int width,
            int height,
            double[] values,
            double min,
            double max,
            Color color,
            float strokeWidth
    ) {
        Stroke originalStroke = g2.getStroke();
        g2.setColor(color);
        g2.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        for (int i = 1; i < values.length; i++) {
            int x1 = toX(x, width, values.length, i - 1);
            int y1 = toY(y, height, min, max, values[i - 1]);
            int x2 = toX(x, width, values.length, i);
            int y2 = toY(y, height, min, max, values[i]);
            g2.drawLine(x1, y1, x2, y2);
        }

        g2.setStroke(originalStroke);
    }

    private void drawNullableLine(
            Graphics2D g2,
            int x,
            int y,
            int width,
            int height,
            Double[] values,
            double min,
            double max,
            Color color,
            float strokeWidth,
            boolean dashed
    ) {
        Stroke originalStroke = g2.getStroke();
        if (dashed) {
            g2.setStroke(new BasicStroke(
                    strokeWidth,
                    BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND,
                    1.0f,
                    new float[]{6.0f, 6.0f},
                    0.0f
            ));
        } else {
            g2.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        }

        g2.setColor(color);
        for (int i = 1; i < values.length; i++) {
            if (values[i - 1] == null || values[i] == null) {
                continue;
            }
            int x1 = toX(x, width, values.length, i - 1);
            int y1 = toY(y, height, min, max, values[i - 1]);
            int x2 = toX(x, width, values.length, i);
            int y2 = toY(y, height, min, max, values[i]);
            g2.drawLine(x1, y1, x2, y2);
        }

        g2.setStroke(originalStroke);
    }

    private void drawMacdHistogram(
            Graphics2D g2,
            int x,
            int y,
            int width,
            int height,
            Double[] values,
            double min,
            double max
    ) {
        int zeroY = toY(y, height, min, max, 0.0);
        int barWidth = Math.max(1, width / Math.max(80, values.length));

        for (int i = 0; i < values.length; i++) {
            if (values[i] == null) {
                continue;
            }
            int px = toX(x, width, values.length, i);
            int py = toY(y, height, min, max, values[i]);
            int rectX = px - (barWidth / 2);
            int rectY = Math.min(py, zeroY);
            int rectH = Math.max(1, Math.abs(py - zeroY));

            g2.setColor(values[i] >= 0 ? MACD_HIST_POSITIVE : MACD_HIST_NEGATIVE);
            g2.fillRect(rectX, rectY, barWidth, rectH);
        }
    }

    private void drawLatestPoint(
            Graphics2D g2,
            int x,
            int y,
            int width,
            int height,
            double[] values,
            double min,
            double max
    ) {
        int lastIndex = values.length - 1;
        int px = toX(x, width, values.length, lastIndex);
        int py = toY(y, height, min, max, values[lastIndex]);

        g2.setColor(PRICE_COLOR);
        g2.fillOval(px - 4, py - 4, 9, 9);
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawOval(px - 4, py - 4, 9, 9);
    }

    private void drawXAxisLabels(Graphics2D g2, List<Sp500DataPoint> data, int x, int y, int width, int height) {
        Sp500DataPoint first = data.get(0);
        Sp500DataPoint middle = data.get(data.size() / 2);
        Sp500DataPoint last = data.get(data.size() - 1);

        g2.setFont(UiTheme.FONT_BODY);
        g2.setColor(UiTheme.TEXT_SECONDARY);
        g2.drawString(first.date().format(dateFormatter), x, y + height + 24);

        String midText = middle.date().format(dateFormatter);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(midText, x + (width / 2) - (fm.stringWidth(midText) / 2), y + height + 24);

        String endText = last.date().format(dateFormatter);
        g2.drawString(endText, x + width - fm.stringWidth(endText), y + height + 24);
    }

    private void drawLegendSide(Graphics2D g2, int legendX, int legendY, int legendWidth) {
        String[] labels = {
                "Preco", "SMA 50", "SMA 200",
                "Bollinger sup", "Bollinger med", "Bollinger inf"
        };

        Color[] colors = {
                PRICE_COLOR, SMA50_COLOR, SMA200_COLOR,
                BOLLINGER_UPPER_COLOR, BOLLINGER_MIDDLE_COLOR, BOLLINGER_LOWER_COLOR
        };

        boolean[] dashed = {false, false, false, true, false, true};

        int lineHeight = 15;
        int padding = 8;
        int legendHeight = padding * 2 + labels.length * lineHeight;

        g2.setColor(new Color(20, 28, 40, 235));
        g2.fillRoundRect(legendX, legendY, legendWidth, legendHeight, 10, 10);
        g2.setColor(new Color(74, 90, 112));
        g2.drawRoundRect(legendX, legendY, legendWidth, legendHeight, 10, 10);

        int textX = legendX + 43;
        int sampleX1 = legendX + 12;
        int sampleX2 = legendX + 34;
        int baseY = legendY + padding + 10;

        Stroke originalStroke = g2.getStroke();
        g2.setFont(UiTheme.FONT_BODY);
        for (int i = 0; i < labels.length; i++) {
            int y = baseY + i * lineHeight;
            setLegendLineStroke(g2, dashed[i]);
            g2.setColor(colors[i]);
            g2.drawLine(sampleX1, y - 4, sampleX2, y - 4);

            g2.setStroke(originalStroke);
            g2.setColor(UiTheme.TEXT_PRIMARY);
            g2.drawString(labels[i], textX, y);
        }
    }

    private void drawLegendBottom(Graphics2D g2, int legendX, int legendY, int legendWidth, int legendHeight) {
        String[] labels = {
                "Preco", "SMA 50", "SMA 200",
                "Bollinger sup", "Bollinger med", "Bollinger inf"
        };

        Color[] colors = {
                PRICE_COLOR, SMA50_COLOR, SMA200_COLOR,
                BOLLINGER_UPPER_COLOR, BOLLINGER_MIDDLE_COLOR, BOLLINGER_LOWER_COLOR
        };

        boolean[] dashed = {false, false, false, true, false, true};

        g2.setColor(new Color(20, 28, 40, 240));
        g2.fillRoundRect(legendX, legendY, legendWidth, legendHeight, 10, 10);
        g2.setColor(new Color(74, 90, 112));
        g2.drawRoundRect(legendX, legendY, legendWidth, legendHeight, 10, 10);

        int columns = 3;
        int rows = 2;
        int rowHeight = legendHeight / rows;
        int cellWidth = legendWidth / columns;
        Stroke originalStroke = g2.getStroke();
        g2.setFont(UiTheme.FONT_STATUS);

        for (int i = 0; i < labels.length; i++) {
            int row = i / columns;
            int col = i % columns;
            int cellX = legendX + col * cellWidth;
            int cellY = legendY + row * rowHeight;

            int sampleX1 = cellX + 10;
            int sampleX2 = cellX + 28;
            int baselineY = cellY + 18;

            setLegendLineStroke(g2, dashed[i]);
            g2.setColor(colors[i]);
            g2.drawLine(sampleX1, baselineY - 4, sampleX2, baselineY - 4);

            g2.setStroke(originalStroke);
            g2.setColor(UiTheme.TEXT_PRIMARY);
            g2.drawString(labels[i], cellX + 34, baselineY);
        }
    }

    private void setLegendLineStroke(Graphics2D g2, boolean dashed) {
        if (dashed) {
            g2.setStroke(new BasicStroke(
                    1.5f,
                    BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND,
                    1.0f,
                    new float[]{6.0f, 6.0f},
                    0.0f
            ));
        } else {
            g2.setStroke(new BasicStroke(1.9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        }
    }

    private void drawLegendDivider(Graphics2D g2, int dividerX, int chartY, int chartHeight) {
        Stroke originalStroke = g2.getStroke();
        g2.setStroke(new BasicStroke(1f));
        g2.setColor(new Color(56, 68, 87));
        g2.drawLine(dividerX, chartY, dividerX, chartY + chartHeight);
        g2.setStroke(originalStroke);
    }

    private int toX(int x, int width, int pointCount, int index) {
        if (pointCount <= 1) {
            return x;
        }
        return x + (int) ((double) index / (pointCount - 1) * width);
    }

    private int toY(int y, int height, double min, double max, double value) {
        return y + (int) ((max - value) / (max - min) * height);
    }

    private Double[] movingAveragePerformance(List<Sp500DataPoint> data, int period, double base) {
        Double[] result = new Double[data.size()];
        double rollingSum = 0.0;

        for (int i = 0; i < data.size(); i++) {
            double close = data.get(i).close();
            rollingSum += close;

            if (i >= period) {
                rollingSum -= data.get(i - period).close();
            }

            if (i >= period - 1) {
                double average = rollingSum / period;
                result[i] = (average / base - 1.0) * 100.0;
            }
        }

        return result;
    }

    private BollingerSeries bollingerPerformance(List<Sp500DataPoint> data, int period, double multiplier, double base) {
        Double[] upper = new Double[data.size()];
        Double[] middle = new Double[data.size()];
        Double[] lower = new Double[data.size()];

        double rollingSum = 0.0;
        double rollingSquareSum = 0.0;

        for (int i = 0; i < data.size(); i++) {
            double close = data.get(i).close();
            rollingSum += close;
            rollingSquareSum += close * close;

            if (i >= period) {
                double removed = data.get(i - period).close();
                rollingSum -= removed;
                rollingSquareSum -= removed * removed;
            }

            if (i >= period - 1) {
                double mean = rollingSum / period;
                double variance = (rollingSquareSum / period) - (mean * mean);
                variance = Math.max(variance, 0.0);
                double stdDev = Math.sqrt(variance);

                double upperPrice = mean + multiplier * stdDev;
                double middlePrice = mean;
                double lowerPrice = mean - multiplier * stdDev;

                upper[i] = (upperPrice / base - 1.0) * 100.0;
                middle[i] = (middlePrice / base - 1.0) * 100.0;
                lower[i] = (lowerPrice / base - 1.0) * 100.0;
            }
        }

        return new BollingerSeries(upper, middle, lower);
    }

    private MacdSeries macdSeries(List<Sp500DataPoint> data, int shortPeriod, int longPeriod, int signalPeriod) {
        int size = data.size();
        Double[] macd = new Double[size];
        Double[] signal = new Double[size];
        Double[] histogram = new Double[size];

        List<Double> closes = new ArrayList<>(size);
        for (Sp500DataPoint point : data) {
            closes.add(point.close());
        }

        Double[] emaShort = emaSeries(closes, shortPeriod);
        Double[] emaLong = emaSeries(closes, longPeriod);

        List<Double> compactMacd = new ArrayList<>();
        List<Integer> compactIndices = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            if (emaShort[i] != null && emaLong[i] != null) {
                macd[i] = emaShort[i] - emaLong[i];
                compactMacd.add(macd[i]);
                compactIndices.add(i);
            }
        }

        if (compactMacd.size() >= signalPeriod) {
            Double[] signalCompact = emaSeries(compactMacd, signalPeriod);
            for (int i = 0; i < signalCompact.length; i++) {
                if (signalCompact[i] != null) {
                    signal[compactIndices.get(i)] = signalCompact[i];
                }
            }
        }

        for (int i = 0; i < size; i++) {
            if (macd[i] != null && signal[i] != null) {
                histogram[i] = macd[i] - signal[i];
            }
        }

        return new MacdSeries(macd, signal, histogram);
    }

    private Double[] rsiSeries(List<Sp500DataPoint> data, int period) {
        int size = data.size();
        Double[] rsi = new Double[size];
        if (size <= period) {
            return rsi;
        }

        List<Double> closes = new ArrayList<>(size);
        for (Sp500DataPoint point : data) {
            closes.add(point.close());
        }

        double gainSum = 0.0;
        double lossSum = 0.0;
        for (int i = 1; i <= period; i++) {
            double delta = closes.get(i) - closes.get(i - 1);
            if (delta >= 0) {
                gainSum += delta;
            } else {
                lossSum += -delta;
            }
        }

        double averageGain = gainSum / period;
        double averageLoss = lossSum / period;
        rsi[period] = computeRsi(averageGain, averageLoss);

        for (int i = period + 1; i < size; i++) {
            double delta = closes.get(i) - closes.get(i - 1);
            double gain = Math.max(delta, 0.0);
            double loss = Math.max(-delta, 0.0);

            averageGain = ((averageGain * (period - 1)) + gain) / period;
            averageLoss = ((averageLoss * (period - 1)) + loss) / period;
            rsi[i] = computeRsi(averageGain, averageLoss);
        }

        return rsi;
    }

    private double computeRsi(double averageGain, double averageLoss) {
        if (averageLoss == 0.0) {
            return 100.0;
        }
        double rs = averageGain / averageLoss;
        return 100.0 - (100.0 / (1.0 + rs));
    }

    private Double[] emaSeries(List<Double> values, int period) {
        Double[] emaValues = new Double[values.size()];
        if (values.size() < period) {
            return emaValues;
        }

        double seedSma = 0.0;
        for (int i = 0; i < period; i++) {
            seedSma += values.get(i);
        }
        seedSma /= period;
        emaValues[period - 1] = seedSma;

        double multiplier = 2.0 / (period + 1.0);
        for (int i = period; i < values.size(); i++) {
            double prevEma = emaValues[i - 1];
            double current = values.get(i);
            emaValues[i] = ((current - prevEma) * multiplier) + prevEma;
        }

        return emaValues;
    }

    private double includeNullableSeriesMin(double currentMin, Double[] series) {
        double min = currentMin;
        for (Double value : series) {
            if (value != null) {
                min = Math.min(min, value);
            }
        }
        return min;
    }

    private double includeNullableSeriesMax(double currentMax, Double[] series) {
        double max = currentMax;
        for (Double value : series) {
            if (value != null) {
                max = Math.max(max, value);
            }
        }
        return max;
    }

    private void drawCenteredMessage(Graphics2D g2, String message) {
        g2.setFont(UiTheme.FONT_BODY_BOLD);
        FontMetrics fm = g2.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(message)) / 2;
        int y = getHeight() / 2;
        g2.setColor(UiTheme.TEXT_SECONDARY);
        g2.drawString(message, x, y);
    }

    private record BollingerSeries(Double[] upper, Double[] middle, Double[] lower) {
    }

    private record MacdSeries(Double[] macd, Double[] signal, Double[] histogram) {
    }

    private record PanelHeights(int mainHeight, int macdHeight, int rsiHeight) {
    }
}
