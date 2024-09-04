package com.xiliulou.electricity.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2024-08-14-17:46
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CabinetCommandRequest {
    
    private String productKey;
    
    private String deviceName;
    
    private String sessionId;
    
    private String type;
    
    private Map<String, Object> content;
}
