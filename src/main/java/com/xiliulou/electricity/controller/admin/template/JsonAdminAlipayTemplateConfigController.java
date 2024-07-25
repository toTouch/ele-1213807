package com.xiliulou.electricity.controller.admin.template;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.TemplateConfigEntity;
import com.xiliulou.electricity.request.template.TemplateConfigOptRequest;
import com.xiliulou.electricity.service.template.TemplateConfigService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.validator.UpdateGroup;
import com.xiliulou.core.base.enums.ChannelEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * 支付宝消息模版配置
 *
 * @author caobotao.cbt
 * @date 2024/7/23 16:58
 */
@RestController
@RequestMapping("admin/alipay/template/config")
public class JsonAdminAlipayTemplateConfigController {
    
    @Autowired
    private TemplateConfigService templateConfigService;
    
    
    /**
     * 租户模板
     */
    @RequestMapping(value = "/info", method = RequestMethod.GET)
    public R info() {
        Integer tenantId = TenantContextHolder.getTenantId();
        TemplateConfigEntity configEntity = templateConfigService.queryByTenantIdAndChannelFromCache(tenantId, ChannelEnum.ALIPAY.getCode());
        return R.ok(configEntity);
    }
    
    /**
     * 保存
     */
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public R save(@RequestBody TemplateConfigOptRequest request) {
        request.setChannel(ChannelEnum.ALIPAY.getCode());
        request.setTenantId(TenantContextHolder.getTenantId());
        return templateConfigService.insert(request);
    }
    
    /**
     * 修改
     */
    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    public R update(@RequestBody @Validated(UpdateGroup.class) TemplateConfigOptRequest request) {
        request.setTenantId(TenantContextHolder.getTenantId());
        request.setChannel(ChannelEnum.ALIPAY.getCode());
        return templateConfigService.update(request);
    }
    
    /**
     * 删除
     */
    @RequestMapping(value = "/delete/{id}", method = RequestMethod.DELETE)
    public R delete(@PathVariable("id") Long id) {
        return templateConfigService.delete(TenantContextHolder.getTenantId(), id);
    }
    
}
