package com.xiliulou.electricity.enums.failureAlarm;

import com.xiliulou.electricity.enums.basic.BasicEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author maxiaodong
 * @date 2024/11/12 13:42
 * @Description:
 **/
@Getter
@AllArgsConstructor
public enum WarnHandleStatusEnum implements BasicEnum<Integer, String> {
    
   
    HANDLE_PROCESSED(1, "处理中"),
    
    HANDLE_FINISHED(2, "已处理"),
    
    HANDLE_IGNORE(3, "忽略"),
    
    HANDLE_WORK(4, "转工单");
    
    
    private final Integer code;
    
    private final String desc;
}
