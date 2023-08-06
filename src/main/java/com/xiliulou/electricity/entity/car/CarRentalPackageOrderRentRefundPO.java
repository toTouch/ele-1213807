package com.xiliulou.electricity.entity.car;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xiliulou.electricity.entity.basic.BasicCarPO;
import com.xiliulou.electricity.enums.PayTypeEnum;
import com.xiliulou.electricity.enums.RefundStateEnum;
import com.xiliulou.electricity.enums.RentalUnitEnum;
import com.xiliulou.electricity.enums.RentalPackageTypeEnum;
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
     * @see RentalPackageTypeEnum
     */
    private Integer rentalPackageType;

    /**
     * 余量
     */
    private Long residue;

    /**
     * 余量单位
     * <pre>
     *     -1-次数
     *     1-天
     *     0-分钟
     * </pre>
     * @see RentalUnitEnum
     */
    private Integer residueUnit;

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
