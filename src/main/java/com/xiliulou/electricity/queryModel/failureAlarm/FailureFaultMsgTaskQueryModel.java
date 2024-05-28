package com.xiliulou.electricity.queryModel.failureAlarm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author maxiaodong
 * @date 2023/12/28 19:05
 * @desc
 */

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class FailureFaultMsgTaskQueryModel {
    
    /**
     * 告警开始时间
     */
    private String startTime;
    
    /**
     * 告警结束时间
     */
    private String endTime;
    
    /**
     * 当天的中午十二点
     */
    private Long time;
    
    /**
     * 限制故障告警的状态：启用
     */
    private Integer limitFailureAlarmStatus;
}
