package com.xiliulou.electricity.controller.outer;

import com.xiliulou.electricity.service.callback.FreeDepositCallBackSerivce;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;


/**
 * @ClassName: JsonOuterFreeDepositCallBackController
 * @description:
 * @author: renhang
 * @create: 2024-08-23 16:26
 */
@RestController
public class JsonOuterFreeDepositCallBackController {
    
    
    @Resource
    private FreeDepositCallBackSerivce freeDepositCallBackSerivce;
    
    /**
     * 免押代扣回调
     *
     * @param channel
     * @return Object
     */
    @PostMapping("/outer/free/notified/{channel}/{business}")
    public Object freeDepositNotified(@PathVariable("channel") Integer channel, @PathVariable("business") Integer business, @RequestBody Map<String, Object> params) {
        return freeDepositCallBackSerivce.freeDepositNotified(channel, business, params);
    }
    
    
}
