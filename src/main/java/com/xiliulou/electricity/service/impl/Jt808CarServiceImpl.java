package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.xiliulou.core.utils.TimeUtils;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.DS;
import com.xiliulou.electricity.entity.ElectricityCar;
import com.xiliulou.electricity.entity.clickhouse.CarAttr;
import com.xiliulou.electricity.mapper.CarAttrMapper;
import com.xiliulou.electricity.service.ElectricityCarService;
import com.xiliulou.electricity.service.Jt808CarService;
import com.xiliulou.electricity.service.retrofit.Jt808RetrofitService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.vo.CarGpsVo;
import com.xiliulou.electricity.vo.Jt808DeviceInfoVo;
import com.xiliulou.electricity.web.query.CarControlRequest;
import com.xiliulou.electricity.web.query.CarGpsQuery;
import com.xiliulou.electricity.web.query.jt808.Jt808DeviceControlRequest;
import com.xiliulou.electricity.web.query.jt808.Jt808GetInfoRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
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
    
        if (!electricityCarService.carLockCtrl(electricityCar, request.getLockType())) {
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
}
