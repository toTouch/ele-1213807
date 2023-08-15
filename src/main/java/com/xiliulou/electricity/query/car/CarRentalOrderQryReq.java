package com.xiliulou.electricity.query.car;

import com.xiliulou.electricity.enums.RentalTypeEnum;
import com.xiliulou.electricity.enums.car.CarRentalStateEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * 车辆租赁订单表，查询模型
 * @author xiaohui.song
 **/
@Data
public class CarRentalOrderQryReq implements Serializable {

    private static final long serialVersionUID = -6034098988106299099L;

    /**
     * 偏移量
     */
    private Integer offset = 0;

    /**
     * 取值数量
     */
    private Integer size = 10;

    /**
     * 租户ID
     */
    private Integer tenantId;

    /**
     * 加盟商ID
     */
    private Integer franchiseeId;

    /**
     * 门店ID
     */
    private Integer storeId;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 用户ID
     */
    private Long uid;

    /**
     * 类型
     * <pre>
     *     1-租借
     *     2-归还
     * </pre>
     * @see RentalTypeEnum
     */
    private Integer type;

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
     * 车辆SN码(模糊搜索)
     */
    private String carSn;

    /**
     * 创建时间起始
     */
    private Long beginCreateTime;

    /**
     * 创建时间截止
     */
    private Long endCreateTime;
}
