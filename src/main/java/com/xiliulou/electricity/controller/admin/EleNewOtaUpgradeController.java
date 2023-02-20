package com.xiliulou.electricity.controller.admin;

import com.xiliulou.electricity.service.EleNewOtaUpgradeService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * (EleNewOtaUpgrade)表控制层
 *
 * @author Hardy
 * @since 2023-02-20 15:58:55
 */
@RestController
@RequestMapping("eleNewOtaUpgrade")
public class EleNewOtaUpgradeController {
    
    /**
     * 服务对象
     */
    @Resource
    private EleNewOtaUpgradeService eleNewOtaUpgradeService;
    
}
