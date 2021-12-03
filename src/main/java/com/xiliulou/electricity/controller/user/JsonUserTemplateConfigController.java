package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.TemplateConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zgw
 * @date 2021/12/3 14:24
 * @mood
 */
@RestController
public class JsonUserTemplateConfigController {

    @Autowired
    TemplateConfigService templateConfigService;

    @GetMapping("/user/getTemplateId")
    public R queryTemplateId(){
        return templateConfigService.queryTemplateId();
    }
}
