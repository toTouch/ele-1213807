package com.xiliulou.electricity.constant;

import com.google.common.collect.Maps;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * app故障类型
 *
 * @author zzString
 * @since 2022-11-03 15:59:06
 */
public class AppMalfunctionConstant {
    
    /**
     * 柜机硬件异常
     */
    private static final Map<String, String> ELE_HARDWARE_ABNORMAL_MAPS = Maps.newHashMap();
    
    /**
     * 格挡硬件异常
     */
    private static final Map<String, String> CELL_HARDWARE_ABNORMAL_MAPS = Maps.newHashMap();
    
    /**
     * 业务异常
     */
    private static final Map<String, String> BUSINESS_ABNORMAL_MAPS = Maps.newHashMap();
    
    /**
     * 操作类型
     */
    private static final Map<String, String> OPERATE_TYPE_MAPS = Maps.newHashMap();
    
    //=================================柜机硬件=====================================
    
    /**
     * 水浸故障
     */
    private static final String WATER_OUT_CODE = "80001";
    
    private static final String WATER_OUT_MESSAGE = "水浸故障";
    
    /**
     * 消防故障
     */
    private static final String FIRE_WARN_CODE = "80002";
    
    private static final String FIRE_WARN_MESSAGE = "消防故障";
    
    /**
     * 过温故障
     */
    private static final String OVER_TEMPERATURE_CODE = "80003";
    
    private static final String OVER_TEMPERATURE_MESSAGE = "过温故障";
    
    /**
     * 烟雾告警
     */
    private static final String SMOKE_WARN_CODE = "80004";
    
    private static final String SMOKE_WARN_MESSAGE = "烟雾告警";
    
    /**
     * 硬件控制故障
     */
    private static final String HARDWARE_WARN_CODE = "80005";
    
    private static final String HARDWARE_WARN_MESSAGE = "硬件控制故障";
    
    /**
     * 水泵开启
     */
    private static final String WATER_PUMP_CODE = "80006";
    
    private static final String WATER_PUMP_MESSAGE = "水泵开启";
    
    /**
     * 水位告警开启
     */
    private static final String WATER_WARN_CODE = "80007";
    
    private static final String WATER_WARN_MESSAGE = "水位告警开启";
    
    /**
     * 后门开启警告
     */
    private static final String BACK_DOOR_CODE = "80008";
    
    private static final String BACK_DOOR_MESSAGE = "后门开启警告";
    
    /**
     * 开核心风扇命令收不到包
     */
    private static final String OPEN_CORE_FAN_PACKAGE_CODE = "9";
    
    private static final String OPEN_CORE_FAN_PACKAGE_MESSAGE = "开核心风扇命令收不到包";
    
    /**
     * 关核心风扇命令收不到包
     */
    private static final String CLOSE_CORE_FAN_PACKAGE_CODE = "10";
    
    private static final String CLOSE_CORE_FAN_PACKAGE_MESSAGE = "关核心风扇命令收不到包";
    
    /**
     * 开核心灯命令收不到包
     */
    private static final String OPEN_CORE_LIGHT_PACKAGE_CODE = "11";
    
    private static final String OPEN_CORE_LIGHT_PACKAGE_MESSAGE = "开核心灯命令收不到包";
    
    /**
     * 关核心灯命令收不到包
     */
    private static final String CLOSE_CORE_LIGHT_PACKAGE_CODE = "12";
    
    private static final String CLOSE_CORE_LIGHT_PACKAGE_MESSAGE = "关核心灯命令收不到包";
    
    /**
     * 读核心板收不到包
     */
    private static final String READ_CORE_LOSE_PACKAGE_CODE = "13";
    
    private static final String READ_CORE_LOSE_PACKAGE_MESSAGE = "读核心板收不到包";
    
    /**
     * 打开水泵命令收不到包
     */
    private static final String OPEN_PUMP_LOSE_PACKAGE_CODE = "27";
    
