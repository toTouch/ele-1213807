package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.FreeDepositOrder;
import com.xiliulou.electricity.query.FreeDepositQuery;
import com.xiliulou.electricity.service.FreeDepositOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : eclair
 * @date : 2023/2/15 09:24
 */
@RestController
@Slf4j
public class JsonUserFreeDepositController extends BaseController {
    
    @Autowired
    FreeDepositOrderService freeDepositOrderService;
    
    /**
     * 押金免押的前置检查
     * @return
     */
    @GetMapping("/user/free/deposit/pre/check")
    public R freeDepositPreCheck() {
        return returnTripleResult(freeDepositOrderService.freeDepositPreCheck());
    }
    
    @PostMapping("/user/free/deposit")
    public R freeDepositOrder(@RequestBody @Validated FreeDepositQuery freeDepositQuery) {
        return returnTripleResult(freeDepositOrderService.freeDepositOrder(freeDepositQuery));
    }
}
