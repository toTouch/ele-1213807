package com.xiliulou.electricity.utils;

import cn.hutool.core.date.DateUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName : DateUtils
 * @Description : 关于时间的工具类
 * @Author : HRP
 * @Date: 2022-07-12 13:55
 */
public class DateUtils {

    static DateTimeFormatter MILLS_FORMAT_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * 解析毫秒的时间字符串
     * @param date
     * @return
     */
    public static long parseMillsDateStrToTimestamp(String date) {
        LocalDateTime datetime = LocalDateTime.parse(date, MILLS_FORMAT_DATE);
        return datetime.toInstant(ZoneOffset.of("+8")).toEpochMilli();
    }
    public static long getTodayStartTimeStamp() {
        return LocalDateTime.of(LocalDate.now(), LocalTime.MIN).toEpochSecond(ZoneOffset.of("+8"))*1000;
    }

    public static long getTodayEndTimeStamp() {
        return LocalDateTime.of(LocalDate.now(), LocalTime.MAX).toEpochSecond(ZoneOffset.of("+8"))*1000;
    }

    public static long get30AgoStartTime() {
        return LocalDateTime.of(LocalDate.now().minusDays(30), LocalTime.MIN).toEpochSecond(ZoneOffset.of("+8"))*1000;
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
     * @param beginTime 开始时间戳，毫秒
     * @param endTime 结束时间戳，毫秒
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
     * @param beginTime 开始时间戳，毫秒
     * @param endTime 结束时间戳，毫秒
     * @return
     */
    public static long diffDay(long beginTime, long endTime) {
        long days = TimeUnit.MILLISECONDS.toDays(endTime - beginTime) + 1;
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

}
