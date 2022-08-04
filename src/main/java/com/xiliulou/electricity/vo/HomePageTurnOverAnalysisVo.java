package com.xiliulou.electricity.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author hrp
 * @date 2022/07/27 14:22
 * @mood 首页营业额分析
 */
@Data
public class HomePageTurnOverAnalysisVo {

   private List<HomePageTurnOverGroupByWeekDayVo> batteryMemberCardAnalysis;

   private List<HomePageTurnOverGroupByWeekDayVo> carMemberCardAnalysis;

   private List<HomePageTurnOverGroupByWeekDayVo> batteryServiceFeeAnalysis;

   private List<HomePageTurnOverGroupByWeekDayVo> BatteryDepositAnalysis;

   private List<HomePageTurnOverGroupByWeekDayVo> carDepositAnalysis;

   private BigDecimal memberCardTurnOver;

   private BigDecimal depositTurnOver;


}