    private static final String OPEN_PUMP_LOSE_PACKAGE_MESSAGE = "打开水泵命令收不到包";
    
    /**
     * 关闭水泵命令收不到包
     */
    private static final String CLOSE_PUMP_LOSE_PACKAGE_CODE = "28";
    
    private static final String CLOSE_PUMP_LOSE_PACKAGE_MESSAGE = "关闭水泵命令收不到包";
    
    /**
     * 读取新核心板命令收不到包
     */
    private static final String READ_CORE_COMMAND_LOSE_PACKAGE_CODE = "29";
    
    private static final String READ_CORE_COMMAND_LOSE_PACKAGE_MESSAGE = "读取新核心板命令收不到包";
    
    /**
     * 读取核心板硬件版本命令收不到包
     */
    private static final String READ_CORE_HARDWARE_LOSE_PACKAGE_CODE = "22";
    
    private static final String READ_CORE_HARDWARE_LOSE_PACKAGE_MESSAGE = "读取核心板硬件版本命令收不到包";
    
    /**
     * 读柜机耗电量收不到包
     */
    private static final String READ_POWER_LOSE_PACKAGE_CODE = "17";
    
    private static final String READ_POWER_LOSE_PACKAGE_MESSAGE = "读柜机耗电量收不到包";
    
    //=================================格挡硬件=====================================
    
    /**
     * 默认的串口收不到包，可以忽略
     */
    private static final String SERIAL_PORT_LOSE_PACKAGE_CODE = "-1";
    
    private static final String SERIAL_PORT_LOSE_PACKAGE_MESSAGE = "默认的串口收不到包，可以忽略";
    
    /**
     * 开门命令收不到包
     */
    private static final String OPEN_DOOR_LOSE_PACKAGE_CODE = "1";
    
    private static final String OPEN_DOOR_LOSE_PACKAGE_MESSAGE = "开门命令收不到包";
    
    /**
     * 开指示灯命令收不到包
     */
    private static final String OPEN_INDICATOR_LOSE_PACKAGE_CODE = "2";
    
    private static final String OPEN_INDICATOR_LOSE_PACKAGE_MESSAGE = "开指示灯命令收不到包";
    
    /**
     * 关指示灯命令收不到包
     */
    private static final String CLOSE_INDICATOR_LOSE_PACKAGE_CODE = "3";
    
    private static final String CLOSE_INDICATOR_LOSE_PACKAGE_MESSAGE = "关指示灯命令收不到包";
    
    /**
     * 开加热命令收不到包
     */
    private static final String OPEN_HEATING_LOSE_PACKAGE_CODE = "4";
    
    private static final String OPEN_HEATING_LOSE_PACKAGE_MESSAGE = "开加热命令收不到包";
    
    /**
     * 关加热命令收不到包
     */
    private static final String CLOSE_HEATING_LOSE_PACKAGE_CODE = "5";
    
    private static final String CLOSE_HEATING_LOSE_PACKAGE_MESSAGE = "关加热命令收不到包";
    
    /**
     * 开子风扇命令收不到包
     */
    private static final String OPEN_FAN_LOSE_PACKAGE_CODE = "6";
    
    private static final String OPEN_FAN_LOSE_PACKAGE_MESSAGE = "开子风扇命令收不到包";
    
    /**
     * 关子风扇命令收不到包
     */
    private static final String CLOSE_FAN_LOSE_PACKAGE_CODE = "7";
    
    private static final String CLOSE_FAN_LOSE_PACKAGE_MESSAGE = "关子风扇命令收不到包";
    
    /**
     * 读格挡命令收不到包
     */
    private static final String READ_CELL_LOSE_PACKAGE_CODE = "8";
    
    private static final String READ_CELL_LOSE_PACKAGE_MESSAGE = "读格挡命令收不到包";
    
    /**
     * 读电池信息收不到包
     */
    private static final String READ_CHARGE_LOSE_PACKAGE_CODE = "14";
    
