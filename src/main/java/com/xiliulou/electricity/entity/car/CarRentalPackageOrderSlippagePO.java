package com.xiliulou.electricity.entity.car;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xiliulou.electricity.entity.basic.BasicCarPO;

import com.xiliulou.electricity.enums.PayStateEnum;
import com.xiliulou.electricity.enums.SlippageTypeEnum;
import com.xiliulou.electricity.enums.car.CarRentalPackageTypeEnum;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 租车套餐订单逾期表
 *
 * @author xiaohui.song
 **/
@Data
@TableName("t_car_rental_package_order_slippage")
public class CarRentalPackageOrderSlippagePO extends BasicCarPO {

    private static final long serialVersionUID = 503236518950550800L;

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
     * 类型
     * <pre>
     *     1-过期
     *     2-冻结
     * </pre>
     * @see SlippageTypeEnum
     */
    private Integer type;

    /**
     * 车辆SN码
     */
    private String carSn;

    /**
     * 电池SN码
     */
    private String batterySn;

    /**
     * 滞纳金(元/天)
     */
    private BigDecimal lateFee;

    /**
     * 滞纳金开始时间
     */
    private Long lateFeeStartTime;

    /**
     * 滞纳金结束时间
     */
    private Long lateFeeEndTime;

    /**
     * 滞纳金缴纳金额
     */
    private BigDecimal lateFeePay;

    /**
     * 支付状态
     * <pre>
     *     1-未支付
     *     2-支付成功
     *     3-支付失败
     *     5-已清除
     * </pre>
     * @see PayStateEnum
     */
    private Integer payState;

    /**
     * 支付时间
     */
    private Long payTime;
}
