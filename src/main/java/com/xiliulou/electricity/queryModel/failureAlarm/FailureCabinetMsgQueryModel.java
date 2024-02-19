package com.xiliulou.electricity.queryModel.failureAlarm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author maxiaodong
 * @date 2024/1/5 11:06
 * @desc
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class FailureCabinetMsgQueryModel {
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
    
    private Long size;
    
    private Long offset;
}
