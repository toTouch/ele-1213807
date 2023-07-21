package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.clickhouse.service.ClickHouseService;
import com.xiliulou.core.utils.TimeUtils;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.DS;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.domain.car.CarInfoDO;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.entity.clickhouse.CarAttr;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.mapper.CarAttrMapper;
import com.xiliulou.electricity.mapper.CarMoveRecordMapper;
import com.xiliulou.electricity.mapper.ElectricityCarMapper;
import com.xiliulou.electricity.query.*;
import com.xiliulou.electricity.query.jt808.CarPositionReportQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.service.retrofit.Jt808RetrofitService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.*;
import com.xiliulou.electricity.web.query.jt808.Jt808DeviceControlRequest;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
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
    RentCarOrderService rentCarOrderService;
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
    CarRefundOrderService carRefundOrderService;

    @Autowired
    ElectricityConfigService electricityConfigService;
    
    @Autowired
    CarLockCtrlHistoryService carLockCtrlHistoryService;

    @Autowired
    private CarMoveRecordMapper carMoveRecordMapper;

    /**
     * 根据 uid 查询车辆信息<br />
     * 复合查询，车辆、门店、车辆经纬度
     * @param tenantId 租户ID
     * @param carId 车辆ID
     * @return
     */
    @Slave
    @Override
    public CarInfoDO queryByCarId(Integer tenantId, Long carId) {
        if (ObjectUtils.allNotNull(tenantId, carId)) {
            return null;
        }

        CarInfoDO carInfoDO = electricityCarMapper.queryByCarId(tenantId, carId);
        if (ObjectUtils.isEmpty(carInfoDO)) {
            return null;
        }

        Pair<Boolean, Object> carDevicePair = jt808CarService.queryDeviceInfo(carInfoDO.getCarSn());
        if (!carDevicePair.getLeft()) {
            return carInfoDO;
        }

        Jt808DeviceInfoVo deviceInfoVo = (Jt808DeviceInfoVo) carDevicePair.getRight();
        carInfoDO.setLatitude(deviceInfoVo.getLatitude());
        carInfoDO.setLongitude(deviceInfoVo.getLongitude());

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
    @Transactional
    public R save(ElectricityCarAddAndUpdate electricityCarAddAndUpdate) {
        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY CAR  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //操作频繁
        boolean result = redisService.setNx(CacheConstant.CAR_SAVE_UID + user.getUid(), "1", 3 * 1000L, false);
        if (!result) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }

        Store store = storeService.queryByIdFromCache(electricityCarAddAndUpdate.getStoreId());
        if(Objects.isNull(store)){
            return R.fail("100204", "未找到门店");
        }

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        //换电柜车辆
        ElectricityCar electricityCar = new ElectricityCar();
        BeanUtil.copyProperties(electricityCarAddAndUpdate, electricityCar);
        electricityCar.setTenantId(tenantId);
        electricityCar.setCreateTime(System.currentTimeMillis());
        electricityCar.setUpdateTime(System.currentTimeMillis());
        electricityCar.setDelFlag(ElectricityCabinet.DEL_NORMAL);
        electricityCar.setFranchiseeId(store.getFranchiseeId());

        //查找车辆型号
        ElectricityCarModel electricityCarModel = electricityCarModelService.queryByIdFromCache(electricityCar.getModelId());
        if (Objects.isNull(electricityCarModel)) {
            return R.fail("100005", "未找到车辆型号");
        }
        ElectricityCar existElectricityCar = electricityCarMapper.selectOne(new LambdaQueryWrapper<ElectricityCar>()
                .eq(ElectricityCar::getSn, electricityCarAddAndUpdate.getSn())
                .eq(ElectricityCar::getDelFlag, ElectricityCar.DEL_NORMAL)
                .eq(ElectricityCar::getTenantId, tenantId));
        if (Objects.nonNull(existElectricityCar)) {
            return R.fail("100017", "已存在该编号车辆");
        }

        electricityCar.setModel(electricityCarModel.getName());
        electricityCar.setModelId(electricityCarModel.getId());

        int insert = electricityCarMapper.insert(electricityCar);
        DbUtils.dbOperateSuccessThen(insert, () -> {
            //新增缓存
            redisService.saveWithHash(CacheConstant.CACHE_ELECTRICITY_CAR + electricityCar.getId(), electricityCar);
            return electricityCar;
        });
        return R.ok(electricityCar.getId());
    }

    @Override
    @Transactional
    public R edit(ElectricityCarAddAndUpdate electricityCarAddAndUpdate) {
        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY CAR  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        //操作频繁
        boolean result = redisService.setNx(CacheConstant.CAR_EDIT_UID + user.getUid(), "1", 3 * 1000L, false);
        if (!result) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }

        //换电柜车辆
        ElectricityCar electricityCar = new ElectricityCar();
        BeanUtil.copyProperties(electricityCarAddAndUpdate, electricityCar);
        ElectricityCar oldElectricityCar = queryByIdFromCache(electricityCar.getId());
        if (Objects.isNull(oldElectricityCar)) {
            return R.fail("100007", "未找到车辆");
        }

        if (!Objects.equals(tenantId, oldElectricityCar.getTenantId())) {
            return R.ok();
        }

        //车辆老型号
        Integer oldModelId = oldElectricityCar.getModelId();
        //查找快递柜型号
        ElectricityCarModel electricityCarModel = electricityCarModelService.queryByIdFromCache(electricityCar.getModelId());
        if (Objects.isNull(electricityCarModel)) {
            return R.fail("100005", "未找到车辆型号");
        }

        if (Objects.equals(tenantId, electricityCarModel.getTenantId())) {
            return R.ok();
        }

        if (!oldModelId.equals(electricityCar.getModelId())) {
            return R.fail("ELECTRICITY.0010", "不能修改型号");
        }
        ElectricityCar existElectricityCar = electricityCarMapper.selectOne(new LambdaQueryWrapper<ElectricityCar>().eq(ElectricityCar::getSn, electricityCarAddAndUpdate.getSn()).eq(ElectricityCar::getTenantId, tenantId));
        if (Objects.nonNull(existElectricityCar)) {
            return R.fail("100017", "已存在该编号车辆");
        }

        electricityCar.setUpdateTime(System.currentTimeMillis());

        if(Objects.nonNull(electricityCarAddAndUpdate.getStoreId())){
            Store store = storeService.queryByIdFromCache(electricityCarAddAndUpdate.getStoreId());
            if(Objects.isNull(store)){
                return R.fail("100204", "未找到门店");
            }
            electricityCar.setFranchiseeId(store.getFranchiseeId());
        }

        int update = electricityCarMapper.updateById(electricityCar);
        DbUtils.dbOperateSuccessThen(update, () -> {
            //更新缓存
            redisService.delete(CacheConstant.CACHE_ELECTRICITY_CAR + electricityCar.getId());
            return null;
        });
        return R.ok();
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

        List<ElectricityCarVO> carVOList = electricityCarVOS.parallelStream().peek(item -> {
            ElectricityBattery electricityBattery = electricityBatteryService.queryByUid(item.getUid());
            if (Objects.nonNull(electricityBattery)) {
                item.setBatterySn(electricityBattery.getSn());
            }
        }).collect(Collectors.toList());

        return R.ok(carVOList);
    }

    @Override
    public Integer queryByModelId(Integer id) {
        return electricityCarMapper.selectCount(Wrappers.<ElectricityCar>lambdaQuery().eq(ElectricityCar::getModelId, id).eq(ElectricityCar::getDelFlag, ElectricityCar.DEL_NORMAL).eq(ElectricityCar::getTenantId, TenantContextHolder.getTenantId()));
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
            log.error("rentBattery  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        UserCar userCar = userCarService.selectByUidFromCache(user.getUid());
        if (Objects.isNull(userCar)) {
            log.error("query  ERROR! not found car! uid:{} ", user.getUid());
            return R.fail("100007", "未找到车辆");
        }

        if (StringUtils.isEmpty(userCar.getSn())) {
            log.error("query  ERROR! not found BatterySn! uid:{} ", user.getUid());
            return R.fail("100007", "未找到车辆");
        }

        String begin = TimeUtils.convertToStandardFormatTime(beginTime);
        String end = TimeUtils.convertToStandardFormatTime(endTime);

        List<CarAttr> query = jt808CarService.queryListBySn(userCar.getSn(), begin, end);
        if (CollectionUtils.isEmpty(query)) {
            query = new ArrayList<>();
        }

        List<CarGpsVo> result = query.parallelStream()
                .map(e -> new CarGpsVo().setLatitude(e.getLatitude()).setLongitude(e.getLongitude())
                        .setDevId(e.getDevId()).setCreateTime(e.getCreateTime().getTime()))
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
        List<ElectricityCarMoveVo> queryList = electricityCarMapper
                .queryEnableMoveCarByStoreId(storeId, sn, size, offset, TenantContextHolder.getTenantId());
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
            log.error("ELECTRICITY_CAR_MOVE ERROR! car size too long ！sourceStore={}， targetStore={}, size={}",
                    electricityCarMoveQuery.getSourceSid(), electricityCarMoveQuery.getTargetSid(), carIds.size());
            return R.fail("100270", "迁移车辆数量过多");
        }
    
        if (Objects.equals(electricityCarMoveQuery.getSourceSid(), electricityCarMoveQuery.getTargetSid())) {
            log.error("ELECTRICITY_CAR_MOVE ERROR! Same store ！sourceStore={}， targetStore={}",
                    electricityCarMoveQuery.getSourceSid(), electricityCarMoveQuery.getTargetSid());
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
        
        if (!Objects.equals(targetStore.getTenantId(), TenantContextHolder.getTenantId()) || !Objects
                .equals(sourceStore.getTenantId(), TenantContextHolder.getTenantId())) {
            return R.ok();
        }
    
        Franchisee franchisee = franchiseeService.queryByIdFromCache(targetStore.getFranchiseeId());
        if (Objects.isNull(franchisee)) {
            log.error("ELECTRICITY_CAR_MOVE ERROR! not found franchisee！franchiseeId={}", franchisee.getId());
            return R.fail("ELECTRICITY.0038", "未找到加盟商");
        }
    
        List<ElectricityCar> queryList = electricityCarMapper
                .queryModelIdBySidAndIds(carIds, electricityCarMoveQuery.getSourceSid(), ElectricityCar.STATUS_NOT_RENT,
                        TenantContextHolder.getTenantId());
        if (CollectionUtils.isEmpty(queryList) || queryList.size() != carIds.size()) {
            log.error("ELECTRICITY_CAR_MOVE ERROR! has illegal cars！carIds={}", carIds);
            return R.fail("100262", "部分车辆不符合迁移条件，请检查后重试");
        }
    
        Map<Integer, List<ElectricityCar>> collect = queryList.parallelStream()
                .collect(Collectors.groupingBy(ElectricityCar::getModelId));
        collect.forEach((k, v) -> {
            //k --> ModelId  v --> List<ElectricityCar>
            ElectricityCarModel electricityCarModel = electricityCarModelService.queryByIdFromCache(k);
            if (Objects.isNull(electricityCarModel)) {
                log.error("ELECTRICITY_CAR_MOVE ERROR! CarModel is null error! carModel={}", k);
                return;
            }
    
            //如果目标门店没同名类型则要创建
            ElectricityCarModel targetCarModel = electricityCarModelService
                    .queryByNameAndStoreId(electricityCarModel.getName(), targetStore.getId());
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
                List<CarModelTag> carModelTags = Optional
                        .ofNullable(carModelTagService.selectByCarModelId(electricityCarModel.getId()))
                        .orElse(new ArrayList<>());
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
     * @param queryList
     * @param sourceStore
     * @param targetStore
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveCarMoveRecords(List<ElectricityCar> queryList, Store sourceStore, Store targetStore){
        //记录迁移的车辆数据信息
        List<CarMoveRecord> carMoveRecords = new ArrayList<>();
        for(ElectricityCar electricityCar : queryList){
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
        log.info("move car records is : {}", carMoveRecords);
        if(CollectionUtils.isNotEmpty(carMoveRecords)){
            carMoveRecordMapper.batchInsertCarMoveRecord(carMoveRecords);
        }
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R bindUser(ElectricityCarBindUser electricityCarBindUser) {

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE CAR  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到操作用户");
        }

        UserInfo userInfo = userInfoService.queryByUidFromCache(electricityCarBindUser.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("ELE CAR ERROR! not found user uid={}", electricityCarBindUser.getUid());
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        if (!Objects.equals(userInfo.getTenantId(), tenantId)) {
            return R.ok();
        }

        //押金
        if (!Objects.equals(userInfo.getCarDepositStatus(), UserInfo.CAR_DEPOSIT_STATUS_YES)) {
            log.error("ELE CAR ERROR! this user not pay deposit,uid={}", userInfo.getUid());
            return R.fail("100012", "未缴纳租车押金");
        }

        UserCarDeposit userCarDeposit = userCarDepositService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userCarDeposit)) {
            log.error("ELE CAR ERROR! this user not pay deposit,uid={}", userInfo.getUid());
            return R.fail("100247", "未找到用户信息");
        }

        //是否绑定车辆
        if (Objects.equals(userInfo.getCarRentStatus(), UserInfo.CAR_RENT_STATUS_YES)) {
            log.error("ELE CAR ERROR! this user already binding car,uid={}", userInfo.getUid());
            return R.fail("100012", "用户已绑定车辆");
        }

        //购买租车套餐
        UserCarMemberCard userCarMemberCard = userCarMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userCarMemberCard) || Objects.isNull(userCarMemberCard.getCardId()) || Objects.isNull(userCarMemberCard.getMemberCardExpireTime())) {
            log.error("ELE CAR ERROR! not found userCarMemberCard,uid={}", userInfo.getUid());
            return R.fail("100014", "未购买租车套餐");
        }

        //套餐是否过期
        if (userCarMemberCard.getMemberCardExpireTime() < System.currentTimeMillis()) {
            log.error("ELE CAR ERROR! rent car memberCard is Expire,uid={}", userInfo.getUid());
            return R.fail("100013", "租车套餐已过期");
        }

        ElectricityCar electricityCar = queryByIdFromCache(electricityCarBindUser.getCarId());
        if (Objects.isNull(electricityCar)) {
            log.error("ELE CAR ERROR! not found electricityCar,uid={}", userInfo.getUid());
            return R.fail("100007", "未找到车辆");
        }
        if (!Objects.equals(electricityCar.getTenantId(), tenantId)) {
            return R.ok();
        }

        UserCar userCar = userCarService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userCar)) {
            log.error("ELE CAR ERROR! not found userCar,uid={}", userInfo.getUid());
            return R.fail("100015", "用户未绑定车辆");
        }

        if (!Objects.equals(electricityCar.getModelId(), userCar.getCarModel().intValue())) {
            log.error("ELE CAR ERROR! user bind carModel not equals will bond carModel,uid={}", userInfo.getUid());
            return R.fail("100016", "用户缴纳的车辆型号押金与绑定的不符");
        }

        UserInfo updateUserInfo = new UserInfo();
        updateUserInfo.setUid(userInfo.getUid());
        updateUserInfo.setCarRentStatus(UserInfo.CAR_RENT_STATUS_YES);
        updateUserInfo.setUpdateTime(System.currentTimeMillis());
        userInfoService.updateByUid(updateUserInfo);

        UserCar updateUserCar = new UserCar();
        updateUserCar.setUid(userInfo.getUid());
        updateUserCar.setCid(electricityCar.getId().longValue());
        updateUserCar.setSn(electricityCar.getSn());
        updateUserCar.setUpdateTime(System.currentTimeMillis());
        userCarService.updateByUid(updateUserCar);

        //生成租车记录
        String orderId = OrderIdUtil.generateBusinessOrderId(BusinessType.RENT_CAR, user.getUid());
        RentCarOrder rentCarOrder = new RentCarOrder();
        rentCarOrder.setOrderId(orderId);
        rentCarOrder.setCarModelId(electricityCar.getModelId().longValue());
        rentCarOrder.setCarDeposit(userCarDeposit.getCarDeposit().doubleValue());
        rentCarOrder.setStatus(RentCarOrder.STATUS_SUCCESS);
        rentCarOrder.setCarSn(electricityCar.getSn());
        rentCarOrder.setType(RentCarOrder.TYPE_RENT);
        rentCarOrder.setUid(user.getUid());
        rentCarOrder.setName(userInfo.getName());
        rentCarOrder.setPhone(userInfo.getPhone());
        rentCarOrder.setTransactionType(RentCarOrder.TYPE_TRANSACTION_ONLINE);
        rentCarOrder.setStoreId(electricityCar.getStoreId());
        rentCarOrder.setFranchiseeId(userInfo.getFranchiseeId());
        rentCarOrder.setTenantId(TenantContextHolder.getTenantId());
        rentCarOrder.setCreateTime(System.currentTimeMillis());
        rentCarOrder.setUpdateTime(System.currentTimeMillis());

        rentCarOrderService.insert(rentCarOrder);

        //新增操作记录
        EleBindCarRecord eleBindCarRecord = EleBindCarRecord.builder()
                .carId(electricityCar.getId())
                .sn(electricityCar.getSn())
                .operateUser(user.getUsername())
                .model(electricityCar.getModel())
                .phone(userInfo.getPhone())
                .status(EleBindCarRecord.BIND_CAR)
                .userName(userInfo.getName())
                .tenantId(electricityCar.getTenantId())
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis()).build();
        eleBindCarRecordService.insert(eleBindCarRecord);

        electricityCar.setStatus(ElectricityCar.STATUS_IS_RENT);
        electricityCar.setUid(userInfo.getUid());
        electricityCar.setPhone(userInfo.getPhone());
        electricityCar.setUserInfoId(userInfo.getId());
        electricityCar.setUserName(userInfo.getName());
        electricityCar.setUpdateTime(System.currentTimeMillis());
    
        //用户绑定解锁
        //ElectricityCar electricityCar = electricityCarService.queryInfoByUid(userInfo.getUid());
        ElectricityConfig electricityConfig = electricityConfigService
                .queryFromCacheByTenantId(TenantContextHolder.getTenantId());
        if (Objects.nonNull(electricityConfig) && Objects
                .equals(electricityConfig.getIsOpenCarControl(), ElectricityConfig.ENABLE_CAR_CONTROL)) {
            boolean result = this.retryCarLockCtrl(electricityCar.getSn(), ElectricityCar.TYPE_UN_LOCK, 3);
        
            CarLockCtrlHistory carLockCtrlHistory = new CarLockCtrlHistory();
            carLockCtrlHistory.setUid(userInfo.getUid());
            carLockCtrlHistory.setName(userInfo.getName());
            carLockCtrlHistory.setPhone(userInfo.getPhone());
            carLockCtrlHistory.setStatus(
                    result ? CarLockCtrlHistory.STATUS_UN_LOCK_SUCCESS : CarLockCtrlHistory.STATUS_UN_LOCK_FAIL);
            carLockCtrlHistory.setType(CarLockCtrlHistory.TYPE_BIND_USER_UN_LOCK);
            carLockCtrlHistory.setCarModelId(electricityCar.getModelId().longValue());
            carLockCtrlHistory.setCarModel(electricityCar.getModel());
            carLockCtrlHistory.setCarId(electricityCar.getId().longValue());
            carLockCtrlHistory.setCarSn(electricityCar.getSn());
            carLockCtrlHistory.setCreateTime(System.currentTimeMillis());
            carLockCtrlHistory.setUpdateTime(System.currentTimeMillis());
            carLockCtrlHistory.setTenantId(TenantContextHolder.getTenantId());
            carLockCtrlHistoryService.insert(carLockCtrlHistory);
        }
        return R.ok(this.update(electricityCar));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R unBindUser(ElectricityCarBindUser electricityCarBindUser) {

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE CAR  ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到操作用户");
        }

        UserInfo userInfo = userInfoService.queryByUidFromCache(electricityCarBindUser.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("ELE CAR ERROR! not found user uid={}", electricityCarBindUser.getUid());
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        if (!Objects.equals(userInfo.getTenantId(), TenantContextHolder.getTenantId())) {
            return R.ok();
        }

        ElectricityCar electricityCar = queryByIdFromCache(electricityCarBindUser.getCarId());
        if (Objects.isNull(electricityCar)) {
            log.error("ELE CAR ERROR! not found car,uid={},carId={}", userInfo.getUid(), electricityCarBindUser.getCarId());
            return R.fail("100007", "未找到车辆");
        }
        if (!Objects.equals(electricityCar.getTenantId(), TenantContextHolder.getTenantId())) {
            return R.ok();
        }

        //用户是否绑定车辆
        if (!Objects.equals(userInfo.getCarRentStatus(), UserInfo.CAR_RENT_STATUS_YES) || !Objects.equals(userInfo.getUid(), electricityCarBindUser.getUid())) {
            log.error("ELE CAR ERROR! user not binding car,uid={}", userInfo.getUid());
            return R.fail("100015", "用户未绑定车辆");
        }

        UserInfo updateUserInfo = new UserInfo();
        updateUserInfo.setUid(userInfo.getUid());
        updateUserInfo.setCarRentStatus(UserInfo.CAR_RENT_STATUS_NO);
        updateUserInfo.setUpdateTime(System.currentTimeMillis());
        userInfoService.updateByUid(updateUserInfo);

        UserCar updateUserCar = new UserCar();
        updateUserCar.setUid(userInfo.getUid());
        updateUserCar.setCid(null);
        updateUserCar.setSn("");
        userCarService.unBindingCarByUid(updateUserCar);

        //新增操作记录
        EleBindCarRecord eleBindCarRecord = EleBindCarRecord.builder()
                .carId(electricityCar.getId())
                .sn(electricityCar.getSn())
                .operateUser(user.getUsername())
                .model(electricityCar.getModel())
                .phone(userInfo.getPhone())
                .status(EleBindCarRecord.NOT_BIND_CAR)
                .userName(userInfo.getName())
                .tenantId(TenantContextHolder.getTenantId())
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis()).build();
        eleBindCarRecordService.insert(eleBindCarRecord);

        electricityCar.setStatus(ElectricityCar.STATUS_NOT_RENT);
        electricityCar.setUid(null);
        electricityCar.setPhone(null);
        electricityCar.setUserInfoId(null);
        electricityCar.setUserName(null);
        electricityCar.setUpdateTime(System.currentTimeMillis());
        this.carUnBindUser(electricityCar);
    
        //用户解绑加锁
        ElectricityConfig electricityConfig = electricityConfigService
                .queryFromCacheByTenantId(TenantContextHolder.getTenantId());
        if (Objects.nonNull(electricityConfig) && Objects
                .equals(electricityConfig.getIsOpenCarControl(), ElectricityConfig.ENABLE_CAR_CONTROL)) {
            boolean result = this.retryCarLockCtrl(electricityCar.getSn(), ElectricityCar.TYPE_LOCK, 3);
        
            CarLockCtrlHistory carLockCtrlHistory = new CarLockCtrlHistory();
            carLockCtrlHistory.setUid(userInfo.getUid());
            carLockCtrlHistory.setName(userInfo.getName());
            carLockCtrlHistory.setPhone(userInfo.getPhone());
            carLockCtrlHistory
                    .setStatus(result ? CarLockCtrlHistory.STATUS_LOCK_SUCCESS : CarLockCtrlHistory.STATUS_LOCK_FAIL);
            carLockCtrlHistory.setType(CarLockCtrlHistory.TYPE_UN_BIND_USER_LOCK);
            carLockCtrlHistory.setCarModelId(electricityCar.getModelId().longValue());
            carLockCtrlHistory.setCarModel(electricityCar.getModel());
            carLockCtrlHistory.setCarId(electricityCar.getId().longValue());
            carLockCtrlHistory.setCarSn(electricityCar.getSn());
            carLockCtrlHistory.setCreateTime(System.currentTimeMillis());
            carLockCtrlHistory.setUpdateTime(System.currentTimeMillis());
            carLockCtrlHistory.setTenantId(TenantContextHolder.getTenantId());
            carLockCtrlHistoryService.insert(carLockCtrlHistory);
        }
        return R.ok();
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
        R<Jt808DeviceInfoVo> result = jt808RetrofitService
                .controlDevice(new Jt808DeviceControlRequest(IdUtil.randomUUID(), sn, lockType));
        if (!result.isSuccess()) {
            log.error("Jt808 error! controlDevice error! carSn={},result={}", sn, result);
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
        
        if (StrUtil.isBlank(query.getDevId()) || Objects.isNull(query.getLatitude()) || Objects
                .isNull(query.getLongitude()) || StrUtil.isBlank(query.getRequestId())) {
            log.warn("CAR POSITION REPORT WARN! args error! requestId={}, query={}", requestId, query);
            return R.failMsg("参数错误");
        }
        
        ElectricityCar electricityCar = selectBySn(query.getDevId(), null);
        if (Objects.isNull(electricityCar)) {
            log.warn("CAR POSITION REPORT WARN! no electricityCar Sn! requestId={}, sn={}", requestId,
                    query.getDevId());
            return R.failMsg("未查询到车辆");
        }
        
        if (Objects.equals(electricityCar.getLatitude(), query.getLatitude()) && Objects
                .equals(electricityCar.getLongitude(), query.getLongitude())) {
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
        List<ElectricityCarOverviewVo> electricityCars = electricityCarMapper
                .queryElectricityCarOverview(carIds, sn, TenantContextHolder.getTenantId());
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
            R<Jt808DeviceInfoVo> result = jt808RetrofitService
                    .controlDevice(new Jt808DeviceControlRequest(IdUtil.randomUUID(), sn, lockType));
            if (result.isSuccess()) {
                return true;
            }
            log.error("Jt808 error! controlDevice error! carSn={},result={}, retryCount={}", sn, result, i);
        }
        
        log.error("Jt808 error! controlDevice error! carSn={}", sn);
        return false;
    }
}
