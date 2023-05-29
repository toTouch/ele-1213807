package com.xiliulou.electricity.controller.user;

import com.xiliulou.clickhouse.service.ClickHouseService;
import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.BatteryAttr;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import jodd.util.StringUtil;
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
import java.util.concurrent.TimeUnit;

/**
 * @program: XILIULOU
 * @description:
 * @author: Mr.YG
 * @create: 2020-12-09 17:20
 **/
@RestController
@Slf4j
public class JsonUserElectricityCabinetBatteryController extends BaseController {


    @Autowired
    ElectricityBatteryService electricityBatteryService;
    @Autowired
    ClickHouseService clickHouseService;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @GetMapping("user/battery")
    public R getSelfBattery(@RequestParam(value = "isNeedLocation", required = false) Integer isNeedLocation) {
        Long uid = SecurityUtils.getUid();
        if (Objects.isNull(uid)) {
            return R.fail("ELECTRICITY.0001", "未找到用户!");
        }

        return returnTripleResult(electricityBatteryService.queryInfoByUid(uid, isNeedLocation));
    }

    @GetMapping(value = "/user/battery/attr/list")
    public R attrList(@RequestParam("beginTime") Long beginTime,
                      @RequestParam("endTime") Long endTime) {

        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("rentBattery  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (endTime - beginTime > TimeUnit.HOURS.toMillis(10)) {
            return R.failMsg("时间跨度不可以超过10小时");
        }

        return returnTripleResult(electricityBatteryService.queryBatteryLocationTrack(user.getUid(), beginTime, endTime));

    }
}
