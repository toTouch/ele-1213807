package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.annotation.Log;
import com.xiliulou.electricity.service.Jt808CarService;
import com.xiliulou.electricity.web.query.CarControlRequest;
import com.xiliulou.electricity.web.query.CarGpsQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * @author : eclair
 * @date : 2022/12/29 09:49
 */
@Slf4j
@RestController
public class JsonAdminJt808CarController extends BaseController {
    
    @Autowired
    Jt808CarService jt808CarService;
    
    @GetMapping("/admin/jt808/car/info")
    public R queryDeviceInfo(@RequestParam("carId") Integer carId) {
        return returnPairResult(jt808CarService.queryDeviceInfo(carId));
    }
    
    @PostMapping("/admin/jt808/car/control")
    @Log(title = "车辆断/启电")
    public R controlCar(@RequestBody @Validated CarControlRequest request) {
        return returnPairResult(jt808CarService.controlCar(request));
    }
    
    @GetMapping("/admin/jt808/gps/list")
    public R getGpstList(@RequestParam("size") Integer size, @RequestParam("offset") Integer offset,
            @RequestParam("carId") Integer carId, @RequestParam("beginTime") Long beginTime,
            @RequestParam("endTime") Long endTime) {
        if (size > 200) {
            size = 10;
        }
        
        if (offset < 0) {
            offset = 0;
        }
        
        if ((endTime - beginTime) > TimeUnit.HOURS.toMillis(48)) {
            return R.fail("时间跨度不可以大于两天");
        }
        
        CarGpsQuery carGpsQuery = new CarGpsQuery().setCarId(carId).setEndTimeMills(endTime).setStartTimeMills(beginTime)
                .setSize(size).setOffset(offset);
        return returnPairResult(jt808CarService.getGpsList(carGpsQuery));
    }
    
    
}
