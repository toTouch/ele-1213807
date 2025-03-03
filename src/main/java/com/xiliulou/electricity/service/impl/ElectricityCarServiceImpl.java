package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.api.client.util.Lists;
import com.google.common.collect.Maps;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.clickhouse.service.ClickHouseService;
import com.xiliulou.core.utils.TimeUtils;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.DS;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.bo.asset.ElectricityCarBO;
import com.xiliulou.electricity.constant.AssetConstant;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.domain.car.CarInfoDO;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.entity.clickhouse.CarAttr;
import com.xiliulou.electricity.enums.DelFlagEnum;
import com.xiliulou.electricity.enums.asset.AssetTypeEnum;
import com.xiliulou.electricity.enums.asset.StockStatusEnum;
import com.xiliulou.electricity.enums.asset.WarehouseOperateTypeEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.mapper.CarAttrMapper;
import com.xiliulou.electricity.mapper.CarMoveRecordMapper;
import com.xiliulou.electricity.mapper.ElectricityCarMapper;
import com.xiliulou.electricity.query.*;
import com.xiliulou.electricity.query.asset.AssetEnableExitWarehouseQueryModel;
import com.xiliulou.electricity.query.jt808.CarPositionReportQuery;
import com.xiliulou.electricity.query.asset.AssetBatchExitWarehouseQueryModel;
import com.xiliulou.electricity.query.asset.ElectricityCarUpdateFranchiseeAndStoreQueryModel;
import com.xiliulou.electricity.query.asset.ElectricityCarListSnByFranchiseeQueryModel;
import com.xiliulou.electricity.request.asset.AssetBatchExitWarehouseRequest;
import com.xiliulou.electricity.request.asset.AssetSnWarehouseRequest;
import com.xiliulou.electricity.request.asset.CarAddRequest;
import com.xiliulou.electricity.request.asset.CarBatchSaveExcelRequest;
import com.xiliulou.electricity.request.asset.CarBatchSaveRequest;
import com.xiliulou.electricity.request.asset.CarOutWarehouseRequest;
import com.xiliulou.electricity.request.asset.CarUpdateRequest;
import com.xiliulou.electricity.request.asset.ElectricityCarBatchUpdateFranchiseeAndStoreRequest;
import com.xiliulou.electricity.request.asset.ElectricityCarSnSearchRequest;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.service.asset.AssetWarehouseRecordService;
import com.xiliulou.electricity.service.asset.AssetWarehouseService;
import com.xiliulou.electricity.service.retrofit.Jt808RetrofitService;
import com.xiliulou.electricity.service.retrofit.Jt808RetrofitService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.*;
import com.xiliulou.electricity.vo.asset.AssetWarehouseNameVO;
import com.xiliulou.electricity.web.query.jt808.Jt808DeviceControlRequest;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 换电柜表(TElectricityCar)表服务实现类
 *
 * @author makejava
 * @since 2022-06-06 16:00:14
 */
@Service("electricityCarService")
@Slf4j
public class ElectricityCarServiceImpl implements ElectricityCarService {
    
    @Resource
    private ElectricityCarMapper electricityCarMapper;
    
    @Resource
    CarAttrMapper carAttrMapper;
    
    @Autowired
    RedisService redisService;
    
    @Autowired
    ElectricityCarModelService electricityCarModelService;
    
    @Autowired
    UserInfoService userInfoService;
    
    @Autowired
    EleBindCarRecordService eleBindCarRecordService;
    
    @Autowired
    ElectricityBatteryService electricityBatteryService;
    
    @Autowired
    UserCarService userCarService;
    
    @Autowired
    UserCarMemberCardService userCarMemberCardService;
    
    @Autowired
    UserCarDepositService userCarDepositService;
    
    @Autowired
    Jt808RetrofitService jt808RetrofitService;
    
    @Autowired
    ClickHouseService clickHouseService;
    
    @Autowired
    Jt808CarService jt808CarService;
    
    @Autowired
    StoreService storeService;
    
    @Autowired
    FranchiseeService franchiseeService;
    
    @Autowired
    CarModelTagService carModelTagService;
    
    @Autowired
    PictureService pictureService;
    
    @Autowired
    ElectricityConfigService electricityConfigService;
    
    @Autowired
    CarLockCtrlHistoryService carLockCtrlHistoryService;
    
    @Resource
    private CarMoveRecordMapper carMoveRecordMapper;
    
    @Autowired
    private AssetWarehouseService assetWarehouseService;
    
    @Resource
    private AssetWarehouseRecordService assetWarehouseRecordService;
    
    /**
     * 根据ID更新车辆绑定用户，包含绑定、解绑
     *
     * @param electricityCarUpdate
     * @return true(成功)、false(失败)
     */
    @Override
    public boolean updateCarBindStatusById(ElectricityCar electricityCarUpdate) {
        if (!ObjectUtils.allNotNull(electricityCarUpdate, electricityCarUpdate.getId())) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }
        
        Integer num = electricityCarMapper.updateCarBindStatusById(electricityCarUpdate);
        redisService.delete(CacheConstant.CACHE_ELECTRICITY_CAR + electricityCarUpdate.getId());
        
