package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.google.api.client.util.Lists;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.StringConstant;
import com.xiliulou.electricity.entity.ElectricityCabinetBox;
import com.xiliulou.electricity.entity.ElectricityCabinetModel;
import com.xiliulou.electricity.mapper.ElectricityCabinetModelMapper;
import com.xiliulou.electricity.query.ElectricityCabinetModelQuery;
import com.xiliulou.electricity.service.ElectricityCabinetModelService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.vo.asset.ElectricityCabinetModelVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
    
    @Autowired
    ElectricityCabinetService electricityCabinetService;
    
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ElectricityCabinetModel queryByIdFromCache(Integer id) {
        //先查缓存
        ElectricityCabinetModel cacheElectricityCabinetModel = redisService.getWithHash(CacheConstant.CACHE_ELECTRICITY_CABINET_MODEL + id, ElectricityCabinetModel.class);
        if (Objects.nonNull(cacheElectricityCabinetModel)) {
            return cacheElectricityCabinetModel;
        }
        //缓存没有再查数据库
        ElectricityCabinetModel electricityCabinetModel = electricityCabinetModelMapper.selectById(id);
        if (Objects.isNull(electricityCabinetModel)) {
            return null;
        }
        //插入缓存
        redisService.saveWithHash(CacheConstant.CACHE_ELECTRICITY_CABINET_MODEL + id, electricityCabinetModel);
        return electricityCabinetModel;
    }
    
    
    @Override
    @Transactional
    public R save(ElectricityCabinetModel electricityCabinetModel) {
        //租户
        Integer tenantId = TenantContextHolder.getTenantId();
        //插入数据库
        electricityCabinetModel.setCreateTime(System.currentTimeMillis());
        electricityCabinetModel.setUpdateTime(System.currentTimeMillis());
        electricityCabinetModel.setDelFlag(ElectricityCabinetBox.DEL_NORMAL);
        electricityCabinetModel.setTenantId(tenantId);
        int insert = electricityCabinetModelMapper.insert(electricityCabinetModel);
        DbUtils.dbOperateSuccessThen(insert, () -> {
            //插入缓存
            redisService.saveWithHash(CacheConstant.CACHE_ELECTRICITY_CABINET_MODEL + electricityCabinetModel.getId(), electricityCabinetModel);
            return null;
        });
        return R.ok();
    }
    
    @Override
    @Transactional
    public R edit(ElectricityCabinetModel electricityCabinetModel) {
        
        //租户
        Integer tenantId = TenantContextHolder.getTenantId();
        
        if (Objects.isNull(electricityCabinetModel) || !Objects.equals(tenantId, electricityCabinetModel.getTenantId())) {
            return R.ok();
        }
        
        if (Objects.isNull(electricityCabinetModel.getId())) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        ElectricityCabinetModel oldElectricityCabinetModel = queryByIdFromCache(electricityCabinetModel.getId());
        if (Objects.isNull(oldElectricityCabinetModel)) {
            return R.fail("ELECTRICITY.0004", "未找到换电柜型号");
        }
        
        if (!Objects.equals(tenantId, oldElectricityCabinetModel.getTenantId())) {
            return R.ok();
        }
        
        Integer count = electricityCabinetService.queryByModelId(electricityCabinetModel.getId());
        if (count > 0) {
            return R.fail("ELECTRICITY.0011", "型号已绑定换电柜，不能操作");
        }
        electricityCabinetModel.setUpdateTime(System.currentTimeMillis());
        int update = electricityCabinetModelMapper.update(electricityCabinetModel);
        DbUtils.dbOperateSuccessThen(update, () -> {
            //更新缓存
            redisService.saveWithHash(CacheConstant.CACHE_ELECTRICITY_CABINET_MODEL + electricityCabinetModel.getId(), electricityCabinetModel);
            return null;
        });
        return R.ok();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R delete(Integer id) {
        
        //租户
        Integer tenantId = TenantContextHolder.getTenantId();
        
        ElectricityCabinetModel electricityCabinetModel = queryByIdFromCache(id);
        if (Objects.isNull(electricityCabinetModel)) {
            return R.fail("ELECTRICITY.0004", "未找到换电柜型号");
        }
        
        if (!Objects.equals(tenantId, electricityCabinetModel.getTenantId())) {
            return R.ok();
        }
        
        Integer count = electricityCabinetService.queryByModelId(electricityCabinetModel.getId());
        if (count > 0) {
            return R.fail("ELECTRICITY.0011", "型号已绑定换电柜，不能操作");
        }
        //删除数据库
        electricityCabinetModel.setId(id);
        electricityCabinetModel.setUpdateTime(System.currentTimeMillis());
        electricityCabinetModel.setDelFlag(ElectricityCabinetModel.DEL_DEL);
        int update = electricityCabinetModelMapper.update(electricityCabinetModel);
        DbUtils.dbOperateSuccessThen(update, () -> {
            //删除缓存
            redisService.delete(CacheConstant.CACHE_ELECTRICITY_CABINET_MODEL + id);
            return null;
        });
        return R.ok();
    }
    
    @Override
    @Slave
    public R queryList(ElectricityCabinetModelQuery electricityCabinetModelQuery) {
        return R.ok(electricityCabinetModelMapper.queryList(electricityCabinetModelQuery));
    }
    
    @Slave
    @Override
    public R queryCount(ElectricityCabinetModelQuery electricityCabinetModelQuery) {
        return R.ok(electricityCabinetModelMapper.queryCount(electricityCabinetModelQuery));
    }
    
    @Override
    public ElectricityCabinetModel selectByNum(Integer num, Integer tenantId) {
        return electricityCabinetModelMapper.selectByNum(num, tenantId);
    }
    
    @Override
    public Integer insert(ElectricityCabinetModel cabinetModelInsert) {
        return electricityCabinetModelMapper.insert(cabinetModelInsert);
    }
    
    @Override
    public List<ElectricityCabinetModelVO> selectListElectricityCabinetModel(ElectricityCabinetModelQuery electricityCabinetModelQuery) {
        List<ElectricityCabinetModel> electricityCabinetModels = electricityCabinetModelMapper.queryList(electricityCabinetModelQuery);
        return electricityCabinetModels.stream().map(electricityCabinetModel -> {
            ElectricityCabinetModelVO cabinetModelVo = new ElectricityCabinetModelVO();
            BeanUtil.copyProperties(electricityCabinetModel, cabinetModelVo);
            
            // 赋值复合字段
            StringBuilder manufacturerNameAndModelName = new StringBuilder();
            if (StringUtils.isNotBlank(electricityCabinetModel.getManufacturerName())) {
                manufacturerNameAndModelName.append(electricityCabinetModel.getManufacturerName());
            }
            
            if (StringUtils.isNotBlank(manufacturerNameAndModelName.toString())) {
                manufacturerNameAndModelName.append(StringConstant.FORWARD_SLASH);
            }
            
            if (StringUtils.isNotBlank(electricityCabinetModel.getName())) {
                manufacturerNameAndModelName.append(electricityCabinetModel.getName());
            }
            cabinetModelVo.setManufacturerNameAndModelName(manufacturerNameAndModelName.toString());
            return cabinetModelVo;
        }).collect(Collectors.toList());
    }
    
    @Override
    public List<ElectricityCabinetModel> selectListByNum(Integer num, Integer tenantId) {
        List<ElectricityCabinetModel> modelList = electricityCabinetModelMapper.selectListByNum(num, tenantId);
        if (CollectionUtils.isEmpty(modelList)) {
            return Lists.newArrayList();
        }
        return modelList;
    }
}
