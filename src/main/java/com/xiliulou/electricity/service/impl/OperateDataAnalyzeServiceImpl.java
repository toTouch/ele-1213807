package com.xiliulou.electricity.service.impl;


import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.bo.OperateDataAnalyzeBO;
import com.xiliulou.electricity.entity.OperateDataAnalyze;
import com.xiliulou.electricity.mapper.OperateDataAnalyzeMapper;
import com.xiliulou.electricity.service.OperateDataAnalyzeService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author : renhang
 * @description OperateDataAnalyzeServiceImpl
 * @date : 2025-03-05 16:27
 **/
@Service
public class OperateDataAnalyzeServiceImpl implements OperateDataAnalyzeService {

    @Resource
    private OperateDataAnalyzeMapper operateDataAnalyzeMapper;

    @Override
    public String queryLatestBatch() {
        return operateDataAnalyzeMapper.selectLatestBatch();
    }

    @Override
    @Slave
    public List<OperateDataAnalyzeBO> queryList(String batch) {
        return operateDataAnalyzeMapper.selectListByBatch(batch);
    }


}
