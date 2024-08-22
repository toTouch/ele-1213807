package com.xiliulou.electricity.utils;

import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName : DateUtils
 * @Description : 关于时间的工具类
 * @Author : HRP
 * @Date: 2022-07-12 13:55
 */
@Slf4j
public class DateUtils {
    
    static DateTimeFormatter MILLS_FORMAT_DATE_V2 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    static DateTimeFormatter MILLS_FORMAT_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    
    static SimpleDateFormat simpleDateFormatYearAndMonth = new SimpleDateFormat("yyyy-MM-dd");
    
    static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");
    
    static final ZoneId CHINA_ZONE_ID = ZoneId.of("Asia/Shanghai");
    
    /**
     * 年月正则表达式：yyyy-MM
     */
    public static final String GREP_YEAR_MONTH = "^\\d{4}-\\d{2}$";
    
    /**
     * 解析毫秒的时间字符串
     *
     * @param date
     * @return
     */
    public static long parseMillsDateStrToTimestampV2(String date) {
        LocalDateTime datetime = LocalDateTime.parse(date, MILLS_FORMAT_DATE_V2);
        return datetime.toInstant(ZoneOffset.of("+8")).toEpochMilli();
    }
    
    /**
     * 解析毫秒的时间字符串
     *
     * @param date
     * @return
     */
    public static long parseMillsDateStrToTimestamp(String date) {
        LocalDateTime datetime = LocalDateTime.parse(date, MILLS_FORMAT_DATE);
        return datetime.toInstant(ZoneOffset.of("+0")).toEpochMilli();
    }
    
    public static long getTodayStartTimeStamp() {
        return LocalDateTime.of(LocalDate.now(), LocalTime.MIN).toEpochSecond(ZoneOffset.of("+8")) * 1000;
    }
    
    public static long getTodayEndTimeStamp() {
        return LocalDateTime.of(LocalDate.now(), LocalTime.MAX).toEpochSecond(ZoneOffset.of("+8")) * 1000;
    }
    
    public static long get30AgoStartTime() {
        return LocalDateTime.of(LocalDate.now().minusDays(30), LocalTime.MIN).toEpochSecond(ZoneOffset.of("+8")) * 1000;
    }
    
    public static long getTimeAgoStartTime(int day) {
        return LocalDateTime.of(LocalDate.now().minusDays(day), LocalTime.MIN).toEpochSecond(ZoneOffset.of("+8")) * 1000;
    }
    
    public static long getTimeAgoEndTime(int day) {
        // 获取当前日期减去day天的日期和时间
        LocalDateTime localDateTime = LocalDateTime.of(LocalDate.now().minusDays(day), LocalTime.MAX);
    
        // 转换为Instant，并指定时区为东八区
        Instant instant = localDateTime.atZone(ZoneOffset.of("+8")).toInstant();
    
        // 返回毫秒数
        return instant.toEpochMilli();
    }
    
    /**
     * 获取本月第N天的开始时间戳
     */
    public static long getDayOfMonthStartTime(int dayOfMonth) {
        return LocalDateTime.of(LocalDateTime.now().toLocalDate().withDayOfMonth(dayOfMonth), LocalTime.MIN).toEpochSecond(ZoneOffset.of("+8")) * 1000;
    }
    
    /**
     * 获取最近N天的开始时间戳(从当前时间往前推7天)
     */
    public static long getLastDayStartTime(Integer lastDay) {
        return LocalDateTime.now().minusDays(lastDay).toEpochSecond(ZoneOffset.of("+8")) * 1000;
    }
    
    /**
     * @param nowTime
     * @param beginTime
     * @param endTime
     * @return boolean
     */
    //判断是否在规定的时间内签到 nowTime 当前时间 beginTime规定开始时间 endTime规定结束时间
    public static boolean timeCalendar(Date nowTime, Date beginTime, Date endTime) {
        //设置当前时间
        Calendar date = Calendar.getInstance();
        date.setTime(nowTime);
        //设置开始时间
        Calendar begin = Calendar.getInstance();
        begin.setTime(beginTime);//开始时间
        //设置结束时间
        Calendar end = Calendar.getInstance();
        end.setTime(endTime);//上午结束时间
        //处于开始时间之后，和结束时间之前的判断
        if ((date.after(begin) && date.before(end))) {
            return true;
        } else {
            return false;
        }
    }
    
