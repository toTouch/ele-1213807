package com.xiliulou.electricity.controller.outer;

import com.xiliulou.electricity.service.EleCabinetSignatureService;
import com.xiliulou.electricity.service.impl.exrefund.WxRefundPayBatteryRentServiceImpl;
import com.xiliulou.esign.entity.resp.EsignCallBackResp;
import com.xiliulou.electricity.enums.WxRefundPayOptTypeEnum;
import com.xiliulou.electricity.factory.paycallback.WxRefundPayServiceFactory;
import com.xiliulou.electricity.service.wxrefund.WxRefundPayService;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiRefundOrderCallBackResource;
import com.xiliulou.pay.weixinv3.query.WechatV3OrderCallBackQuery;
import com.xiliulou.pay.weixinv3.query.WechatV3RefundOrderCallBackQuery;
import com.xiliulou.pay.weixinv3.rsp.WechatV3CallBackResult;
import com.xiliulou.pay.weixinv3.service.WechatV3PostProcessHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;


/**
 * @program: XILIULOU
 * @description:
 * @author: Mr.YG
 * @create: 2020-12-04 10:53
 **/
@RestController
@Slf4j
public class JsonOuterCallBackController extends JsonOuterCallBackBasicController {

    @Autowired
    WechatV3PostProcessHandler wechatV3PostProcessHandler;
    @Qualifier("newRedisTemplate")
    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    private EleCabinetSignatureService eleCabinetSignatureService;

    @Autowired
    private WxRefundPayBatteryRentServiceImpl batteryRentRefundServiceImpl;

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

    /**
     * 微信退款通知(电池租金)
     *
     * @return
     */
    @PostMapping("/outer/wechat/battery/membercard/refund/notified/{tenantId}")
    public WechatV3CallBackResult batteryMembercardRefundNotified(@PathVariable("tenantId") Integer tenantId, @RequestBody WechatV3RefundOrderCallBackQuery wechatV3RefundOrderCallBackQuery) {
        wechatV3RefundOrderCallBackQuery.setTenantId(tenantId);
        WechatJsapiRefundOrderCallBackResource callBackParam = handCallBackParam(wechatV3RefundOrderCallBackQuery);
        batteryRentRefundServiceImpl.process(callBackParam);
        return WechatV3CallBackResult.success();
    }


    /**
     * 微信退款回调(租车押金)
     * @param tenantId 租户ID
     * @param wechatV3RefundOrderCallBackQuery 微信回调参数
     * @return
     */
    @PostMapping("/outer/wechat/refund/car/deposit/notified/{tenantId}")
    public WechatV3CallBackResult carDepositRefundCallBackUrl(@PathVariable("tenantId") Integer tenantId, @RequestBody WechatV3RefundOrderCallBackQuery wechatV3RefundOrderCallBackQuery) {
        wechatV3RefundOrderCallBackQuery.setTenantId(tenantId);
        WechatJsapiRefundOrderCallBackResource callBackParam = handCallBackParam(wechatV3RefundOrderCallBackQuery);
        WxRefundPayService service = WxRefundPayServiceFactory.getService(WxRefundPayOptTypeEnum.BATTERY_RENT_REFUND_CALL_BACK.getCode());
        service.process(callBackParam);
        return WechatV3CallBackResult.success();
    }

    /**
     * 微信退款回调(租车租金)
     * @param tenantId 租户ID
     * @param wechatV3RefundOrderCallBackQuery 微信回调参数
     * @return
     */
    @PostMapping("/outer/wechat/refund/car/rent/notified/{tenantId}")
    public WechatV3CallBackResult carRentRefundCallBackUrl(@PathVariable("tenantId") Integer tenantId, @RequestBody WechatV3RefundOrderCallBackQuery wechatV3RefundOrderCallBackQuery) {
        wechatV3RefundOrderCallBackQuery.setTenantId(tenantId);
        WechatJsapiRefundOrderCallBackResource callBackParam = handCallBackParam(wechatV3RefundOrderCallBackQuery);
        WxRefundPayService service = WxRefundPayServiceFactory.getService(WxRefundPayOptTypeEnum.CAR_RENT_REFUND_CALL_BACK.getCode());
        service.process(callBackParam);
        return WechatV3CallBackResult.success();
    }

    @PostMapping("/outer/esign/signNotice/{esignConfigId}")
    public EsignCallBackResp signResultNotice(@PathVariable("esignConfigId") Integer esignConfigId, HttpServletRequest request){
        eleCabinetSignatureService.handleCallBackReq(esignConfigId, request);
        return EsignCallBackResp.success();
    }
}
