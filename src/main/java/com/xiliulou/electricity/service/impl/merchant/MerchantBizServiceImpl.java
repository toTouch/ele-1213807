package com.xiliulou.electricity.service.impl.merchant;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.Tenant;
import com.xiliulou.electricity.entity.merchant.Merchant;
import com.xiliulou.electricity.enums.battery.BatteryLabelEnum;
import com.xiliulou.electricity.query.ElectricityBatteryDataQuery;
import com.xiliulou.electricity.request.battery.BatteryLabelBatchUpdateRequest;
import com.xiliulou.electricity.service.ElectricityBatteryDataService;
import com.xiliulou.electricity.service.TenantService;
import com.xiliulou.electricity.service.battery.ElectricityBatteryLabelBizService;
import com.xiliulou.electricity.service.battery.ElectricityBatteryLabelService;
import com.xiliulou.electricity.service.merchant.MerchantBizService;
import com.xiliulou.electricity.service.merchant.MerchantService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author SJP
 * @date 2025-02-21 17:27
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantBizServiceImpl implements MerchantBizService {
    
    private final MerchantService merchantService;
    
    private final ElectricityBatteryLabelService electricityBatteryLabelService;
    
    private final ElectricityBatteryLabelBizService electricityBatteryLabelBizService;
    
    private final ElectricityBatteryDataService electricityBatteryDataService;
    
    private final TenantService tenantService;
    
    
    @Override
    public R<Integer> countReceived(Long uid) {
        Merchant merchant = merchantService.queryByUid(uid);
        if (Objects.isNull(merchant)) {
            log.warn("MERCHANT COUNT RECEIVED WARN! merchant is null");
            return R.fail("120212", "商户不存在");
        }
        
        return R.ok(electricityBatteryLabelService.countReceived(merchant.getId()));
    }
    
    @Override
    public R receiveBattery(BatteryLabelBatchUpdateRequest request) {
        if (!Objects.equals(request.getLabel(), BatteryLabelEnum.RECEIVED_MERCHANT.getCode())) {
            log.warn("MERCHANT RECEIVE BATTERY WARN! label wrong, request={}", request);
            return R.fail("300154", "领用异常，请联系管理员");
        }
        
        Merchant merchant = merchantService.queryByUid(SecurityUtils.getUid());
        if (Objects.isNull(merchant)) {
            log.warn("MERCHANT RECEIVE BATTERY WARN! merchant is null");
            return R.fail("120212", "商户不存在");
        }
        request.setReceiverId(merchant.getId());
        
        return electricityBatteryLabelBizService.batchUpdate(request);
    }
    
    @Override
    public R listReceivedBatteriesDetail(ElectricityBatteryDataQuery request) {
        Merchant merchant = merchantService.queryByUid(SecurityUtils.getUid());
        if (Objects.isNull(merchant)) {
            log.warn("MERCHANT RECEIVE BATTERY WARN! merchant is null");
            return R.fail("120212", "商户不存在");
        }
        
        Integer tenantId = TenantContextHolder.getTenantId();
        Tenant tenant = tenantService.queryByIdFromCache(tenantId);
        if (Objects.isNull(tenant)) {
            log.error("MERCHANT RECEIVE BATTERY WARN! tenant is null id={}", tenantId);
            return R.ok(Collections.EMPTY_LIST);
        }
        
        Long size = (request.getSize() < 0 || request.getSize() > 50) ? 10L : request.getSize();
        Long offset = request.getOffset() < 0 ? 0L : request.getOffset();
        
        request.setSize(size);
        request.setOffset(offset);
        request.setTenantId(tenantId);
        request.setTenant(tenant);
        request.setLabel(List.of(BatteryLabelEnum.RECEIVED_MERCHANT.getCode()));
        request.setReceiverId(merchant.getId());
        return electricityBatteryDataService.selectAllBatteryPageData(request);
    }
}
