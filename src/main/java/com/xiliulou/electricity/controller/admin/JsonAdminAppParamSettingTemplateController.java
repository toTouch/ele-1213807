package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.AppParamSettingTemplateQuery;
import com.xiliulou.electricity.service.AppParamSettingTemplateService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * (AppParamSettingTemplate)表控制层
 *
 * @author Hardy
 * @since 2023-03-30 19:53:11
 */
@RestController
public class JsonAdminAppParamSettingTemplateController {
    
    /**
     * 服务对象
     */
    @Resource
    private AppParamSettingTemplateService appParamSettingTemplateService;
    
    @GetMapping("/admin/appParamSettingTemplate/list")
    public R queryList(@RequestParam(value = "size", required = false, defaultValue = "10") Long size,
            @RequestParam(value = "offset", required = false, defaultValue = "0") Long offset) {
        if (Objects.isNull(size) || size < 0 || size > 10) {
            size = 10L;
        }
    
        if (Objects.isNull(offset) || offset < 0) {
            offset = 0L;
        }
        
        return appParamSettingTemplateService.queryList(size, offset);
    }
    
    @GetMapping("/admin/appParamSettingTemplate/queryCount")
    public R queryCount() {
        return appParamSettingTemplateService.queryCount();
    }
    
    @PostMapping("/admin/appParamSettingTemplate")
    public R saveOne(@RequestBody @Validated AppParamSettingTemplateQuery query) {
        return appParamSettingTemplateService.saveOne(query);
    }
    
    @PutMapping("/admin/appParamSettingTemplate")
    public R updateOne(@RequestBody @Validated AppParamSettingTemplateQuery query) {
        return appParamSettingTemplateService.updateOne(query);
    }
    
    @DeleteMapping("/admin/appParamSettingTemplate/{id}")
    public R deleteOne(@PathVariable("id") Long id) {
        return appParamSettingTemplateService.deleteOne(id);
    }
}
