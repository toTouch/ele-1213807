package com.xiliulou.electricity.vo.failureAlarm;

import lombok.Data;

/**
 * @author maxiaodong
 * @date 2023/12/29 9:14
 * @desc
 */
@Data
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
