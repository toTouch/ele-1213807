package com.xiliulou.electricity.service.impl;

import com.google.common.collect.Lists;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.entity.EleTurnoverStatistic;
import com.xiliulou.electricity.mapper.EleTurnoverStatisticMapper;
import com.xiliulou.electricity.query.TurnoverStatisticQueryModel;
import com.xiliulou.electricity.service.TurnoverStatisticService;
import com.xiliulou.electricity.vo.EleTurnoverStatisticVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @ClassName : TurnoverStatisticServiceImpl
 * @Description :
 * @Author : zhangyongbo
 * @since: 2024-01-23
 */
@Service
@Slf4j
public class TurnoverStatisticServiceImpl implements TurnoverStatisticService {
    
    @Autowired
    EleTurnoverStatisticMapper eleTurnoverStatisticMapper;
    
    @Override
    public List<EleTurnoverStatisticVO> listTurnoverStatistic(TurnoverStatisticQueryModel queryModel) {
        List<EleTurnoverStatistic> eleTurnoverStatisticList = eleTurnoverStatisticMapper.selectListEleTurnoverStatistic(queryModel);
        List<EleTurnoverStatisticVO> result = Lists.newArrayList();
        if(CollectionUtils.isEmpty(eleTurnoverStatisticList)){
            return result;
        }
        log.info("eleTurnoverStatisticList queryModel ={}", JsonUtil.toJson(queryModel));
        log.info("eleTurnoverStatisticList size ={}", JsonUtil.toJson(eleTurnoverStatisticList));
        
        for (EleTurnoverStatistic eleTurnoverStatistic : eleTurnoverStatisticList) {
            EleTurnoverStatisticVO eleTurnoverStatisticVO = new EleTurnoverStatisticVO();
            BeanUtils.copyProperties(eleTurnoverStatistic,eleTurnoverStatisticVO);
            result.add(eleTurnoverStatisticVO);
        }
        return result;
    }
}
