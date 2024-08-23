package com.xiliulou.electricity.controller.outer;

import com.xiliulou.pay.weixinv3.rsp.WechatV3CallBackResult;
import com.xiliulou.pay.weixinv3.v2.query.WechatV3OrderCallBackRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


/**
 * @ClassName: JsonOuterFreeDepositCallBackController
 * @description:
 * @author: renhang
 * @create: 2024-08-23 16:26
 */
@RestController
public class JsonOuterFreeDepositCallBackController {
    
    /**
     * todo 解冻回调通知
     *
     * @param wechatV3OrderCallBackQuery
     * @return
     */
    @PostMapping("/outer/unFree/notified/{orderId}")
    public WechatV3CallBackResult payNotified(@PathVariable("tenantId") String orderId, @RequestBody WechatV3OrderCallBackRequest wechatV3OrderCallBackQuery) {
        
        return WechatV3CallBackResult.success();
    }
}
