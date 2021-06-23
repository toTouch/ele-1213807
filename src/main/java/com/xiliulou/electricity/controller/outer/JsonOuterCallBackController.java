package com.xiliulou.electricity.controller.outer;

import com.xiliulou.pay.weixinv3.query.WechatV3OrderCallBackQuery;
import com.xiliulou.pay.weixinv3.query.WechatV3RefundOrderCallBackQuery;
import com.xiliulou.pay.weixinv3.rsp.WechatV3CallBackResult;
import com.xiliulou.pay.weixinv3.service.WechatV3PostProcessHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @program: XILIULOU
 * @description:
 * @author: Mr.YG
 * @create: 2020-12-04 10:53
 **/
@RestController
@Slf4j
public class JsonOuterCallBackController {
    @Autowired
    WechatV3PostProcessHandler wechatV3PostProcessHandler;

    /**
     * 微信支付通知
     *
     * @return
     */
    @PostMapping("/outer/wechat/pay/notified/{tenantId}")
    public WechatV3CallBackResult payNotified(@PathVariable("tenantId") Integer tenantId, @RequestBody WechatV3OrderCallBackQuery wechatV3OrderCallBackQuery) {
        wechatV3OrderCallBackQuery.setTenantId(tenantId);
        wechatV3PostProcessHandler.postProcessAfterWechatPay(wechatV3OrderCallBackQuery);
        return WechatV3CallBackResult.success();
    }

    /**
     * 微信退款通知
     *
     * @return
     */
    @PostMapping("/outer/wechat/refund/notified/{tenantId}")
    public WechatV3CallBackResult refundNotified(@PathVariable("tenantId") Integer tenantId, @RequestBody WechatV3RefundOrderCallBackQuery wechatV3RefundOrderCallBackQuery) {
        wechatV3RefundOrderCallBackQuery.setTenantId(tenantId);
        wechatV3PostProcessHandler.postProcessAfterWechatRefund(wechatV3RefundOrderCallBackQuery);
        return WechatV3CallBackResult.success();
    }
}
