package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.MaintenanceRecordHandleQuery;
import com.xiliulou.electricity.query.MaintenanceRecordListQuery;
import com.xiliulou.electricity.service.MaintenanceRecordService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : eclair
 * @date : 2021/9/26 4:02 下午
 */
@RestController
public class JsonAdminMaintenanceRecordController extends BaseController {
    @Autowired
    MaintenanceRecordService maintenanceRecordService;

    @GetMapping("/admin/maintenance/record/list")
    public R getList(@RequestParam(value = "beginTime", required = false) Long beginTime,
                     @RequestParam(value = "endTime", required = false) Long endTime,
                     @RequestParam(value = "size") Integer size,
                     @RequestParam(value = "offset") Integer offset,
                     @RequestParam(value = "status", required = false) String status,
                     @RequestParam(value = "type", required = false) String type,
                     @RequestParam(value = "electricityCabinetId", required = false) Integer electricityCabinetId) {

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

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
                .electricityCabinetId(electricityCabinetId)
                .tenantId(tenantId)
                .build();
        return returnTripleResult(maintenanceRecordService.queryListForAdmin(query));
    }


    @GetMapping("/admin/maintenance/record/queryCount")
    public R queryCount(@RequestParam(value = "beginTime", required = false) Long beginTime,
            @RequestParam(value = "endTime", required = false) Long endTime,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "electricityCabinetId", required = false) Integer electricityCabinetId) {


        MaintenanceRecordListQuery query = MaintenanceRecordListQuery.builder()
                .beginTime(beginTime)
                .endTime(endTime)
                .status(status)
                .type(type)
                .electricityCabinetId(electricityCabinetId)
                .build();
        return maintenanceRecordService.queryCountForAdmin(query);
    }



    @PostMapping("/admin/maintenance/handle")
    public R handleMaintenance(@RequestBody @Validated MaintenanceRecordHandleQuery maintenanceRecordHandleQuery) {
        return returnTripleResult(maintenanceRecordService.handleMaintenanceRecord(maintenanceRecordHandleQuery));
    }


}
