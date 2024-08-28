package com.xiliulou.electricity.controller.outer;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.request.meituan.MeiTuanTradAbleRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    private VirtualTradeService virtualTradeService;
    
    @PostMapping("/outer/batteryMemberCard/tradAble/meiTuan")
    public R refundNotified(@RequestBody @Validated MeiTuanTradAbleRequest meetingTuanTradAbleRequest) {
        
        return R.ok();
    }
}
