package com.xiliulou.electricity.vo.rental;

import com.xiliulou.electricity.enums.MemberTermStatusEnum;
import com.xiliulou.electricity.vo.car.CarRentalPackageOrderVO;
import com.xiliulou.electricity.vo.car.CarVO;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 租赁套餐展示层数据模型
 *
 * @author xiaohui.song
 **/
@Data
public class RentalPackageVO implements Serializable {

    private static final long serialVersionUID = -83061401678360392L;

    /**
     * 当前套餐订单信息
     */
    private CarRentalPackageOrderVO carRentalPackageOrder;

    /**
     * 车辆信息
     * */
    private CarVO car;

    // TODO 电池信息

    // TODO 保险信息

    // TODO 滞纳金信息

    /**
     * 截止时间（所有订单的总计）
     */
    private Long deadlineTime;

    /**
     * 滞纳金金额
     */
    private BigDecimal lateFeeAmount;

    /**
     * 状态
     * <pre>
     *     0-初始化
     *     1-正常
     *     2-申请冻结
     *     3-冻结
     *     4-申请退押
     *     5-申请退租
     * </pre>
     * @see MemberTermStatusEnum
     */
    private Integer status;



}
