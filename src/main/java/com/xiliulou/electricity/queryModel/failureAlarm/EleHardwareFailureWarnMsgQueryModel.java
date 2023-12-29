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
public class EleHardwareFailureWarnMsgQueryModel {
    private Long startTime;
    private Long endTime;
    private Long time;
}
