package com.xiliulou.electricity.controller.admin.battery;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.battery.BatteryLabelRecord;
import com.xiliulou.electricity.request.battery.BatteryLabelRecordRequest;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.service.battery.BatteryLabelRecordService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author SJP
 * @date 2025-02-21 14:33
 **/
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/battery/labelRecord")
public class JsonAdminElectricityBatteryLabelRecordController {
    
    private final UserDataScopeService userDataScopeService;
    
    private final BatteryLabelRecordService batteryLabelRecordService;
    
    
    @PostMapping("/page")
    public R<List<BatteryLabelRecord>> page(@RequestBody @Validated BatteryLabelRecordRequest request) {
        if (Objects.isNull(request)) {
            log.warn("BATTERY LABEL RECORD PAGE WARN! request is null");
            return R.fail("300150", "数据不合规，请联系管理员");
        }
        
        if (request.getSize() < 0 || request.getSize() > 50) {
            request.setSize(10L);
        }
        
        if (request.getOffset() < 0) {
            request.setOffset(0L);
        }
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(Collections.emptyList());
            }
        }
        
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            request.setOperatorUid(user.getUid());
        }
        
        request.setTenantId(TenantContextHolder.getTenantId());
        request.setFranchiseeIds(franchiseeIds);
        return R.ok(batteryLabelRecordService.listPage(request));
    }
    
    @PostMapping("/count")
    public R<Long> count(@RequestBody BatteryLabelRecordRequest request) {
        if (Objects.isNull(request)) {
            log.warn("BATTERY LABEL RECORD COUNT WARN! request is null");
            return R.fail("300150", "数据不合规，请联系管理员");
        }
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok();
            }
        }
        
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            request.setOperatorUid(user.getUid());
        }
        
        request.setTenantId(TenantContextHolder.getTenantId());
        request.setFranchiseeIds(franchiseeIds);
        return R.ok(batteryLabelRecordService.countAll(request));
    }
}
