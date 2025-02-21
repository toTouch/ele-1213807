package com.xiliulou.electricity.service.impl.merchant;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.merchant.Merchant;
import com.xiliulou.electricity.enums.battery.BatteryLabelEnum;
import com.xiliulou.electricity.request.battery.BatteryLabelBatchUpdateRequest;
import com.xiliulou.electricity.service.battery.ElectricityBatteryLabelBizService;
import com.xiliulou.electricity.service.battery.ElectricityBatteryLabelService;
import com.xiliulou.electricity.service.merchant.MerchantBizService;
import com.xiliulou.electricity.service.merchant.MerchantService;
import com.xiliulou.electricity.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
}
