package com.xiliulou.electricity.service.impl;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.BatteryMaterial;
import com.xiliulou.electricity.mapper.BatteryMaterialMapper;
import com.xiliulou.electricity.query.BatteryMaterialQuery;
import com.xiliulou.electricity.service.BatteryMaterialService;
import com.xiliulou.electricity.service.BatteryModelService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 电池材质(BatteryMaterial)表服务实现类
 *
 * @author zzlong
 * @since 2023-04-11 10:56:47
 */
@Service("batteryMaterialService")
@Slf4j
public class BatteryMaterialServiceImpl implements BatteryMaterialService {
    @Resource
    private BatteryMaterialMapper batteryMaterialMapper;
    @Autowired
    private BatteryModelService batteryModelService;
    @Autowired
    private RedisService redisService;

    @Slave
    @Override
    public List<BatteryMaterial> selectByPage(BatteryMaterialQuery query) {
        List<BatteryMaterial> list = this.batteryMaterialMapper.selectByPage(query);
        if (CollectionUtils.isEmpty(list)) {
            Collections.emptyList();
        }

        return list;
    }

    @Slave
    @Override
    public Integer selectByPageCount(BatteryMaterialQuery query) {
        return this.batteryMaterialMapper.selectByPageCount(query);
    }

    @Override
    public Integer checkExistByName(String name) {
        return this.batteryMaterialMapper.checkExistByName(name);
    }

    @Override
    public Triple<Boolean, String, Object> save(BatteryMaterialQuery batteryMaterialQuery) {
        Integer result = this.checkExistByName(batteryMaterialQuery.getName());
        if (Objects.nonNull(result)) {
            return Triple.of(false, "100343", "电池材质已存在");
        }

        BatteryMaterial batteryMaterial = new BatteryMaterial();
        BeanUtils.copyProperties(batteryMaterialQuery, batteryMaterial);
        batteryMaterial.setDelFlag(BatteryMaterial.DEL_NORMAL);
        batteryMaterial.setCreateTime(System.currentTimeMillis());
        batteryMaterial.setUpdateTime(System.currentTimeMillis());
        this.insert(batteryMaterial);

        return Triple.of(true, null, null);
    }

    @Override
    public Triple<Boolean, String, Object> modify(BatteryMaterialQuery batteryMaterialQuery) {
        BatteryMaterial batteryMaterial = this.queryByIdFromCache(batteryMaterialQuery.getId());
        if(Objects.isNull(batteryMaterial)){
            return Triple.of(true,null,null);
        }

        return Triple.of(true, null, null);
    }

    @Override
    public Triple<Boolean, String, Object> delete(Long id) {
        Integer checkMidExist = batteryModelService.checkMidExist(id);
        if (Objects.nonNull(checkMidExist)) {
            return Triple.of(false, "100345", "已绑定电池型号");
        }

        this.deleteById(id);

        return Triple.of(true, null, null);
    }

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public BatteryMaterial queryByIdFromDB(Long id) {
        return this.batteryMaterialMapper.queryById(id);
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public BatteryMaterial queryByIdFromCache(Long id) {
        BatteryMaterial cacheBatteryMaterial = redisService.getWithHash(CacheConstant.CACHE_BATTERY_MATERIAL + id, BatteryMaterial.class);
        if (Objects.nonNull(cacheBatteryMaterial)) {
            return cacheBatteryMaterial;
        }

        BatteryMaterial batteryMaterial = this.queryByIdFromDB(id);
        if (Objects.isNull(batteryMaterial)) {
            return batteryMaterial;
        }

        redisService.saveWithHash(CacheConstant.CACHE_BATTERY_MATERIAL + id, batteryMaterial);
        return batteryMaterial;
    }

    /**
     * 新增数据
     *
     * @param batteryMaterial 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatteryMaterial insert(BatteryMaterial batteryMaterial) {
        this.batteryMaterialMapper.insertOne(batteryMaterial);
        return batteryMaterial;
    }

    /**
     * 修改数据
     *
     * @param batteryMaterial 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(BatteryMaterial batteryMaterial) {
        return this.batteryMaterialMapper.update(batteryMaterial);

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
        return this.batteryMaterialMapper.deleteById(id) > 0;
    }
}
