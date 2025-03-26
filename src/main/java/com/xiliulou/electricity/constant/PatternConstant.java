package com.xiliulou.electricity.constant;


import java.util.regex.Pattern;

/**
 * @author : renhang
 * @description PatternConstant
 * @date : 2025-03-26 13:47
 **/
public class PatternConstant {

    public static final Pattern BATTERY_PATTERN = Pattern.compile("^B_\\\\d{2}V");
}
