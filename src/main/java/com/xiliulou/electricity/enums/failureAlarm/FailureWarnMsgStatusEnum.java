package com.xiliulou.electricity.enums.failureAlarm;

import com.xiliulou.electricity.enums.basic.BasicEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author zhangzhe
 * @date 2023/11/16 13:42
 * @Description:
 **/
@Getter
@AllArgsConstructor
public enum FailureWarnMsgStatusEnum implements BasicEnum<Integer, String> {
    
    /**
     * 告警中
     */
    FAILURE_WARN_MSG_STATUS_WARN(1, "告警中"),
    
    /**
     * 已恢复
     */
    FAILURE_WARN_MSG_STATUS_RECOVER(0, "已恢复");
    
    
    private final Integer code;
    
    private final String desc;
}
