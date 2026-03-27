package com.analista.sp500;

public record TechnicalIndicators(
        double lastClose,
        Double sma20,
        Double sma50,
        Double sma200,
        Double ema20,
        Double rsi14,
        Double macd,
        Double macdSignal,
        Double macdHistogram,
        Double bollingerUpper,
        Double bollingerMiddle,
        Double bollingerLower,
        Double high52Weeks,
        Double low52Weeks
) {
}
