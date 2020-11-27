package com.xiliulou.electricity.service.impl;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.ElectricityCabinetBox;
import com.xiliulou.electricity.entity.ElectricityCabinetModel;
import com.xiliulou.electricity.mapper.ElectricityCabinetModelMapper;
import com.xiliulou.electricity.query.ElectricityCabinetModelQuery;
import com.xiliulou.electricity.service.ElectricityCabinetModelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * 换电柜型号表(TElectricityCabinetModel)表服务实现类
 *
 * @author makejava
 * @since 2020-11-25 11:01:04
 */
@Service("electricityCabinetModelService")
@Slf4j
public class ElectricityCabinetModelServiceImpl implements ElectricityCabinetModelService {
    @Resource
    private ElectricityCabinetModelMapper electricityCabinetModelMapper;
    @Autowired
    RedisService redisService;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ElectricityCabinetModel queryByIdFromDB(Integer id) {
        return this.electricityCabinetModelMapper.queryById(id);
    }
    
        /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ElectricityCabinetModel queryByIdFromCache(Integer id) {
        //先查缓存
        ElectricityCabinetModel cacheElectricityCabinetModel=redisService.getWithHash(ElectricityCabinetConstant.CACHE_ELECTRICITY_CABINET_MODEL +id,ElectricityCabinetModel.class);
        if(Objects.nonNull(cacheElectricityCabinetModel)){
            return cacheElectricityCabinetModel;
        }
        //缓存没有再查数据库
        ElectricityCabinetModel electricityCabinetModel=electricityCabinetModelMapper.queryById(id);
        if(Objects.isNull(electricityCabinetModel)){
            return null;
        }
        //插入缓存
        redisService.saveWithHash(ElectricityCabinetConstant.CACHE_ELECTRICITY_CABINET_MODEL +id,electricityCabinetModel);
        return electricityCabinetModel;
    }


    /**
     * 新增数据
     *
     * @param electricityCabinetModel 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ElectricityCabinetModel insert(ElectricityCabinetModel electricityCabinetModel) {
        this.electricityCabinetModelMapper.insert(electricityCabinetModel);
        return electricityCabinetModel;
    }

    /**
     * 修改数据
     *
     * @param electricityCabinetModel 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(ElectricityCabinetModel electricityCabinetModel) {
       return this.electricityCabinetModelMapper.update(electricityCabinetModel);
         
    }

    @Override
    public R save(ElectricityCabinetModel electricityCabinetModel) {
        //TODO 判断参数
        //插入数据库
        electricityCabinetModel.setCreateTime(System.currentTimeMillis());
        electricityCabinetModel.setUpdateTime(System.currentTimeMillis());
        electricityCabinetModel.setDelFlag(ElectricityCabinetBox.DEL_NORMAL);
        electricityCabinetModelMapper.insertOne(electricityCabinetModel);
        //插入缓存
        redisService.saveWithHash(ElectricityCabinetConstant.CACHE_ELECTRICITY_CABINET_MODEL +electricityCabinetModel.getId(),electricityCabinetModel);
        return R.ok();
    }

    @Override
    public R edit(ElectricityCabinetModel electricityCabinetModel) {
        if(Objects.isNull(electricityCabinetModel.getId())){
            return R.fail("SYSTEM.0007","不合法的参数");
        }
        ElectricityCabinetModel oldElectricityCabinetModel = queryByIdFromCache(electricityCabinetModel.getId());
        if(Objects.isNull(oldElectricityCabinetModel)){
            return R.fail("SYSTEM.0004","未找到换电柜型号");
        }
        electricityCabinetModel.setUpdateTime(System.currentTimeMillis());
        electricityCabinetModelMapper.update(electricityCabinetModel);
        //更新缓存
        redisService.saveWithHash(ElectricityCabinetConstant.CACHE_ELECTRICITY_CABINET_MODEL +electricityCabinetModel.getId(),electricityCabinetModel);
        return R.ok();
    }

    @Override
    public R delete(Integer id) {
        ElectricityCabinetModel electricityCabinetModel = queryByIdFromCache(id);
        if(Objects.isNull(electricityCabinetModel)){
            return R.fail("SYSTEM.0004","未找到换电柜型号");
        }
        //删除数据库
        electricityCabinetModel.setId(id);
        electricityCabinetModel.setUpdateTime(System.currentTimeMillis());
        electricityCabinetModel.setDelFlag(ElectricityCabinetModel.DEL_DEL);
        electricityCabinetModelMapper.update(electricityCabinetModel);
        //删除缓存
        redisService.deleteKeys(ElectricityCabinetConstant.CACHE_ELECTRICITY_CABINET_MODEL +id);
        return R.ok();
    }

    @Override
    public R queryList(ElectricityCabinetModelQuery electricityCabinetModelQuery) {
        List<ElectricityCabinetModel> electricityCabinetModelList= electricityCabinetModelMapper.queryList(electricityCabinetModelQuery);
        return R.ok(electricityCabinetModelList);
    }

}