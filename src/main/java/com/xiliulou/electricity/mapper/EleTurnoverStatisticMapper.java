package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.EleTurnoverStatistic;
import com.xiliulou.electricity.query.TurnoverStatisticQueryModel;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @ClassName : EleTurnoverStatisticMapper
 * @Description :
 * @Author : zhangyongbo
 * @since: 2024-01-23
 */

@Repository
public interface EleTurnoverStatisticMapper {
   List<EleTurnoverStatistic> selectListEleTurnoverStatistic(TurnoverStatisticQueryModel queryModel);
}
