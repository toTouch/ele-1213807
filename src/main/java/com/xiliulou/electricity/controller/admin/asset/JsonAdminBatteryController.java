package com.xiliulou.electricity.controller.admin.asset;


import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.enums.asset.StockStatusEnum;
import com.xiliulou.electricity.request.asset.BatteryAddRequest;
import com.xiliulou.electricity.request.asset.ElectricityBatteryEnableAllocateRequest;
import com.xiliulou.electricity.request.asset.ElectricityBatterySnSearchRequest;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@RestController
@Slf4j
public class JsonAdminBatteryController {
    
    @Autowired
    private ElectricityBatteryService electricityBatteryService;
    
    @PostMapping(value = "/admin/battery/save")
    public R save(@RequestBody @Validated(value = CreateGroup.class) BatteryAddRequest batteryAddRequest) {
        return electricityBatteryService.saveElectricityBatteryV2(batteryAddRequest);
    }
    
    /**
     * @description 根据运营商查找sn列表
     * @date 2023/11/29 08:34:06
     * @author HeYafeng
     */
    @GetMapping("/admin/battery/snSearch")
    public R snSearchByFranchiseeId(@RequestParam("size") long size, @RequestParam("offset") long offset, @RequestParam(value = "franchiseeId") Long franchiseeId,
            @RequestParam(value = "storeId", required = false) Long storeId, @RequestParam(value = "sn", required = false) String sn) {
        if (size < 0 || size > 50) {
            size = 10L;
        }
        
        if (offset < 0) {
            offset = 0L;
        }
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.warn("ELE WARN! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        ElectricityBatterySnSearchRequest electricityBatterySnSearchRequest = ElectricityBatterySnSearchRequest.builder().tenantId(TenantContextHolder.getTenantId())
                .franchiseeId(franchiseeId).stockStatus(StockStatusEnum.UN_STOCK.getCode()).sn(sn).size(size).offset(offset).build();
        
        return R.ok(electricityBatteryService.listSnByFranchiseeId(electricityBatterySnSearchRequest));
        
    }
    
    /**
     * @description 查询可调拨的电池
     * @date 2023/11/30 18:49:01
     * @author HeYafeng
     */
    @GetMapping("/admin/battery/enableAllocate")
    public R listEnableAllocate(@RequestParam("size") long size, @RequestParam("offset") long offset, @RequestParam(value = "franchiseeId") Long franchiseeId,
            @RequestParam(value = "storeId") Long storeId, @RequestParam(value = "sn", required = false) String sn) {
        if (size < 0 || size > 50) {
            size = 10L;
        }
        
        if (offset < 0) {
            offset = 0L;
        }
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.warn("ELE WARN! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
        
        List<Integer> businessStatusList = List.of(ElectricityBattery.BUSINESS_STATUS_INPUT, ElectricityBattery.BUSINESS_STATUS_RETURN);
        
        ElectricityBatteryEnableAllocateRequest enableAllocateRequest = ElectricityBatteryEnableAllocateRequest.builder().tenantId(TenantContextHolder.getTenantId())
                .franchiseeId(franchiseeId).physicsStatus(ElectricityBattery.PHYSICS_STATUS_WARE_HOUSE).businessStatusList(businessStatusList).sn(sn).size(size).offset(offset)
                .build();
        
        return R.ok(electricityBatteryService.listEnableAllocateBattery(enableAllocateRequest));
    }
}
