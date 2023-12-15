package com.xiliulou.electricity.enums.failureAlarm;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 保护措施
 * @author maxiaodong
 * @since 2023-12-15 14:06:24
 * 保护措施(1- 上报， 2- 发出告警，3- 喇叭长鸣，4 -蜂鸣器响，5 -启动灭火装置，6-关闭电源、切断交流，
 * 7-切断交流，8-切断电源输出，9-切断输出，10，-启动加热，11-启动排风，12-启动充电，13-停止充电，14-停止放电，15-锁闭电池仓，】
 * 16-通信诊断后，可恢复解除原告警信息，17-仓门关闭且通信诊断后，可恢复解除原告警信息，18-禁止充电)
 */

@Getter
@AllArgsConstructor
public enum ProtectMeasureEnum {
    PROTECT_MEASURE_UP(1, "上报"),
    PROTECT_MEASURE_WARING(2, "发出告警"),
    PROTECT_MEASURE_BUGLE(3, "喇叭长鸣"),
    PROTECT_MEASURE_BEE(4, "蜂鸣器响"),
    PROTECT_MEASURE_FIRE(5, "启动灭火装置"),
    PROTECT_MEASURE_POWER(6, "关闭电源、切断交流"),
    PROTECT_MEASURE_CUT_CONNECT(7, "切断交流"),
    PROTECT_MEASURE_CUT_POWER(8, "切断电源输出"),
    PROTECT_MEASURE_CUT_OUTPUT(9, "切断输出"),
    PROTECT_MEASURE_START_WARM(10, "启动加热"),
    PROTECT_MEASURE_START_WIND(11, "启动排风"),
    PROTECT_MEASURE_START_CHARGE(12, "启动充电"),
    PROTECT_MEASURE_STOP_CHARGE(13, "停止充电"),
    PROTECT_MEASURE_stop_ELECTRICITY(14, "停止放电"),
    PROTECT_MEASURE_SHUT_BATTERY(15, "锁闭电池仓"),
    PROTECT_MEASURE_CONNECT_DIAGNOSE(16, "通信诊断后，可恢复解除原告警信息"),
    PROTECT_MEASURE_CELL_SHUT(17, "仓门关闭且通信诊断后，可恢复解除原告警信息"),
    PROTECT_MEASURE_FORBID_CHARGE(18, "禁止充电");
    
    private final Integer code;
    
    private final String desc;
    
    public static String getDescByCodeList(List<Integer> codeList) {
        List<String> descList = Arrays.stream(values()).filter(item -> codeList.contains(item.getCode())).map(ProtectMeasureEnum::getDesc).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(descList)) {
            return "";
        }
        
        String desc = StringUtils.join(descList, ",");
        return desc;
        
    }
}
