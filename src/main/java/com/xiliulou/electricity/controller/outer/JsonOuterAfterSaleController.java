package com.xiliulou.electricity.controller.outer;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.EleDeviceCodeOuterQuery;
import com.xiliulou.electricity.query.EleDeviceCodeRegisterQuery;
import com.xiliulou.electricity.service.EleDeviceCodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zzlong
 */
@Slf4j
@RestController
public class JsonOuterAfterSaleController extends BaseController {
    
    @Autowired
    private EleDeviceCodeService eleDeviceCodeService;
    
    @PostMapping(value = "/outer/afterSale/device/register")
    public R deviceRegister(@RequestBody @Validated EleDeviceCodeRegisterQuery query) {
        return returnTripleResult(eleDeviceCodeService.deviceRegister(query));
    }
    
    @PostMapping(value = "/outer/afterSale/device/info")
    public R deviceInfo(@RequestBody @Validated EleDeviceCodeOuterQuery query) {
        return returnTripleResult(eleDeviceCodeService.deviceInfo(query));
    }
    
    
}
