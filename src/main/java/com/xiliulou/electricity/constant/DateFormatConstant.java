package com.xiliulou.electricity.constant;

/**
 * @author maxiaodong
 * @date 2024/3/5 19:53
 * @desc 日期格式常量
 */
public interface DateFormatConstant {
    /**
     * 年月
     */
    String MONTH_DATE_FORMAT = "yyyy-MM";
    /**
     * 年月日
     */
    String MONTH_DAY_DATE_FORMAT = "yyyy-MM-dd";
    
    /**
     * 年月日 时分秒
     */
    String MONTH_DAY_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    
    /**
     * 两月前
     */
    Integer TOW_MONTH_BEFORE = 2;
    /**
     * 上个月
     */
    Integer LAST_MONTH = 1;
    
    /**
     * 上个月
     */
    Integer CURRENT_MONTH = 0;
}
