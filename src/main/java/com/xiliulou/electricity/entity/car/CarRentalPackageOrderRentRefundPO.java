package com.xiliulou.electricity.entity.car;

import com.baomidou.mybatisplus.annotation.TableName;

import com.xiliulou.electricity.entity.car.basic.BasicCarPO;
import com.xiliulou.electricity.enums.RefundStateEnum;
import com.xiliulou.electricity.enums.ThirdPayChannelEnum;
import com.xiliulou.electricity.enums.TimeUnitEnum;
import com.xiliulou.electricity.enums.car.CarRentalPackageTypeEnum;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 租车套餐订单租金退款表
 *
 * @author xiaohui.song
 **/
@Data
@TableName("t_car_rental_package_order_rent_refund")
public class CarRentalPackageOrderRentRefundPO extends BasicCarPO {

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
     * @see CarRentalPackageTypeEnum
     */
    private Integer rentalPackageType;

    /**
     * 余量
     */
    private String residue;

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
     * 三方支付单号
     */
    private String thirdPayNo;

    /**
     * 三方支付渠道
     * <pre>
     *     1-微信
     * </pre>
     * @see ThirdPayChannelEnum
     */
    private Integer thirdPayChannel;

    /**
     * 租金单价单位
     * <pre>
     *     1-天
     *     2-分钟
     * </pre>
     * @see TimeUnitEnum
     */
    private Integer rentUnit;

    /**
     * 租金单价
     */
    private BigDecimal rentUnitPrice;

    /**
     * 租金(支付价格)
     */
    private BigDecimal rentPayment;
}
