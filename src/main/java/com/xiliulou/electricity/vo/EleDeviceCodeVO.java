package com.xiliulou.electricity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EleDeviceCodeVO {
    private Long id;
    
    private String productKey;
    
    private String deviceName;
    
    /**
     * 密钥
     */
    private String secret;
    
    /**
     * 在线状态（0--在线，1--离线）
     */
    private Integer onlineStatus;
    
    private String remark;
    
    private Long createTime;
    
    private Long updateTime;
}
