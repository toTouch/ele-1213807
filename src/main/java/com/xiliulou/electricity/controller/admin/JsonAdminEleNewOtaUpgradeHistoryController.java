package com.xiliulou.electricity.controller.admin;

import com.xiliulou.electricity.entity.EleNewOtaUpgradeHistory;
import com.xiliulou.electricity.service.EleNewOtaUpgradeHistoryService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * (EleNewOtaUpgradeHistory)表控制层
 *
 * @author Hardy
 * @since 2023-02-20 15:52:07
 */
@RestController
@RequestMapping("eleNewOtaUpgradeHistory")
public class JsonAdminEleNewOtaUpgradeHistoryController {
    
    /**
     * 服务对象
     */
    @Resource
    private EleNewOtaUpgradeHistoryService eleNewOtaUpgradeHistoryService;
    
}
