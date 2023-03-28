package com.xiliulou.electricity.controller.admin;

import com.xiliulou.electricity.service.EleParamSettingTemplateService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * (EleParamSettingTemplate)表控制层
 *
 * @author Hardy
 * @since 2023-03-28 09:53:19
 */
@RestController
public class JsonAdminEleParamSettingTemplateController {
    
    /**
     * 服务对象
     */
    @Resource
    private EleParamSettingTemplateService eleParamSettingTemplateService;
    
    
}
