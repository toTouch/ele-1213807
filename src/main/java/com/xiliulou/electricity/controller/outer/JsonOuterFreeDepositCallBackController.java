package com.xiliulou.electricity.controller.outer;

import com.xiliulou.electricity.service.callback.FreeDepositCallBackSerivce;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
     * todo 免押回调通知
     *
     * @param
     * @return
     */
    @PostMapping("/outer/free/notified/{channel}")
    public void freeNotified(@PathVariable("channel") Integer channel, Map<String, Object> params) {
        //return freeDepositCallBackSerivce.freeNotified(channel, params);
    }
    
    
    /**
     * todo 解冻回调通知
     *
     * @param
     * @return
     */
    @PostMapping("/outer/unFree/notified/{channel}")
    public void payNotified(@PathVariable("channel") Integer channel, Map<String, Object> params) {
        
        return;
    }
    
    
    /**
     * 代扣回调通知
     *
     * @param
     * @return
     */
    @PostMapping("/outer/authPay/notified/{channel}")
    public String authPayNotified(@PathVariable("channel") Integer channel, Map<String, Object> params) {
        
        return freeDepositCallBackSerivce.authPayNotified(channel, params);
    }
}
