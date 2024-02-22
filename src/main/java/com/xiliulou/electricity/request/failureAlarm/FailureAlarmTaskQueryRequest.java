package com.xiliulou.electricity.request.failureAlarm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author maxiaodong
 * @date 2024/1/2 9:51
 * @desc
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class FailureAlarmTaskQueryRequest {
    
    /**
     * 告警开始时间
     */
    private Long startTime;
    
    /**
     * 告警结束时间
     */
    private Long endTime;
    
    /**
     * 当天的中午十二点
     */
    private Long time;
}
