package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.EleOtherConfig;
import com.xiliulou.electricity.service.EleOtherConfigService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * @author: eclair
 * @Date: 2021/4/19 09:43
 * @Description:
 */
@RestController
public class JsonAdminEleOtherConfigController extends BaseController {
    @Autowired
    EleOtherConfigService eleOtherConfigService;

    @GetMapping("/admin/ele/other/config/{eid}")
    public R queryEleOtherConfigByCid(@PathVariable("eid") Integer eid) {

        EleOtherConfig eleOtherConfig = eleOtherConfigService.queryByEidFromCache(eid);
        if(Objects.nonNull(eleOtherConfig) && SecurityUtils.isAdmin()){
            return R.ok(eleOtherConfig);
        }
        
        if (Objects.isNull(eleOtherConfig) || !Objects.equals(eleOtherConfig.getTenantId(), TenantContextHolder.getTenantId())) {
            return R.ok();
        }

        return R.ok(eleOtherConfig);
    }

    @PutMapping("/admin/ele/other/config")
    public R updateEleOtherConfig(@RequestBody EleOtherConfig eleOtherConfig) {
        return eleOtherConfigService.updateEleOtherConfig(eleOtherConfig);
    }


}
