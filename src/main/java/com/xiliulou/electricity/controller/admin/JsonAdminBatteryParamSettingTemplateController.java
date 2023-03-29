package com.xiliulou.electricity.controller.admin;


import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.BatteryParamSettingTemplateQuery;
import com.xiliulou.electricity.service.BatteryParamSettingTemplateService;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * (BatteryParamSettingTemplate)表控制层
 *
 * @author Hardy
 * @since 2023-03-29 09:20:24
 */
@RestController
public class JsonAdminBatteryParamSettingTemplateController extends BaseController {
    
    /**
     * 服务对象
     */
    @Resource
    private BatteryParamSettingTemplateService batteryParamSettingTemplateService;
    
    @GetMapping("/admin/batteryParamSettingTemplate")
    public R queryList(@RequestParam("offset") Long offset, @RequestParam("size") Long size,
            @RequestParam(value = "name", required = false) String name) {
        return this.returnTripleResult(batteryParamSettingTemplateService.queryList(offset, size, name));
    }
    
    @GetMapping("/admin/batteryParamSettingTemplate")
    public R queryCount(@RequestParam(value = "name", required = false) String name) {
        return this.returnTripleResult(batteryParamSettingTemplateService.queryCount(name));
    }
    
    @DeleteMapping("/admin/batteryParamSettingTemplate/{id}")
    public R deleteOne(@PathVariable("id") Long id) {
        return this.returnTripleResult(batteryParamSettingTemplateService.deleteOne(id));
    }
    
    @PostMapping("/admin/batteryParamSettingTemplate")
    public R saveOne(@RequestBody @Validated(value = CreateGroup.class) BatteryParamSettingTemplateQuery query) {
        return this.returnTripleResult(batteryParamSettingTemplateService.saveOne(query));
    }
    
    @PutMapping("/admin/batteryParamSettingTemplate")
    public R updateOne(@RequestBody @Validated(value = UpdateGroup.class) BatteryParamSettingTemplateQuery query) {
        return this.returnTripleResult(batteryParamSettingTemplateService.updateOne(query));
    }
}
