package com.xiliulou.electricity.utils;

import java.util.Calendar;
import java.util.Date;

/**
 * @ClassName : DateUtils
 * @Description : 关于时间的工具类
 * @Author : HRP
 * @Date: 2022-07-12 13:55
 */
public class DateUtils {

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
    public static long getTodayStartTime(){
        Calendar calendar=Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        return calendar.getTime().getTime();
    }
}
