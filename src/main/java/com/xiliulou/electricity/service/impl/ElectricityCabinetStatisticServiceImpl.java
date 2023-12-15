package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.ElectricityCabinetStatistic;
import com.xiliulou.electricity.mapper.ElectricityCabinetStatisticMapper;
import com.xiliulou.electricity.service.ElectricityCabinetStatisticService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("electricityCabinetStatisticService")
@Slf4j
public class ElectricityCabinetStatisticServiceImpl implements ElectricityCabinetStatisticService {
    
    @Autowired
    private ElectricityCabinetStatisticMapper statisticMapper;
    
    @Override
    public ElectricityCabinetStatistic queryByElectricityCabinetId(Integer eid, Long statisticTime) {
        return statisticMapper.selectByElectricityCabinetId(eid, statisticTime);
    }
    
    @Override
    public List<ElectricityCabinetStatistic> listByElectricityCabinetIdList(List<Integer> eidList, Long statisticTime) {
        return statisticMapper.selectListByElectricityCabinetIdList(eidList, statisticTime);
    }
    
    @Override
    public Integer update(ElectricityCabinetStatistic statistic) {
        return statisticMapper.update(statistic);
    }
    
    @Override
    public Integer insertOne(ElectricityCabinetStatistic statistic) {
        return statisticMapper.insertOne(statistic);
    }
    
    @Override
    public Integer batchInsert(List<ElectricityCabinetStatistic> statisticList) {
        return statisticMapper.batchInsert(statisticList);
    }
}
