package com.xiliulou.electricity.vo.failureAlarm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author maxiaodong
 * @date 2023/12/29 9:14
 * @desc
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EleHardwareFailureWarnMsgVo {
    /**
     * 租户Id
     */
    private Integer tenantId;
    
    /**
     * 柜子id
     */
    private Integer cabinetId;
    
    /**
     * 类型 0-告警  1-故障
     */
    private Integer type;
    
    /**
     * 故障告警次数
     */
    private Integer failureWarnNum;
}
