package com.analista.sp500;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ScreenshotGenerator {
    private static final Path SCREENSHOT_DIR = Path.of("docs", "screenshots");
    private static final Dimension FRAME_SIZE = new Dimension(1720, 980);

    public static void main(String[] args) throws Exception {
        configureLookAndFeel();
        Files.createDirectories(SCREENSHOT_DIR);

        List<Sp500DataPoint> data = loadDataOrFallback();
        if (data.size() < 2) {
            throw new IllegalStateException("Nao foi possivel preparar dados suficientes para screenshots.");
        }

        SwingUtilities.invokeAndWait(() -> {
            MainFrame frame = new MainFrame(new Sp500DataService(), false);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(FRAME_SIZE);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            frame.setDataSnapshot(data, LocalDateTime.now());

            captureRange(frame, TimeRange.FIVE_DAYS, "ui-full-5-dias.png");
            captureRange(frame, TimeRange.ONE_YEAR, "ui-full-1-ano.png");
            captureRange(frame, TimeRange.FIVE_YEARS, "ui-full-5-anos.png");

            frame.dispose();
        });
    }

    private static void configureLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Mantem o look and feel padrao.
        }
    }

    private static void captureRange(MainFrame frame, TimeRange range, String fileName) {
        frame.selectTimeRange(range);
        frame.revalidate();
        frame.doLayout();
        frame.repaint();
        BufferedImage image = new BufferedImage(frame.getWidth(), frame.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        try {
            frame.printAll(g2);
        } finally {
            g2.dispose();
        }

        Path output = SCREENSHOT_DIR.resolve(fileName);
        try {
            ImageIO.write(image, "png", output.toFile());
            System.out.println("Screenshot gerado: " + output);
        } catch (IOException exception) {
            throw new RuntimeException("Falha ao guardar screenshot: " + output, exception);
        }
    }

    private static List<Sp500DataPoint> loadDataOrFallback() {
        try {
            return new Sp500DataService().fetchDailyCloseData();
        } catch (Exception exception) {
            System.err.println("Falha ao obter dados online. A usar serie sintetica para screenshots.");
            return buildSyntheticSeries();
        }
    }

    private static List<Sp500DataPoint> buildSyntheticSeries() {
        List<Sp500DataPoint> points = new ArrayList<>();
        LocalDate start = LocalDate.now().minusYears(8);
        LocalDate end = LocalDate.now();
        Random random = new Random(42);

        double value = 1800.0;
        int dayIndex = 0;
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            if (date.getDayOfWeek().getValue() >= 6) {
                continue;
            }

            double trend = 0.00018;
            double cycle = Math.sin(dayIndex / 65.0) * 0.0035;
            double noise = (random.nextDouble() - 0.5) * 0.01;
            double shock = random.nextDouble() < 0.015 ? ((random.nextDouble() - 0.5) * 0.07) : 0.0;

            double variation = trend + cycle + noise + shock;
            value = Math.max(900.0, value * (1.0 + variation));
            points.add(new Sp500DataPoint(date, value));
            dayIndex++;
        }

        return points;
    }
}
