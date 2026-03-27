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
    private static final int BOTTOM_MARGIN = 44;

    private static final int SIDE_LEGEND_WIDTH = 184;
    private static final int SIDE_LEGEND_GAP = 12;
    private static final int MIN_PLOT_WIDTH_WITH_SIDE_LEGEND = 560;

    private static final int BOTTOM_LEGEND_HEIGHT = 54;
    private static final int BOTTOM_LEGEND_GAP = 8;

    private static final Color PRICE_COLOR = new Color(0, 105, 184);
    private static final Color PRICE_FILL_TOP = new Color(0, 105, 184, 70);
    private static final Color PRICE_FILL_BOTTOM = new Color(0, 105, 184, 8);
    private static final Color SMA50_COLOR = new Color(217, 120, 0);
    private static final Color SMA200_COLOR = new Color(170, 57, 57);
    private static final Color BOLLINGER_UPPER_COLOR = new Color(120, 120, 120);
    private static final Color BOLLINGER_MIDDLE_COLOR = new Color(88, 88, 88);
    private static final Color BOLLINGER_LOWER_COLOR = new Color(150, 150, 150);
    private static final Color GRID_COLOR = new Color(226, 233, 243);
    private static final Color AXIS_COLOR = new Color(159, 171, 188);
    private static final Color PLOT_BG_TOP = new Color(245, 249, 255);
    private static final Color PLOT_BG_BOTTOM = new Color(255, 255, 255);

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
            drawCenteredMessage(g2, "Sem dados suficientes para desenhar o grafico.");
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
        int chartHeight = Math.max(100, getHeight() - TOP_MARGIN - BOTTOM_MARGIN - extraLegendBottom);

        double base = data.get(0).close();
        double[] perf = new double[data.size()];
        for (int i = 0; i < data.size(); i++) {
            perf[i] = (data.get(i).close() / base - 1.0) * 100.0;
        }

        Double[] sma50 = movingAveragePerformance(data, 50, base);
        Double[] sma200 = movingAveragePerformance(data, 200, base);
        BollingerSeries bollinger = bollingerPerformance(data, 20, 2.0, base);

        double minPerf = perf[0];
        double maxPerf = perf[0];
        for (double value : perf) {
            minPerf = Math.min(minPerf, value);
            maxPerf = Math.max(maxPerf, value);
        }
        minPerf = includeNullableSeriesMin(minPerf, sma50);
        maxPerf = includeNullableSeriesMax(maxPerf, sma50);
        minPerf = includeNullableSeriesMin(minPerf, sma200);
        maxPerf = includeNullableSeriesMax(maxPerf, sma200);
        minPerf = includeNullableSeriesMin(minPerf, bollinger.upper());
        maxPerf = includeNullableSeriesMax(maxPerf, bollinger.upper());
        minPerf = includeNullableSeriesMin(minPerf, bollinger.middle());
        maxPerf = includeNullableSeriesMax(maxPerf, bollinger.middle());
        minPerf = includeNullableSeriesMin(minPerf, bollinger.lower());
        maxPerf = includeNullableSeriesMax(maxPerf, bollinger.lower());

        minPerf = Math.min(minPerf, 0.0);
        maxPerf = Math.max(maxPerf, 0.0);
        double padding = Math.max((maxPerf - minPerf) * 0.08, 1.2);
        maxPerf += padding;
        minPerf -= padding;
        if (Math.abs(maxPerf - minPerf) < 0.0001) {
            maxPerf += 1.0;
            minPerf -= 1.0;
        }

        drawGridAndAxes(g2, chartX, chartY, chartWidth, chartHeight, minPerf, maxPerf);
        drawAreaUnderPrice(g2, chartX, chartY, chartWidth, chartHeight, perf, minPerf, maxPerf);
        drawNullableLine(g2, chartX, chartY, chartWidth, chartHeight, bollinger.upper(), minPerf, maxPerf, BOLLINGER_UPPER_COLOR, 1.2f, true);
        drawNullableLine(g2, chartX, chartY, chartWidth, chartHeight, bollinger.middle(), minPerf, maxPerf, BOLLINGER_MIDDLE_COLOR, 1.1f, false);
        drawNullableLine(g2, chartX, chartY, chartWidth, chartHeight, bollinger.lower(), minPerf, maxPerf, BOLLINGER_LOWER_COLOR, 1.2f, true);
        drawNullableLine(g2, chartX, chartY, chartWidth, chartHeight, sma200, minPerf, maxPerf, SMA200_COLOR, 1.8f, false);
        drawNullableLine(g2, chartX, chartY, chartWidth, chartHeight, sma50, minPerf, maxPerf, SMA50_COLOR, 1.8f, false);
        drawLine(g2, chartX, chartY, chartWidth, chartHeight, perf, minPerf, maxPerf, PRICE_COLOR, 2.5f);
        drawLatestPoint(g2, chartX, chartY, chartWidth, chartHeight, perf, minPerf, maxPerf);
        drawXAxisLabels(g2, data, chartX, chartY, chartWidth, chartHeight);

        if (useSideLegend) {
            int dividerX = chartX + chartWidth + (SIDE_LEGEND_GAP / 2);
            drawLegendDivider(g2, dividerX, chartY, chartHeight);
            drawLegendSide(g2, chartX + chartWidth + SIDE_LEGEND_GAP, chartY + 8, SIDE_LEGEND_WIDTH);
        } else {
            drawLegendBottom(g2, chartX, chartY + chartHeight + BOTTOM_LEGEND_GAP, chartWidth, BOTTOM_LEGEND_HEIGHT);
        }

        g2.dispose();
    }

    private void drawGridAndAxes(Graphics2D g2, int x, int y, int width, int height, double min, double max) {
        g2.setPaint(new GradientPaint(0, y, PLOT_BG_TOP, 0, y + height, PLOT_BG_BOTTOM));
        g2.fillRoundRect(x, y, width, height, 12, 12);

        int steps = 6;
        g2.setFont(UiTheme.FONT_BODY);
        for (int i = 0; i <= steps; i++) {
            int yLine = y + (int) ((double) i / steps * height);
            g2.setColor(GRID_COLOR);
            g2.drawLine(x, yLine, x + width, yLine);

            double value = max - (max - min) * i / steps;
            g2.setColor(UiTheme.TEXT_SECONDARY);
            g2.drawString(String.format(Locale.US, "%.1f%%", value), 10, yLine + 4);
        }

        g2.setColor(AXIS_COLOR);
        g2.drawRoundRect(x, y, width, height, 12, 12);

        if (min <= 0 && max >= 0) {
            Stroke originalStroke = g2.getStroke();
            int zeroY = y + (int) Math.round((max / (max - min)) * height);
            g2.setColor(new Color(179, 189, 204));
            g2.setStroke(new BasicStroke(1.1f));
            g2.drawLine(x, zeroY, x + width, zeroY);
            g2.setStroke(originalStroke);
        }
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
        int firstY = y + (int) ((max - values[0]) / (max - min) * height);
        path.moveTo(firstX, firstY);

        for (int i = 1; i < values.length; i++) {
            int px = x + (int) ((double) i / (values.length - 1) * width);
            int py = y + (int) ((max - values[i]) / (max - min) * height);
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
            int x1 = x + (int) ((double) (i - 1) / (values.length - 1) * width);
            int y1 = y + (int) ((max - values[i - 1]) / (max - min) * height);
            int x2 = x + (int) ((double) i / (values.length - 1) * width);
            int y2 = y + (int) ((max - values[i]) / (max - min) * height);
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
            int x1 = x + (int) ((double) (i - 1) / (values.length - 1) * width);
            int y1 = y + (int) ((max - values[i - 1]) / (max - min) * height);
            int x2 = x + (int) ((double) i / (values.length - 1) * width);
            int y2 = y + (int) ((max - values[i]) / (max - min) * height);
            g2.drawLine(x1, y1, x2, y2);
        }

        g2.setStroke(originalStroke);
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
        int px = x + (int) ((double) lastIndex / (values.length - 1) * width);
        int py = y + (int) ((max - values[lastIndex]) / (max - min) * height);

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

        g2.setColor(new Color(255, 255, 255, 224));
        g2.fillRoundRect(legendX, legendY, legendWidth, legendHeight, 10, 10);
        g2.setColor(new Color(206, 216, 229));
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

        g2.setColor(new Color(255, 255, 255, 230));
        g2.fillRoundRect(legendX, legendY, legendWidth, legendHeight, 10, 10);
        g2.setColor(new Color(206, 216, 229));
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
        g2.setColor(new Color(220, 228, 238));
        g2.drawLine(dividerX, chartY, dividerX, chartY + chartHeight);
        g2.setStroke(originalStroke);
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
}
