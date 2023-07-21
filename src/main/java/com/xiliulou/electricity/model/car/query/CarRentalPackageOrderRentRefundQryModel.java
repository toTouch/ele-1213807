package com.xiliulou.electricity.model.car.query;

import com.xiliulou.electricity.enums.PayTypeEnum;
import com.xiliulou.electricity.enums.RefundStateEnum;
import com.xiliulou.electricity.enums.car.CarRentalPackageTypeEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * 租车套餐订单租金退款表，DB层查询模型
 *
 * @author xiaohui.song
 **/
@Data
public class CarRentalPackageOrderRentRefundQryModel implements Serializable {

    private static final long serialVersionUID = 3053707906521929530L;

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
     * 购买订单编号
     */
    private String rentalPackageOrderNo;

    /**
     * 用户ID
     */
    private Long uid;

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
     * 创建时间开始
     */
    private Long createTimeBegin;

    /**
     * 创建时间截止
     */
    private Long createTimeEnd;
}
