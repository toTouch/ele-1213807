package com.xiliulou.electricity.controller.admin;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.BatteryTrackRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * @author : eclair
 * @date : 2023/1/3 17:16
 */
@RestController
@Slf4j
public class JsonAdminBatteryTrackRecordController extends BaseController {
    
    @Autowired
    BatteryTrackRecordService batteryTrackRecordService;
    
    @GetMapping("/admin/battery/track/record")
    public R queryTrackRecord(@RequestParam("sn") String sn, @RequestParam("size") Integer size,
            @RequestParam("offset") Integer offset, @RequestParam("startTime") Long startTime,
            @RequestParam("endTime") Long endTime) {
        
        if (size > 50) {
            size = 10;
        }
        
        if (offset < 0) {
            offset = 0;
        }
        
        if ((endTime - startTime) > TimeUnit.HOURS.toMillis(48)) {
            return R.fail("时间跨度不可以大于两天");
        }
        
        if(StrUtil.isEmpty(sn)) {
            return R.fail("电池编号不可以为空");
        }
        
        return returnPairResult(batteryTrackRecordService.queryTrackRecord(sn, size, offset, startTime, endTime));
        
    }
}
