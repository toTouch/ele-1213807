package com.xiliulou.electricity.entity.car;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xiliulou.electricity.entity.car.basic.BasicCarPO;

import com.xiliulou.electricity.enums.PayTypeEnum;
import com.xiliulou.electricity.enums.car.CarRentalStateEnum;
import com.xiliulou.electricity.enums.car.CarRentalTypeEnum;
import lombok.Data;

/**
 * 车辆租赁订单表
 *
 * @author xiaohui.song
 **/
@Data
@TableName("t_car_rental_order")
public class CarRentalOrderPO extends BasicCarPO {

    private static final long serialVersionUID = -1949828948420819144L;

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
     * 类型
     * <pre>
     *     1-租车
     *     2-还车
     * </pre>
     * @see CarRentalTypeEnum
     */
    private Integer type;

    /**
     * 车辆型号ID
     */
    private Integer carModelId;

    /**
     * 车辆SN码
     */
    private String carSn;

    /**
     * 电池型号ID
     */
    private Long batteryModelId;

    /**
     * 电池SN码
     */
    private String batterySn;

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
     * 订单状态
     * <pre>
     *     1-审核中
     *     2-成功
     *     3-审核拒绝
     * </pre>
     * @see CarRentalStateEnum
     */
    private Integer rentalState;

    /**
     * 备注
     */
    private String remark;
}
