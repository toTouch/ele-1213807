package com.xiliulou.electricity.request.failureAlarm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author maxiaodong
 * @date 2024/1/2 9:23
 * @desc 柜机故障告警请求
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class FailureWarnCabinetMsgPageRequest {
    /**
     * 类型：0-告警，1-故障
     */
    private Integer type;
    
    /**
     * 告警/告警开始时间
     */
    private Long alarmStartTime;
    
    /**
     * 告警/告警结束时间
     */
    private Long alarmEndTime;
    
    /**
     * 运作状态(0-启用， 1-禁用)
     */
    private Integer status;
    
    private Long size;
    
    private Long offset;
}
