package com.xiliulou.electricity.vo;

import lombok.Data;

import java.util.List;

@Data
public class TurnoverAndUserStatisticVo {

    private List<WeekOrderStatisticVo> weekUserStatistic;

    private List<WeekTurnoverStatisticVo> weekTurnOverStatistic;
}
