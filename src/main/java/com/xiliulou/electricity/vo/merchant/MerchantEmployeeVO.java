package com.xiliulou.electricity.vo.merchant;

import lombok.Data;

/**
 * @author BaoYu
 * @description:
 * @date 2024/2/19 8:48
 */

@Data
public class MerchantEmployeeVO {
    
    private Long id;
    
    private Long uid;
    
    private String name;
    
    private String phone;
    
    private Integer status;
    
    private Long merchantUid;
    
    private String merchantName;
    
    private Long placeId;
    
    private String placeName;
    
    private Long tenantId;
    
    private Integer delFlag;
    
    private String remark;
    
    private Long createTime;
    
    private Long updateTime;
    
}
