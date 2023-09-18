package com.xiliulou.electricity.query.car;

import com.xiliulou.electricity.enums.PayStateEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * 租车套餐订单逾期表，查询模型
 * @author xiaohui.song
 **/
@Data
public class CarRentalPackageOrderSlippageQryReq implements Serializable {

    private static final long serialVersionUID = 2273061223218238582L;

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
     * 支付状态
     * <pre>
     *     1-未支付
     *     2-支付成功
     *     3-支付失败
     * </pre>
     * @see PayStateEnum
     */
    private Integer payState;

    /**
     * 创建时间开始
     */
    private Long createTimeBegin;

    /**
     * 创建时间截止
     */
    private Long createTimeEnd;

    /**
     * 支付时间开始
     */
    private Long payTimeBegin;

    /**
     * 支付时间截止
     */
    private Long payTimeEnd;
}