    private static final String READ_CHARGE_LOSE_PACKAGE_MESSAGE = "读电池信息收不到包";
    
    /**
     * 开充电器收不到包
     */
    private static final String OPEN_CHARGE_LOSE_PACKAGE_CODE = "15";
    
    private static final String OPEN_CHARGE_LOSE_PACKAGE_MESSAGE = "开充电器收不到包";
    
    /**
     * 设置充电器电压和电流收不到包
     */
    private static final String SET_CHARGE_SUMV_CODE = "16";
    
    private static final String SET_CHARGE_SUMV_MESSAGE = "设置充电器电压和电流收不到包";
    
    /**
     * ping充电器命令收不到包
     */
    private static final String PING_CHARGE_CODE = "18";
    
    private static final String PING_CHARGE_MESSAGE = "ping充电器命令收不到包";
    
    /**
     * 读按距离收费的电池命令收不到包
     */
    private static final String READ_DISATANCE_FEE_CODE = "19";
    
    private static final String READ_DISATANCE_FEE_MESSAGE = "读按距离收费的电池命令收不到包";
    
    /**
     * 设置按距离收费的电池命令收不到包
     */
    private static final String SET_DISATANCE_FEE_CODE = "20";
    
    private static final String SET_DISATANCE_FEE_MESSAGE = "设置按距离收费的电池命令收不到包";
    
    /**
     * 清空电池按距离收费命令收不到包
     */
    private static final String CLEAR_DISATANCE_FEE_CODE = "21";
    
    private static final String CLEAR_DISATANCE_FEE_MESSAGE = "清空电池按距离收费命令收不到包";
    
    /**
     * 开电磁阀命令收不到包
     */
    private static final String OPEN_SOLENOID_LOSE_PACKAGE_CODE = "23";
    
    private static final String OPEN_SOLENOID_LOSE_PACKAGE_MESSAGE = "开电磁阀命令收不到包";
    
    /**
     * 关电磁阀命令收不到包
     */
    private static final String CLOSE_SOLENOID_LOSE_PACKAGE_CODE = "24";
    
    private static final String CLOSE_SOLENOID_LOSE_PACKAGE_MESSAGE = "关电磁阀命令收不到包";
    
    /**
     * 开舱内的灯命令收不到包
     */
    private static final String OPEN_LIGHT_LOSE_PACKAGE_CODE = "25";
    
    private static final String OPEN_LIGHT_LOSE_PACKAGE_MESSAGE = "开舱内的灯命令收不到包";
    
    /**
     * 关舱内的灯命令收不到包
     */
    private static final String CLOSE_LIGHT_LOSE_PACKAGE_CODE = "26";
    
    private static final String CLOSE_LIGHT_LOSE_PACKAGE_MESSAGE = "关舱内的灯命令收不到包";
    
    /**
     * ota通用命令收不到包
     */
    private static final String OTA_LOSE_PACKAGE_CODE = "30";
    
    private static final String OTA_LOSE_PACKAGE_MESSAGE = "ota通用命令收不到包";
    
    /**
     * ota更新命令收不到包
     */
    private static final String OTA_UPDATE_LOSE_PACKAGE_CODE = "31";
    
    private static final String OTA_UPDATE_LOSE_PACKAGE_MESSAGE = "ota更新命令收不到包";
    
    /**
     * 关充电器收不到包
     */
    private static final String CLOSE_CHARGE_CODE = "32";
    
    private static final String CLOSE_CHARGE_MESSAGE = "关充电器收不到包";
    
    /**
     * 读取子板硬件收不到包
     */
    private static final String SON_LOSE_PACKAGE_CODE = "33";
    
    private static final String SON_LOSE_PACKAGE_MESSAGE = "读取子板硬件收不到包";
    
    /**
     * 硬件操作失败(开门，开灯，开充电器)
     */
    private static final String HARDWARE_OPERATION_FAILURE_CODE = "70001";
    
