package com.xiliulou.electricity.service.impl;

import com.google.common.collect.Lists;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.DS;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.BatteryConstant;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.dto.RentCarTypeDTO;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mapper.ElectricityCarModelMapper;
import com.xiliulou.electricity.query.ElectricityCarModelQuery;
import com.xiliulou.electricity.query.UserCarQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.ElectricityCarModelVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
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
    FranchiseeService franchiseeService;
    @Autowired
    PictureService pictureService;
    @Autowired
    CarModelTagService carModelTagService;
    @Autowired
    UserCarService userCarService;
    @Autowired
    ElectricityConfigService electricityConfigService;


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
    public Integer insert(ElectricityCarModel electricityCarModel) {
        return electricityCarModelMapper.insert(electricityCarModel);
    }
    
    
    @Override
    @Transactional
    public R save(ElectricityCarModelQuery query) {
        Pair<Boolean, String> verifyResult = verifyCarModelQuery(query);
        if (!verifyResult.getLeft()) {
            return R.failMsg(verifyResult.getRight());
        }

        //校验当前加盟商是否为待迁移加盟商，若是  不允许新建
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(TenantContextHolder.getTenantId());
        if (Objects.isNull(electricityConfig)) {
            log.error("ELE ERROR!not found electricityConfig,tenantId={}", TenantContextHolder.getTenantId());
            return R.fail( "000001", "系统异常");
        }

        if (Objects.equals(electricityConfig.getIsMoveFranchisee(), ElectricityConfig.MOVE_FRANCHISEE_OPEN)) {
            FranchiseeMoveInfo franchiseeMoveInfo = JsonUtil.fromJson(electricityConfig.getFranchiseeMoveInfo(), FranchiseeMoveInfo.class);
            if (Objects.isNull(franchiseeMoveInfo)) {
                log.error("ELE ERROR!not found franchiseeMoveInfo,tenantId={}", TenantContextHolder.getTenantId());
                return R.fail( "100354", "用户加盟商迁移配置信息不存在");
            }

            //判断用户绑定的加盟商是否与待迁移加盟商一致
            if (Objects.equals(query.getFranchiseeId(), franchiseeMoveInfo.getFromFranchiseeId())) {
                log.error("ELE ERROR! move franchisee not apply create car model,tenantId={}", TenantContextHolder.getTenantId());
                return R.fail( "100365", "迁移加盟商不支持新建车辆型号");
            }
        }

        ElectricityCarModelQuery electricityCarModelQuery = ElectricityCarModelQuery.builder()
                .franchiseeId(query.getFranchiseeId())
                .storeId(query.getStoreId())
                .name(query.getName()).build();
        Integer count = electricityCarModelMapper.queryCount(electricityCarModelQuery);
        if (count > 0) {
            return R.fail("该型号车辆已存在!");
        }

        ElectricityCarModel electricityCarModel = new ElectricityCarModel();
        BeanUtils.copyProperties(query, electricityCarModel);
        electricityCarModel.setCreateTime(System.currentTimeMillis());
        electricityCarModel.setUpdateTime(System.currentTimeMillis());
        electricityCarModel.setDelFlag(ElectricityCabinetBox.DEL_NORMAL);
        electricityCarModel.setTenantId(TenantContextHolder.getTenantId());
        int insert = electricityCarModelMapper.insert(electricityCarModel);

        DbUtils.dbOperateSuccessThen(insert, () -> {
            //插入缓存
            redisService.saveWithHash(CacheConstant.CACHE_ELECTRICITY_CAR_MODEL + electricityCarModel.getId(), electricityCarModel);

            //保存车辆标签
            carModelTagService.batchInsert(buildCarModelTagList(query, electricityCarModel));

            return null;
        });

        return R.ok(electricityCarModel.getId());
    }

    @Override
    @Transactional
    public R edit(ElectricityCarModelQuery query) {
        Pair<Boolean, String> verifyResult = verifyCarModelQuery(query);
        if (!verifyResult.getLeft()) {
            return R.failMsg(verifyResult.getRight());
        }

        ElectricityCarModel oldElectricityCarModel = queryByIdFromCache(query.getId());
        if (Objects.isNull(oldElectricityCarModel)) {
            return R.fail("100005", "未找到车辆型号");
        }
        if (!Objects.equals(oldElectricityCarModel.getTenantId(), TenantContextHolder.getTenantId())) {
            return R.ok();
        }


//        Integer count = electricityCarService.queryByModelId(query.getId());
//        if (count > 0) {
//            return R.fail("100006", "型号已绑定车辆，不能操作");
//        }


        ElectricityCarModel updateCarModel = new ElectricityCarModel();
        BeanUtils.copyProperties(query, updateCarModel);

        updateCarModel.setUpdateTime(System.currentTimeMillis());
        updateCarModel.setTenantId(TenantContextHolder.getTenantId());
        int update = electricityCarModelMapper.update(updateCarModel);

        DbUtils.dbOperateSuccessThen(update, () -> {
            redisService.delete(CacheConstant.CACHE_ELECTRICITY_CAR_MODEL + updateCarModel.getId());

            carModelTagService.deleteByCarModelId(updateCarModel.getId().longValue());
            //保存车辆标签
            carModelTagService.batchInsert(buildCarModelTagList(query, updateCarModel));
            return null;
        });

        return R.ok();
    }

    @Override
    @Transactional
    public R delete(Integer id) {
        ElectricityCarModel electricityCarModel = queryByIdFromCache(id);
        if (Objects.isNull(electricityCarModel) || !Objects.equals(electricityCarModel.getTenantId(), TenantContextHolder.getTenantId())) {
            return R.fail("100005", "未找到车辆型号");
        }

        //TODO 优化
        Integer count = electricityCarService.queryByModelId(electricityCarModel.getId());
        if (count > 0) {
            return R.fail("100006", "型号已绑定车辆，不能操作");
        }

        //判断是否有用户绑定该车辆型号
        UserCarQuery userCarQuery = new UserCarQuery();
        userCarQuery.setCarModel(electricityCarModel.getId().longValue());
        userCarQuery.setDelFlag(UserCar.DEL_NORMAL);
        List<UserCar> userCarList=userCarService.selectByQuery(userCarQuery);
        if(!CollectionUtils.isEmpty(userCarList)){
            return R.fail("100256", "车辆型号已绑定用户，不能操作");
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
    @Slave
    public R queryList(ElectricityCarModelQuery electricityCarModelQuery) {
        List<ElectricityCarModelVO> electricityCarModelVOS = electricityCarModelMapper.queryList(electricityCarModelQuery);
        if (CollectionUtils.isEmpty(electricityCarModelVOS)) {
            return R.ok(Collections.EMPTY_LIST);
        }

        List<ElectricityCarModelVO> list = electricityCarModelVOS.parallelStream().peek(item -> {
            if (Objects.nonNull(item.getFranchiseeId())) {
                Franchisee franchisee = franchiseeService.queryByIdFromCache(item.getFranchiseeId());
                item.setFranchiseeName(Objects.nonNull(franchisee) ? franchisee.getName() : "");
            }

            List<CarModelTag> tagList = carModelTagService.selectByCarModelId(item.getId());
            if (!CollectionUtils.isEmpty(tagList)) {
                List<String> tags = tagList.stream().map(CarModelTag::getTitle).collect(Collectors.toList());
                item.setCarModelTag(tags);
            }

            Store store = storeService.queryByIdFromCache(item.getStoreId());
            if (Objects.nonNull(store)) {
                item.setStoreName(store.getName());
            }

        }).collect(Collectors.toList());

        return R.ok(list);
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

        return rentCarTypeDTOS.stream().collect(Collectors.toMap(RentCarTypeDTO::getType, RentCarTypeDTO::getPrice,(item1, item2)->item1));
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
            carModelVO.setPictures(pictureService.pictureParseVO(pictures));
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
        if (Objects.isNull(electricityCarModel) || !Objects.equals(electricityCarModel.getTenantId(), TenantContextHolder.getTenantId())) {
            return carModelVO;
        }

        BeanUtils.copyProperties(electricityCarModel, carModelVO);

        List<Picture> pictures = pictureService.selectByByBusinessId(id);
        carModelVO.setPictures(pictureService.pictureParseVO(pictures));

        List<CarModelTag> carModelTags = carModelTagService.selectByCarModelId(id.intValue());
        if(!CollectionUtils.isEmpty(carModelTags)){
            List<String> tags = carModelTags.stream().map(CarModelTag::getTitle).collect(Collectors.toList());
            carModelVO.setCarModelTag(tags);
        }

        return carModelVO;
    }
    
    @Override
    public R queryPull(Long size, Long offset, Long franchiseeId, String name) {
        return R.ok(electricityCarModelMapper
                .queryPull(size, offset, franchiseeId, name, TenantContextHolder.getTenantId()));
    }

    @Override
    public Triple<Boolean, String, Object> acquireUserCarModelInfo() {

        UserCar userCar = userCarService.selectByUidFromCache(SecurityUtils.getUid());
        if (Objects.isNull(userCar)) {
            return Triple.of(true, "", null);
        }

        ElectricityCarModel electricityCarModel = this.queryByIdFromCache(userCar.getCarModel().intValue());
        if (Objects.nonNull(electricityCarModel.getRentType())) {
            return Triple.of(true, "", electricityCarModel.getRentType());
        }

        return Triple.of(true, "", null);
    }
    
    @Override
    public ElectricityCarModel queryByNameAndStoreId(String name, Long storeId) {
        return electricityCarModelMapper.queryByNameAndStoreId(name, storeId);
    }
    
    @Override
    public R selectByStoreId(ElectricityCarModelQuery electricityCarModelQuery) {
        electricityCarModelQuery.setTenantId(TenantContextHolder.getTenantId());

        List<ElectricityCarModel> electricityCarModels = electricityCarModelMapper.selectByQuery(electricityCarModelQuery);
        if (CollectionUtils.isEmpty(electricityCarModels)) {
            return R.ok(Collections.EMPTY_LIST);
        }

        return R.ok(electricityCarModels);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer updateFranchiseeById(List<ElectricityCarModel> electricityCarModels, Long franchiseeId) {
        for (ElectricityCarModel electricityCarModel : electricityCarModels) {
            electricityCarModel.setFranchiseeId(franchiseeId);
            electricityCarModel.setUpdateTime(System.currentTimeMillis());
            electricityCarModelMapper.update(electricityCarModel);
        }

        return null;
    }

    @Override
    public void moveCarModel(FranchiseeMoveInfo franchiseeMoveInfo) {
        ElectricityCarModelQuery modelQuery = ElectricityCarModelQuery.builder().delFlag(ElectricityCarModel.DEL_NORMAL).
                tenantId(TenantContextHolder.getTenantId())
                .franchiseeId(franchiseeMoveInfo.getFromFranchiseeId()).build();

        List<ElectricityCarModel> electricityCarModels = this.selectByQuery(modelQuery);
        if (CollectionUtils.isEmpty(electricityCarModels)) {
            return;
        }

        //根据新加盟商更新车辆型号绑定的加盟商
        this.updateFranchiseeById(electricityCarModels,franchiseeMoveInfo.getToFranchiseeId());
    }

    private Pair<Boolean, String> verifyCarModelQuery(ElectricityCarModelQuery query) {
        if (NumberConstant.ZERO_BD.compareTo(query.getCarDeposit()) == NumberConstant.ONE) {
            return Pair.of(false, "车辆押金不合法！");
        }

        return Pair.of(true, "");
    }

    private List<CarModelTag> buildCarModelTagList(ElectricityCarModelQuery query, ElectricityCarModel carModel) {
        List<CarModelTag> list = Lists.newArrayList();
        if (StringUtils.isBlank(query.getCarModelTag())) {
            return list;
        }

        List<String> carModelTags = JsonUtil.fromJsonArray(query.getCarModelTag(), String.class);
        if (!CollectionUtils.isEmpty(carModelTags)) {
            for (int i = 0; i < carModelTags.size(); i++) {
                CarModelTag carModelTag = new CarModelTag();
                carModelTag.setSeq(i);
                carModelTag.setTitle(carModelTags.get(i));
                carModelTag.setCarModelId(carModel.getId().longValue());
                carModelTag.setStatus(CarModelTag.STATUS_ENABLE);
                carModelTag.setDelFlag(CarModelTag.DEL_NORMAL);
                carModelTag.setTenantId(carModel.getTenantId());
                carModelTag.setCreateTime(System.currentTimeMillis());
                carModelTag.setUpdateTime(System.currentTimeMillis());
                list.add(carModelTag);
            }
        }

        return list;
    }
}
