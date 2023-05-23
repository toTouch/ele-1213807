package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.BatteryModelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-04-15-11:18
 */
@Slf4j
@RestController
public class JsonUserBatteryModelController {

    @Autowired
    private BatteryModelService batteryModelService;

    /**
     * 获取租户所有电池型号
     */
    @GetMapping("/user/battery/model/all")
    public R selectBatteryTypeAll() {
        return R.ok(batteryModelService.selectBatteryTypeAll());
    }


}
