package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.MaintenanceRecordListQuery;
import com.xiliulou.electricity.query.UserMaintenanceQuery;
import com.xiliulou.electricity.service.MaintenanceRecordService;
import com.xiliulou.electricity.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : eclair
 * @date : 2021/9/26 3:04 下午
 */
@RestController
public class JsonUserSubmitMaintenanceController extends BaseController {
    @Autowired
    MaintenanceRecordService maintenanceRecordService;

    @PostMapping("/user/submit/maintenance/record")
    public R submitRecord(@RequestBody @Validated UserMaintenanceQuery userMaintenanceQuery) {
        return returnTripleResult(maintenanceRecordService.saveSubmitRecord(userMaintenanceQuery));
    }

    @GetMapping("/user/maintenance/list")
    public R getList(@RequestParam(value = "beginTime", required = false) Long beginTime,
                     @RequestParam(value = "endTime", required = false) Long endTime,
                     @RequestParam(value = "size") Integer size,
                     @RequestParam(value = "offset") Integer offset,
                     @RequestParam(value = "status", required = false) String status,
                     @RequestParam(value = "type", required = false) String type) {
        if (size <= 0 || size >= 50) {
            size = 10;
        }

        if (offset < 0) {
            offset = 0;
        }

        MaintenanceRecordListQuery query = MaintenanceRecordListQuery.builder()
                .beginTime(beginTime)
                .endTime(endTime)
                .offset(offset)
                .size(size)
                .status(status)
                .type(type)
                .uid(SecurityUtils.getUid()).build();


        return returnTripleResult(maintenanceRecordService.queryListForUser(query));
    }

    @GetMapping("/user/maintenance/queryCount")
    public R queryCount(@RequestParam(value = "beginTime", required = false) Long beginTime,
            @RequestParam(value = "endTime", required = false) Long endTime,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "type", required = false) String type) {


        MaintenanceRecordListQuery query = MaintenanceRecordListQuery.builder()
                .beginTime(beginTime)
                .endTime(endTime)
                .status(status)
                .type(type)
                .uid(SecurityUtils.getUid()).build();


        return maintenanceRecordService.queryCountForUser(query);
    }
}
