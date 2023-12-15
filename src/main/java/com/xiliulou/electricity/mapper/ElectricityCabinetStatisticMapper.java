package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.ElectricityCabinetStatistic;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author zhangyongbo
 * @description 柜机统计mapper
 * @date 2023/12/15 15:04:55
 */

@Repository
public interface ElectricityCabinetStatisticMapper {
    
    List<ElectricityCabinetStatistic> selectListByElectricityCabinetIdList(@Param("eidList") List<Integer> eidList, @Param("statisticDate") Long statisticTime);
    ElectricityCabinetStatistic selectByElectricityCabinetId(@Param("eid") Integer eid, @Param("statisticDate") Long statisticTime);
    Integer update(ElectricityCabinetStatistic statistic);
    Integer insertOne(ElectricityCabinetStatistic statistic);
    
    Integer batchInsert(List<ElectricityCabinetStatistic> statisticList);
}
