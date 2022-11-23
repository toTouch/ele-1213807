package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.EleOtaUpgradeHistory;
import com.xiliulou.electricity.service.EleOtaUpgradeHistoryService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * (EleOtaUpgradeHistory)表控制层
 *
 * @author Hardy
 * @since 2022-10-14 14:35:41
 */
@RestController
public class JsonAdminEleOtaUpgradeHistoryController {
    
    /**
     * 服务对象
     */
    @Resource
    private EleOtaUpgradeHistoryService eleOtaUpgradeHistoryService;
    
    
    @GetMapping("admin/eleOtaUpgradeHistory/queryList")
    public R queryList(@RequestParam(value = "eid") Integer eid,
            @RequestParam(value = "cellNo", required = false) Integer cellNo,
            @RequestParam(value = "type", required = false) Integer type,
            @RequestParam(value = "upgradeVersion", required = false) String upgradeVersion,
            @RequestParam(value = "historyVersion", required = false) String historyVersion,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "startTime", required = false) Long startTime,
            @RequestParam(value = "endTime", required = false) Long endTime, @RequestParam(value = "offset", required = false) Long offset,
            @RequestParam(value = "size", required = false) Long size) {
    
        if (Objects.isNull(offset)) {
            offset = 0L;
        }
    
        if (Objects.isNull(size)) {
            size = 50L;
        }
       
        return eleOtaUpgradeHistoryService
                .queryList(eid, cellNo, type, upgradeVersion, historyVersion, status, startTime, endTime, offset, size);
    }
}
