package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityCabinetPhysicsOperRecord;
import com.xiliulou.electricity.service.ElectricityCabinetPhysicsOperRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Anotate:zgw
 */
@RestController
public class JsonAdminElectricityCabinetPhysicsOperRecordController {

    @Autowired
    ElectricityCabinetPhysicsOperRecordService electricityCabinetPhysicsOperRecordService;

    @GetMapping("/admin/electricityCabinet/physics/oper/record/list")
    public R cupboardOperRecordList(@RequestParam("size") Integer size,
                                    @RequestParam("offset") Integer offset,
                                    @RequestParam(value = "beginTime", required = false) Long beginTime,
                                    @RequestParam(value = "endTime", required = false) Long endTime,
                                    @RequestParam(value = "cellNo", required = false) Integer cellNo,
                                    @RequestParam(value = "eleId", required = false) Integer eleId,
                                    @RequestParam(value = "operateType", required = false) Integer operateType,
                                    @RequestParam(value = "userName", required = false) String userName,
                                    @RequestParam(value = "phone", required = false) String phone) {

        return electricityCabinetPhysicsOperRecordService.electricityCabinetOperRecordList(size, offset, eleId, operateType, beginTime, endTime, cellNo, userName, phone);
    }
}
