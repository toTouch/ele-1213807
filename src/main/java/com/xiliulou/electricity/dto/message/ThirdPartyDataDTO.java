package com.xiliulou.electricity.dto.message;


import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * @author HeYafeng
 * @description 推送给第三方消息DTO
 * @date 2024/9/19 15:23:09
 */
@Data
public class ThirdPartyDataDTO implements Serializable {
    
    private static final long serialVersionUID = -3461773846886373949L;
    
    private String traceId;
    
    private String type;
    
    private Integer tenantId;
    
    /**
     * 第三方渠道
     */
    private Integer channel;
    
    private Map<String, Object> context;
}
