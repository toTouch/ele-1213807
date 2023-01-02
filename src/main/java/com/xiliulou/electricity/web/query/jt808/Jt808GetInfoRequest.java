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
public class Jt808GetInfoRequest extends Jt808CommonRequest {
    
    private String devId;
    
    public Jt808GetInfoRequest(String requestId, String devId) {
        super(requestId);
        this.devId = devId;
    }
    
    
}
