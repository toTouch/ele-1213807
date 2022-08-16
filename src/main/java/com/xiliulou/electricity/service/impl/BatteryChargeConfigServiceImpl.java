package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.dto.BatteryMultiConfigDTO;
import com.xiliulou.electricity.entity.BatteryChargeConfig;
import com.xiliulou.electricity.mapper.BatteryChargeConfigMapper;
import com.xiliulou.electricity.query.BatteryChargeConfigQuery;
import com.xiliulou.electricity.service.BatteryChargeConfigService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

/**
 * (BatteryChargeConfig)表服务实现类
 *
 * @author zzlong
 * @since 2022-08-12 14:49:37
 */
@Service("batteryChargeConfigService")
@Slf4j
public class BatteryChargeConfigServiceImpl implements BatteryChargeConfigService {
    @Autowired
    private BatteryChargeConfigMapper batteryChargeConfigMapper;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public BatteryChargeConfig selectByIdFromDB(Long id) {
        return this.batteryChargeConfigMapper.selectById(id);
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public BatteryChargeConfig selectByIdFromCache(Long id) {
        return null;
    }


    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    @Override
    public List<BatteryChargeConfig> selectByPage(int offset, int limit) {
        return this.batteryChargeConfigMapper.selectByPage(offset, limit);
    }

    /**
     * 新增数据
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatteryChargeConfig insert(BatteryChargeConfigQuery batteryChargeConfigQuery) {
        BatteryChargeConfig batteryChargeConfig = new BatteryChargeConfig();
        BeanUtils.copyProperties(batteryChargeConfigQuery, batteryChargeConfig);
        this.batteryChargeConfigMapper.insertOne(batteryChargeConfig);
        return batteryChargeConfig;
    }

    /**
     * 修改数据
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(BatteryChargeConfigQuery batteryChargeConfigQuery) {
        BatteryChargeConfig batteryChargeConfig = new BatteryChargeConfig();
        BeanUtils.copyProperties(batteryChargeConfigQuery, batteryChargeConfig);
        return this.batteryChargeConfigMapper.update(batteryChargeConfig);

    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteById(Long id) {
        return this.batteryChargeConfigMapper.deleteById(id) > 0;
    }

    @Override
    public BatteryChargeConfigQuery selectByElectricityCabinetId(Long electricityCabinetId) {
        BatteryChargeConfigQuery batteryChargeConfigQuery = new BatteryChargeConfigQuery();

        BatteryChargeConfig batteryChargeConfig = this.batteryChargeConfigMapper.selectOne(new LambdaQueryWrapper<BatteryChargeConfig>()
                .eq(BatteryChargeConfig::getElectricityCabinetId, electricityCabinetId).eq(BatteryChargeConfig::getDelFlag, BatteryChargeConfig.DEL_NORMAL));

        if (Objects.isNull(batteryChargeConfig)) {
            return batteryChargeConfigQuery;
        }
        BeanUtils.copyProperties(batteryChargeConfig, batteryChargeConfigQuery);
        batteryChargeConfigQuery.setConfigList(JsonUtil.fromJsonArray(batteryChargeConfig.getConfig(), BatteryMultiConfigDTO.class));

        return batteryChargeConfigQuery;
    }

    @Override
    public int atomicUpdate(BatteryChargeConfigQuery query) {

        BatteryChargeConfig batteryChargeConfig = new BatteryChargeConfig();
        BeanUtils.copyProperties(query, batteryChargeConfig);
        batteryChargeConfig.setConfig(JsonUtil.toJson(query.getConfigList()));
        batteryChargeConfig.setCreateTime(System.currentTimeMillis());
        batteryChargeConfig.setUpdateTime(System.currentTimeMillis());

        return this.batteryChargeConfigMapper.atomicUpdate(batteryChargeConfig);
    }
}
