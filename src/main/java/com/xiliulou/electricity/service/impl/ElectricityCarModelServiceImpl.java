package com.xiliulou.electricity.service.impl;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.DS;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mapper.ElectricityCarModelMapper;
import com.xiliulou.electricity.query.ElectricityCarModelQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.vo.ElectricityCarModelVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
    @Autowired
    PictureService pictureService;


    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ElectricityCarModel queryByIdFromCache(Integer id) {
        //先查缓存
        ElectricityCarModel cacheElectricityCarModel = redisService.getWithHash(CacheConstant.CACHE_ELECTRICITY_CAR_MODEL + id, ElectricityCarModel.class);
        if (Objects.nonNull(cacheElectricityCarModel)) {
            return cacheElectricityCarModel;
        }
        //缓存没有再查数据库
        ElectricityCarModel electricityCarModel = electricityCarModelMapper.selectById(id);
        if (Objects.isNull(electricityCarModel)) {
            return null;
        }
        //插入缓存
        redisService.saveWithHash(CacheConstant.CACHE_ELECTRICITY_CAR_MODEL + id, electricityCarModel);
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
            redisService.saveWithHash(CacheConstant.CACHE_ELECTRICITY_CAR_MODEL + electricityCarModel.getId(), electricityCarModel);
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
        
        if(!Objects.equals(oldElectricityCarModel.getTenantId(),TenantContextHolder.getTenantId())){
            return R.ok();
        }
        
        Integer count = electricityCarService.queryByModelId(electricityCarModel.getId());
        if (count > 0) {
            return R.fail("100006", "型号已绑定车辆，不能操作");
        }
        electricityCarModel.setUpdateTime(System.currentTimeMillis());
        electricityCarModel.setTenantId(TenantContextHolder.getTenantId());
        int update = electricityCarModelMapper.update(electricityCarModel);
        DbUtils.dbOperateSuccessThen(update, () -> {
            //更新缓存
            redisService.saveWithHash(CacheConstant.CACHE_ELECTRICITY_CAR_MODEL + electricityCarModel.getId(), electricityCarModel);
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
        int update = electricityCarModelMapper.update(electricityCarModel);
        DbUtils.dbOperateSuccessThen(update, () -> {
            //删除缓存
            redisService.delete(CacheConstant.CACHE_ELECTRICITY_CAR_MODEL + id);
            return null;
        });
        return R.ok();
    }

    @Override
    @DS("slave_1")
    public R queryList(ElectricityCarModelQuery electricityCarModelQuery) {
        return R.ok(electricityCarModelMapper.queryList(electricityCarModelQuery));
    }

    @Override
    public R queryCount(ElectricityCarModelQuery electricityCarModelQuery) {
        return R.ok(electricityCarModelMapper.queryCount(electricityCarModelQuery));
    }

    @Override
    public List<ElectricityCarModel> selectByQuery(ElectricityCarModelQuery query) {
        //TODO
        return null;
    }

    @Override
    public List<ElectricityCarModel> selectByPage(ElectricityCarModelQuery query) {
        //TODO
        return null;
    }

    /**
     * 用户端车辆型号分页列表
     * @param query
     * @return
     */
    @Override
    public List<ElectricityCarModelVO> selectList(ElectricityCarModelQuery query) {
        List<ElectricityCarModel> electricityCarModels = this.selectByPage(query);
        if(CollectionUtils.isEmpty(electricityCarModels)){
            return Collections.EMPTY_LIST;
        }

        List<ElectricityCarModelVO> modelVOList = electricityCarModels.parallelStream().map(item -> {
            ElectricityCarModelVO carModelVO = new ElectricityCarModelVO();
            BeanUtils.copyProperties(item, carModelVO);

            List<Picture> pictures = pictureService.selectByByBusinessId(item.getId().longValue());
            carModelVO.setPictures(pictures);
            return carModelVO;
        }).collect(Collectors.toList());

        return modelVOList;
    }

    /**
     * 车辆型号详情
     * @param
     * @return
     */
    @Override
    public ElectricityCarModelVO selectDetailById(Long id) {
        ElectricityCarModelVO carModelVO = new ElectricityCarModelVO();

        ElectricityCarModel electricityCarModel = this.queryByIdFromCache(id.intValue());
        if(Objects.isNull(electricityCarModel) || Objects.equals(electricityCarModel.getTenantId(),TenantContextHolder.getTenantId())){
            return carModelVO;
        }

        BeanUtils.copyProperties(electricityCarModel,carModelVO);

        List<Picture> pictures = pictureService.selectByByBusinessId(id);
        carModelVO.setPictures(pictures);

        return carModelVO;
    }



    @Override
    public R selectByStoreId(ElectricityCarModelQuery electricityCarModelQuery) {
        Store store = storeService.queryByIdFromCache(electricityCarModelQuery.getStoreId());
        if(Objects.isNull(store)){
            return R.ok(Collections.EMPTY_LIST);
        }
    
        ElectricityCarModelQuery modelQuery = new ElectricityCarModelQuery();
        modelQuery.setFranchiseeId(store.getFranchiseeId());
        modelQuery.setOffset(0L);
        modelQuery.setSize(Long.MAX_VALUE);
        List<ElectricityCarModelVO> electricityCarModelVOS = electricityCarModelMapper.queryList(modelQuery);
        if(!CollectionUtils.isEmpty(electricityCarModelVOS)){
            return R.ok(electricityCarModelVOS);
        }
    
        return R.ok(Collections.EMPTY_LIST);
    }
}
