package com.xiliulou.electricity.entity.car;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xiliulou.electricity.entity.basic.BasicCarPo;
import com.xiliulou.electricity.enums.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 租车套餐订单租金退款表
 *
 * @author xiaohui.song
 **/
@Data
@TableName("t_car_rental_package_order_rent_refund")
public class CarRentalPackageOrderRentRefundPo extends BasicCarPo {

    private static final long serialVersionUID = 4224921643482546889L;

    /**
     * 用户ID
     */
    private Long uid;

    /**
     * 订单编码
     */
    private String orderNo;

    /**
     * 购买订单编号
     */
    private String rentalPackageOrderNo;

    /**
     * 套餐ID
     */
    private Long rentalPackageId;

    /**
     * 套餐类型
     * <pre>
     *     1-单车
     *     2-车电一体
     * </pre>
     * @see RentalPackageTypeEnum
     */
    private Integer rentalPackageType;

    /**
     * 租期余量
     */
    private Long tenancyResidue;

    /**
     * 租期余量单位
     * <pre>
     *     1-天
     *     0-分钟
     * </pre>
     * @see RentalUnitEnum
     */
    private Integer tenancyResidueUnit;

    /**
     * 套餐限制
     * <pre>
     *     0-不限制
     *     1-次数
     * </pre>
     * @see RenalPackageConfineEnum
     */
    private Integer confine;

    /**
     * 限制余量
     */
    private Long confineResidue;

    /**
     * 退款金额
     */
    private BigDecimal refundAmount;

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

    /**
     * 租金单价
     */
    private BigDecimal rentUnitPrice;

    /**
     * 租金(支付价格)
     */
    private BigDecimal rentPayment;

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
     * 审核时间
     */
    private Long auditTime;
}
