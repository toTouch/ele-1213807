package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 退款订单表(TEleRefundOrder)实体类
 *
 * @author makejava
 * @since 2021-02-22 10:17:06
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_ele_refund_order_history")
public class EleRefundOrderHistory {
    /**
    * 退款Id
    */
    @TableId(value = "id",type = IdType.AUTO)
    private Long id;
    /**
    * 退款单号
    */
    private String refundOrderNo;
    /**
    * 退款金额,单位元
    */
    private BigDecimal refundAmount;
    /**
    * 创建时间
    */
    private Long createTime;

    //租户id
    private Integer tenantId;

}
