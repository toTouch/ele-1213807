package com.xiliulou.electricity.vo.merchant;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName: RebateRecordVO
 * @description: 返利记录pageVo
 * @author: renhang
 * @create: 2024-03-26 09:47
 */
@Data
public class RebateRecordVO {
    private Long id;
    
    /**
     * 用户Id
     */
    private Long uid;
    
    /**
     * 用户名称
     */
    private String name;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 订单Id
     */
    private String orderId;
    
    /**
     * 原始订单Id
     */
    private String originalOrderId;
    
    /**
     * 套餐id
     */
    private Long memberCardId;
    private String memberCardName;
    
    /**
     * 返利类型
     */
    private Integer type;
    
    private Integer orderType;
    
    /**
     * 加盟商id
     */
    private Long franchiseeId;
    private String franchiseeName;
    
    /**
     * 商户等级
     */
    private String level;
    
    private Long merchantId;
    private String merchantName;
    
    /**
     * 结算状态（0-未结算,1-已结算,2-已退回,3-已失效）
     */
    private Integer status;
    
    /**
     * 渠道员
     */
    private Long channeler;
    private String channelerName;
    
    /**
     * 渠道员返现
     */
    private BigDecimal channelerRebate;
    
    /**
     * 商户返现
     */
    private BigDecimal merchantRebate;
    
    /**
     * 商户场地id
     */
    private Long placeId;
    private String placeName;
    
    /**
     * 商户场地员工
     */
    private Long placeUid;
    private String placeUserName;
    
    /**
     * 返现时间
     */
    private Long rebateTime;
    
    /**
     * 结算时间
     */
    private Long settleTime;
    
    private Integer tenantId;
    
    /**
     * 创建时间
     */
    private Long createTime;
    
    /**
     * 更新时间
     */
    private Long updateTime;
}
