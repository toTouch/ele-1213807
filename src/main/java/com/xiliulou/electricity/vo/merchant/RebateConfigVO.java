package com.xiliulou.electricity.vo.merchant;

import lombok.Data;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2024-02-05-10:47
 */
@Data
public class RebateConfigVO {
    
    private Long id;
    
    /**
     * 商户等级
     */
    private String level;
    
    /**
     * 套餐Id
     */
    private Long mid;
    
    private String memberCardName;
    
    private Long franchiseeId;
    
    private String franchiseeName;
    
    /**
     * 渠道员拉新返现
     */
    private Double channelerInvitation;
    
    /**
     * 渠道员续费返现
     */
    private Double channelerRenewal;
    
    /**
     * 商户拉新返现
     */
    private Double merchantInvitation;
    
    /**
     * 商户续费返现
     */
    private Double merchantRenewal;
    
    /**
     * 状态 0:关闭,1:开启
     */
    private Integer status;
    
    private Long createTime;
    
    private Long updateTime;
}
