package com.xiliulou.electricity.vo.userinfo;

import lombok.Data;

/**
 * @author zgw
 * @date 2023/2/13 15:27
 * @mood
 */
@Data
public class UserBasicInfoCarProVO {
    
    private Long uid;
    
    private String name;
    
    private String phone;
    
    private Long franchiseeId;
    
    private Long storeId;
    
    private Integer modelType;
    
    /**
     * 是否可解绑微信 0：不可解绑 1：可解绑
     */
    private Integer bindWX;
    
    /**
     * 是否可解绑微信 0：不可解绑 1：可解绑
     */
    private Integer bindAlipay;
    
}
