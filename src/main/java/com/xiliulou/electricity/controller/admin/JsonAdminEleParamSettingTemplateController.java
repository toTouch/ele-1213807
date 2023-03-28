package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.EleParamSettingTemplateBatchSettingQuery;
import com.xiliulou.electricity.query.EleParamSettingTemplateQuery;
import com.xiliulou.electricity.service.EleParamSettingTemplateService;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * (EleParamSettingTemplate)表控制层
 *
 * @author Hardy
 * @since 2023-03-28 09:53:19
 */
@RestController
public class JsonAdminEleParamSettingTemplateController extends BaseController {
    
    /**
     * 服务对象
     */
    @Resource
    private EleParamSettingTemplateService eleParamSettingTemplateService;
    
    @GetMapping("/admin/eleParamSettingTemplate/list")
    public R queryList(@RequestParam("offset") Long offset, @RequestParam("size") Long size,
            @RequestParam(value = "name", required = false) String name) {
        return this.returnTripleResult(eleParamSettingTemplateService.queryList(offset, size, name));
    }
    
    @GetMapping("/admin/eleParamSettingTemplate/queryCount")
    public R queryCount(@RequestParam(value = "name", required = false) String name) {
        return this.returnTripleResult(eleParamSettingTemplateService.queryCount(name));
    }
    
    @DeleteMapping("/admin/eleParamSettingTemplate/{id}")
    public R deleteOne(@PathVariable("id") Long id) {
        return this.returnTripleResult(eleParamSettingTemplateService.deleteOne(id));
    }
    
    @PostMapping("/admin/eleParamSettingTemplate")
    public R saveOne(
            @RequestBody @Validated(value = CreateGroup.class) EleParamSettingTemplateQuery eleParamSettingTemplateQuery) {
        return this.returnTripleResult(eleParamSettingTemplateService.saveOne(eleParamSettingTemplateQuery));
    }
    
    @PutMapping("/admin/eleParamSettingTemplate")
    public R updateOne(
            @RequestBody @Validated(value = UpdateGroup.class) EleParamSettingTemplateQuery eleParamSettingTemplateQuery) {
        return this.returnTripleResult(eleParamSettingTemplateService.updateOne(eleParamSettingTemplateQuery));
    }
}
