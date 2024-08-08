package com.xiliulou.electricity.service.impl;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.ElectricityCabinetChooseCellConfig;
import com.xiliulou.electricity.mapper.ElectricityCabinetChooseCellConfigMapper;
import com.xiliulou.electricity.service.ElectricityCabinetChooseCellConfigService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @ClassName: ElectricityCabinetChooseCellConfigServiceImpl
 * @description:
 * @author: renhang
 * @create: 2024-08-08 09:04
 */
@Service
public class ElectricityCabinetChooseCellConfigServiceImpl implements ElectricityCabinetChooseCellConfigService {
    
    @Resource
    private ElectricityCabinetChooseCellConfigMapper electricityCabinetChooseCellConfigMapper;
    
    @Resource
    RedisService redisService;
    
    @Override
    public ElectricityCabinetChooseCellConfig queryConfigByNumFromDB(Integer num) {
        return electricityCabinetChooseCellConfigMapper.selectConfigByNum(num);
    }
    
    @Override
    public ElectricityCabinetChooseCellConfig queryConfigByNumFromCache(Integer num) {
        if (Objects.isNull(num)) {
            return null;
        }
        //先查缓存
        ElectricityCabinetChooseCellConfig chooseCellConfig = redisService.getWithHash(CacheConstant.CACHE_ELECTRICITY_CABINET_CELL_CONFIG + num,
                ElectricityCabinetChooseCellConfig.class);
        if (Objects.nonNull(chooseCellConfig)) {
            return chooseCellConfig;
        }
        
        ElectricityCabinetChooseCellConfig chooseCellConfigFromDb = this.queryConfigByNumFromDB(num);
        if (Objects.isNull(chooseCellConfigFromDb)) {
            return null;
        }
        
        redisService.saveWithHash(CacheConstant.CACHE_ELECTRICITY_CABINET_MODEL + num, chooseCellConfigFromDb);
        return chooseCellConfigFromDb;
    }
}
