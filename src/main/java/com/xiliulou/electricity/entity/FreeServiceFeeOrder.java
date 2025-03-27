package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @Description: t_free_service_fee_order
 * @Author: RenHang
 * @Date: 2025/03/27
 */
@Builder
@Data
@TableName("t_free_service_fee_order")
public class FreeServiceFeeOrder {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 免押服务费订单号
     */
    private String orderId;

    /**
     * 免押订单号
     */
    private String freeDepositOrderId;

    /**
     * 用户ID
     */
    private Long uid;

    /**
     * 免押服务费金额
     */
    private BigDecimal payAmount;

    /**
     * 支付状态
     * @see com.xiliulou.electricity.enums.FreeServiceFeeStatusEnum
     */
    private Integer status;

    /**
     * 支付渠道：WECHAT-微信支付,ALIPAY-支付宝
     */
    private String paymentChannel;

    /**
     * 租户ID
     */
    private Integer tenantId;

    /**
     * 加盟商id
     */
    private Long franchiseeId;

    /**
     * 门店id
     */
    private Long storeId;

    /**
     * 支付时间
     */
    private Long payTime;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 更新时间
     */
    private Long updateTime;


}


