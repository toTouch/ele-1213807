package com.xiliulou.electricity.query.car;

import com.xiliulou.electricity.enums.BuyTypeEnum;
import com.xiliulou.electricity.enums.PayStateEnum;
import com.xiliulou.electricity.enums.PayTypeEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * 租车套餐订单表，查询模型
 *
 * @author xiaohui.song
 **/
@Data
public class CarRentalPackageOrderQryReq implements Serializable {

    private static final long serialVersionUID = -8993659063684214223L;

    /**
     * 偏移量
     */
    private Integer offset = 0;

    /**
     * 取值数量
     */
    private Integer limitNum = 10;

    /**
     * 租户ID
     */
    private Integer tenantId;

    /**
     * 加盟商ID
     */
    private Integer franchiseeId;

    /**
     * 订单编码
     */
    private String orderNo;

    /**
     * 用户ID
     */
    private Long uid;

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
    private Integer buyType;

    /**
     * 购买时间开始
     */
    private Long buyTimeBegin;

    /**
     * 购买时间截止
     */
    private Long buyTimeEnd;
}
