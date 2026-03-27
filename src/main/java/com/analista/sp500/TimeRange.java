package com.analista.sp500;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

public enum TimeRange {
    ONE_DAY("1 dia", 1, ChronoUnit.DAYS),
    FIVE_DAYS("5 dias", 5, ChronoUnit.DAYS),
    ONE_MONTH("1 mes", 1, ChronoUnit.MONTHS),
    ONE_YEAR("1 ano", 1, ChronoUnit.YEARS),
    THREE_YEARS("3 anos", 3, ChronoUnit.YEARS),
    FIVE_YEARS("5 anos", 5, ChronoUnit.YEARS),
    ALL("Desde o inicio", null, null);

    private final String label;
    private final Integer amount;
    private final TemporalUnit unit;

    TimeRange(String label, Integer amount, TemporalUnit unit) {
        this.label = label;
        this.amount = amount;
        this.unit = unit;
    }

    public String getLabel() {
        return label;
    }

    public LocalDate getStartDate(LocalDate latestDate) {
        if (amount == null || unit == null) {
            return LocalDate.MIN;
        }
        return latestDate.minus(amount, unit);
    }
}