    //获取当天零点时间戳
    public static long getTodayStartTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime().getTime();
    }
    
    //时间格式化
    public static String parseTimeToStringDate(Long timeStamp) {
        return DateUtil.format(new Date(timeStamp), "YYYY-MM-dd HH:mm:ss");
    }
    
    /**
     * 计算两个时间差的分钟数，不足一分钟，按一分钟处理<br />
     * TODO 入参为负数的时候，缺少判定
     * <pre>
     *     beginTime：1689154972318L(2023-07-12 17:42:52) endTime：1689154972318L(2023-07-12 17:42:52) return：1
     *     beginTime：1689154972318L(2023-07-12 17:42:52) endTime：1689155033319L(2023-07-12 17:43:53) return：2
     *     beginTime：1689154972318L(2023-07-12 17:42:52) endTime：1689154973000L(2023-07-12 17:42:53) return：1
     * </pre>
     *
     * @param beginTime 开始时间戳，毫秒
     * @param endTime   结束时间戳，毫秒
     * @return
     */
    public static long diffMinute(long beginTime, long endTime) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(endTime - beginTime) + 1;
        return minutes;
    }
    
    /**
     * 计算两个时间差的天数，不足一天，按一天处理<br />
     * TODO 入参为负数的时候，缺少判定
     * <pre>
     *     beginTime：1689129464594L(2023-07-12 10:37:44) endTime：1689129464594L(2023-07-12 10:37:44) return：1
     *     beginTime：1689129464594L(2023-07-12 10:37:44) endTime：1689216044595L(2023-07-13 10:40:44) return：2
     *     beginTime：1689129464594L(2023-07-12 10:37:44) endTime：1689212264000L(2023-07-13 09:40:44) return：1
     * </pre>
     *
     * @param beginTime 开始时间戳，毫秒
     * @param endTime   结束时间戳，毫秒
     * @return
     */
    public static long diffDay(long beginTime, long endTime) {
        long days = TimeUnit.MILLISECONDS.toDays(endTime - beginTime) + 1;
        return days;
    }
    
    public static long diffDayV2(long beginTime, long endTime) {
        long days = TimeUnit.MILLISECONDS.toDays((endTime - 1) - beginTime) + 1;
        return days;
    }
    
    /**
     * 判断两个时间段是否有交集
     *
     * @param leftStartDate  第一个时间段的开始时间
     * @param leftEndDate    第一个时间段的结束时间
     * @param rightStartDate 第二个时间段的开始时间
     * @param rightEndDate   第二个时间段的结束时间
     * @return 若有交集, 返回true, 否则返回false
     */
    public static boolean hasOverlap(long leftStartDate, long leftEndDate, long rightStartDate, long rightEndDate) {
        
        return ((leftStartDate >= rightStartDate) && leftStartDate < rightEndDate) || ((leftStartDate > rightStartDate) && leftStartDate <= rightEndDate) || (
                (rightStartDate >= leftStartDate) && rightStartDate < leftEndDate) || ((rightStartDate > leftStartDate) && rightStartDate <= leftEndDate);
        
    }
    
    /**
     * 根据LocalDate获取当天的0点时间戳
     */
    public static long getDayStartTimeByLocalDate(LocalDate localDate) {
        return localDate.atStartOfDay().toEpochSecond(ZoneOffset.of("+8")) * 1000;
    }
    
    public static String getYearAndMonthAndDayByTimeStamps(Long timeStamp) {
        return simpleDateFormatYearAndMonth.format(timeStamp);
    }
    
    
    /**
     * 根据某个时间戳获取当天的结束时间戳
     *
     * @param timeStamp
     * @return
     */
    public static Long getDayEndTimeStampByDate(Long timeStamp) {
        LocalDate localDate = Instant.ofEpochMilli(timeStamp).atZone(ZoneOffset.of("+8")).toLocalDate();
        return LocalDateTime.of(localDate, LocalTime.MAX).toEpochSecond(ZoneOffset.of("+8")) * 1000;
    }
    
    /**
     * 根据年月获取当月第一天 年月：2024-01 返回: 2024-01-01
     */
    public static String getFirstDayByMonth(String yearMonth) {
        // 分割年月字符串并转换为整数
        int year = Integer.parseInt(yearMonth.split("-")[0]);
        int month = Integer.parseInt(yearMonth.split("-")[1]);
        
        // 创建指定年月的第一天日期
        LocalDate startDate = LocalDate.of(year, month, 1);
        
        return startDate.format(DATE_FORMATTER);
    }
    
    /**
     * 根据年月获取当月最后一天 年月：2024-01 返回: 2024-01-31
     */
    public static String getLastDayByMonth(String yearMonth) {
        // 分割年月字符串并转换为整数
        int year = Integer.parseInt(yearMonth.split("-")[0]);
        int month = Integer.parseInt(yearMonth.split("-")[1]);
        
        // 创建指定年月的第一天日期
        LocalDate startDate = LocalDate.of(year, month, 1);
        
        // 获取该月的最后一天
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        
        return endDate.format(DATE_FORMATTER);
    }
    
    /**
     * 根据某个时间戳获取当天的结束时间戳
     *
     * @param timeStamp
     * @return
     */
    public static Long getMonthEndTimeStampByDate(Long timeStamp) {
        LocalDate localDate = Instant.ofEpochMilli(timeStamp).atZone(ZoneOffset.of("+8")).toLocalDate();
        return Date.from(localDate.with(TemporalAdjusters.lastDayOfMonth()).atTime(LocalTime.MAX).atZone(ZoneOffset.of("+8")).toInstant()).getTime();
        // return LocalDateTime.of(localDate, LocalTime.MAX).toEpochSecond(ZoneOffset.of("+8"))*1000;
    }
    
    /**
     * 获取某个月：yyyy-MM
     */
    public static String getMonthDate(Long month) {
        LocalDate yesterdayInChina = LocalDate.now(CHINA_ZONE_ID).minusMonths(month);
        return yesterdayInChina.format(MONTH_FORMATTER);
    }
    
    /*
     * 获取前某月第一天00:00:00的时间戳
     */
    public static long getBeforeMonthFirstDayTimestamp(Integer minusMonth) {
        LocalDate lastMonthFirstDay = LocalDate.now().minusMonths(minusMonth).withDayOfMonth(1);
        return lastMonthFirstDay.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
    
    /**
     * 获取前某月最后一天23:59:59时间戳
     */
    public static long getBeforeMonthLastDayTimestamp(Integer minusMonth) {
        LocalDate lastMonthFirstDay = LocalDate.now().minusMonths(minusMonth).withDayOfMonth(1);
        LocalDate lastMonthLastDay = lastMonthFirstDay.with(TemporalAdjusters.lastDayOfMonth());
        return lastMonthLastDay.atTime(23, 59, 59, 999999999).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
    
    public static boolean isSameDay(long time1, long time2) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
        return fmt.format(new Date(time1)).equals(fmt.format(new Date(time2)));
    }
    
    public static boolean isSameMonth(long time1, long time2) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMM");
        return fmt.format(new Date(time1)).equals(fmt.format(new Date(time2)));
    }
    
    /**
     * 根据时间戳获取当天0点的时间戳
     */
    public static long getStartTimeByTimeStamp(long timestamp) {
        // 将输入的时间戳转换为指定时区的ZonedDateTime对象，默认使用系统默认时区
        ZonedDateTime zonedDateTime = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault());
        
        // 获取当天零点的ZonedDateTime对象
        ZonedDateTime startOfDay = zonedDateTime.toLocalDate().atStartOfDay(zonedDateTime.getZone());
        return startOfDay.toInstant().toEpochMilli();
    }
    
    /**
     * @description 获取本年截至本月(minusMonth = 0)的月份
     * @date 2024/3/4 22:07:49
     * @author HeYafeng
     */
    public static List<String> getMonthsUntilCurrent(int minusMonth) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        List<String> yearList = new ArrayList<>();
        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue() - minusMonth;
        
        for (int month = 1; month <= currentMonth; month++) {
            String monthStr = LocalDate.of(currentYear, month, 1).format(formatter);
            yearList.add(monthStr);
        }
        
        return yearList;
    }
    
    /**
     * 根据时间戳获取当天开始时间
     *
     * @return 今天的开始时间
     */
    public static long getTimeByTimeStamp(long timestamp) {
        // 将时间戳转换为UTC的Instant对象
        Instant instant = Instant.ofEpochMilli(timestamp);
        // 转换为指定时区的ZonedDateTime
        ZonedDateTime zonedDateTime = instant.atZone(CHINA_ZONE_ID);
        // 获取当天的开始时间（即00:00:00）
        ZonedDateTime startOfDay = zonedDateTime.toLocalDate().atStartOfDay(CHINA_ZONE_ID);
        // 如果需要再次转换回时间戳
        return startOfDay.toInstant().toEpochMilli();
    }
    
    public static long getEndOfDayTimestamp(long timestamp){
        // 将时间戳转换为Instant对象
        Instant instant = Instant.ofEpochMilli(timestamp);
    
        // 转换为LocalDateTime并设置为当天的开始时间
        LocalDateTime localDateTime = instant.atZone(CHINA_ZONE_ID).toLocalDate().atStartOfDay();
    
        // 计算当天的最后一刻（即23:59:59.999）
        LocalDateTime endOfDay = localDateTime.plus(1, ChronoUnit.DAYS).minus(1, ChronoUnit.MILLIS);
    
        // 再次转换回时间戳
        return endOfDay.atZone(CHINA_ZONE_ID).toInstant().toEpochMilli();
    }
}
