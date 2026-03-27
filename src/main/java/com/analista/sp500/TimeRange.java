package com.analista.sp500;

import java.time.LocalDate;

public enum TimeRange {
    ONE_YEAR("1 ano", 1),
    THREE_YEARS("3 anos", 3),
    FIVE_YEARS("5 anos", 5),
    ALL("Desde o inicio", null);

    private final String label;
    private final Integer years;

    TimeRange(String label, Integer years) {
        this.label = label;
        this.years = years;
    }

    public String getLabel() {
        return label;
    }

    public LocalDate getStartDate(LocalDate latestDate) {
        if (years == null) {
            return LocalDate.MIN;
        }
        return latestDate.minusYears(years);
    }
}
