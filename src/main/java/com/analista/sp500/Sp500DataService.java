package com.analista.sp500;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Sp500DataService {
    private static final String STOOQ_URL = "https://stooq.com/q/d/l/?s=%5Espx&i=d";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public List<Sp500DataPoint> fetchDailyCloseData() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(STOOQ_URL))
                .timeout(Duration.ofSeconds(20))
                .header("User-Agent", "Java Swing S&P500 App")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Falha ao obter dados do S&P 500. HTTP " + response.statusCode());
        }

        List<Sp500DataPoint> parsed = parseCsv(response.body());
        if (parsed.isEmpty()) {
            throw new IOException("A resposta nao contem dados validos para o S&P 500.");
        }
        return parsed;
    }

    private List<Sp500DataPoint> parseCsv(String csvContent) {
        List<Sp500DataPoint> points = new ArrayList<>();
        String[] lines = csvContent.split("\\R");
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) {
                continue;
            }

            String[] parts = line.split(",");
            if (parts.length < 5) {
                continue;
            }

            try {
                LocalDate date = LocalDate.parse(parts[0].trim());
                String closeRaw = parts[4].trim();
                if (closeRaw.isEmpty() || closeRaw.equalsIgnoreCase("null")) {
                    continue;
                }

                double close = Double.parseDouble(closeRaw);
                points.add(new Sp500DataPoint(date, close));
            } catch (DateTimeParseException | NumberFormatException ignored) {
                // Ignora linhas invalidas sem interromper o carregamento.
            }
        }

        points.sort(Comparator.comparing(Sp500DataPoint::date));
        return List.copyOf(points);
    }
}
