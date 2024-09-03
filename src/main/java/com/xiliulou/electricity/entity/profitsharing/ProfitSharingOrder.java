package com.xiliulou.electricity.entity.profitsharing;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 分账订单表(ProfitSharingOrder)实体类
 *
 * @author maxiaodong
 * @since 2024-08-22 16:58:54
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_profit_sharing_order")
public class ProfitSharingOrder implements Serializable {
    
    private static final long serialVersionUID = 156185949640543751L;
    
    private Long id;
    
    /**
     * 第三方支付订单号
     */
    private String thirdTradeOrderNo;
    
    /**
     * 分账单号
     */
    private String orderNo;
    
    /**
     * 第三方分账单号
     */
    private String thirdOrderNo;
    
    /**
     * 业务订单号
     */
    private String businessOrderNo;
    
    /**
     * 订单金额,单位元
     */
    private BigDecimal amount;
    
    /**
     * 业务类型：0：换电-套餐购买、1：换电-保险购买、2：换电-滞纳金缴纳、3：换电-押金缴纳, 98: 解冻，99：系统级别
     */
    private Integer businessType;
    
    /**
     * 分账状态：0：已受理、1：处理中、2：分账完成
     */
    private Integer status;
    
    /**
     * 分账类型：0：分出、1：解冻
     */
    private Integer type;
    
    /**
     * 租户id
     */
    private Integer tenantId;
    
    /**
     * 加盟商id
     */
    private Long franchiseeId;
    
    /**
     * 分账方类型 0:默认，1：加盟商
     */
    private Integer outAccountType;
    
    /**
     * 第三方商户号
     */
    private String thirdMerchantId;
    
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
     * 支付渠道：ALIPAY：支付宝，WECHAT：微信
     */
    private String channel;
}

