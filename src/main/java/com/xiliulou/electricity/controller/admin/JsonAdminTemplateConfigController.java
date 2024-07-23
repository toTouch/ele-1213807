package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.TemplateConfigEntity;
import com.xiliulou.electricity.request.template.TemplateConfigOptRequest;
import com.xiliulou.electricity.service.template.TemplateConfigService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.pay.base.enums.ChannelEnum;
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
    @RequestMapping(value = "/info", method = RequestMethod.GET)
    public R info() {
        Integer tenantId = TenantContextHolder.getTenantId();
        TemplateConfigEntity configEntity = templateConfigService.queryByTenantIdAndChannelFromCache(tenantId, ChannelEnum.WECHAT.getCode());
        return R.ok(configEntity);
    }
    
    /**
     * 保存
     */
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public R save(@RequestBody TemplateConfigOptRequest request) {
        request.setChannel(ChannelEnum.WECHAT.getCode());
        request.setTenantId(TenantContextHolder.getTenantId());
        return templateConfigService.insert(request);
    }
    
    /**
     * 修改
     */
    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    public R update(@RequestBody TemplateConfigOptRequest request) {
        return templateConfigService.update(request);
    }
    
    /**
     * 删除
     */
    @RequestMapping(value = "/delete/{id}", method = RequestMethod.DELETE)
    public R delete(@PathVariable("id") Long id) {
        return templateConfigService.delete(TenantContextHolder.getTenantId(),id);
    }
    
}
