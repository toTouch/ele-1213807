package com.xiliulou.electricity.controller.outer;

import com.xiliulou.electricity.callback.FreeDepositNotifyService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


/**
 * @ClassName: JsonOuterFreeDepositCallBackController
 * @description:
 * @author: renhang
 * @create: 2024-08-23 16:26
 */
@RestController
@Slf4j
@AllArgsConstructor
public class JsonOuterFreeDepositCallBackController {
    
    
    private final FreeDepositNotifyService freeDepositNotifyService;
    
    /**
     * 免押代扣回调
     *
     * @param channel
     * @return Object
     */
    @PostMapping("/outer/free/notified/{channel}/{business}/{tenantId}")
    public Object freeDepositNotified(@PathVariable("channel") Integer channel, @PathVariable("business") Integer business,@PathVariable("tenantId") Integer tenantId, @RequestBody Map<String, Object> params) {
        return freeDepositNotifyService.notify(channel, business,tenantId, params);
    }

    
}
