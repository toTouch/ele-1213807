package com.xiliulou.electricity.controller.admin;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.BatteryTrackRecordService;
import com.xiliulou.electricity.service.EleBatterySnapshotService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
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
    
    @Autowired
    EleBatterySnapshotService batterySnapshotService;
    
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
        double days = (Double.valueOf(endTime - startTime)) / 1000 / 3600 / 24;
        if (days > 92) {
            return R.fail("时间跨度不可以大于3个月");
        }

        if (StrUtil.isEmpty(sn)) {
            return R.fail("电池编号不可以为空");
        }
        
        return returnPairResult(batteryTrackRecordService.queryTrackRecord(sn, size, offset, startTime, endTime));
        
    }
    
    @GetMapping("/admin/battery/snapshot/list")
    public R getBatterySnapshot(@RequestParam("eId") Integer eId, @RequestParam("size") Integer size,
            @RequestParam("offset") Integer offset, @RequestParam("startTime") Long startTime,
            @RequestParam("endTime") Long endTime) {
        
        if (size > 50) {
            size = 10;
        }
        
        if (offset < 0) {
            offset = 0;
        }
        
        if ((endTime - startTime) > TimeUnit.HOURS.toMillis(72)) {
            return R.fail("时间跨度不可以大于三天");
        }
        
        if (Objects.isNull(eId)) {
            return R.fail("柜机id不可以为空");
        }
        return returnPairResult(batterySnapshotService.queryBatterySnapshot(eId, size, offset, startTime, endTime));
    }
    
    
}