    private static final String HARDWARE_OPERATION_FAILURE_MESSAGE = "硬件操作失败(开门，开灯，开充电器)";
    
    /**
     * 过温故障
     */
    private static final String HYPERTHERMIA_CODE = "70002";
    
    private static final String HYPERTHERMIA_MESSAGE = "过温故障  ";
    
    //=================================业务异常=====================================
    
    /**
     * 电池在柜机中异常消失，并且超过10分钟也没有恢复
     */
    private static final String BATTERY_ABNORMAL_VANISH_CODE = "60001";
    
    private static final String BATTERY_ABNORMAL_VANISH_MESSAGE = "电池在柜机中异常消失，并且超过10分钟也没有恢复";
    
    /**
     * 仓门异常自动加锁：电池不见了
     */
    private static final String CELL_ABNORMAL_AUTO_LOCK_CODE = "60003";
    
    private static final String CELL_ABNORMAL_AUTO_LOCK_MESSAGE = "仓门异常自动加锁：电池不见了";
    
    /**
     * 仓门异常自动解锁：电池突然又可以被读
     */
    private static final String CELL_ABNORMAL_UNLOCK_CODE = "60004";
    
    private static final String cELL_ABNORMAL_UNLOCK_MESSAGE = "仓门异常自动解锁：电池突然又可以被读";
    
    /**
     * 仓门异常锁定：电池出现异常
     */
    private static final String CELL_ABNORMAL_LOCK_CODE = "60006";
    
    private static final String CELL_ABNORMAL_LOCK_MESSAGE = "仓门异常锁定：电池出现异常";
    
    /**
     * 电池突然被调包
     */
    private static final String BATTERY_ABNORMAL_EXCHANGE_CODE = "60005";
    
    private static final String BATTERY_ABNORMAL_EXCHANGE_MESSAGE = "电池突然被调包";
    
    /**
     * 电池异常消失
     */
    private static final String BATTERY_ABNORMAL_DISAPPEAR_CODE = "60007";
    
    private static final String BATTERY_ABNORMAL_DISAPPEAR_MESSAGE = "电池异常消失";
    
    /**
     * 电池异常出现
     */
    private static final String BATTERY_ABNORMAL_APPEAR_CODE = "60008";
    
    private static final String BATTERY_ABNORMAL_APPEAR_MESSAGE = "电池异常出现";
    
    //=================================操作类型=====================================
    private static final String NON_OPERATE_code = "0";
    
    private static final String NON_OPERATE_message = "无";
    
    private static final String OPERATE_CELL_code = "1";
    
    private static final String OPERATE_CELL_message = "开门";
    
    private static final String OPERATE_OPEN_CHARGE_code = "2";
    
    private static final String OPERATE_OPEN_CHARGE_message = "开充电器";
    
    private static final String OPERATE_CLOSE_CHARGE_code = "3";
    
    private static final String OPERATE_CLOSE_CHARGE_message = "关充电器";
    
    private static final String OPERATE_SET_CHARGE_code = "4";
    
    private static final String OPERATE_SET_CHARGE_message = "设置充电电压";
    
    private static final String OPERATE_OPEN_LIGHT_code = "5";
    
    private static final String OPERATE_OPEN_LIGHT_message = "开灯";
    
    private static final String OPERATE_CLOSE_LIGHT_code = "6";
    
