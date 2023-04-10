package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.clickhouse.CarAttr;
import com.xiliulou.electricity.query.CarControlQuery;
import com.xiliulou.electricity.web.query.CarControlRequest;
import com.xiliulou.electricity.web.query.CarGpsQuery;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;

/**
 * @author : eclair
 * @date : 2022/12/29 09:53
 */
public interface Jt808CarService {
    
    Pair<Boolean, Object> queryDeviceInfo(Integer carId);
    
    Pair<Boolean, Object> controlCar(CarControlRequest request);
    
    Pair<Boolean, Object> getGpsList(CarGpsQuery carGpsQuery);
    
    List<CarAttr> queryListBySn(String sn, String begin, String end);
    
    Triple<Boolean, String, Object> userControlCar(CarControlQuery query);
    
    Triple<Boolean, String, Object> controlCarCheck();
}
