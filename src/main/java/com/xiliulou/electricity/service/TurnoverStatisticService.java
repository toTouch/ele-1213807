package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.EleTurnoverStatistic;
import com.xiliulou.electricity.query.TurnoverStatisticQueryModel;
import com.xiliulou.electricity.vo.EleTurnoverStatisticVO;

import java.util.List;

/**
 * @ClassName : TurnoverStatisticService
 * @Description :
 * @Author : zhangyongbo
 * @since: 2024-01-22
 */
public interface TurnoverStatisticService {
    List<EleTurnoverStatistic>  listTurnoverStatistic(TurnoverStatisticQueryModel queryModel);
}
