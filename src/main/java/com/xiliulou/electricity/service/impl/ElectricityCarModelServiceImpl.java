package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.DS;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mapper.ElectricityCabinetModelMapper;
import com.xiliulou.electricity.mapper.ElectricityCarModelMapper;
import com.xiliulou.electricity.query.ElectricityCabinetModelQuery;
import com.xiliulou.electricity.query.ElectricityCarModelQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * 换电柜型号表(TElectricityCarModel)表服务实现类
 *
 * @author makejava
 * @since 2020-11-25 11:01:04
 */
@Service("electricityCarModelService")
@Slf4j
public class ElectricityCarModelServiceImpl implements ElectricityCarModelService {
    @Resource
    private ElectricityCarModelMapper electricityCarModelMapper;
    @Autowired
    RedisService redisService;
    @Autowired
    ElectricityCabinetService electricityCabinetService;
    @Autowired
    ElectricityCarService electricityCarService;
    @Autowired
    StoreService storeService;


    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ElectricityCarModel queryByIdFromCache(Integer id) {
        //先查缓存
        ElectricityCarModel cacheElectricityCarModel = redisService.getWithHash(ElectricityCabinetConstant.CACHE_ELECTRICITY_CAR_MODEL + id, ElectricityCarModel.class);
        if (Objects.nonNull(cacheElectricityCarModel)) {
            return cacheElectricityCarModel;
        }
        //缓存没有再查数据库
        ElectricityCarModel electricityCarModel = electricityCarModelMapper.selectById(id);
        if (Objects.isNull(electricityCarModel)) {
            return null;
        }
        //插入缓存
        redisService.saveWithHash(ElectricityCabinetConstant.CACHE_ELECTRICITY_CAR_MODEL + id, electricityCarModel);
        return electricityCarModel;
    }


    @Override
    @Transactional
    public R save(ElectricityCarModel electricityCarModel) {
        ElectricityCarModelQuery electricityCarModelQuery=ElectricityCarModelQuery.builder()
                .franchiseeId(electricityCarModel.getFranchiseeId())
                .name(electricityCarModel.getName()).build();
        Integer count = electricityCarModelMapper.queryCount(electricityCarModelQuery);
        if (count > 0) {
            return R.fail("该型号车辆已存在!");
        }
        //租户
        Integer tenantId = TenantContextHolder.getTenantId();
        //插入数据库
        electricityCarModel.setCreateTime(System.currentTimeMillis());
        electricityCarModel.setUpdateTime(System.currentTimeMillis());
        electricityCarModel.setDelFlag(ElectricityCabinetBox.DEL_NORMAL);
        electricityCarModel.setTenantId(tenantId);
        int insert = electricityCarModelMapper.insert(electricityCarModel);
        DbUtils.dbOperateSuccessThen(insert, () -> {
            //插入缓存
            redisService.saveWithHash(ElectricityCabinetConstant.CACHE_ELECTRICITY_CAR_MODEL + electricityCarModel.getId(), electricityCarModel);
            return null;
        });
        return R.ok();
    }

    @Override
    @Transactional
    public R edit(ElectricityCarModel electricityCarModel) {
        if (Objects.isNull(electricityCarModel.getId())) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        ElectricityCarModel oldElectricityCarModel = queryByIdFromCache(electricityCarModel.getId());
        if (Objects.isNull(oldElectricityCarModel)) {
            return R.fail("100005", "未找到车辆型号");
        }
        Integer count = electricityCarService.queryByModelId(electricityCarModel.getId());
        if (count > 0) {
            return R.fail("100006", "型号已绑定车辆，不能操作");
        }
        electricityCarModel.setUpdateTime(System.currentTimeMillis());
        int update = electricityCarModelMapper.updateById(electricityCarModel);
        DbUtils.dbOperateSuccessThen(update, () -> {
            //更新缓存
            redisService.saveWithHash(ElectricityCabinetConstant.CACHE_ELECTRICITY_CAR_MODEL + electricityCarModel.getId(), electricityCarModel);
            return null;
        });
        return R.ok();
    }

    @Override
    @Transactional
    public R delete(Integer id) {
        ElectricityCarModel electricityCarModel = queryByIdFromCache(id);
        if (Objects.isNull(electricityCarModel)) {
            return R.fail("100005", "未找到车辆型号");
        }
        Integer count = electricityCarService.queryByModelId(electricityCarModel.getId());
        if (count > 0) {
            return R.fail("100006", "型号已绑定车辆，不能操作");
        }
        //删除数据库
        electricityCarModel.setId(id);
        electricityCarModel.setUpdateTime(System.currentTimeMillis());
        electricityCarModel.setDelFlag(ElectricityCabinetModel.DEL_DEL);
        int update = electricityCarModelMapper.updateById(electricityCarModel);
        DbUtils.dbOperateSuccessThen(update, () -> {
            //删除缓存
            redisService.delete(ElectricityCabinetConstant.CACHE_ELECTRICITY_CAR_MODEL + id);
            return null;
        });
        return R.ok();
    }

    @Override
    @DS("slave_1")
    public R queryList(ElectricityCarModelQuery electricityCarModelQuery) {
        //查询门店的车辆型号
        Store store = null;
        if (Objects.nonNull(electricityCarModelQuery.getUid())) {
            store = storeService.queryByUid(electricityCarModelQuery.getUid());
        }
        if (Objects.nonNull(electricityCarModelQuery.getStoreId())) {
            store = storeService.queryByIdFromCache(electricityCarModelQuery.getStoreId());
        }
        if (Objects.nonNull(store)){
            electricityCarModelQuery.setFranchiseeId(store.getFranchiseeId());
        }
        return R.ok(electricityCarModelMapper.queryList(electricityCarModelQuery));
    }

    @Override
    public R queryCount(ElectricityCarModelQuery electricityCarModelQuery) {
        return R.ok(electricityCarModelMapper.queryCount(electricityCarModelQuery));
    }

}
