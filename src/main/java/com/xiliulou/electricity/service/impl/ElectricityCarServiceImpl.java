package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.clickhouse.service.ClickHouseService;
import com.xiliulou.core.utils.TimeUtils;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.DS;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.entity.clickhouse.CarAttr;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.mapper.CarAttrMapper;
import com.xiliulou.electricity.mapper.ElectricityCarMapper;
import com.xiliulou.electricity.query.ElectricityCarAddAndUpdate;
import com.xiliulou.electricity.query.ElectricityCarBindUser;
import com.xiliulou.electricity.query.ElectricityCarQuery;
import com.xiliulou.electricity.query.jt808.CarPositionReportQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.service.retrofit.Jt808RetrofitService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.ElectricityCarOverviewVo;
import com.xiliulou.electricity.vo.CarGpsVo;
import com.xiliulou.electricity.vo.ElectricityCarVO;
import com.xiliulou.electricity.vo.Jt808DeviceInfoVo;
import com.xiliulou.electricity.web.query.CarGpsQuery;
import com.xiliulou.electricity.web.query.jt808.Jt808DeviceControlRequest;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
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

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        //换电柜车辆
        ElectricityCar electricityCar = new ElectricityCar();
        BeanUtil.copyProperties(electricityCarAddAndUpdate, electricityCar);
        electricityCar.setTenantId(tenantId);
        electricityCar.setCreateTime(System.currentTimeMillis());
        electricityCar.setUpdateTime(System.currentTimeMillis());
        electricityCar.setDelFlag(ElectricityCabinet.DEL_NORMAL);

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
    @DS("slave_1")
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
        return R.ok(this.carUnBindUser(electricityCar));
    }

    @Override
    public ElectricityCar queryInfoByUid(Long uid) {
        return electricityCarMapper.selectOne(new LambdaQueryWrapper<ElectricityCar>().eq(ElectricityCar::getUid, uid));
    }

    @Override
    public Integer queryCountByStoreIds(Integer tenantId, List<Long> storeIds) {
        return electricityCarMapper.queryCountByStoreIds(tenantId, storeIds);
    }

    @Override
    public ElectricityCar selectBySn(String sn, Integer tenantId) {
        return electricityCarMapper.selectBySn(sn, tenantId);
    }
    
    @Override
    public Boolean carLockCtrl(ElectricityCar electricityCar, Integer lockType) {
        R<Jt808DeviceInfoVo> result = jt808RetrofitService
                .controlDevice(new Jt808DeviceControlRequest(IdUtil.randomUUID(), electricityCar.getSn(), lockType));
        if (!result.isSuccess()) {
            log.error("Jt808 error! controlDevice error! carId={},result={}", electricityCar.getId(), result);
            return false;
        }
        
        ElectricityCar update = new ElectricityCar();
        update.setId(electricityCar.getId());
        update.setLockType(lockType);
        update.setUpdateTime(System.currentTimeMillis());
        update(update);
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
        update.setLockType(query.getDoorStatus());
        update.setUpdateTime(System.currentTimeMillis());
        update(update);
        
        return R.ok();
    }
    
    @Override
    public List<ElectricityCar> queryByStoreIds(List<Long> storeIds) {
        return electricityCarMapper.queryByStoreIds(storeIds, TenantContextHolder.getTenantId());
    }
    
    @Override
    public R queryElectricityCarOverview(String sn, List<Integer> carIds) {
        List<ElectricityCarOverviewVo> electricityCars = electricityCarMapper
                .queryElectricityCarOverview(carIds, sn, TenantContextHolder.getTenantId());
        return R.ok(electricityCars);
    }
    
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
}
