package com.xiliulou.electricity.service.impl;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.ElectricityBatteryModel;
import com.xiliulou.electricity.mapper.ElectricityBatteryModelMapper;
import com.xiliulou.electricity.service.ElectricityBatteryModelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * 电池型号(ElectricityBatteryModel)表服务实现类
 *
 * @author makejava
 * @since 2020-11-26 14:44:44
 */
@Service("ElectricityBatteryModelService")
@Slf4j
public class ElectricityBatteryModelServiceImpl implements ElectricityBatteryModelService {
    @Resource
    private ElectricityBatteryModelMapper electricityBatteryModelMapper;

    @Resource
    RedisService redisService;

    @Override
    public R saveElectricityBatteryModel(ElectricityBatteryModel electricityBatteryModel) {
        electricityBatteryModel.setCreateTime(System.currentTimeMillis());
        electricityBatteryModel.setUpdateTime(System.currentTimeMillis());
        return R.ok(electricityBatteryModelMapper.insert(electricityBatteryModel));
    }

    /**
     * 修改电池型号
     *
     * @param electricityBatteryModel
     * @return
     */
    @Override
    public R updateElectricityBatteryModel(ElectricityBatteryModel electricityBatteryModel) {
        ElectricityBatteryModel electricityBatteryModelDb = getElectricityBatteryModelById(electricityBatteryModel.getId());
        if (Objects.isNull(electricityBatteryModelDb)) {
            log.error("UPDATE_ELECTRICITY_BATTERY_MODEL ERROR ,NOT FOUND ELECTRICITY_BATTERY_MODEL BY ID:{}", electricityBatteryModel.getId());
            return R.failMsg("未找到电池型号!");
        }
        electricityBatteryModel.setUpdateTime(System.currentTimeMillis());
        return R.ok(electricityBatteryModelMapper.updateById(electricityBatteryModel));
    }

    /**
     * 从缓存中获取电池型号
     *
     * @param id
     * @return
     */
    @Override
    public ElectricityBatteryModel getElectricityBatteryModelById(Integer id) {
        ElectricityBatteryModel electricityBatteryModel = null;

        electricityBatteryModel = redisService.getWithHash(ElectricityCabinetConstant.CACHE_ELECTRICITY_BATTERY_MODEL + id, ElectricityBatteryModel.class);
        if (Objects.isNull(electricityBatteryModel)) {
            electricityBatteryModel = electricityBatteryModelMapper.selectById(id);
            if (Objects.nonNull(electricityBatteryModel)) {
                redisService.saveWithHash(ElectricityCabinetConstant.CACHE_ELECTRICITY_BATTERY_MODEL + id, electricityBatteryModel);
            }
        }
        return electricityBatteryModel;
    }

    @Override
    public ElectricityBatteryModel queryById(Integer modelId) {
        return ElectricityBatteryModelMapper.selectById(modelId);
    }
}