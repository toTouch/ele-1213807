package com.xiliulou.electricity.controller.user;

import com.xiliulou.clickhouse.service.ClickHouseService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.BatteryAttr;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
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
public class JsonUserElectricityCabinetBatteryController {


    @Autowired
    ElectricityBatteryService electricityBatteryService;
    @Autowired
    ClickHouseService clickHouseService;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @GetMapping("user/battery")
    public R getSelfBattery() {
        Long uid = SecurityUtils.getUid();
        if (Objects.isNull(uid)) {
            return R.fail("ELECTRICITY.0001", "未找到用户!");
        }

        return R.ok(electricityBatteryService.queryInfoByUid(uid));
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

        ElectricityBattery electricityBattery = electricityBatteryService.queryByUid(user.getUid());
        if (Objects.isNull(electricityBattery)){
            log.error("query  ERROR! not found Battery! uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0020", "未找到电池");
        }

        LocalDateTime beginLocalDateTime = LocalDateTime.ofEpochSecond(beginTime / 1000, 0, ZoneOffset.ofHours(8));
        LocalDateTime endLocalDateTime = LocalDateTime.ofEpochSecond(endTime / 1000, 0, ZoneOffset.ofHours(8));
        String begin = formatter.format(beginLocalDateTime);
        String end = formatter.format(endLocalDateTime);


        //给加的搜索，没什么意义
        String sql = "select * from t_battery_attr where devId=? and createTime>=? AND createTime<=? order by  createTime desc";
        return R.ok(clickHouseService.query(BatteryAttr.class, sql, electricityBattery.getSn(), begin, end));
    }
}
