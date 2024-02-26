package com.xiliulou.electricity.dto.merchant;

import lombok.Data;

/**
 * @ClassName : MerchantPlaceCabinetBindDTO
 * @Description :
 * @Author : zhangyongbo
 * @since: 2024-02-23
 */
@Data
public class MerchantPlaceCabinetBindDTO {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 场地id
     */
    private Long placeId;
    
    /**
     * 柜机id
     */
    private Long cabinetId;
    
    /**
     * 绑定时间
     */
    private Long bindTime;
    
    /**
     * 是否需要修改月结标记
     */
    private Boolean isNeedMonthSettle;
    
    /**
     * 解绑时间
     */
    private Long unBindTime;
    
    /**
     * 状态(0-绑定，1-解绑)
     */
    private Integer status;
    
    /**
     * 场地费补日结算标记(0：需要补的  1：不需要补)
     */
    private Integer placeDailySettlement;
    
    /**
     * 场地费月结算标记(0-否，1-是)
     */
    private Integer placeMonthSettlement;
    
    /**
     * 场地费月结算详情(为json数组记录具体的年月)
     */
    private String placeMonthSettlementDetail;
    
    /**
     * 电费月结算标记(0-否，1-是)
     */
    private Integer monthSettlement;
    
    /**
     * 电费月结算详情(为json数组记录具体的年月)
     */
    private Integer monthSettlementDetail;
    
    /**
     * 租户Id
     */
    private Integer tenantId;
    
    /**
     * 删除标记(0-未删除，1-已删除)
     */
    private Byte delFlag;
    
    /**
     * 创建时间
     */
    private Long createTime;
    
    /**
     * 更新时间
     */
    private Long updateTime;
    
}
