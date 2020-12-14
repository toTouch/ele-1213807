package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.ElectricityBatteryModel;
import com.xiliulou.electricity.mapper.ElectricityBatteryModelMapper;
import com.xiliulou.electricity.service.ElectricityBatteryModelService;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * 电池型号(ElectricityBatteryModel)表服务实现类
 *
 * @author makejava
 * @since 2020-11-26 14:44:44
 */
@Service
@Slf4j
public class ElectricityBatteryModelServiceImpl implements ElectricityBatteryModelService {
    @Resource
    private ElectricityBatteryModelMapper electricityBatteryModelMapper;
    @Autowired
    ElectricityBatteryService electricityBatteryService;
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
        int rows = electricityBatteryModelMapper.updateById(electricityBatteryModel);
        if (rows > 0) {
            delElectricityBatteryModelCacheById(electricityBatteryModel.getId());
            return R.ok();
        } else {
            return R.failMsg("修改失败!");
        }
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

    /**
     * 删除型号缓存
     *
     * @param id
     */
    @Override
    public void delElectricityBatteryModelCacheById(Integer id) {
        redisService.deleteKeys(ElectricityCabinetConstant.CACHE_ELECTRICITY_BATTERY_MODEL + id);
    }

    /**
     * 删除型号
     *
     * @param id
     * @return
     */
    @Override
    public R delElectricityBatteryModelById(Integer id) {
        ElectricityBatteryModel electricityBatteryModelDb = getElectricityBatteryModelById(id);
        if (Objects.isNull(electricityBatteryModelDb)) {
            log.error("DELETE ELECTRICITY_BATTERY_MODEL  ERROR ,NOT FOUND  ELECTRICITY_BATTERY_MODEL ID:{}", id);
            return R.failMsg("电池型号不存在!");
        }
        Integer count = electricityBatteryService.count(Wrappers.<ElectricityBattery>lambdaQuery().eq(ElectricityBattery::getModelId, id));
        if (count > 0) {
            log.error("DELETE ELECTRICITY_BATTERY_MODEL  ERROR , FOUND  ELECTRICITY_BATTERY  USING ID:{}", id);
            return R.failMsg("还存在电池使用此型号!");
        }
        int rows = electricityBatteryModelMapper.deleteById(id);
        if (rows > 0) {
            delElectricityBatteryModelCacheById(id);
            return R.ok();
        }
        return R.failMsg("删除失败!");
    }

    /**
     * 分页
     *
     * @param offset
     * @param size
     * @param name
     * @return
     */
    @Override

    public R getElectricityBatteryModelPage(Long offset, Long size, String name) {
        return R.ok(electricityBatteryModelMapper.getElectricityBatteryModelPage(offset, size, name));
    }
}