    private static final String OPERATE_CLOSE_LIGHT_message = "关灯";
    
    
    static {
        
        ELE_HARDWARE_ABNORMAL_MAPS.put(WATER_OUT_CODE, WATER_OUT_MESSAGE);
        ELE_HARDWARE_ABNORMAL_MAPS.put(FIRE_WARN_CODE, FIRE_WARN_MESSAGE);
        ELE_HARDWARE_ABNORMAL_MAPS.put(OVER_TEMPERATURE_CODE, OVER_TEMPERATURE_MESSAGE);
        ELE_HARDWARE_ABNORMAL_MAPS.put(SMOKE_WARN_CODE, SMOKE_WARN_MESSAGE);
        ELE_HARDWARE_ABNORMAL_MAPS.put(HARDWARE_WARN_CODE, HARDWARE_WARN_MESSAGE);
        ELE_HARDWARE_ABNORMAL_MAPS.put(WATER_PUMP_CODE, WATER_PUMP_MESSAGE);
        ELE_HARDWARE_ABNORMAL_MAPS.put(WATER_WARN_CODE, WATER_WARN_MESSAGE);
        ELE_HARDWARE_ABNORMAL_MAPS.put(BACK_DOOR_CODE, BACK_DOOR_MESSAGE);
        ELE_HARDWARE_ABNORMAL_MAPS.put(OPEN_CORE_FAN_PACKAGE_CODE, OPEN_CORE_FAN_PACKAGE_MESSAGE);
        ELE_HARDWARE_ABNORMAL_MAPS.put(CLOSE_CORE_FAN_PACKAGE_CODE, CLOSE_CORE_FAN_PACKAGE_MESSAGE);
        ELE_HARDWARE_ABNORMAL_MAPS.put(OPEN_CORE_LIGHT_PACKAGE_CODE, OPEN_CORE_LIGHT_PACKAGE_MESSAGE);
        ELE_HARDWARE_ABNORMAL_MAPS.put(CLOSE_CORE_LIGHT_PACKAGE_CODE, CLOSE_CORE_LIGHT_PACKAGE_MESSAGE);
        ELE_HARDWARE_ABNORMAL_MAPS.put(READ_CORE_LOSE_PACKAGE_CODE, READ_CORE_LOSE_PACKAGE_MESSAGE);
        ELE_HARDWARE_ABNORMAL_MAPS.put(OPEN_PUMP_LOSE_PACKAGE_CODE, OPEN_PUMP_LOSE_PACKAGE_MESSAGE);
        ELE_HARDWARE_ABNORMAL_MAPS.put(CLOSE_PUMP_LOSE_PACKAGE_CODE, CLOSE_PUMP_LOSE_PACKAGE_MESSAGE);
        ELE_HARDWARE_ABNORMAL_MAPS.put(READ_CORE_COMMAND_LOSE_PACKAGE_CODE, READ_CORE_COMMAND_LOSE_PACKAGE_MESSAGE);
        ELE_HARDWARE_ABNORMAL_MAPS.put(READ_CORE_COMMAND_LOSE_PACKAGE_CODE, READ_CORE_COMMAND_LOSE_PACKAGE_MESSAGE);
        ELE_HARDWARE_ABNORMAL_MAPS.put(READ_CORE_HARDWARE_LOSE_PACKAGE_CODE, READ_CORE_HARDWARE_LOSE_PACKAGE_MESSAGE);
        ELE_HARDWARE_ABNORMAL_MAPS.put(READ_POWER_LOSE_PACKAGE_CODE, READ_POWER_LOSE_PACKAGE_MESSAGE);
        
        CELL_HARDWARE_ABNORMAL_MAPS.put(SERIAL_PORT_LOSE_PACKAGE_CODE, SERIAL_PORT_LOSE_PACKAGE_MESSAGE);
        CELL_HARDWARE_ABNORMAL_MAPS.put(OPEN_DOOR_LOSE_PACKAGE_CODE, OPEN_DOOR_LOSE_PACKAGE_MESSAGE);
        CELL_HARDWARE_ABNORMAL_MAPS.put(OPEN_INDICATOR_LOSE_PACKAGE_CODE, OPEN_INDICATOR_LOSE_PACKAGE_MESSAGE);
        CELL_HARDWARE_ABNORMAL_MAPS.put(CLOSE_INDICATOR_LOSE_PACKAGE_CODE, CLOSE_INDICATOR_LOSE_PACKAGE_MESSAGE);
        CELL_HARDWARE_ABNORMAL_MAPS.put(OPEN_HEATING_LOSE_PACKAGE_CODE, OPEN_HEATING_LOSE_PACKAGE_MESSAGE);
        CELL_HARDWARE_ABNORMAL_MAPS.put(CLOSE_HEATING_LOSE_PACKAGE_CODE, CLOSE_HEATING_LOSE_PACKAGE_MESSAGE);
        CELL_HARDWARE_ABNORMAL_MAPS.put(OPEN_FAN_LOSE_PACKAGE_CODE, OPEN_FAN_LOSE_PACKAGE_MESSAGE);
        CELL_HARDWARE_ABNORMAL_MAPS.put(CLOSE_FAN_LOSE_PACKAGE_CODE, CLOSE_FAN_LOSE_PACKAGE_MESSAGE);
        CELL_HARDWARE_ABNORMAL_MAPS.put(READ_CELL_LOSE_PACKAGE_CODE, READ_CELL_LOSE_PACKAGE_MESSAGE);
        CELL_HARDWARE_ABNORMAL_MAPS.put(READ_CHARGE_LOSE_PACKAGE_CODE, READ_CHARGE_LOSE_PACKAGE_MESSAGE);
        CELL_HARDWARE_ABNORMAL_MAPS.put(OPEN_CHARGE_LOSE_PACKAGE_CODE, OPEN_CHARGE_LOSE_PACKAGE_MESSAGE);
        CELL_HARDWARE_ABNORMAL_MAPS.put(SET_CHARGE_SUMV_CODE, SET_CHARGE_SUMV_MESSAGE);
        CELL_HARDWARE_ABNORMAL_MAPS.put(PING_CHARGE_CODE, PING_CHARGE_MESSAGE);
        CELL_HARDWARE_ABNORMAL_MAPS.put(READ_DISATANCE_FEE_CODE, READ_DISATANCE_FEE_MESSAGE);
        CELL_HARDWARE_ABNORMAL_MAPS.put(SET_DISATANCE_FEE_CODE, SET_DISATANCE_FEE_MESSAGE);
        CELL_HARDWARE_ABNORMAL_MAPS.put(CLEAR_DISATANCE_FEE_CODE, CLEAR_DISATANCE_FEE_MESSAGE);
        CELL_HARDWARE_ABNORMAL_MAPS.put(OPEN_SOLENOID_LOSE_PACKAGE_CODE, OPEN_SOLENOID_LOSE_PACKAGE_MESSAGE);
        CELL_HARDWARE_ABNORMAL_MAPS.put(CLOSE_SOLENOID_LOSE_PACKAGE_CODE, CLOSE_SOLENOID_LOSE_PACKAGE_MESSAGE);
        CELL_HARDWARE_ABNORMAL_MAPS.put(OPEN_LIGHT_LOSE_PACKAGE_CODE, OPEN_LIGHT_LOSE_PACKAGE_MESSAGE);
        CELL_HARDWARE_ABNORMAL_MAPS.put(CLOSE_LIGHT_LOSE_PACKAGE_CODE, CLOSE_LIGHT_LOSE_PACKAGE_MESSAGE);
        CELL_HARDWARE_ABNORMAL_MAPS.put(OTA_LOSE_PACKAGE_CODE, OTA_LOSE_PACKAGE_MESSAGE);
        CELL_HARDWARE_ABNORMAL_MAPS.put(OTA_UPDATE_LOSE_PACKAGE_CODE, OTA_UPDATE_LOSE_PACKAGE_MESSAGE);
        CELL_HARDWARE_ABNORMAL_MAPS.put(CLOSE_CHARGE_CODE, CLOSE_CHARGE_MESSAGE);
        CELL_HARDWARE_ABNORMAL_MAPS.put(SON_LOSE_PACKAGE_CODE, SON_LOSE_PACKAGE_MESSAGE);
        CELL_HARDWARE_ABNORMAL_MAPS.put(HARDWARE_OPERATION_FAILURE_CODE, HARDWARE_OPERATION_FAILURE_MESSAGE);
        CELL_HARDWARE_ABNORMAL_MAPS.put(HYPERTHERMIA_CODE, HYPERTHERMIA_MESSAGE);
        
        BUSINESS_ABNORMAL_MAPS.put(BATTERY_ABNORMAL_VANISH_CODE, BATTERY_ABNORMAL_VANISH_MESSAGE);
        BUSINESS_ABNORMAL_MAPS.put(CELL_ABNORMAL_AUTO_LOCK_CODE, CELL_ABNORMAL_AUTO_LOCK_MESSAGE);
        BUSINESS_ABNORMAL_MAPS.put(CELL_ABNORMAL_UNLOCK_CODE, cELL_ABNORMAL_UNLOCK_MESSAGE);
        BUSINESS_ABNORMAL_MAPS.put(CELL_ABNORMAL_LOCK_CODE, CELL_ABNORMAL_LOCK_MESSAGE);
        BUSINESS_ABNORMAL_MAPS.put(BATTERY_ABNORMAL_EXCHANGE_CODE, BATTERY_ABNORMAL_EXCHANGE_MESSAGE);
        BUSINESS_ABNORMAL_MAPS.put(BATTERY_ABNORMAL_DISAPPEAR_CODE, BATTERY_ABNORMAL_DISAPPEAR_MESSAGE);
        BUSINESS_ABNORMAL_MAPS.put(BATTERY_ABNORMAL_APPEAR_CODE, BATTERY_ABNORMAL_APPEAR_MESSAGE);
        
        OPERATE_TYPE_MAPS.put(NON_OPERATE_code, NON_OPERATE_message);
        OPERATE_TYPE_MAPS.put(OPERATE_CELL_code, OPERATE_CELL_message);
        OPERATE_TYPE_MAPS.put(OPERATE_OPEN_CHARGE_code, OPERATE_OPEN_CHARGE_message);
        OPERATE_TYPE_MAPS.put(OPERATE_CLOSE_CHARGE_code, OPERATE_CLOSE_CHARGE_message);
        OPERATE_TYPE_MAPS.put(OPERATE_SET_CHARGE_code, OPERATE_SET_CHARGE_message);
        OPERATE_TYPE_MAPS.put(OPERATE_OPEN_LIGHT_code, OPERATE_OPEN_LIGHT_message);
        OPERATE_TYPE_MAPS.put(OPERATE_CLOSE_LIGHT_code, OPERATE_CLOSE_LIGHT_message);
    }
    
