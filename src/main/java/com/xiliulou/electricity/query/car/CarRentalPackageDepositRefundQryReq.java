package com.xiliulou.electricity.query.car;

import com.xiliulou.electricity.enums.PayTypeEnum;
import com.xiliulou.electricity.enums.RefundStateEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * 租车套餐押金退款表，查询模型
 * @author xiaohui.song
 **/
@Data
public class CarRentalPackageDepositRefundQryReq implements Serializable {

    private static final long serialVersionUID = 8517519076859233590L;

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
     * 押金缴纳订单编码
     */
    private String depositPayOrderNo;

    /**
     * 用户ID
     */
    private Long uid;

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
