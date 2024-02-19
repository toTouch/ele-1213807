package com.xiliulou.electricity.service.impl.merchant;

import com.xiliulou.electricity.entity.merchant.EleChargeConfigRecord;
import com.xiliulou.electricity.mapper.merchant.EleChargeConfigRecordMapper;
import com.xiliulou.electricity.service.merchant.EleChargeConfigRecordService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author HeYafeng
 * @description 电价配置记录
 * @date 2024/2/19 09:21:26
 */
@Service
public class EleChargeConfigRecordServiceImpl implements EleChargeConfigRecordService {
    
    @Resource
    private EleChargeConfigRecordMapper eleChargeConfigRecordMapper;
    
    @Override
    public Integer insertOne(EleChargeConfigRecord record) {
        return eleChargeConfigRecordMapper.insertOne(record);
    }
}
