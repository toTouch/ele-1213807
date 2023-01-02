package com.xiliulou.electricity.web.query.jt808;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * @author : eclair
 * @date : 2022/12/28 14:15
 */
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
public class Jt808DeviceControlRequest extends Jt808CommonRequest {
    
    private String devId;
    
    /**
     * 0--解锁 1--加锁
     */
    private Integer lockType;
    
    
    public Jt808DeviceControlRequest(String requestId, String devId, Integer lockType) {
        super(requestId);
        this.devId = devId;
        this.lockType = lockType;
    }
    
    
}
