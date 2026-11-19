package com.example.mongo.util;

import org.apache.commons.lang3.tuple.Pair;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

/**
 * 操作LocalDateTime的工具类
 *
 * @author zhouliliang
 * @version 1.0
 * @since 2026/4/22
 */
public class TimeUtil {
    public static final String START_TIME = "000000";
    public static final String END_TIME = "235959";
    public static final DateTimeFormatter PURE_DATETIME_FORMATTER = createFormatter("yyyyMMddHHmmss");
    public static final DateTimeFormatter FGU_FORMATTER = createFormatter("yyyy.MM.dd HH:mm:ss");
    public static final DateTimeFormatter PURE_DATE_FORMATTER = createFormatter("yyyyMMdd");
    public static final DateTimeFormatter DATETIME_MS_FORMATTER = createFormatter("yyyy/MM/dd HH:mm:ss.SSS");
    public static final ZoneId CHINA_ZONE_ID = ZoneId.of("Asia/Shanghai");

    public static DateTimeFormatter createFormatter(String pattern) {
        return DateTimeFormatter.ofPattern(pattern, Locale.getDefault()).withZone(CHINA_ZONE_ID);
    }

    public static String getCurrentTime() {
        return toPureDateTimeStr(LocalDateTime.now());
    }

    public static LocalDateTime parsePureDateTime(String dateTimeStr) {
        return LocalDateTime.parse(dateTimeStr, PURE_DATETIME_FORMATTER);
    }

    public static String toPureDateTimeStr(LocalDateTime localDateTime) {
        return PURE_DATETIME_FORMATTER.format(localDateTime);
    }

    public static String toFguDateTime(String dateTimeStr) {
        return FGU_FORMATTER.format(parsePureDateTime(dateTimeStr));
    }

    public static String getStartTime(LocalDate localDate) {
        return PURE_DATE_FORMATTER.format(localDate) + START_TIME;
    }

    public static Pair<String, String> getStartAndEndTimePair(LocalDate localDate) {
        return Pair.of(getStartTime(localDate.minusDays(1L)), getStartTime(localDate));
    }

    public static Pair<Date, Date> getDatePair(LocalDate localDate) {
        return Pair.of(getDate(localDate.minusDays(1L).atStartOfDay(), CHINA_ZONE_ID),
                getDate(localDate.atStartOfDay(), CHINA_ZONE_ID));
    }

    public static Pair<Date, Date> getYesterdayDatePair() {
        return getDatePair(LocalDate.now());
    }

    public static Date getDate(LocalDateTime startDateTime, ZoneId zoneId) {
        return Date.from(startDateTime.atZone(zoneId).toInstant());
    }

    public static String toPureDateStr(LocalDate localDate) {
        return PURE_DATE_FORMATTER.format(localDate);
    }

    public static Pair<String, String> getYesterdayTimePair() {
        LocalDate today = LocalDate.now();
        return Pair.of(getStartTime(today.minusDays(1L)), getStartTime(today));
    }

    public static String getYesterdayDateStr(LocalDate localDate) {
        return toPureDateStr(localDate.minusDays(1L));
    }
}
