package com.xiliulou.electricity.controller.outer;

import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.electricity.enums.FreeDepositServiceWayEnums;
import com.xiliulou.electricity.service.handler.BaseFreeDepositService;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Objects;


/**
 * @ClassName: JsonOuterFreeDepositCallBackController
 * @description:
 * @author: renhang
 * @create: 2024-08-23 16:26
 */
@RestController
public class JsonOuterFreeDepositCallBackController {
    
    
    @Resource
    private ApplicationContext applicationContext;
    
    /**
     * 免押代扣回调
     *
     * @param channel
     * @return Object
     */
    @PostMapping("/outer/free/notified/{channel}/{business}")
    public Object freeDepositNotified(@PathVariable("channel") Integer channel, @PathVariable("business") Integer business, @RequestBody Map<String, Object> params) {
        if (Objects.isNull(channel)) {
            throw new CustomBusinessException("免押回调异常");
        }
        BaseFreeDepositService service = applicationContext.getBean(FreeDepositServiceWayEnums.getClassStrByChannel(channel), BaseFreeDepositService.class);
        return service.freeDepositNotified(business, params);
    }
    
    
}
