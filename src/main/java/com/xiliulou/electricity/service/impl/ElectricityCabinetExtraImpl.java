package com.xiliulou.electricity.service.impl;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.ElectricityCabinetExtra;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.mapper.ElectricityCabinetExtraMapper;
import com.xiliulou.electricity.query.ElectricityCabinetBatchEditRentReturnCountQuery;
import com.xiliulou.electricity.service.ElectricityCabinetExtraService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * @author HeYafeng
 * @description 柜机扩展表服务层
 * @date 2024/4/23 13:57:12
 */
@Slf4j
@Service
public class ElectricityCabinetExtraImpl implements ElectricityCabinetExtraService {
    
    @Resource
    private ElectricityCabinetExtraMapper electricityCabinetExtraMapper;
    
    @Resource
    private RedisService redisService;
    
    @Slave
    @Override
    public ElectricityCabinetExtra queryByEid(Long eid) {
        return electricityCabinetExtraMapper.selectByEid(eid);
    }
    
    @Override
    public ElectricityCabinetExtra queryByEidFromCache(Long eid) {
        ElectricityCabinetExtra cacheEleCabinetExtra = redisService.getWithHash(CacheConstant.CACHE_ELECTRICITY_CABINET_EXTRA + eid, ElectricityCabinetExtra.class);
        if (Objects.nonNull(cacheEleCabinetExtra)) {
            return cacheEleCabinetExtra;
        }
        
        ElectricityCabinetExtra electricityCabinetExtra = this.queryByEid(eid);
        if (Objects.isNull(electricityCabinetExtra)) {
            return null;
        }
        
        redisService.saveWithHash(CacheConstant.CACHE_ELECTRICITY_CABINET_EXTRA + eid, electricityCabinetExtra);
        return electricityCabinetExtra;
    }
    
    @Override
    public Integer insertOne(ElectricityCabinetExtra electricityCabinetExtra) {
        return electricityCabinetExtraMapper.insertOne(electricityCabinetExtra);
    }
    
    @Override
    public Integer update(ElectricityCabinetExtra electricityCabinetExtra) {
        return electricityCabinetExtraMapper.update(electricityCabinetExtra);
    }
    
    @Override
    public Integer updateElectricityCabinetExtra(Integer minRetainBatteryCount, Integer maxRetainBatteryCount, Integer id) {
        return electricityCabinetExtraMapper.updateElectricityCabinetExtra(minRetainBatteryCount, maxRetainBatteryCount, id, System.currentTimeMillis());
    }
    
}
