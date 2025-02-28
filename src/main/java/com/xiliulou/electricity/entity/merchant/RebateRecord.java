package com.xiliulou.electricity.entity.merchant;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * (RebateRecord)实体类
 *
 * @author Eclair
 * @since 2024-02-20 14:31:51
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_rebate_record")
public class RebateRecord {
    
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
    
    /**
     * 订单类型 0:新租，1:续费
     */
    private Integer orderType;
    
    /**
     * 是否退租 0:否，1:是
     */
    private Integer refundFlag;
    
    /**
     * 加盟商id
     */
    private Long franchiseeId;
    
    /**
     * 商户等级
     */
    private String level;
    
    /**
     * 商户id
     */
    private Long merchantId;
    
    private Long merchantUid;
    
    /**
     * 结算状态（0-未结算,1-已结算,2-已退回,3-已失效）
     */
    private Integer status;
    
    /**
     * 渠道员
     */
    private Long channeler;
    
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
    
    /**
     * 商户场地员工
     */
    private Long placeUid;
    
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
    
    /**
     * 创建日期 yyyy-MM-dd
     */
    private String monthDate;

    /**
     * 消息id
     */
    private String messageId;
}
