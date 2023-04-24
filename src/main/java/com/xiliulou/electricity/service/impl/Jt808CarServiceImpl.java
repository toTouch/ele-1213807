package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.utils.TimeUtils;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.DS;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.ElectricityCar;
import com.xiliulou.electricity.entity.UserCarDeposit;
import com.xiliulou.electricity.entity.UserCarMemberCard;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.clickhouse.CarAttr;
import com.xiliulou.electricity.mapper.CarAttrMapper;
import com.xiliulou.electricity.query.CarControlQuery;
import com.xiliulou.electricity.service.ElectricityCarService;
import com.xiliulou.electricity.service.Jt808CarService;
import com.xiliulou.electricity.service.UserCarDepositService;
import com.xiliulou.electricity.service.UserCarMemberCardService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.retrofit.Jt808RetrofitService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.CarGpsVo;
import com.xiliulou.electricity.vo.Jt808DeviceInfoVo;
import com.xiliulou.electricity.web.query.CarControlRequest;
import com.xiliulou.electricity.web.query.CarGpsQuery;
import com.xiliulou.electricity.web.query.jt808.Jt808DeviceControlRequest;
import com.xiliulou.electricity.web.query.jt808.Jt808GetInfoRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author : eclair
 * @date : 2022/12/29 09:54
 */
@Service
@Slf4j
public class Jt808CarServiceImpl implements Jt808CarService {
    
    @Autowired
    ElectricityCarService electricityCarService;
    
    @Autowired
    Jt808RetrofitService jt808RetrofitService;
    
    @Autowired
    CarAttrMapper carAttrMapper;
    
    @Autowired
    UserInfoService userInfoService;
    
    @Autowired
    UserCarDepositService userCarDepositService;
    
    @Autowired
    UserCarMemberCardService userCarMemberCardService;
    
    @Autowired
    RedisService redisService;
    
    @Override
    public Pair<Boolean, Object> queryDeviceInfo(Integer carId) {
        ElectricityCar electricityCar = electricityCarService.queryByIdFromCache(carId);
        if (Objects.isNull(electricityCar)) {
            return Pair.of(false, "未能查询到车辆");
        }
        
        if (!electricityCar.getTenantId().equals(TenantContextHolder.getTenantId())) {
            return Pair.of(false, "未能查询到车辆");
        }
        
        if (StrUtil.isEmpty(electricityCar.getSn())) {
            return Pair.of(false, "车辆sn为空");
        }
        
        R<Jt808DeviceInfoVo> result = jt808RetrofitService.getInfo(
                new Jt808GetInfoRequest(IdUtil.randomUUID(), electricityCar.getSn()));
        if (!result.isSuccess()) {
            log.error("Jt808 error! queryDevice error! carId={},result={}", carId, result);
            return Pair.of(false, result.getErrMsg());
        }
        
        return Pair.of(true, result.getData());
    }
    
    @Override
    public Pair<Boolean, Object> controlCar(CarControlRequest request) {
        ElectricityCar electricityCar = electricityCarService.queryByIdFromCache(request.getCarId());
        if (Objects.isNull(electricityCar)) {
            return Pair.of(false, "未能查询到车辆");
        }
        
        if (!electricityCar.getTenantId().equals(TenantContextHolder.getTenantId())) {
            return Pair.of(false, "未能查询到车辆");
        }
        
        if (StrUtil.isEmpty(electricityCar.getSn())) {
            return Pair.of(false, "车辆sn为空");
        }
    
        //        R<Jt808DeviceInfoVo> result = jt808RetrofitService.controlDevice(
        //                new Jt808DeviceControlRequest(IdUtil.randomUUID(), electricityCar.getSn(), request.getLockType()));
        //        if (!result.isSuccess()) {
        //            log.error("Jt808 error! controlDevice error! carId={},result={}", request.getCarId(), result);
        //            return Pair.of(false, result.getErrMsg());
        //        }
    
        if (!electricityCarService.carLockCtrl(electricityCar.getSn(), request.getLockType())) {
            return Pair.of(false, "请求失败");
        }
        
        return Pair.of(true, null);
    }
    
    @Override
    @DS(value = "clickhouse")
    public Pair<Boolean, Object> getGpsList(CarGpsQuery carGpsQuery) {
        carGpsQuery.setBeginTime(TimeUtils.convertToStandardFormatTime(carGpsQuery.getStartTimeMills()));
        carGpsQuery.setEndTime(TimeUtils.convertToStandardFormatTime(carGpsQuery.getEndTimeMills()));
        
        ElectricityCar electricityCar = electricityCarService.queryByIdFromCache(carGpsQuery.getCarId());
        if (Objects.isNull(electricityCar)) {
            return Pair.of(false, "未能查询到车辆");
        }
        
        if (!electricityCar.getTenantId().equals(TenantContextHolder.getTenantId())) {
            return Pair.of(false, "未能查询到车辆");
        }
        
        if (StrUtil.isEmpty(electricityCar.getSn())) {
            return Pair.of(false, "车辆sn为空");
        }
        
        carGpsQuery.setDevId(electricityCar.getSn());
        List<CarGpsVo> result = carAttrMapper.getGpsList(carGpsQuery).parallelStream()
                .map(e -> new CarGpsVo().setLatitude(e.getLatitude()).setLongitude(e.getLongitude()).setDevId(e.getDevId())
                        .setCreateTime(e.getCreateTime().getTime())).collect(Collectors.toList());
        return Pair.of(true, result);
    }
    
