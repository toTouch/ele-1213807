package com.xiliulou.electricity.entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

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
@TableName("t_ele_refund_order")
public class EleRefundOrder {
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
    * 支付单号
    */
    private String orderId;
    /**
    * 支付金额,单位元
    */
    private BigDecimal payAmount;
    /**
    * 退款金额,单位元
    */
    private BigDecimal refundAmount;
    /**
    * 退款状态
    */
    private Integer status;
    /**
    * 错误原因
    */
    private String errMsg;
    /**
    * 创建时间
    */
    private Long createTime;
    /**
    * 更新时间
    */
    private Long updateTime;

    //租户id
    private Integer tenantId;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

    //订单生成
    public static final Integer STATUS_INIT = 0;
    //后台同意退款
    public static final Integer STATUS_AGREE_REFUND = 1;
    //后台拒绝退款
    public static final Integer STATUS_REFUSE_REFUND = 2;
    //退款中
    public static final Integer STATUS_REFUND = 3;
    //退款成功
    public static final Integer STATUS_SUCCESS = 4;
    //退款失败
    public static final Integer STATUS_FAIL = -1;

}
