package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.BatteryAttr;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.UserCar;
import com.xiliulou.electricity.service.ElectricityCarService;
import com.xiliulou.electricity.service.UserCarService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * @program: XILIULOU
 * @description:
 * @author: Mr.YG
 * @create: 2020-12-09 17:20
 **/
@RestController
@Slf4j
public class JsonUserElectricityCarController {

    @Autowired
    ElectricityCarService electricityCarService;
    
    @Autowired
    UserCarService userCarService;

    @GetMapping("user/electricityCar")
    public R getSelfElectricityCar() {
        Long uid = SecurityUtils.getUid();
        if (Objects.isNull(uid)) {
            return R.fail("ELECTRICITY.0001", "未找到用户!");
        }

        return R.ok(electricityCarService.queryInfoByUid(uid));
    }
    
    @GetMapping(value = "/user/car/attr/list")
    public R attrList(@RequestParam("beginTime") Long beginTime, @RequestParam("endTime") Long endTime) {
        
        return electricityCarService.attrList(beginTime, endTime);
    }
}