    @Override
    @DS(value = "clickhouse")
    public List<CarAttr> queryListBySn(String sn, String begin, String end) {
        return carAttrMapper.queryListBySn(sn, begin, end);
    }
    
    
    @Override
    public Triple<Boolean, String, Object> userControlCar(CarControlQuery query) {
        Long uid = SecurityUtils.getUid();
        if (Objects.isNull(uid)) {
            log.error("USER CONTROL CAR ERROR! not found user ");
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户");
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.error("USER CONTROL CAR ERROR! not found user! uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户");
        }
        
        //用户是否可用
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("USER CONTROL CAR ERROR! user is disable!uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
        }
        
        //未实名认证
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.error("USER CONTROL CAR ERROR! user not auth,uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0041", "未实名认证");
        }
        
        //判断是否缴纳押金
        UserCarDeposit userCarDeposit = userCarDepositService.selectByUidFromCache(uid);
        if (Objects.isNull(userCarDeposit)) {
            log.error("USER CONTROL CAR ERROR! userCarDeposit is null,uid={}", uid);
            return Triple.of(false, "100238", "未缴纳押金");
        }
        
        //判断是否缴纳押金
        if (!Objects.equals(userInfo.getCarDepositStatus(), UserInfo.CAR_DEPOSIT_STATUS_YES)) {
            log.error("USER CONTROL CAR ERROR! not pay deposit,uid={}", uid);
            return Triple.of(false, "100238", "未缴纳押金");
        }
        
        //是否购买套餐
        UserCarMemberCard userCarMemberCard = userCarMemberCardService.selectByUidFromCache(uid);
        if (Objects.isNull(userCarMemberCard)) {
            log.error("USER CONTROL CAR ERROR! not pay rent car memberCard,uid={}", uid);
            return Triple.of(false, "100232", "未购买租车套餐");
        }
        
        //套餐是否过期
        if (userCarMemberCard.getMemberCardExpireTime() < System.currentTimeMillis()) {
            log.error("USER CONTROL CAR ERROR! rent car memberCard expired,uid={}", uid);
            return Triple.of(false, "100233", "租车套餐已过期");
        }
        
        ElectricityCar electricityCar = electricityCarService.queryInfoByUid(uid);
        if (Objects.isNull(electricityCar) || !Objects
                .equals(electricityCar.getTenantId(), TenantContextHolder.getTenantId())) {
            log.error("USER CONTROL CAR ERROR! not found electricityCar, uid={}", uid);
            return Triple.of(false, "100007", "车辆不存在");
        }
        
        if (StrUtil.isBlank(electricityCar.getSn())) {
            log.error("USER CONTROL CAR ERROR! not found electricityCar, uid={}", uid);
            return Triple.of(false, "", "车辆sn码为空");
        }
    
        if (!electricityCarService.carLockCtrl(electricityCar.getSn(), query.getLockType())) {
            return Triple.of(false, "", "请求失败");
        }
    
        //缓存车辆锁状态  同步给客户进行通知  前端超时15S
        redisService.set(CacheConstant.CACHE_CAR_LOCK_STATUS + uid, String.valueOf(query.getLockType()));
        redisService.expire(CacheConstant.CACHE_CAR_LOCK_STATUS + uid, 17000L, false);
        return Triple.of(true, null, null);
    }
    
    @Override
    public Triple<Boolean, String, Object> controlCarCheck() {
    
        Long uid = SecurityUtils.getUid();
        if (Objects.isNull(uid)) {
            log.error("USER CONTROL CAR CHECK ERROR! not found user ");
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户");
        }
    
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.error("USER CONTROL CAR CHECK ERROR! not found user! uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户");
        }
    
        ElectricityCar electricityCar = electricityCarService.queryInfoByUid(uid);
        if (Objects.isNull(electricityCar) || !Objects
                .equals(electricityCar.getTenantId(), TenantContextHolder.getTenantId())) {
            log.error("USER CONTROL CAR CHECK ERROR! not found electricityCar, uid={}", uid);
            return Triple.of(false, "100007", "车辆不存在");
        }
    
        String status = redisService.get(CacheConstant.CACHE_CAR_LOCK_STATUS + uid);
        if (Objects.isNull(status)) {
            return Triple.of(true, "", "001");
        }
    
        R<Jt808DeviceInfoVo> result = jt808RetrofitService
                .getInfo(new Jt808GetInfoRequest(IdUtil.randomUUID(), electricityCar.getSn()));
        if (!result.isSuccess()) {
            log.error("Jt808 error! control Car Check! sn={},result={}", electricityCar.getSn(), result);
            return Triple.of(true, "", "003");
        }
        
        if (Objects.equals(status, String.valueOf(result.getData().getDoorStatus()))) {
            return Triple.of(true, "", "001");
        }
        
        return Triple.of(true, "", "002");
    }
}
