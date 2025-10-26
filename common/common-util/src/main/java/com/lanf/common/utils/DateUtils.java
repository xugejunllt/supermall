package com.lanf.common.utils;

import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.Date;

public class DateUtils {


    // 例如:2020-03-15
    public static final String DATE = "yyyy-MM-dd";
    public static final String DATE_ = "yyyyMMdd";
    // 例如:2020-03-15 10:00:00
    public static final String DATE_TIME = "yyyy-MM-dd HH:mm:ss";

    // 例如:10:00:00
    public static final String TIME = "HHmmss";

    // 例如:10:00
    public static final String TIME_WITHOUT_SECOND = "HH:mm";

    // 例如:2020-03-15 10:00
    public static final String DATE_TIME_WITHOUT_SECONDS = "yyyy-MM-dd HH:mm";


    /**
     * 格式化日期为字符串
     *
     * @param date    日期
     * @param pattern 格式
     * @return 日期字符串
     */
    public static String format(Date date, String pattern) {

        if (date == null || StringUtils.isEmpty(pattern)) {
            throw new IllegalArgumentException();
        }
        Instant instant = date.toInstant();
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        return localDateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 解析字符串日期为Date
     *
     * @param dateStr 日期字符串
     * @param pattern 格式
     * @return 日期
     */
    public static Date parse(String dateStr, String pattern) {
        if (StringUtils.isEmpty(dateStr) || StringUtils.isEmpty(pattern)) {
            throw new IllegalArgumentException();
        }
        LocalDateTime localDateTime = LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern(pattern));
        Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
        return Date.from(instant);
    }
    public static Date parse(String dateStr) {

        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE);
        Date parse = null;
        try {
            parse = dateFormat.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return parse;
    }

    /**
     * 获取年
     *
     * @return 年
     */
    public static int getYear() {
        LocalTime localTime = LocalTime.now();
        return localTime.get(ChronoField.YEAR);
    }

    /**
     * 获取月份
     *
     * @return 月份
     */
    public static int getMonth() {
        LocalTime localTime = LocalTime.now();
        return localTime.get(ChronoField.MONTH_OF_YEAR);
    }

    /**
     * 获取某月的第几天
     *
     * @return 几号
     */
    public static int getMonthOfDay() {
        LocalTime localTime = LocalTime.now();
        return localTime.get(ChronoField.DAY_OF_MONTH);
    }

    /**
     * 获取当前日期
     *
     * @param pattern 格式，默认格式yyyyMMdd
     * @return 按照指定格式返回当前日期
     */
    public static String getCurrentDay(String pattern) {
        LocalDateTime localDateTime = LocalDateTime.now();
        if (StringUtils.isEmpty(pattern)) {
            pattern = "yyyyMMdd";
        }
        Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        // format()为 “第3节 日期和字符串的格式转换“中自写的工具方法
        return format(date, pattern);
    }

    /**
     * 为Date增加分钟
     *
     * @param date        日期
     * @param plusMinutes 要增加的分钟数，如果要减则传负数
     * @return 新的日期
     */
    public static Date addMinutes(Date date, Long plusMinutes) {
        if (date == null || plusMinutes == null) {
            throw new IllegalArgumentException();
        }
        LocalDateTime dateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        LocalDateTime newDateTime = dateTime.plusMinutes(plusMinutes);
        return Date.from(newDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 为Date增加小时
     *
     * @param date      日期
     * @param plusHours 要增加的小时数，如果要减则传负数
     * @return 新的日期
     */
    public static Date addHour(Date date, Long plusHours) {

        if (date == null || plusHours == null) {
            throw new IllegalArgumentException();
        }
        LocalDateTime dateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        LocalDateTime localDateTime = dateTime.plusHours(plusHours);
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * @return 返回当天的起始时间
     */
    public static Date getStartTime() {
        LocalDateTime now = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        Date date = Date.from(now.atZone(ZoneId.systemDefault()).toInstant());
        return date;
    }

    /**
     * @return 返回当天的结束时间
     */
    public static Date getEndTime() {
        LocalDateTime now = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(999);
        Date date = Date.from(now.atZone(ZoneId.systemDefault()).toInstant());
        return date;
    }

    /**
     * 查询当前年的第一天
     *
     * @param pattern 格式，默认格式yyyyMMdd
     * @return 按照指定格式返回当前年的第一天
     */
    public static String getFirstDayOfCurrentYear(String pattern) {
        LocalDateTime localDateTime = LocalDateTime.now().withMonth(1).withDayOfMonth(1);

        if (StringUtils.isEmpty(pattern)) {
            pattern = "yyyyMMdd";
        }
        Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        // format()为 “第3节 日期和字符串的格式转换“中自写的工具方法
        return format(date, pattern);
    }

    public static void main(String[] args) throws ParseException {
        String time = "2023-04-03";
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//1.string->date
        Date parse = dateFormat.parse(time);
        System.out.println(parse(time) );


    }
}
