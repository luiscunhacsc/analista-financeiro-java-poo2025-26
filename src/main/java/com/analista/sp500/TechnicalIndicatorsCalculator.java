package com.analista.sp500;

import java.util.ArrayList;
import java.util.List;

public class TechnicalIndicatorsCalculator {
    public TechnicalIndicators calculate(List<Sp500DataPoint> data) {
        if (data == null || data.isEmpty()) {
            return null;
        }

        List<Double> closes = new ArrayList<>(data.size());
        for (Sp500DataPoint point : data) {
            closes.add(point.close());
        }

        double lastClose = closes.get(closes.size() - 1);
        Double sma20 = sma(closes, 20);
        Double sma50 = sma(closes, 50);
        Double sma200 = sma(closes, 200);
        Double ema20 = ema(closes, 20);
        Double rsi14 = rsi(closes, 14);

        MacdResult macdResult = macd(closes, 12, 26, 9);
        BollingerResult bollinger = bollinger(closes, 20, 2.0);
        RangeResult range52 = rollingRange(closes, 252);

        return new TechnicalIndicators(
                lastClose,
                sma20,
                sma50,
                sma200,
                ema20,
                rsi14,
                macdResult.macd,
                macdResult.signal,
                macdResult.histogram,
                bollinger.upper,
                bollinger.middle,
                bollinger.lower,
                range52.high,
                range52.low
        );
    }

    private Double sma(List<Double> values, int period) {
        if (values.size() < period) {
            return null;
        }
        double sum = 0.0;
        for (int i = values.size() - period; i < values.size(); i++) {
            sum += values.get(i);
        }
        return sum / period;
    }

    private Double ema(List<Double> values, int period) {
        if (values.size() < period) {
            return null;
        }
        Double[] series = emaSeries(values, period);
        return series[series.length - 1];
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

    private Double rsi(List<Double> values, int period) {
        if (values.size() <= period) {
            return null;
        }

        double gainSum = 0.0;
        double lossSum = 0.0;
        for (int i = 1; i <= period; i++) {
            double delta = values.get(i) - values.get(i - 1);
            if (delta >= 0) {
                gainSum += delta;
            } else {
                lossSum += -delta;
            }
        }

        double averageGain = gainSum / period;
        double averageLoss = lossSum / period;

        for (int i = period + 1; i < values.size(); i++) {
            double delta = values.get(i) - values.get(i - 1);
            double gain = Math.max(delta, 0.0);
            double loss = Math.max(-delta, 0.0);

            averageGain = ((averageGain * (period - 1)) + gain) / period;
            averageLoss = ((averageLoss * (period - 1)) + loss) / period;
        }

        if (averageLoss == 0.0) {
            return 100.0;
        }
        double rs = averageGain / averageLoss;
        return 100.0 - (100.0 / (1.0 + rs));
    }

    private MacdResult macd(List<Double> values, int shortPeriod, int longPeriod, int signalPeriod) {
        if (values.size() < longPeriod) {
            return MacdResult.EMPTY;
        }

        Double[] emaShort = emaSeries(values, shortPeriod);
        Double[] emaLong = emaSeries(values, longPeriod);

        Double[] macdValues = new Double[values.size()];
        List<Double> compactMacd = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            if (emaShort[i] != null && emaLong[i] != null) {
                macdValues[i] = emaShort[i] - emaLong[i];
                compactMacd.add(macdValues[i]);
            }
        }

        if (compactMacd.size() < signalPeriod) {
            return new MacdResult(macdValues[values.size() - 1], null, null);
        }

        Double[] signalCompact = emaSeries(compactMacd, signalPeriod);

        Double latestMacd = macdValues[values.size() - 1];
        Double latestSignal = null;
        Double latestHistogram = null;

        for (int i = signalCompact.length - 1; i >= 0; i--) {
            if (signalCompact[i] != null) {
                latestSignal = signalCompact[i];
                break;
            }
        }

        if (latestMacd != null && latestSignal != null) {
            latestHistogram = latestMacd - latestSignal;
        }

        return new MacdResult(
                latestMacd,
                latestSignal,
                latestHistogram
        );
    }

    private BollingerResult bollinger(List<Double> values, int period, double deviationMultiplier) {
        if (values.size() < period) {
            return BollingerResult.EMPTY;
        }

        double mean = sma(values, period);
        double variance = 0.0;
        for (int i = values.size() - period; i < values.size(); i++) {
            double diff = values.get(i) - mean;
            variance += diff * diff;
        }
        variance /= period;
        double stdDev = Math.sqrt(variance);

        double upper = mean + (deviationMultiplier * stdDev);
        double lower = mean - (deviationMultiplier * stdDev);

        return new BollingerResult(upper, mean, lower);
    }

    private RangeResult rollingRange(List<Double> values, int period) {
        if (values.isEmpty()) {
            return RangeResult.EMPTY;
        }
        int effectivePeriod = Math.min(period, values.size());
        double high = Double.NEGATIVE_INFINITY;
        double low = Double.POSITIVE_INFINITY;

        for (int i = values.size() - effectivePeriod; i < values.size(); i++) {
            double v = values.get(i);
            high = Math.max(high, v);
            low = Math.min(low, v);
        }

        return new RangeResult(high, low);
    }

    private record MacdResult(Double macd, Double signal, Double histogram) {
        private static final MacdResult EMPTY = new MacdResult(null, null, null);
    }

    private record BollingerResult(Double upper, Double middle, Double lower) {
        private static final BollingerResult EMPTY = new BollingerResult(null, null, null);
    }

    private record RangeResult(Double high, Double low) {
        private static final RangeResult EMPTY = new RangeResult(null, null);
    }
}
