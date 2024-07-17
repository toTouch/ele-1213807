/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/7/17
 */

package com.xiliulou.electricity.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author caobotao.cbt
 * @date 2024/7/17 14:03
 */
public class IdCardValidator {
    
    // 正则表达式用于校验身份证号码的基本格式
    private static final String IDCARD_PATTERN = "^(1[1-5]|2[1-3]|3[1-7]|4[1-6]|5[0-4]|6[1-5]|7[1-8]|8[1-3])\\d{4}((19|20)\\d{2})(0[1-9]|1[0-2])(0[1-9]|[12][0-9]|3[01])\\d{3}(\\d|X|x)$";
    
    
    
    
    /**
     * 校验身份证年龄是否满18周岁
     *
     * @param idCard 身份证号码
     * @return 如果年龄满18周岁且格式正确返回true，否则返回false
     */
    public static boolean isOver18(String idCard) {
        // 使用正则表达式校验身份证号码格式
        Pattern pattern = Pattern.compile(IDCARD_PATTERN);
        Matcher matcher = pattern.matcher(idCard);
        if (!matcher.matches()) {
            return false;
        }
        
        // 提取出生日期
        String birthStr = idCard.substring(6, 14);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        try {
            Date birthDate = sdf.parse(birthStr);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(birthDate);
            calendar.add(Calendar.YEAR, 18); // 加上18年
            
            // 获取当前日期
            Date currentDate = new Date();
            
            // 比较日期
            return currentDate.after(calendar.getTime());
            
        } catch (ParseException e) {
            // 日期格式解析失败（理论上这里不会失败，因为已经从正确的格式中提取）
            return false;
        }
    }
}
