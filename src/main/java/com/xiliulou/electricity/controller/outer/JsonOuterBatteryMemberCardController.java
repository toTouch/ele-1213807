package com.xiliulou.electricity.controller.outer;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.request.meituan.LimitTradeRequest;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.thirdmall.constant.meituan.virtualtrade.VirtualTradeConstant;
import com.xiliulou.thirdmall.enums.meituan.virtualtrade.VirtualTradeStatusEnum;
import com.xiliulou.thirdmall.util.meituan.MeiTuanRiderMallUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author HeYafeng
 * @description 美团骑手商城
 * @date 2024/8/28 12:50:35
 */
@RestController
@Slf4j
public class JsonOuterBatteryMemberCardController {
    
    @Resource
    private BatteryMemberCardService batteryMemberCardService;
    
    @PostMapping("/outer/batteryMemberCard/limitTrade")
    public R meiTuanLimitTradeCheck(@RequestBody @Validated LimitTradeRequest limitTradeRequest) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put(VirtualTradeConstant.TIMESTAMP, limitTradeRequest.getTimestamp());
        paramMap.put(VirtualTradeConstant.APP_ID, limitTradeRequest.getAppId());
        paramMap.put(VirtualTradeConstant.APP_KEY, limitTradeRequest.getAppKey());
        paramMap.put(VirtualTradeConstant.ACCOUNT, limitTradeRequest.getAccount());
        paramMap.put(VirtualTradeConstant.PROVIDER_SKU_ID, limitTradeRequest.getProviderSkuId());
        
        Boolean checkSign = MeiTuanRiderMallUtil.checkSign(paramMap, limitTradeRequest.getSign());
        if (!checkSign) {
            return R.fail(VirtualTradeStatusEnum.CHECK_SIGN_ERROR.getDesc());
        }
        
        return R.ok(batteryMemberCardService.meiTuanLimitTradeCheck(limitTradeRequest));
    }
}
