package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.TemplateConfigEntity;
import com.xiliulou.electricity.service.TemplateConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author Hardy
 * @date 2021/11/30 19:19
 * @mood
 */
@RestController
@RequestMapping("admin/template/config")
public class JsonAdminTemplateConfigController {
    @Autowired
    private TemplateConfigService templateConfigService;

    /**
     * 租户模板
     */
    @RequestMapping(value = "/info",method = RequestMethod.GET)
    public R info(){
        return templateConfigService.queryByTenantId();
    }

    /**
     * 保存
     */
    @RequestMapping(value = "/save",method = RequestMethod.POST)
    public R save(@RequestBody TemplateConfigEntity templateConfigEntity ){
        return templateConfigService.saveDBandCache(templateConfigEntity);
    }

    /**
     * 修改
     */
    @RequestMapping(value = "/update",method = RequestMethod.PUT)
    public R update(@RequestBody TemplateConfigEntity templateConfig){
        return templateConfigService.updateByIdFromDB(templateConfig);
    }

    /**
     * 删除
     */
    @RequestMapping(value = "/delete/{id}",method = RequestMethod.DELETE)
    public R delete(@PathVariable("id") Long id){
        return templateConfigService.removeByIdFromDB(id);
    }

}