        return num >= 0;
    }
    
    /**
     * 根据用户ID查询车辆信息
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @return 车辆信息
     */
    @Slave
    @Override
    public ElectricityCar selectByUid(Integer tenantId, Long uid) {
        if (!ObjectUtils.allNotNull(tenantId, tenantId)) {
            return null;
        }
        LambdaQueryWrapper<ElectricityCar> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ElectricityCar::getTenantId, tenantId).eq(ElectricityCar::getUid, uid).eq(ElectricityCar::getDelFlag, DelFlagEnum.OK.getCode());
        return electricityCarMapper.selectOne(queryWrapper);
    }
    
    /**
     * 根据车辆型号ID判定，是否存在未租车辆
     *
     * @param carModelId 车辆型号ID
     * @return true(存在)、false(不存在)
     */
    @Slave
    @Override
    public boolean checkUnleasedByCarModelId(Integer carModelId) {
        if (ObjectUtils.isEmpty(carModelId)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }
        LambdaQueryWrapper<ElectricityCar> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ElectricityCar::getModelId, carModelId).eq(ElectricityCar::getDelFlag, DelFlagEnum.OK.getCode())
                .eq(ElectricityCar::getStatus, ElectricityCar.STATUS_NOT_RENT);
        Integer count = electricityCarMapper.selectCount(queryWrapper);
        return count > 0;
    }
    
    /**
     * 根据车辆型号ID，判定是否进行绑定
     *
     * @param carModelId 车辆型号ID
     * @return true(绑定)、false(未绑定)
     */
    @Slave
    @Override
    public boolean checkBindingByCarModelId(Integer carModelId) {
        if (ObjectUtils.isEmpty(carModelId)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }
        LambdaQueryWrapper<ElectricityCar> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ElectricityCar::getModelId, carModelId).eq(ElectricityCar::getDelFlag, DelFlagEnum.OK.getCode());
        Integer count = electricityCarMapper.selectCount(queryWrapper);
        return count > 0;
    }
    
    /**
     * 根据 uid 查询车辆信息<br /> 复合查询，车辆、门店、车辆经纬度
     *
     * @param tenantId 租户ID
     * @param carId    车辆ID
     * @return
     */
    @Slave
    @Override
    public CarInfoDO queryByCarId(Integer tenantId, Long carId) {
        if (!ObjectUtils.allNotNull(tenantId, carId)) {
            return null;
        }
        
        CarInfoDO carInfoDO = electricityCarMapper.queryByCarId(tenantId, carId);
        if (ObjectUtils.isEmpty(carInfoDO)) {
            return null;
        }
        
        return carInfoDO;
    }
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ElectricityCar queryByIdFromCache(Integer id) {
        //先查缓存
        ElectricityCar cacheElectricityCar = redisService.getWithHash(CacheConstant.CACHE_ELECTRICITY_CAR + id, ElectricityCar.class);
        if (Objects.nonNull(cacheElectricityCar)) {
            return cacheElectricityCar;
        }
        //缓存没有再查数据库
        ElectricityCar electricityCar = electricityCarMapper.selectById(id);
        if (Objects.isNull(electricityCar)) {
            return null;
        }
        //放入缓存
        redisService.saveWithHash(CacheConstant.CACHE_ELECTRICITY_CAR + id, electricityCar);
        return electricityCar;
    }

    @Override
    public R saveV2(CarAddRequest carAddRequest) {
        //操作频繁
        boolean result = redisService.setNx(CacheConstant.CAR_SAVE_UID + SecurityUtils.getUid(), "1", 3 * 1000L, false);
        if (!result) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }
        
        //换电柜车辆
        ElectricityCar electricityCar = new ElectricityCar();
        BeanUtil.copyProperties(carAddRequest, electricityCar);
        electricityCar.setTenantId(TenantContextHolder.getTenantId());
        electricityCar.setCreateTime(System.currentTimeMillis());
        electricityCar.setUpdateTime(System.currentTimeMillis());
        electricityCar.setDelFlag(ElectricityCabinet.DEL_NORMAL);
        electricityCar.setStoreId(NumberConstant.ZERO_L);
        electricityCar.setStockStatus(StockStatusEnum.STOCK.getCode());
        electricityCar.setLicensePlateNumber(carAddRequest.getLicensePlateNumber());
        electricityCar.setVin(carAddRequest.getVin());
        electricityCar.setMotorNumber(carAddRequest.getMotorNumber());
        
        //查找车辆型号
        ElectricityCarModel electricityCarModel = electricityCarModelService.queryByIdFromCache(electricityCar.getModelId());
        if (Objects.isNull(electricityCarModel)) {
            return R.fail("100005", "未找到车辆型号");
        }
        
        ElectricityCar existElectricityCar = electricityCarMapper.selectBySn(carAddRequest.getSn(), TenantContextHolder.getTenantId());
        
        if (Objects.nonNull(existElectricityCar)) {
            return R.fail("100017", "已存在该编号车辆");
        }
        
        electricityCar.setModel(electricityCarModel.getName());
        electricityCar.setModelId(electricityCarModel.getId());
        
        electricityCarMapper.insert(electricityCar);
        
        // 异步记录
        Long warehouseId = carAddRequest.getWarehouseId();
        if (Objects.nonNull(warehouseId) && !Objects.equals(warehouseId, NumberConstant.ZERO_L)) {
            Long uid = Objects.requireNonNull(SecurityUtils.getUserInfo()).getUid();
            String sn = carAddRequest.getSn();
            
            assetWarehouseRecordService.asyncRecordOne(TenantContextHolder.getTenantId(), uid, warehouseId, sn, AssetTypeEnum.ASSET_TYPE_CAR.getCode(),
                    WarehouseOperateTypeEnum.WAREHOUSE_OPERATE_TYPE_IN.getCode());
        }
        
        return R.ok(electricityCar.getId());
    }
    
    @Override
    @Transactional
    public R delete(Integer id) {
        
        Integer tenantId = TenantContextHolder.getTenantId();
        
        ElectricityCar electricityCar = queryByIdFromCache(id);
        if (Objects.isNull(electricityCar)) {
            return R.fail("100007", "未找到车辆");
        }
        
        if (!Objects.equals(tenantId, electricityCar.getTenantId())) {
            return R.ok();
        }
        
        if (Objects.nonNull(electricityCar.getUid()) || StringUtils.isNotBlank(electricityCar.getUserName())) {
            return R.fail("100231", "车辆已绑定用户！");
        }
        
        //删除数据库
        electricityCar.setId(id);
        electricityCar.setUpdateTime(System.currentTimeMillis());
        electricityCar.setDelFlag(ElectricityCar.DEL_DEL);
        electricityCar.setWarehouseId(NumberConstant.ZERO_L);
        int update = electricityCarMapper.updateById(electricityCar);
        DbUtils.dbOperateSuccessThen(update, () -> {
            redisService.delete(CacheConstant.CACHE_ELECTRICITY_CAR + id);
            return null;
        });
        return R.ok();
    }
    
    @Override
    @Slave
    public R queryList(ElectricityCarQuery electricityCarQuery) {
        List<ElectricityCarVO> electricityCarVOS = electricityCarMapper.queryList(electricityCarQuery);
        if (CollectionUtils.isEmpty(electricityCarVOS)) {
            return R.ok(Collections.EMPTY_LIST);
        }
        
        // 获取库房名称列表 根据库房id查询库房名称，不需要过滤库房状态是已删除的
        List<Long> warehouseIdList = electricityCarVOS.stream().map(ElectricityCarVO::getWarehouseId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        List<AssetWarehouseNameVO> assetWarehouseNameVOS = assetWarehouseService.selectByIdList(warehouseIdList);
        
        Map<Long, String> warehouseNameVOMap = Maps.newHashMap();
        if (CollectionUtils.isNotEmpty(assetWarehouseNameVOS)) {
            warehouseNameVOMap = assetWarehouseNameVOS.stream().collect(Collectors.toMap(AssetWarehouseNameVO::getId, AssetWarehouseNameVO::getName, (item1, item2) -> item2));
        }
        
        Map<Long, String> finalWarehouseNameVOMap = warehouseNameVOMap;
        List<ElectricityCarVO> carVOList = electricityCarVOS.stream().peek(item -> {
            ElectricityBattery electricityBattery = electricityBatteryService.queryByUid(item.getUid());
            if (Objects.nonNull(electricityBattery)) {
                item.setBatterySn(electricityBattery.getSn());
            }
            
            // 设置用户名称
            UserInfo userInfo = userInfoService.queryByUidFromCache(item.getUid());
            if (Objects.nonNull(userInfo)) {
                item.setUserName(userInfo.getName());
            }
            
            // 设置门店名称
            Store store = storeService.queryByIdFromCache(Long.valueOf(item.getStoreId()));
            if (Objects.nonNull(store)) {
                item.setStoreName(store.getName());
            }
            
            if (finalWarehouseNameVOMap.containsKey(item.getWarehouseId())) {
                item.setWarehouseName(finalWarehouseNameVOMap.get(item.getWarehouseId()));
            }
            
        }).collect(Collectors.toList());
        
        return R.ok(carVOList);
    }
    
    @Override
    public Integer queryByModelId(Integer id) {
        return electricityCarMapper.selectCount(Wrappers.<ElectricityCar>lambdaQuery().eq(ElectricityCar::getModelId, id).eq(ElectricityCar::getDelFlag, ElectricityCar.DEL_NORMAL)
                .eq(ElectricityCar::getTenantId, TenantContextHolder.getTenantId()));
    }
    
    @Slave
    @Override
    public R queryCount(ElectricityCarQuery electricityCarQuery) {
        return R.ok(electricityCarMapper.queryCount(electricityCarQuery));
    }
    
    @Override
    public Integer update(ElectricityCar updateElectricityCar) {
        int update = electricityCarMapper.updateById(updateElectricityCar);
        DbUtils.dbOperateSuccessThen(update, () -> {
            //更新缓存
            redisService.delete(CacheConstant.CACHE_ELECTRICITY_CAR + updateElectricityCar.getId());
            return null;
        });
        return update;
    }
    
    @Override
    public Integer carUnBindUser(ElectricityCar updateElectricityCar) {
        int update = electricityCarMapper.updateBindUser(updateElectricityCar);
        DbUtils.dbOperateSuccessThen(update, () -> {
            //更新缓存
            redisService.delete(CacheConstant.CACHE_ELECTRICITY_CAR + updateElectricityCar.getId());
            return null;
        });
        return update;
    }
    
    @Override
    public Integer updateLockTypeByIds(List<Long> tempIds, Integer typeLock) {
        int update = electricityCarMapper.updateLockTypeById(tempIds, typeLock);
        //更新缓存
        DbUtils.dbOperateSuccessThen(update, () -> {
            tempIds.forEach(id -> {
                redisService.delete(CacheConstant.CACHE_ELECTRICITY_CAR + id);
            });
            return null;
        });
        return update;
    }
    
    
    @Override
    public R attrList(Long beginTime, Long endTime) {
        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

      /*  String sn = null;
        UserCar userCar = userCarService.selectByUidFromCache(user.getUid());
        if (Objects.isNull(userCar)) {
            Integer tenantId = TenantContextHolder.getTenantId();
            ElectricityCar electricityCar = selectByUid(tenantId, user.getUid());
            if (ObjectUtils.isEmpty(electricityCar)) {
                log.error("query  ERROR! not found car! uid:{} ", user.getUid());
                return R.fail("100007", "未找到车辆");
            }
            sn = electricityCar.getSn();
        } else {
            if (StringUtils.isEmpty(userCar.getSn())) {
                log.error("query  ERROR! not found BatterySn! uid:{} ", user.getUid());
                return R.fail("100007", "未找到车辆");
            }
            sn = userCar.getSn();
        }*/
        
        Integer tenantId = TenantContextHolder.getTenantId();
        ElectricityCar electricityCar = selectByUid(tenantId, user.getUid());
        if (ObjectUtils.isEmpty(electricityCar)) {
            throw new BizException("100015", "用户未绑定车辆");
        }
        
        String begin = TimeUtils.convertToStandardFormatTime(beginTime);
        String end = TimeUtils.convertToStandardFormatTime(endTime);
        
        List<CarAttr> query = jt808CarService.queryListBySn(electricityCar.getSn(), begin, end);
        if (CollectionUtils.isEmpty(query)) {
            query = new ArrayList<>();
        }
        
        List<CarGpsVo> result = query.parallelStream()
                .map(e -> new CarGpsVo().setLatitude(e.getLatitude()).setLongitude(e.getLongitude()).setDevId(e.getDevId()).setCreateTime(e.getCreateTime().getTime()))
                .collect(Collectors.toList());
        return R.ok(result);
    }
    
    @Override
    @DS(value = "clickhouse")
    public CarAttr queryLastReportPointBySn(String sn) {
        return carAttrMapper.queryLastReportPointBySn(sn);
    }
    
    @Slave
    @Override
    public R queryElectricityCarMove(Long storeId, String sn, Long size, Long offset) {
        List<ElectricityCarMoveVo> queryList = electricityCarMapper.queryEnableMoveCarByStoreId(storeId, sn, size, offset, TenantContextHolder.getTenantId());
        return R.ok(queryList);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R electricityCarMove(ElectricityCarMoveQuery electricityCarMoveQuery) {
        List<Long> carIds = electricityCarMoveQuery.getCarIds();
        if (CollectionUtils.isEmpty(carIds)) {
            return R.ok();
        }
        
        if (carIds.size() > 50) {
            log.error("ELECTRICITY_CAR_MOVE ERROR! car size too long ！sourceStore={}， targetStore={}, size={}", electricityCarMoveQuery.getSourceSid(),
                    electricityCarMoveQuery.getTargetSid(), carIds.size());
            return R.fail("100270", "迁移车辆数量过多");
        }
        
        if (Objects.equals(electricityCarMoveQuery.getSourceSid(), electricityCarMoveQuery.getTargetSid())) {
            log.error("ELECTRICITY_CAR_MOVE ERROR! Same store ！sourceStore={}， targetStore={}", electricityCarMoveQuery.getSourceSid(), electricityCarMoveQuery.getTargetSid());
            return R.fail("ELECTRICITY.0018", "车辆迁移门店不能相同");
        }
        
        Integer tenantId = TenantContextHolder.getTenantId();
        
        Store targetStore = storeService.queryByIdFromCache(electricityCarMoveQuery.getTargetSid());
        if (Objects.isNull(targetStore)) {
            log.error("ELECTRICITY_CAR_MOVE ERROR! not found store！storeId={}", electricityCarMoveQuery.getTargetSid());
            return R.fail("ELECTRICITY.0018", "未找到门店");
        }
        
        Store sourceStore = storeService.queryByIdFromCache(electricityCarMoveQuery.getSourceSid());
        if (Objects.isNull(sourceStore)) {
            log.error("ELECTRICITY_CAR_MOVE ERROR! not found store！storeId={}", electricityCarMoveQuery.getSourceSid());
            return R.fail("ELECTRICITY.0018", "未找到门店");
        }
        
        if (!Objects.equals(targetStore.getTenantId(), TenantContextHolder.getTenantId()) || !Objects.equals(sourceStore.getTenantId(), TenantContextHolder.getTenantId())) {
            return R.ok();
        }
        
        Franchisee franchisee = franchiseeService.queryByIdFromCache(targetStore.getFranchiseeId());
        if (Objects.isNull(franchisee)) {
            log.error("ELECTRICITY_CAR_MOVE ERROR! not found franchisee！franchiseeId={}", targetStore.getFranchiseeId());
            return R.fail("ELECTRICITY.0038", "未找到加盟商");
        }
        
        List<ElectricityCar> queryList = electricityCarMapper.queryModelIdBySidAndIds(carIds, electricityCarMoveQuery.getSourceSid(), ElectricityCar.STATUS_NOT_RENT,
                TenantContextHolder.getTenantId());
        if (CollectionUtils.isEmpty(queryList) || queryList.size() != carIds.size()) {
            log.error("ELECTRICITY_CAR_MOVE ERROR! has illegal cars！carIds={}", carIds);
            return R.fail("100262", "部分车辆不符合迁移条件，请检查后重试");
        }
        
        Map<Integer, List<ElectricityCar>> collect = queryList.parallelStream().collect(Collectors.groupingBy(ElectricityCar::getModelId));
        collect.forEach((k, v) -> {
            //k --> ModelId  v --> List<ElectricityCar>
            ElectricityCarModel electricityCarModel = electricityCarModelService.queryByIdFromCache(k);
            if (Objects.isNull(electricityCarModel)) {
                log.error("ELECTRICITY_CAR_MOVE ERROR! CarModel is null error! carModel={}", k);
                return;
            }
            
            //如果目标门店没同名类型则要创建
            ElectricityCarModel targetCarModel = electricityCarModelService.queryByNameAndStoreId(electricityCarModel.getName(), targetStore.getId());
            if (Objects.isNull(targetCarModel)) {
                //拷贝类型
                targetCarModel = new ElectricityCarModel();
                BeanUtil.copyProperties(electricityCarModel, targetCarModel);
                targetCarModel.setFranchiseeId(franchisee.getId());
                targetCarModel.setStoreId(targetStore.getId());
                targetCarModel.setUpdateTime(System.currentTimeMillis());
                targetCarModel.setCreateTime(System.currentTimeMillis());
                electricityCarModelService.insert(targetCarModel);
                
                //拷贝标签
                List<CarModelTag> carModelTags = Optional.ofNullable(carModelTagService.selectByCarModelId(electricityCarModel.getId())).orElse(new ArrayList<>());
                for (CarModelTag carModelTag : carModelTags) {
                    carModelTag.setId(null);
                    carModelTag.setCarModelId(targetCarModel.getId().longValue());
                    carModelTag.setCreateTime(System.currentTimeMillis());
                    carModelTag.setUpdateTime(System.currentTimeMillis());
                }
                carModelTagService.batchInsert(carModelTags);
                
                ///拷贝图片
                PictureQuery pictureQuery = new PictureQuery();
                pictureQuery.setBusinessId(electricityCarModel.getId().longValue());
                pictureQuery.setStatus(Picture.STATUS_ENABLE);
                pictureQuery.setImgType(Picture.TYPE_CAR_IMG);
                pictureQuery.setDelFlag(Picture.DEL_NORMAL);
                pictureQuery.setTenantId(tenantId);
                List<Picture> pictures = pictureService.queryListByQuery(pictureQuery);
                for (Picture picture : pictures) {
                    picture.setId(null);
                    picture.setBusinessId(targetCarModel.getId().longValue());
                    picture.setCreateTime(System.currentTimeMillis());
                    picture.setUpdateTime(System.currentTimeMillis());
                }
                pictureService.batchInsert(pictures);
            }
            
            Integer targetCarModelId = targetCarModel.getId();
            
            //修改被迁移车辆门店及类型
            Optional.ofNullable(v).orElse(new ArrayList<>()).parallelStream().forEach(item -> {
                ElectricityCar updateElectricityCar = new ElectricityCar();
                updateElectricityCar.setId(item.getId());
                updateElectricityCar.setModelId(targetCarModelId);
                updateElectricityCar.setStoreId(targetStore.getId());
                updateElectricityCar.setUpdateTime(System.currentTimeMillis());
                this.update(updateElectricityCar);
            });
        });
        
        saveCarMoveRecords(queryList, sourceStore, targetStore);
        
        return R.ok();
    }
    
    /**
     * 保存车辆迁移信息
     *
     * @param queryList
     * @param sourceStore
     * @param targetStore
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveCarMoveRecords(List<ElectricityCar> queryList, Store sourceStore, Store targetStore) {
        //记录迁移的车辆数据信息
        List<CarMoveRecord> carMoveRecords = new ArrayList<>();
        for (ElectricityCar electricityCar : queryList) {
            CarMoveRecord carMoveRecord = new CarMoveRecord();
            carMoveRecord.setTenantId(TenantContextHolder.getTenantId().longValue());
            carMoveRecord.setCarId(electricityCar.getId().longValue());
            carMoveRecord.setCarSn(electricityCar.getSn());
            carMoveRecord.setCarModelId(electricityCar.getModelId().longValue());
            carMoveRecord.setOldFranchiseeId(sourceStore.getFranchiseeId());
            carMoveRecord.setOldStoreId(sourceStore.getId());
            carMoveRecord.setNewFranchiseeId(targetStore.getFranchiseeId());
            carMoveRecord.setNewStoreId(targetStore.getId());
            carMoveRecord.setOperator(SecurityUtils.getUid());
            carMoveRecord.setDelFlag(CommonConstant.DEL_N);
            carMoveRecord.setCreateTime(System.currentTimeMillis());
            carMoveRecord.setUpdateTime(System.currentTimeMillis());
            carMoveRecords.add(carMoveRecord);
        }
        log.error("move car records is : {}", carMoveRecords);
        if (CollectionUtils.isNotEmpty(carMoveRecords)) {
            carMoveRecordMapper.batchInsertCarMoveRecord(carMoveRecords);
        }
    }
    
    @Override
    public ElectricityCar queryInfoByUid(Long uid) {
        return electricityCarMapper.selectOne(new LambdaQueryWrapper<ElectricityCar>().eq(ElectricityCar::getUid, uid));
    }
    
    @Slave
    @Override
    public Integer queryCountByStoreIds(Integer tenantId, List<Long> storeIds) {
        return electricityCarMapper.queryCountByStoreIds(tenantId, storeIds);
    }
    
    @Override
    public ElectricityCar selectBySn(String sn, Integer tenantId) {
        return electricityCarMapper.selectBySn(sn, tenantId);
    }
    
    @Override
    public Boolean carLockCtrl(String sn, Integer lockType) {
        R<Jt808DeviceInfoVo> result = jt808RetrofitService.controlDevice(new Jt808DeviceControlRequest(IdUtil.randomUUID(), sn, lockType));
        if (!result.isSuccess()) {
            log.warn("Jt808 warn! controlDevice error! carSn={},result={}", sn, result);
            return false;
        }
        
        //        ElectricityCar update = new ElectricityCar();
        //        update.setId(electricityCar.getId());
        //        update.setLockType(lockType);
        //        update.setUpdateTime(System.currentTimeMillis());
        //        update(update);
        return true;
    }
    
    @Override
    public R positionReport(CarPositionReportQuery query) {
        if (Objects.isNull(query)) {
            log.error("CAR POSITION REPORT WARN! query is null! ");
            return R.failMsg("参数错误");
        }
        
        final String requestId = query.getRequestId();
        
        if (StrUtil.isBlank(query.getDevId()) || Objects.isNull(query.getLatitude()) || Objects.isNull(query.getLongitude()) || StrUtil.isBlank(query.getRequestId())) {
            log.warn("CAR POSITION REPORT WARN! args error! requestId={}, query={}", requestId, query);
            return R.failMsg("参数错误");
        }
        
        ElectricityCar electricityCar = selectBySn(query.getDevId(), null);
        if (Objects.isNull(electricityCar)) {
            log.warn("CAR POSITION REPORT WARN! no electricityCar Sn! requestId={}, sn={}", requestId, query.getDevId());
            return R.failMsg("未查询到车辆");
        }
        
        if (Objects.equals(electricityCar.getLatitude(), query.getLatitude()) && Objects.equals(electricityCar.getLongitude(), query.getLongitude())) {
            return R.ok();
        }
        
        ElectricityCar update = new ElectricityCar();
        update.setId(electricityCar.getId());
        update.setLongitude(query.getLongitude());
        update.setLatitude(query.getLatitude());
        update.setUpdateTime(System.currentTimeMillis());
        update(update);
        
        return R.ok();
    }
    
    @Override
    public List<ElectricityCar> queryByStoreIds(List<Long> storeIds) {
        return electricityCarMapper.queryByStoreIds(storeIds, TenantContextHolder.getTenantId());
    }
    
    @Slave
    @Override
    public R queryElectricityCarOverview(String sn, List<Integer> carIds) {
        List<ElectricityCarOverviewVo> electricityCars = electricityCarMapper.queryElectricityCarOverview(carIds, sn, TenantContextHolder.getTenantId());
        return R.ok(electricityCars);
    }
    
    @Slave
    @Override
    public R batteryStatistical(List<Integer> carIdList, Integer tenantId) {
        return R.ok(electricityCarMapper.batteryStatistical(carIdList, tenantId));
    }
    
    /**
     * 判断用户是否绑定的有车
     *
     * @return
     */
    @Override
    public Integer isUserBindCar(Long uid, Integer tenantId) {
        return electricityCarMapper.isUserBindCar(uid, tenantId);
    }
    
    @Override
    public Boolean retryCarLockCtrl(String sn, Integer lockType, Integer retryCount) {
        if (Objects.isNull(retryCount)) {
            retryCount = 1;
        }
        
        retryCount = retryCount > 5 ? 5 : retryCount;
        
        for (int i = 0; i < retryCount; i++) {
            R<Jt808DeviceInfoVo> result = jt808RetrofitService.controlDevice(new Jt808DeviceControlRequest(IdUtil.randomUUID(), sn, lockType));
            if (result.isSuccess()) {
                return true;
            }
            log.warn("Jt808 warn! controlDevice error! carSn={},result={}, retryCount={}", sn, result, i);
        }
        
        return false;
    }
    
    @Slave
    @Override
    public List<ElectricityCarVO> listBySnList(List<String> snList, Integer tenantId, Long franchiseeId) {
        return electricityCarMapper.selectListBySnList(snList, tenantId, franchiseeId);
    }
    
    @Slave
    @Override
    public List<ElectricityCarVO> listByFranchiseeIdAndStockStatus(ElectricityCarSnSearchRequest electricityCarSnSearchRequest) {
        
        ElectricityCarListSnByFranchiseeQueryModel queryModel = new ElectricityCarListSnByFranchiseeQueryModel();
        BeanUtil.copyProperties(electricityCarSnSearchRequest, queryModel);
        queryModel.setTenantId(TenantContextHolder.getTenantId());
        
        List<ElectricityCarVO> rspList = null;
        
        List<ElectricityCarBO> electricityCarBOList = electricityCarMapper.selectListByFranchiseeIdAndStockStatus(queryModel);
        if (CollectionUtils.isNotEmpty(electricityCarBOList)) {
            rspList = electricityCarBOList.stream().map(item -> {
                
                ElectricityCarVO electricityCarVO = new ElectricityCarVO();
                BeanUtil.copyProperties(item, electricityCarVO);
                
                return electricityCarVO;
                
            }).collect(Collectors.toList());
        }
        
        if (CollectionUtils.isEmpty(rspList)) {
            return Collections.emptyList();
        }
        
        return rspList;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer batchExitWarehouse(AssetBatchExitWarehouseRequest assetBatchExitWarehouseRequest) {
        AssetBatchExitWarehouseQueryModel assetBatchExitWarehouseQueryModel = new AssetBatchExitWarehouseQueryModel();
        BeanUtils.copyProperties(assetBatchExitWarehouseRequest, assetBatchExitWarehouseQueryModel);
        assetBatchExitWarehouseQueryModel.setUpdateTime(System.currentTimeMillis());
        
        return electricityCarMapper.batchExitWarehouse(assetBatchExitWarehouseQueryModel);
    }
    
    @Override
    public R batchUpdateFranchiseeIdAndStoreId(CarOutWarehouseRequest carOutWarehouseRequest) {
        List<Integer> idList = carOutWarehouseRequest.getIdList();
        Integer exist = electricityCarMapper.existOutWarehouse(idList, TenantContextHolder.getTenantId());
        if (Objects.nonNull(exist)) {
            return R.fail("100561", "已选择项中有已出库车辆，请重新选择后操作");
        }
        
        electricityCarMapper.batchUpdateFranchiseeIdAndStoreByIdList(idList, carOutWarehouseRequest.getFranchiseeId(), carOutWarehouseRequest.getStoreId(),
                TenantContextHolder.getTenantId(), System.currentTimeMillis(), StockStatusEnum.UN_STOCK.getCode());
        
        idList.forEach(item -> {
            redisService.delete(CacheConstant.CACHE_ELECTRICITY_CAR + item);
        });
        
        // 异步记录
        Integer operateType = carOutWarehouseRequest.getType();
        List<ElectricityCarBO> electricityCarBOList = electricityCarMapper.selectListByIds(new HashSet<>(idList));
        if (CollectionUtils.isNotEmpty(electricityCarBOList) && Objects.nonNull(operateType)) {
            
            List<AssetSnWarehouseRequest> snWarehouseList = electricityCarBOList.stream().filter(item -> Objects.nonNull(item.getWarehouseId()))
                    .map(item -> AssetSnWarehouseRequest.builder().sn(item.getSn()).warehouseId(item.getWarehouseId()).build()).collect(Collectors.toList());
            
            Integer tenantId = TenantContextHolder.getTenantId();
            Long uid = Objects.requireNonNull(SecurityUtils.getUserInfo()).getUid();
            
            assetWarehouseRecordService.asyncRecords(tenantId, uid, snWarehouseList, AssetTypeEnum.ASSET_TYPE_CAR.getCode(), operateType);
        }
        
        return R.ok();
    }
    
    @Override
    public R bathSaveCar(CarBatchSaveRequest carBatchSaveRequest) {
        //操作频繁
        boolean result = redisService.setNx(CacheConstant.CAR_SAVE_UID + SecurityUtils.getUid(), "1", 5 * 1000L, false);
        if (!result) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }
        
        if (CommonConstant.EXCEL_MAX_COUNT_TWO_THOUSAND < carBatchSaveRequest.getCarList().size()) {
            return R.fail("100600", "Excel模版中数据不能超过2000条，请检查修改后再操作");
        }
        
        // 校验车辆型号是否存在
        ElectricityCarModel electricityCarModel = electricityCarModelService.queryByIdFromCache(carBatchSaveRequest.getModelId());
        if (Objects.isNull(electricityCarModel)) {
            ;
            return R.fail("100005", "未找到车辆型号");
        }
        
        List<CarBatchSaveExcelRequest> carList = carBatchSaveRequest.getCarList();
    
        Set<String> snSet = carList.stream().filter(item -> Objects.nonNull(item) && StringUtils.isNotBlank(item.getSn())).map(CarBatchSaveExcelRequest::getSn)
                .collect(Collectors.toSet());
    
        List<ElectricityCarVO> electricityCarVOList = electricityCarMapper.selectListBySnList(new ArrayList<>(snSet), TenantContextHolder.getTenantId(), null);
        
        // 已存在的sn
        List<String> existSnList = electricityCarVOList.stream().map(ElectricityCarVO::getSn).collect(Collectors.toList());
        
        // 准备新增的车辆
        List<ElectricityCar> electricityCarList = Lists.newArrayList();
    
        for (CarBatchSaveExcelRequest car : carList) {
            String sn = car.getSn();
            String licensePlateNumber = car.getLicensePlateNumber();
            String vin = car.getVin();
            String motorNumber = car.getMotorNumber();
            
            if (StringUtils.isNotBlank(licensePlateNumber) && licensePlateNumber.length() > AssetConstant.ASSET_CAR_BATCH_SAVE_LICENSE_PLATE_NUMBER_SIZE) {
                return R.fail("100604", "车牌号输入长度超限，最长10位，请检查修改后再导入");
            }
    
            if (StringUtils.isNotBlank(vin) && vin.length() > AssetConstant.ASSET_CAR_BATCH_SAVE_VIN_SIZE) {
                return R.fail("100605", "车架号输入长度超限，最长17位，请检查修改后再导入");
            }
    
            if (StringUtils.isNotBlank(motorNumber) && motorNumber.length() > AssetConstant.ASSET_CAR_BATCH_SAVE_MOTOR_NUMBER_SIZE) {
                return R.fail("100606", "电机号输入长度超限，最长17位，请检查修改后再导入");
            }
    
            // 校验数据库中是否已存在该sn或sn是否为空
            if (existSnList.contains(sn) || StringUtils.isBlank(sn)) {
                continue;
            }
            //换电柜车辆
            ElectricityCar electricityCar = new ElectricityCar();
            electricityCar.setSn(sn);
            electricityCar.setTenantId(TenantContextHolder.getTenantId());
            electricityCar.setCreateTime(System.currentTimeMillis());
            electricityCar.setUpdateTime(System.currentTimeMillis());
            electricityCar.setDelFlag(ElectricityCabinet.DEL_NORMAL);
            electricityCar.setStoreId(NumberConstant.ZERO_L);
            electricityCar.setStockStatus(StockStatusEnum.STOCK.getCode());
            electricityCar.setStatus(ElectricityCar.STATUS_NOT_RENT);
            electricityCar.setFranchiseeId(NumberConstant.ZERO_L);
            electricityCar.setModel(electricityCarModel.getName());
            electricityCar.setModelId(electricityCarModel.getId());
            electricityCar.setLicensePlateNumber(licensePlateNumber);
            electricityCar.setVin(vin);
            electricityCar.setMotorNumber(motorNumber);
            
            electricityCarList.add(electricityCar);
        }
        
        if (CollectionUtils.isEmpty(electricityCarList)) {
            return R.fail("100562", "Excel模版中所有车辆数据均已存在，请勿重复导入");
        }
        // 保存到本地数据库
        electricityCarMapper.batchInsertCar(electricityCarList);
        return R.ok();
    }
    
    @Slave
    @Override
    public List<ElectricityCar> queryModelIdBySidAndIds(List<Long> carIds, Long sourceSid, Integer status, Integer tenantId) {
        return electricityCarMapper.queryModelIdBySidAndIds(carIds, sourceSid, ElectricityCar.STATUS_NOT_RENT, TenantContextHolder.getTenantId());
    }
    
    @Slave
    @Override
    public Integer existsByWarehouseId(Long wareHouseId) {
        
        return electricityCarMapper.existsByWarehouseId(wareHouseId);
    }
    
    @Slave
    @Override
    public List<ElectricityCarVO> listByIds(Set<Integer> idSet) {
        List<ElectricityCarBO> electricityCarBOList = electricityCarMapper.selectListByIds(idSet);
        
        List<ElectricityCarVO> rspList = null;
        if (CollectionUtils.isNotEmpty(electricityCarBOList)) {
            rspList = electricityCarBOList.stream().map(item -> {
                ElectricityCarVO electricityCarVO = new ElectricityCarVO();
                BeanUtils.copyProperties(item, electricityCarVO);
                
                return electricityCarVO;
                
            }).collect(Collectors.toList());
        }
        
        if (CollectionUtils.isEmpty(rspList)) {
            rspList = Collections.emptyList();
        }
        
        return rspList;
    }
    
    @Slave
    @Override
    public List<ElectricityCarVO> listEnableExitWarehouseCar(AssetEnableExitWarehouseQueryModel queryModel) {
        List<ElectricityCarBO> electricityCarBOList = electricityCarMapper.selectListEnableExitWarehouseCar(queryModel);
        
        List<ElectricityCarVO> rspList = null;
        if (CollectionUtils.isNotEmpty(electricityCarBOList)) {
            rspList = electricityCarBOList.stream().map(item -> {
                ElectricityCarVO electricityCarVO = new ElectricityCarVO();
                BeanUtils.copyProperties(item, electricityCarVO);
                
                return electricityCarVO;
                
            }).collect(Collectors.toList());
        }
        
        if (CollectionUtils.isEmpty(rspList)) {
            rspList = Collections.emptyList();
        }
        
        return rspList;
        
    }
    
    @Override
    public Integer batchUpdateRemove(List<ElectricityCarBatchUpdateFranchiseeAndStoreRequest> carBatchUpdateFranchiseeAndStoreRequestList) {
        Integer count = NumberConstant.ZERO;
        
        for (ElectricityCarBatchUpdateFranchiseeAndStoreRequest updateFranchiseeAndStoreRequest : carBatchUpdateFranchiseeAndStoreRequestList) {
            ElectricityCarUpdateFranchiseeAndStoreQueryModel updateFranchiseeAndStoreQueryModel = new ElectricityCarUpdateFranchiseeAndStoreQueryModel();
            BeanUtil.copyProperties(updateFranchiseeAndStoreRequest, updateFranchiseeAndStoreQueryModel);
            updateFranchiseeAndStoreQueryModel.setUpdateTime(System.currentTimeMillis());
            
            electricityCarMapper.updateFranchiseeIdAndStoreId(updateFranchiseeAndStoreQueryModel);
            count += 1;
            
            //清理缓存
            redisService.delete(CacheConstant.CACHE_ELECTRICITY_CAR + updateFranchiseeAndStoreQueryModel.getId());
        }
        
        return count;
    }
    
    @Slave
    @Override
    public R queryCountByWarehouse(ElectricityCarQuery electricityCarQuery) {
        return R.ok(electricityCarMapper.queryCountByWarehouse(electricityCarQuery));
    }
    
    
    /**
     * 更新用户手机号
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @param newPhone 新号码
     * @return 影响行数
     */
    @Override
    public Integer updatePhoneByUid(Integer tenantId, Long uid, String newPhone) {
        return electricityCarMapper.updatePhoneByUid(tenantId, uid, newPhone);
    }
    
    
    /**
     * <p>
     * Description: queryCarSnByLike P0需求 15.1 实名用户列表（16条优化项）iv.4 模糊搜索车辆SN码
     * </p>
     *
     * @param likeSnQuery likeSnQuery
     * @return com.xiliulou.core.web.R<java.util.List < com.xiliulou.electricity.vo.UserCarLikeVo>>
     * <p>Project: ElectricityCarServiceImpl</p>
     * <p>Copyright: Copyright (c) 2024</p>
     * <p>Company: www.xiliulou.com</p>
     * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
     * @since V1.0 2024/3/13
     */
    @Slave
    @Override
    public R<List<UserCarLikeVO>> listSnByLike(UserCarLikeSnQuery likeSnQuery) {
        List<UserCarLikeVO> result = this.electricityCarMapper.selectListCarSnByLike(likeSnQuery);
        return R.ok(result);
    }
    
    /**
     * <p>
     *    Description: queryIdsBySnArray
     * </p>
     * @param snList snList
     * @param tenantId tenantId
     * @param sourceFranchiseeId sourceFranchiseeId
     * @return java.util.List<java.lang.Long>
     * <p>Project: ElectricityCarServiceImpl</p>
     * <p>Copyright: Copyright (c) 2024</p>
     * <p>Company: www.xiliulou.com</p>
     * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
     * @since V1.0 2024/3/18
    */
    @Slave
    @Override
    public Map<String,Long> listIdsBySnArray(List<String> snList, Integer tenantId, Long sourceFranchiseeId) {
        List<ElectricityCarBO> cars = this.electricityCarMapper.selectListBySnArray(snList, tenantId, sourceFranchiseeId);
        if (CollectionUtils.isEmpty(cars)){
            return MapUtil.empty();
        }
        return cars.stream().collect(Collectors.toMap(ElectricityCarBO::getSn,ElectricityCarBO::getId,(k1,k2)->k1));
    }
    
    
    @Override
    public R editV2(CarUpdateRequest carUpdateRequest, Long uid) {
        //租户
        Integer tenantId = TenantContextHolder.getTenantId();
    
        //操作频繁
        boolean result = redisService.setNx(CacheConstant.CAR_EDIT_UID + uid, "1", 3 * 1000L, false);
        if (!result) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }
    
        //换电柜车辆
        ElectricityCar electricityCar = queryByIdFromCache(carUpdateRequest.getId().intValue());
        if (Objects.isNull(electricityCar)) {
            return R.fail("100007", "未找到车辆");
        }
    
        if (!Objects.equals(tenantId, electricityCar.getTenantId())) {
            return R.ok();
        }
    
        electricityCar.setLicensePlateNumber(carUpdateRequest.getLicensePlateNumber());
        electricityCar.setVin(carUpdateRequest.getVin());
        electricityCar.setMotorNumber(carUpdateRequest.getMotorNumber());
    
        int update = electricityCarMapper.updateById(electricityCar);
        
        DbUtils.dbOperateSuccessThenHandleCache(update, i -> {
            //更新缓存
            redisService.delete(CacheConstant.CACHE_ELECTRICITY_CAR + electricityCar.getId());
        });
        
        return R.ok();
    }
    
    @Slave
    @Override
    public List<ElectricityCar> queryListByTenantIdAndUidList(Integer tenantId, List<Long> uidList) {
        return electricityCarMapper.selectListByTenantIdAndUidList(tenantId,uidList);
    }
    
    @Slave
    @Override
    public List<ElectricityCar> listNoDelByUidList(Integer tenantId, List<Long> uidList) {
        return electricityCarMapper.selectListNoDelByUidList(tenantId, uidList);
    }
    
    @Slave
    @Override
    public List<ElectricityCar> listByUidList(List<Long> uidList, Integer userInfoDelFlag) {
        return electricityCarMapper.selectListByUidList(uidList, userInfoDelFlag);
    }
}
