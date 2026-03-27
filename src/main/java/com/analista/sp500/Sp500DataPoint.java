package com.analista.sp500;

import java.time.LocalDate;

public record Sp500DataPoint(LocalDate date, double close) {
}
