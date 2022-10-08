package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.ElectricityCabinetServerOperRecordService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * (ElectricityCabinetServerOperRecord)表控制层
 *
 * @author zgw
 * @since 2022-09-26 17:54:55
 */
@RestController @RequestMapping("admin/electricityCabinetServerOperRecord")
public class JsonAdminElectricityCabinetServerOperRecordController {
    /**
     * 服务对象
     */
    @Resource private ElectricityCabinetServerOperRecordService electricityCabinetServerOperRecordService;

    @GetMapping("queryList")
    public R queryList(@RequestParam(value = "userName", required = false) String createUserName,
        @RequestParam("eleServerId") Long eleServerId, @RequestParam("offset") Long offset,
        @RequestParam("size") Long size) {

        return electricityCabinetServerOperRecordService.queryList(createUserName, eleServerId, offset, size);
    }
}
