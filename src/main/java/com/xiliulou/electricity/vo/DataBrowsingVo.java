package com.xiliulou.electricity.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author hrp
 * @date 2022/3/24 14:22
 * @mood 数据大屏数据总览
 */
@Data
public class DataBrowsingVo {

    /**
     * 总营业额
     */
    private BigDecimal sumTurnover;

    /**
     * 电池月卡营业额
     */
    private BigDecimal MemberCardTurnover;

    /**
     * 缴纳押金营业额
     */
    private BigDecimal DepositTurnover;

    /**
     * 退押金总数
     */
    private BigDecimal refundDepositTurnOver;

    /**
     * 总订单数
     */
    private Integer sumOrderCount;

    /**
     * 总用户数
     */
    private Integer sumUserCount;

    /**
     * 总租户数
     */
    private Integer tenantCount;

    /**
     * 换电柜总数
     */
    private Integer electricityCabinetCount;

    /**
     * 电池总数
     */
    private Integer batteryCount;

    /**
     * 换电成功率
     */
    private BigDecimal electricityOrderSuccessRate;
}
