package com.xiliulou.electricity.model.car.query;

import com.xiliulou.electricity.enums.PayStateEnum;
import com.xiliulou.electricity.enums.PayTypeEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * 租车套餐押金缴纳订单表，DB层查询模型
 * @author xiaohui.song
 **/
@Data
public class CarRentalPackageDepositPayQryModel implements Serializable {

    private static final long serialVersionUID = 1456275094579149620L;

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
     * </pre>
     * @see PayStateEnum
     */
    private Integer payState;
}
