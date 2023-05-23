package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.BatteryChargeConfig;
import com.xiliulou.electricity.query.BatteryChargeConfigQuery;
import com.xiliulou.electricity.service.BatteryChargeConfigService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * app电池充电设置
 *
 * @author zzlong
 * @since 2022-08-12 14:49:37
 */
@RestController
@RequestMapping("/admin/")
public class JsonAdminBatteryChargeConfigController {

    @Autowired
    private BatteryChargeConfigService batteryChargeConfigService;

    /**
     * 根据柜机id获取
     *
     * @param electricityCabinetId
     * @return
     */
    @GetMapping("batteryChargeConfig/{electricityCabinetId}")
    public R selectByElectricityCabinetId(@PathVariable("electricityCabinetId") Long electricityCabinetId) {
        
        Integer tenantId=null;
        
        if(!SecurityUtils.isAdmin()){
            tenantId= TenantContextHolder.getTenantId();
        }
    
        BatteryChargeConfigQuery query = new BatteryChargeConfigQuery();
        query.setElectricityCabinetId(electricityCabinetId);
        query.setDelFlag(BatteryChargeConfig.DEL_NORMAL);
        query.setTenantId(tenantId);
    
        return R.ok(this.batteryChargeConfigService.selectByElectricityCabinetId(query));
    }

}
