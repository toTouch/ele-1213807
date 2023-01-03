package com.xiliulou.electricity.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2022-12-22-19:17
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RentCarMemberCardOrderQuery {

    private Long size;
    private Long offset;
    private String phone;

    private Long beginTime;
    private Long endTime;
    private Integer status;
    private String orderId;

    private Integer tenantId;

    private List<Long> franchiseeIds;

    private Long franchiseeId;

    private List<Long> storeIds;

    private Long storeId;

    private Long id;
    /**
     * 用户Id
     */
    private Long uid;

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


}
