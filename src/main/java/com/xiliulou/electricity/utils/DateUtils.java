package com.xiliulou.electricity.utils;

import cn.hutool.core.date.DateUtil;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

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
}
