package com.xiliulou.electricity.utils;

import com.xiliulou.electricity.constant.ExchangeFailReaonConstants;

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
    
    public static final String EMPTY_CELL_HAVE_BATTERY = "检测空仓有电池";
    
    public final static String BATTERY_TYPE_NOT_NORMAL = "放入的电池型号不匹配";
    
    public final static String USER_BATTERY_NAME_NOT_NORMAL = "用户电池名字不匹配";
    
    public final static String CELL_EXCHANGE_ALWAYS_CLOSE = "仓门换电过程检测一直未关闭";
    
    
    public static Integer judgeNewOrOldCell(Integer newCell, Integer oldCell, String msg) {
        if (msg.contains(OPEN_CELL_FAIL)) {
            // 定义匹配数字的正则表达式
            Pattern pattern = Pattern.compile(NUMBER);
            Matcher matcher = pattern.matcher(msg);
            
            Integer cell = null;
            // 提取并打印匹配到的数字
            if (matcher.find()) {
                cell = Integer.valueOf(matcher.group());
            }
            if (Objects.isNull(cell)) {
                return null;
            }
            
            return Objects.equals(cell, newCell) ? ExchangeFailReaonConstants.NEW_CELL : ExchangeFailReaonConstants.OLD_CELL;
        } else if (msg.contains(BATTERY_CHECK_FAIL_TIME) || msg.contains(EMPTY_CELL_HAVE_BATTERY) || msg.contains(BATTERY_TYPE_NOT_NORMAL) || msg.contains(
                USER_BATTERY_NAME_NOT_NORMAL) || msg.contains(CELL_EXCHANGE_ALWAYS_CLOSE)) {
            return ExchangeFailReaonConstants.NEW_CELL;
        }
        
        return null;
    }
}
