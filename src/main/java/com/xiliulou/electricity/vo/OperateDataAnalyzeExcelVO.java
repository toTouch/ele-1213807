package com.xiliulou.electricity.vo;


import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author : renhang
 * @description OperateDataAnalyzeExcelVO
 * @date : 2025-03-05 17:30
 **/
@Data
public class OperateDataAnalyzeExcelVO {

    @ExcelProperty("租户id")
    /** 租户ID */
    private Integer tenantId;

    @ExcelProperty("租户名称")
    /** 租户名称 */
    private String tenantName;



    @ExcelProperty("总柜机数")
    /** 总柜机数 */
    private Long sumCabinetCount;

    @ExcelProperty("正常使用柜机数")
    /** 正常柜机数 */
    private Long normalCabinetCount;

    @ExcelProperty("柜机使用率")
    /** 柜机使用率 */
    private BigDecimal cabinetUseRate;

    @ExcelProperty("总仓门数")
    /** 仓门数 */
    private Long sumCellCount;

    @ExcelProperty("正常使用仓门数")
    /** 正常使用仓门数 */
    private Long normalCellCount;

    @ExcelProperty("仓门使用率")
    /** 仓门使用率 */
    private BigDecimal cellUseRate;

    @ExcelProperty("电池总数")
    /** 电池总数 */
    private Long sumBatteryCount;

    @ExcelProperty("电池出租数")
    /** 电池出租数 */
    private Long rentBatteryCount;

    @ExcelProperty("电池出租率")
    /** 电池出租率 */
    private BigDecimal batteryRentRate;

    @ExcelProperty("累计实名人数")
    /** 累计实名人数 */
    private Long totalRealCount;

    @ExcelProperty("累计购买人数")
    /** 累计购买套餐人数 */
    private Long totalBuyMemberCount;

    @ExcelProperty("累计购买率")
    /** 累计购买率（累计购买套餐人数/累计实名人数） */
    private BigDecimal totalBuyRate;

    @ExcelProperty("仅购买过一次的用户")
    /** 仅购买过一次的人数 */
    private Long onlyBuyMemberCount;

    @ExcelProperty("累计流失率")
    /** 累计流失率（仅购买过一次的人数/累计购买人数） */
    private BigDecimal totalChurnRate;

    @ExcelProperty("当前在使用套餐人数")
    /** 当前在使用的套餐人数 */
    private Long currentUseMemberCount;

    @ExcelProperty("人仓比")
    /** 人仓比（上月套餐人数/总仓门数） */
    private BigDecimal peopleCellRate;

    @ExcelProperty("上周新增实名认证人数")
    /** 上周新增实名认证数 */
    private Long lastWeekAddRealCount;

    @ExcelProperty("上周新增购买套餐人数")
    /** 上周新增购买套餐人数 */
    private Long lastWeekAddBuyCount;

    @ExcelProperty("上周购买套餐转化率")
    /** 上周购买套餐转化率 */
    private BigDecimal lastWeekAddBuyRate;

    @ExcelProperty("上周复购率")
    /** 上周复购率（上周复购人数/上周到期人数） */
    private BigDecimal lastWeekRepurchaseRate;

    @ExcelProperty("上周忠实用户占比")
    /**上周忠实用户占比（使用超过3个月的用户/当前总套餐人数） */
    private BigDecimal loyalUserRate;
}