    /**
     * 获取柜机异常message
     *
     * @param errorCode
     * @return
     */
    public static String acquireEleHardwareAbnormal(Long errorCode) {
        if (Objects.isNull(errorCode)) {
            return "";
        }
        
        return ELE_HARDWARE_ABNORMAL_MAPS.getOrDefault(String.valueOf(errorCode), "");
    }
    
    /**
     * 获取格挡异常message
     *
     * @param errorCode
     * @return
     */
    public static String acquireCellHardwareAbnormal(Long errorCode) {
        if (Objects.isNull(errorCode)) {
            return "";
        }
        
        return CELL_HARDWARE_ABNORMAL_MAPS.getOrDefault(String.valueOf(errorCode), "");
    }
    
    /**
     * 获取业务异常message
     *
     * @param errorCode
     * @return
     */
    public static String acquireBusinessAbnormal(Long errorCode) {
        if (Objects.isNull(errorCode)) {
            return "";
        }
        
        return BUSINESS_ABNORMAL_MAPS.getOrDefault(String.valueOf(errorCode), "");
    }
    
    /**
     * 获取操作类型
     *
     * @param operateTypeCode
     * @return
     */
    public static String acquireOperateType(Integer operateTypeCode) {
        if (Objects.isNull(operateTypeCode)) {
            return "";
        }
        
        return BUSINESS_ABNORMAL_MAPS.getOrDefault(String.valueOf(operateTypeCode), "");
    }
    
    
}
