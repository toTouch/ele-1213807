package com.xiliulou.electricity.entity.car;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xiliulou.electricity.entity.car.basic.BasicCarPO;
import com.xiliulou.electricity.enums.*;
import com.xiliulou.electricity.enums.car.CarRentalPackageTypeEnum;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 租车套餐购买订单表
 *
 * @author xiaohui.song
 **/
@Data
@TableName("t_car_rental_package_order")
public class CarRentalPackageOrderPO extends BasicCarPO {

    private static final long serialVersionUID = -2568173202959559791L;

    /**
     * 用户ID
     */
    private Long uid;

    /**
     * 订单编码
     */
    private String orderNo;

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
     * 套餐限制
     * <pre>
     *     1-不限制
     *     2-次数
     * </pre>
     * @see RenalPackageConfineEnum
     */
    private Integer confine;

    /**
     * 限制数量
     */
    private Integer confineNum;

    /**
     * 租期
     */
    private Integer tenancy;

    /**
     * 租期单位
     * <pre>
     *     1-天
     *     2-分钟
     * </pre>
     * @see TimeUnitEnum
     */
    private Integer tenancyUnit;

    /**
     * 租金单价，单位同租期单位
     */
    private BigDecimal rentUnitPrice;

    /**
     * 租金(原价)
     */
    private BigDecimal rent;

    /**
     * 租金(支付价格)
     */
    private BigDecimal rentPayment;

    /**
     * 车辆型号ID
     */
    private Integer carModelId;

    /**
     * 电池型号ID集，用英文逗号分割
     */
    private String batteryModelIds;

    /**
     * 适用类型
     * <pre>
     *     1-全部
     *     2-新租套餐
     *     3-续租套餐
     * </pre>
     * @see ApplicableTypeEnum
     */
    private Integer applicableType;

    /**
     * 租金可退
     * <pre>
     *     1-是
     *     2-否
     * </pre>
     * @see YesNoEnum
     */
    private Integer rentRebate;

    /**
     * 租金退还期限(天)
     */
    private Integer rentRebateTerm;

    /**
     * 租金退还截止时间
     */
    private Long rentRebateEndTime;

    /**
     * 押金
     */
    private BigDecimal deposit;

    /**
     * 押金缴纳订单编号
     */
    private String depositPayOrderNo;

    /**
     * 滞纳金(天)
     */
    private BigDecimal lateFee;

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
     * 购买方式
     * <pre>
     *     1-线上
     *     2-线下
     *     3-赠送
     * </pre>
     * @see BuyTypeEnum
     */
    @Deprecated
    private Integer buyType;

    /**
     * 柜机ID
     */
    @Deprecated
    private Integer cabinetId;

    /**
     * 赠送的优惠券ID
     */
    private Long couponId;

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
     * 使用状态
     * <pre>
     *     1-未使用
     *     2-使用中
     *     3-已失效
     *     4-已退租
     * </pre>
     * @see UseStateEnum
     */
    private Integer useState;

    /**
     * 备注
     */
    private String remark;

    /**
     * 三方支付单号
     */
    @Deprecated
    private String thirdPayNo;

    /**
     * 三方支付渠道
     * <pre>
     *     1-微信
     * </pre>
     * @see ThirdPayChannelEnum
     */
    @Deprecated
    private Integer thirdPayChannel;

}
