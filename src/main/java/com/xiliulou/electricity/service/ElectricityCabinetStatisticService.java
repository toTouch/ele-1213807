package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.ElectricityCabinetStatistic;

import java.util.List;

/**
 * 换电柜统计表表服务接口
 *
 * @author zhangyongbo
 * @since 2023-12-15 11:00:14
 */
public interface ElectricityCabinetStatisticService {
    
    ElectricityCabinetStatistic queryByElectricityCabinetId(Integer eid, Long statisticTime);
    
    List<ElectricityCabinetStatistic> listByElectricityCabinetIdList(List<Integer> eidList, Long statisticTime);
    
    Integer update(ElectricityCabinetStatistic statistic);
    
    Integer insertOne(ElectricityCabinetStatistic statistic);
    
    Integer batchInsert(List<ElectricityCabinetStatistic> statisticList);
}
