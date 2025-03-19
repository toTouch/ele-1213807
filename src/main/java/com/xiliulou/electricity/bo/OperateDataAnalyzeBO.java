package com.xiliulou.electricity.bo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author : renhang
 * @description OperateDataAnalyzeBO
 * @date : 2025-03-06 09:04
 **/
@Data
public class OperateDataAnalyzeBO {

    /**
     * 租户ID
     */
    private Integer tenantId;

    /**
     * 租户名称
     */
    private String tenantName;

    /**
     * 总柜机数
     */
    private Long sumCabinetCount;

    /**
     * 正常柜机数
     */
    private Long normalCabinetCount;

    /**
     * 柜机使用率
     */
    private BigDecimal cabinetUseRate;

    /**
     * 仓门数
     */
    private Long sumCellCount;


    /**
     * 正常使用仓门数
     */
    private Long normalCellCount;


    /**
     * 仓门使用率
     */
    private BigDecimal cellUseRate;


    /**
     * 电池总数
     */
    private Long sumBatteryCount;


    /**
     * 电池出租数
     */
    private Long rentBatteryCount;


    /**
     * 电池出租率
     */
    private BigDecimal batteryRentRate;


    /**
     * 累计实名人数
     */
    private Long totalRealCount;


    /**
     * 累计购买套餐人数
     */
    private Long totalBuyMemberCount;


    /**
     * 累计购买率（累计购买套餐人数/累计实名人数）
     */
    private BigDecimal totalBuyRate;


    /**
     * 仅购买过一次的人数
     */
    private Long onlyBuyMemberCount;


    /**
     * 累计流失率（仅购买过一次的人数/累计购买人数）
     */
    private BigDecimal totalChurnRate;


    /**
     * 当前在使用的套餐人数
     */
    private Long currentUseMemberCount;


    /**
     * 人仓比（上月套餐人数/总仓门数）
     */
    private BigDecimal peopleCellRate;


    /**
     * 上周新增实名认证数
     */
    private Long lastWeekAddRealCount;


    /**
     * 上周新增购买套餐人数
     */
    private Long lastWeekAddBuyCount;


    /**
     * 上周购买套餐转化率
     */
    private BigDecimal lastWeekAddBuyRate;


    /**
     * 上周复购率（上周复购人数/上周到期人数）
     */
    private BigDecimal lastWeekRepurchaseRate;


    /**
     * 上周忠实用户占比（使用超过3个月的用户/当前总套餐人数）
     */
    private BigDecimal loyalUserRate;
}
