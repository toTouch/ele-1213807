package com.xiliulou.electricity.entity.car;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xiliulou.electricity.entity.basic.BasicCarPO;
import com.xiliulou.electricity.enums.PayTypeEnum;
import com.xiliulou.electricity.enums.RefundStateEnum;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 租车套餐押金退款表
 *
 * @author xiaohui.song
 **/
@Data
@TableName("t_car_rental_package_deposit_refund")
public class CarRentalPackageDepositRefundPO extends BasicCarPO {

    private static final long serialVersionUID = 1268475913696945741L;

    /**
     * 用户ID
     */
    private Long uid;

    /**
     * 订单编码
     */
    private String orderNo;

    /**
     * 押金缴纳订单编号
     */
    private String depositPayOrderNo;

    /**
     * 申请金额
     */
    private BigDecimal applyAmount;

    /**
     * 实际退款金额
     */
    private BigDecimal realAmount;

    /**
     * 交易方式
     * <pre>
     *     1-线上
     *     2-线下
     * </pre>
     * @see PayTypeEnum
     */
    private Integer payType;

    /**
     * 退款订单状态
     * <pre>
     *     1-待审核
     *     2-审核通过
     *     3-审核拒绝
     *     4-退款中
     *     5-退款成功
     *     6-退款失败
     * </pre>
     * @see RefundStateEnum
     */
    private Integer refundState;

    /**
     * 备注
     */
    private String remark;

}
