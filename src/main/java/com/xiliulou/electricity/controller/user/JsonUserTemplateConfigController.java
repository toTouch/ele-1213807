package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.template.TemplateConfigService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.core.base.enums.ChannelEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
    public R queryTemplateId() {
        List<String> ids = templateConfigService.queryTemplateIdByTenantIdChannel(TenantContextHolder.getTenantId(), ChannelEnum.WECHAT.getCode());
        return R.ok(ids);
    }
    
    /**
     * 根据渠道查询模版id集合
     *
     * @param channel ${@link ChannelEnum}
     * @author caobotao.cbt
     * @date 2024/7/23 17:25
     */
    @GetMapping("/user/queryTemplateIdByChannel")
    public R queryTemplateIdByChannel(@RequestParam("channel") String channel) {
        List<String> ids = templateConfigService.queryTemplateIdByTenantIdChannel(TenantContextHolder.getTenantId(), channel);
        return R.ok(ids);
    }
}
