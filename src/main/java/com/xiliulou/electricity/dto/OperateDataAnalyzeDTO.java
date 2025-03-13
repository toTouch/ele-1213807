package com.xiliulou.electricity.dto;


import lombok.Data;

/**
 * @author : renhang
 * @description OperateDataAnalyzeDTO
 * @date : 2025-03-10 09:31
 **/
@Data
public class OperateDataAnalyzeDTO {

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
    private String cabinetUseRate;

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
    private String cellUseRate;


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
    private String batteryRentRate;


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
    private String totalBuyRate;


    /**
     * 仅购买过一次的人数
     */
    private Long onlyBuyMemberCount;


    /**
     * 累计流失率（仅购买过一次的人数/累计购买人数）
     */
    private String totalChurnRate;


    /**
     * 当前在使用的套餐人数
     */
    private Long currentUseMemberCount;


    /**
     * 人仓比（上月套餐人数/总仓门数）
     */
    private String peopleCellRate;


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
    private String lastWeekAddBuyRate;


    /**
     * 上周复购率（上周复购人数/上周到期人数）
     */
    private String lastWeekRepurchaseRate;


    /**
     * 上周忠实用户占比（使用超过3个月的用户/当前总套餐人数）
     */
    private String loyalUserRate;
}
