package com.xiliulou.electricity.controller.outer;

import com.xiliulou.electricity.request.thirdPartyMall.LimitTradeRequest;
import com.xiliulou.electricity.service.thirdPartyMall.MeiTuanRiderMallOrderService;
import com.xiliulou.thirdmall.entity.meituan.response.JsonR;
import com.xiliulou.thirdmall.enums.meituan.virtualtrade.VirtualTradeStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author HeYafeng
 * @description 美团骑手商城
 * @date 2024/8/28 12:50:35
 */
@RestController
@Slf4j
public class JsonOuterBatteryMemberCardController {
    
    @Resource
    private MeiTuanRiderMallOrderService meiTuanRiderMallOrderService;
    
    @PostMapping("/outer/batteryMemberCard/limitTrade")
    public JsonR meiTuanLimitTradeCheck(LimitTradeRequest limitTradeRequest) {
        String providerSkuId = limitTradeRequest.getProviderSkuId();
        String phone = limitTradeRequest.getAccount();
        if (Objects.isNull(providerSkuId) || StringUtils.isBlank(phone)) {
            log.error("meiTuanLimitTradeCheck error! providerSkuId={}, phone={}", providerSkuId, phone);
            
            return JsonR.fail(VirtualTradeStatusEnum.FAIL_CHECK_SIGN.getCode(), VirtualTradeStatusEnum.FAIL_CHECK_SIGN.getDesc());
        }
        
        return JsonR.ok(meiTuanRiderMallOrderService.meiTuanLimitTradeCheck(providerSkuId, phone));
    }
}
