package com.xiliulou.electricity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2022-12-28-17:10
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CarMemberCardOrderVO {
    private Long id;
    /**
     * 用户Id
     */
    private Long uid;
    /**
     * 订单Id
     */
    private String orderId;
    /**
     * 状态（0,未支付,1,支付成功 2,支付失败）
     */
    private Integer status;
    /**
     * 车辆型号id
     */
    private Long carModelId;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 套餐类型
     */
    private String memberCardType;
    /**
     * 套餐名称
     */
    private String cardName;
    /**
     * 交易方式 0--线上 1--线下
     */
    private Integer payType;
    /**
     * 支付金额
     */
    private BigDecimal payAmount;
    /**
     * 有效天数
     */
    private Integer validDays;
    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * 套餐到期时间
     */
    private Long memberCardExpireTime;
    /**
     * 门店id
     */
    private Long storeId;
    /**
     * 加盟商id
     */
    private Long franchiseeId;

    private Integer tenantId;


}
