package com.xiliulou.electricity.controller.outer;

import com.xiliulou.electricity.request.meituan.LimitTradeRequest;
import com.xiliulou.electricity.service.meituan.MeiTuanRiderMallOrderService;
import com.xiliulou.electricity.utils.ThirdMallConfigHolder;
import com.xiliulou.thirdmall.entity.meituan.response.JsonR;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

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
    public JsonR meiTuanLimitTradeCheck(@Validated LimitTradeRequest limitTradeRequest) {
        log.info("meiTuanLimitTradeCheck request:{}, tenantId={}", limitTradeRequest, ThirdMallConfigHolder.getTenantId());
        
        return JsonR.ok(meiTuanRiderMallOrderService.meiTuanLimitTradeCheck(limitTradeRequest));
    }
}
