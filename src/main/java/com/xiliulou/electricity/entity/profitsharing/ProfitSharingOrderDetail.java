package com.xiliulou.electricity.entity.profitsharing;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 分账订单明细表(ProfitSharingOrderDetail)实体类
 *
 * @author maxiaodong
 * @since 2024-08-22 17:00:34
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_profit_sharing_order_detail")
public class ProfitSharingOrderDetail implements Serializable {
    
    private static final long serialVersionUID = -25124564943378708L;
    
    private Long id;
    
    /**
     * 分账订单表Id
     */
    private Long profitSharingOrderId;
    
    
    /**
     * 第三方支付订单号
     */
    private String thirdTradeOrderNo;
    
    /**
     * 分账明细单号
     */
    private String orderDetailNo;
    
    /**
     * 第三方分账明细单号
     */
    private String thirdOrderDetailNo;
    
    /**
     * 分账接收方
     */
    private String profitSharingReceiveAccount;
    
    /**
     * 分账比例
     */
    private BigDecimal scale;
    
    /**
     * 分账金额
     */
    private BigDecimal profitSharingAmount;
    
    /**
     * 状态：0：已受理、1：处理中、2：分账完成，3：分账失败
     */
    private Integer status;
    
    /**
     * 失败原因
     */
    private String failReason;
    
    /**
     * 分账完成时间
     */
    private Long finishTime;
    
    /**
     * 解冻状态：0：解冻待处理、1：无需解冻，2：解冻中，3：解冻失败，4：解冻成功，5：已失效
     */
    private Integer unfreezeStatus;
    
    /**
     * 租户id
     */
    private Integer tenantId;
    
    /**
     * 加盟商id
     */
    private Long franchiseeId;
    
    /**
     * 删除标识：0-未删除 1-已删除
     */
    private Integer delFlag;
    
    /**
     * 创建时间
     */
    private Long createTime;
    
    /**
     * 更新时间
     */
    private Long updateTime;
    
    /**
     * 分账方类型 0:默认，1：加盟商
     */
    private Integer outAccountType;
    
    /**
     * 业务类型：0：换电-套餐购买、1：换电-保险购买、2：换电-滞纳金缴纳、3：换电-押金缴纳, 98: 解冻，99：系统级别
     */
    private Integer businessType;
    
    /**
     * 支付渠道：ALIPAY：支付宝，WECHAT：微信
     */
    private String channel;
}

