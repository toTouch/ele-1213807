package com.xiliulou.electricity.vo.thirdPartyMall;

import lombok.Data;

/**
 * @author HeYafeng
 * @description 美团棋手商城配置信息VO
 * @date 2024/8/28 11:30:48
 */
@Data
public class MeiTuanRiderMallConfigVO {
    
    private Long id;
    
    /**
     * appId
     */
    private String appId;
    
    /**
     * appKey
     */
    private String appKey;
    
    /**
     * secret
     */
    private String secret;
    
    /**
     * 租户ID
     */
    private Integer tenantId;
}
