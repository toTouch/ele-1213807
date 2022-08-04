package com.xiliulou.electricity.vo;

import lombok.Data;

import java.util.List;

/**
 * @author hrp
 * @date 2022/07/27 14:22
 * @mood 首页用户分析
 */
@Data
public class HomePageUserAnalysisVo {

   private List<HomePageUserByWeekDayVo> authenticationUserAnalysis;

   private List<HomePageUserByWeekDayVo> normalUserAnalysis;

   private Integer userCount;


}
