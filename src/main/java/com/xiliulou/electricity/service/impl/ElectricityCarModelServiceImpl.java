package com.xiliulou.electricity.service.impl;

import com.google.common.collect.Lists;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.DS;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.dto.RentCarTypeDTO;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mapper.ElectricityCarModelMapper;
import com.xiliulou.electricity.query.CallBackQuery;
import com.xiliulou.electricity.query.ElectricityCarModelQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.vo.ElectricityCarModelVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
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
    @Autowired
    CarModelTagService carModelTagService;


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
        ElectricityCarModelQuery electricityCarModelQuery = ElectricityCarModelQuery.builder()
                .franchiseeId(electricityCarModel.getFranchiseeId())
                .storeId(electricityCarModel.getStoreId())
                .name(electricityCarModel.getName()).build();
        Integer count = electricityCarModelMapper.queryCount(electricityCarModelQuery);
        if (count > 0) {
            return R.fail("该型号车辆已存在!");
        }

        electricityCarModel.setCreateTime(System.currentTimeMillis());
        electricityCarModel.setUpdateTime(System.currentTimeMillis());
        electricityCarModel.setDelFlag(ElectricityCabinetBox.DEL_NORMAL);
        electricityCarModel.setTenantId(TenantContextHolder.getTenantId());
        int insert = electricityCarModelMapper.insert(electricityCarModel);
        DbUtils.dbOperateSuccessThen(insert, () -> {
            //插入缓存
            redisService.saveWithHash(CacheConstant.CACHE_ELECTRICITY_CAR_MODEL + electricityCarModel.getId(), electricityCarModel);

            //保存车辆标签
            carModelTagService.batchInsert(buildCarModelTagList(electricityCarModel));

            return null;
        });
        return R.ok(electricityCarModel);
    }

    @Override
    @Transactional
    public R edit(ElectricityCarModel electricityCarModel) {

        ElectricityCarModel oldElectricityCarModel = queryByIdFromCache(electricityCarModel.getId());
        if (Objects.isNull(oldElectricityCarModel)) {
            return R.fail("100005", "未找到车辆型号");
        }

        if (!Objects.equals(oldElectricityCarModel.getTenantId(), TenantContextHolder.getTenantId())) {
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

            carModelTagService.deleteByCarModelId(electricityCarModel.getId().longValue());
            //保存车辆标签
            carModelTagService.batchInsert(buildCarModelTagList(electricityCarModel));
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

            carModelTagService.deleteByCarModelId(electricityCarModel.getId().longValue());
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
        return electricityCarModelMapper.selectByQuery(query);
    }

    @Override
    public List<ElectricityCarModel> selectByPage(ElectricityCarModelQuery query) {
        return electricityCarModelMapper.selectByPage(query);
    }

    @Override
    public Map<String, Double> parseRentCarPriceRule(ElectricityCarModel electricityCarModel) {
        Map<String, Double> rentTypeMap = new HashMap<>(2);

        if (StringUtils.isBlank(electricityCarModel.getRentType())) {
            return rentTypeMap;
        }

        List<RentCarTypeDTO> rentCarTypeDTOS = JsonUtil.fromJsonArray(electricityCarModel.getRentType(), RentCarTypeDTO.class);
        if (CollectionUtils.isEmpty(rentCarTypeDTOS)) {
            return rentTypeMap;
        }

        return rentCarTypeDTOS.stream().collect(Collectors.toMap(RentCarTypeDTO::getRentType, RentCarTypeDTO::getPrice));
    }

    /**
     * 用户端车辆型号分页列表
     *
     * @param query
     * @return
     */
    @Override
    public List<ElectricityCarModelVO> selectList(ElectricityCarModelQuery query) {
        List<ElectricityCarModel> electricityCarModels = this.selectByPage(query);
        if (CollectionUtils.isEmpty(electricityCarModels)) {
            return Collections.EMPTY_LIST;
        }

        return electricityCarModels.parallelStream().map(item -> {
            ElectricityCarModelVO carModelVO = new ElectricityCarModelVO();
            BeanUtils.copyProperties(item, carModelVO);

            List<Picture> pictures = pictureService.selectByByBusinessId(item.getId().longValue());
            carModelVO.setPictures(pictures);
            return carModelVO;
        }).collect(Collectors.toList());

    }

    /**
     * 车辆型号详情
     *
     * @param
     * @return
     */
    @Override
    public ElectricityCarModelVO selectDetailById(Long id) {
        ElectricityCarModelVO carModelVO = new ElectricityCarModelVO();

        ElectricityCarModel electricityCarModel = this.queryByIdFromCache(id.intValue());
        if (Objects.isNull(electricityCarModel) || Objects.equals(electricityCarModel.getTenantId(), TenantContextHolder.getTenantId())) {
            return carModelVO;
        }

        BeanUtils.copyProperties(electricityCarModel, carModelVO);

        List<Picture> pictures = pictureService.selectByByBusinessId(id);
        carModelVO.setPictures(pictures);

        return carModelVO;
    }


    @Override
    public R selectByStoreId(ElectricityCarModelQuery electricityCarModelQuery) {
        Store store = storeService.queryByIdFromCache(electricityCarModelQuery.getStoreId());
        if (Objects.isNull(store)) {
            return R.ok(Collections.EMPTY_LIST);
        }

        ElectricityCarModelQuery modelQuery = new ElectricityCarModelQuery();
        modelQuery.setFranchiseeId(store.getFranchiseeId());
        modelQuery.setOffset(0L);
        modelQuery.setSize(Long.MAX_VALUE);
        List<ElectricityCarModelVO> electricityCarModelVOS = electricityCarModelMapper.queryList(modelQuery);
        if (!CollectionUtils.isEmpty(electricityCarModelVOS)) {
            return R.ok(electricityCarModelVOS);
        }

        return R.ok(Collections.EMPTY_LIST);
    }

    private List<CarModelTag> buildCarModelTagList(ElectricityCarModel carModel) {
        if (StringUtils.isBlank(carModel.getCarModelTag())) {
            return null;
        }

        List<CarModelTag> carModelTags = JsonUtil.fromJsonArray(carModel.getCarModelTag(), CarModelTag.class);
        if (!CollectionUtils.isEmpty(carModelTags)) {
            carModelTags.forEach(item -> {
                item.setCarModelId(carModel.getId().longValue());
            });
        }

        return carModelTags;
    }
}
