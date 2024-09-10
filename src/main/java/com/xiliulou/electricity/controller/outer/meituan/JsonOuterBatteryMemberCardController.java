package com.xiliulou.electricity.controller.outer.meituan;

import com.xiliulou.electricity.entity.meituan.MeiTuanRiderMallConfig;
import com.xiliulou.electricity.service.meituan.MeiTuanRiderMallConfigService;
import com.xiliulou.electricity.service.meituan.MeiTuanRiderMallOrderService;
import com.xiliulou.thirdmall.constant.meituan.virtualtrade.VirtualTradeConstant;
import com.xiliulou.thirdmall.entity.meituan.response.JsonR;
import com.xiliulou.thirdmall.enums.meituan.virtualtrade.VirtualTradeStatusEnum;
import com.xiliulou.thirdmall.util.meituan.MeiTuanRiderMallUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;
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
    private MeiTuanRiderMallConfigService meiTuanRiderMallConfigService;
    
    @Resource
    private MeiTuanRiderMallOrderService meiTuanRiderMallOrderService;
    
    @PostMapping("/outer/batteryMemberCard/limitTrade")
    public JsonR meiTuanLimitTradeCheck(@RequestBody Map<String, Object> paramMap) {
        MeiTuanRiderMallConfig meiTuanRiderMallConfig = meiTuanRiderMallConfigService.queryByConfigFromCache(
                MeiTuanRiderMallConfig.builder().appId(paramMap.get(VirtualTradeConstant.APP_ID).toString()).appKey(paramMap.get(VirtualTradeConstant.APP_KEY).toString()).build());
        
        if (Objects.isNull(meiTuanRiderMallConfig)) {
            return JsonR.fail(VirtualTradeStatusEnum.FAIL_APP_CONFIG.getCode(), VirtualTradeStatusEnum.FAIL_APP_CONFIG.getDesc());
        }
        
        Boolean checkSign = MeiTuanRiderMallUtil.checkSign(paramMap, meiTuanRiderMallConfig.getSecret(), paramMap.get(VirtualTradeConstant.SIGN).toString());
        if (!checkSign) {
            return JsonR.fail(VirtualTradeStatusEnum.FAIL_CHECK_SIGN.getCode(), VirtualTradeStatusEnum.FAIL_CHECK_SIGN.getDesc());
        }
        
        return JsonR.ok(meiTuanRiderMallOrderService.meiTuanLimitTradeCheck(paramMap, meiTuanRiderMallConfig));
    }
}
