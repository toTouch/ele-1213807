package com.xiliulou.electricity.entity.car;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xiliulou.electricity.entity.basic.BasicCarPo;

import com.xiliulou.electricity.enums.*;
import com.xiliulou.electricity.enums.RentalPackageTypeEnum;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 租车套餐押金缴纳订单表
 *
 * @author xiaohui.song
 **/
@Data
@TableName("t_car_rental_package_deposit_pay")
public class CarRentalPackageDepositPayPo extends BasicCarPo {

    private static final long serialVersionUID = -2666716651430506414L;

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
     * 类型
     * <pre>
     *     1-正常缴纳
     *     2-转押
     * </pre>
     *
     * @see DepositTypeEnum
     */
    private Integer type;

    /**
     * 变动金额，可以为负数
     */
    private BigDecimal changeAmount;

    /**
     * 实缴押金
     */
    private BigDecimal deposit;

    /**
     * 免押
     * <pre>
     *     0-是
     *     1-否
     * </pre>
     * @see YesNoEnum
     */
    private Integer freeDeposit;

    /**
     * 交易方式
     * <pre>
     *     1-线上
     *     2-线下
     *     3-免押
     * </pre>
     * @see PayTypeEnum
     */
    private Integer payType;

    /**
     * 支付状态
     * <pre>
     *     1-未支付
     *     2-支付成功
     *     3-支付失败
     *     4-取消支付
     * </pre>
     * @see PayStateEnum
     */
    private Integer payState;

    /**
     * 备注
     */
    private String remark;

    /**
     * 来源订单编码
     */
    private String sourceOrderNo;

    /**
     * 套餐押金
     */
    private BigDecimal rentalPackageDeposit;

}
