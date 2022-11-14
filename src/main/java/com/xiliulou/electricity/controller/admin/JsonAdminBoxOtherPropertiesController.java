package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.BoxOtherProperties;
import com.xiliulou.electricity.query.BoxOtherPropertiesQuery;
import com.xiliulou.electricity.service.BoxOtherPropertiesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2022-11-03-20:00
 */
@RestController
@Slf4j
public class JsonAdminBoxOtherPropertiesController {
    
    @Autowired
    private BoxOtherPropertiesService boxOtherPropertiesService;
    
    
    @PostMapping(value = "/admin/boxOtherProperties")
    public R save(@RequestBody @Validated BoxOtherProperties query) {
        return R.ok(boxOtherPropertiesService.insert(query));
    }
    
}
