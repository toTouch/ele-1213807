package com.xiliulou.electricity.controller.admin;

import com.xiliulou.electricity.entity.EleOtaUpgrade;
import com.xiliulou.electricity.service.EleOtaUpgradeService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * (EleOtaUpgrade)表控制层
 *
 * @author Hardy
 * @since 2022-10-14 09:02:01
 */
@RestController
public class JsonAdminEleOtaUpgradeController {
    
    /**
     * 服务对象
     */
    @Resource
    private EleOtaUpgradeService eleOtaUpgradeService;
    
    
}
