package com.xiliulou.electricity.utils;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @ClassName: ExchangeFailCellUtil
 * @description:
 * @author: renhang
 * @create: 2024-07-19 15:25
 */
public class ExchangeFailCellUtil {
    
    public static final String NUMBER = "\\d+\\.?\\d*";
    
    public static final String OPEN_CELL_FAIL = "开门失败";
    
    public static final String BATTERY_CHECK_FAIL_TIME = "电池检测失败或者超时";
    
    public static final String CABINET_IN_USEING = "换电柜正在使用中操作取消";
    
    
    public static Boolean judgeOpenFailIsNewCell(Integer cell, String msg) {
        // 定义匹配数字的正则表达式
        Pattern pattern = Pattern.compile(NUMBER);
        Matcher matcher = pattern.matcher(msg);
        
        Integer msgCell = null;
        // 提取并打印匹配到的数字
        if (matcher.find()) {
            msgCell = Integer.valueOf(matcher.group());
        }
        return Objects.equals(msgCell, cell);
    }
}
