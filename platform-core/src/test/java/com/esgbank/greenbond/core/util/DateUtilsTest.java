package com.esgbank.greenbond.core.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class DateUtilsTest {

    private LocalDate testDate;
    private LocalDateTime testDateTime;

    @BeforeEach
    void setUp() {
        TestMdcUtils.setupTestMdc();
        testDate = LocalDate.of(2023, 12, 25);
        testDateTime = LocalDateTime.of(2023, 12, 25, 10, 30, 0);
    }

    @Test
    void shouldFormatAndParseDate() {
        // When
        String formatted = DateUtils.formatDate(testDate);
        LocalDate parsed = DateUtils.parseDate(formatted);

        // Then
        assertThat(parsed).isEqualTo(testDate);
        assertThat(formatted).isEqualTo("2023-12-25");
    }

    @Test
    void shouldFormatAndParseDateTime() {
        // When
        String formatted = DateUtils.formatDateTime(testDateTime);
        LocalDateTime parsed = DateUtils.parseDateTime(formatted);

        // Then
        assertThat(parsed).isEqualTo(testDateTime);
        assertThat(formatted).isEqualTo("2023-12-25T10:30:00");
    }

    @Test
    void shouldConvertBetweenDateAndLocalDate() {
        // When
        Date date = DateUtils.toDate(testDate);
        LocalDate convertedBack = DateUtils.toLocalDate(date);

        // Then
        assertThat(convertedBack).isEqualTo(testDate);
    }

    @Test
    void shouldCalculateDateDifferences() {
        // Given
        LocalDate start = LocalDate.of(2023, 1, 1);
        LocalDate end = LocalDate.of(2023, 1, 10);

        // When
        long daysBetween = DateUtils.daysBetween(start, end);

        // Then
        assertThat(daysBetween).isEqualTo(9);
    }

    @Test
    void shouldHandleDateComparisons() {
        // Given
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        // When & Then
        assertTrue(DateUtils.isBefore(yesterday, tomorrow));
        assertTrue(DateUtils.isAfter(tomorrow, yesterday));
    }

    @Test
    void shouldHandleDateManipulations() {
        // When
        LocalDate futureDate = DateUtils.addDays(testDate, 5);
        LocalDate pastDate = DateUtils.subtractDays(testDate, 5);

        // Then
        assertThat(futureDate).isEqualTo(LocalDate.of(2023, 12, 30));
        assertThat(pastDate).isEqualTo(LocalDate.of(2023, 12, 20));
    }

    @Test
    void shouldDetectWeekend() {
        // Given
        LocalDate saturday = LocalDate.of(2023, 12, 23); // Saturday
        LocalDate sunday = LocalDate.of(2023, 12, 24);   // Sunday
        LocalDate monday = LocalDate.of(2023, 12, 25);   // Monday

        // When & Then
        assertTrue(DateUtils.isWeekend(saturday));
        assertTrue(DateUtils.isWeekend(sunday));
        assertFalse(DateUtils.isWeekend(monday));
    }

    @Test
    void shouldGetNextBusinessDay() {
        // Given
        LocalDate friday = LocalDate.of(2023, 12, 22); // Friday
        LocalDate expectedMonday = LocalDate.of(2023, 12, 25); // Monday

        // When
        LocalDate nextBusinessDay = DateUtils.getNextBusinessDay(friday);

        // Then
        assertThat(nextBusinessDay).isEqualTo(expectedMonday);
    }
}