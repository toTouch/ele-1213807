package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.ElectricityCabinetHysicsOperRecordService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * (ElectricityCabinetHysicsOperRecord)表控制层
 *
 * @author zgw
 * @since 2022-08-08 14:42:08
 */
@RestController
public class JsonAdminElectricityCabinetHysicsOperRecordController {
    /**
     * 服务对象
     */
    @Resource
    private ElectricityCabinetHysicsOperRecordService electricityCabinetHysicsOperRecordService;


    @GetMapping("/admin/electricity/physics/oper/record/list")
    public R electricityOperRecordList(@RequestParam("size") Integer size,
                                    @RequestParam("offset") Integer offset,
                                    @RequestParam(value = "beginTime", required = false) Long beginTime,
                                    @RequestParam(value = "endTime", required = false) Long endTime,
                                    @RequestParam(value = "cellNo", required = false) Integer cellNo,
                                    @RequestParam(value = "cupboardId", required = false) Integer cupboardId,
                                    @RequestParam(value = "type", required = false) Integer type) {

        return electricityCabinetHysicsOperRecordService.electricityOperRecordList(size, offset, cupboardId, type, beginTime, endTime, cellNo);
    }
}
