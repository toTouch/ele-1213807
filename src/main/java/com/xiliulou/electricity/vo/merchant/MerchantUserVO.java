package com.xiliulou.electricity.vo.merchant;

import lombok.Data;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2024-02-28-11:32
 */
@Data
public class MerchantUserVO {
    private Long uid;
   
    private String phone;
   
    private String avatar;
    
    private String name;
    
    private Integer gender;
    
    private Integer tenantId;
    /**
     * 0--超级管理员 1--运营商  11--微信小程序用户  12--商户，13--渠道员
     */
    private Integer userType;
    /**
     * 商户等级
     */
    private String merchantLevel;
    private String merchantLevelName;
    
    private Long merchantId;
    private Long merchantUid;
    private Integer type;
    private String code;
}
