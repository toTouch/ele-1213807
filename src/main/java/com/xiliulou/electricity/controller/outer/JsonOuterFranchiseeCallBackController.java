package com.xiliulou.electricity.controller.outer;

import com.xiliulou.electricity.enums.RefundPayOptTypeEnum;
import com.xiliulou.electricity.factory.paycallback.RefundPayServiceFactory;
import com.xiliulou.electricity.service.impl.exrefund.RefundPayBatteryRentServiceImpl;
import com.xiliulou.electricity.service.wxrefund.RefundPayService;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiRefundOrderCallBackResource;
import com.xiliulou.pay.weixinv3.rsp.WechatV3CallBackResult;
import com.xiliulou.pay.weixinv3.v2.handler.WechatV3PostProcessExecuteHandler;
import com.xiliulou.pay.weixinv3.v2.query.WechatV3OrderCallBackRequest;
import com.xiliulou.pay.weixinv3.v2.query.WechatV3RefundOrderCallBackRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Objects;


/**
 * 新回调地址
 *
 * @author caobotao.cbt
 * @date 2024/6/13 13:38
 */
@RestController
@Slf4j
public class JsonOuterFranchiseeCallBackController extends JsonOuterCallBackBasicController {
    
    @Resource
    WechatV3PostProcessExecuteHandler wechatV3PostProcessExecuteHandler;
    
    @Qualifier("newRedisTemplate")
    @Autowired
    RedisTemplate redisTemplate;
    
    
    @Autowired
    private RefundPayBatteryRentServiceImpl batteryRentRefundServiceImpl;
    
    /**
     * 微信支付通知
     *
     * @return
     */
    @PostMapping("/outer/wechat/franchisee/pay/notified/{tenantId}/{franchiseeId}")
    public WechatV3CallBackResult payNotified(@PathVariable("tenantId") Integer tenantId, @PathVariable(value = "franchiseeId") Long franchiseeId,
            @RequestBody WechatV3OrderCallBackRequest wechatV3OrderCallBackQuery) {
        
        if (Objects.isNull(franchiseeId)){
            log.warn("JsonOuterFranchiseeCallBackController.payNotified :franchiseeId={}",franchiseeId);
            franchiseeId=0L;
        }
        
        wechatV3OrderCallBackQuery.setTenantId(tenantId);
        wechatV3OrderCallBackQuery.setFranchiseeId(franchiseeId);
        wechatV3PostProcessExecuteHandler.postProcessAfterWechatPay(wechatV3OrderCallBackQuery);
        return WechatV3CallBackResult.success();
    }
    
    /**
     * 微信退款通知
     *
     * @return
     */
    @PostMapping("/outer/wechat/franchisee/refund/notified/{tenantId}/{franchiseeId}")
    public WechatV3CallBackResult refundNotified(@PathVariable("tenantId") Integer tenantId, @PathVariable(value = "franchiseeId") Long franchiseeId,
            @RequestBody WechatV3RefundOrderCallBackRequest request) {
        if (Objects.isNull(franchiseeId)){
            log.warn("JsonOuterFranchiseeCallBackController.refundNotified :franchiseeId={}",franchiseeId);
            franchiseeId=0L;
        }
        
        request.setTenantId(tenantId);
        request.setFranchiseeId(franchiseeId);
        wechatV3PostProcessExecuteHandler.postProcessAfterWechatRefund(request);
        return WechatV3CallBackResult.success();
    }
    
    /**
     * 微信退款通知(电池租金)
     *
     * @return
     */
    @PostMapping("/outer/wechat/franchisee/battery/membercard/refund/notified/{tenantId}/{franchiseeId}")
    public WechatV3CallBackResult batteryMembercardRefundNotified(@PathVariable("tenantId") Integer tenantId, @PathVariable(value = "franchiseeId") Long franchiseeId,
            @RequestBody WechatV3RefundOrderCallBackRequest request) {
        if (Objects.isNull(franchiseeId)){
            log.warn("JsonOuterFranchiseeCallBackController.batteryMembercardRefundNotified :franchiseeId={}",franchiseeId);
            franchiseeId=0L;
        }
        request.setTenantId(tenantId);
        request.setFranchiseeId(franchiseeId);
        WechatJsapiRefundOrderCallBackResource callBackParam = handCallBackParam(request);
        batteryRentRefundServiceImpl.process(callBackParam);
        return WechatV3CallBackResult.success();
    }
    
    
    /**
     * 微信退款回调(租车押金)
     *
     * @param tenantId                         租户ID
     * @param request 微信回调参数
     * @return
     */
    @PostMapping("/outer/wechat/franchisee/refund/car/deposit/notified/{tenantId}/{franchiseeId}")
    public WechatV3CallBackResult carDepositRefundCallBackUrl(@PathVariable("tenantId") Integer tenantId, @PathVariable(value = "franchiseeId") Long franchiseeId,
            @RequestBody WechatV3RefundOrderCallBackRequest request) {
        if (Objects.isNull(franchiseeId)){
            log.warn("JsonOuterFranchiseeCallBackController.carDepositRefundCallBackUrl :franchiseeId={}",franchiseeId);
            franchiseeId=0L;
        }
        request.setTenantId(tenantId);
        request.setFranchiseeId(franchiseeId);
        WechatJsapiRefundOrderCallBackResource callBackParam = handCallBackParam(request);
        RefundPayService service = RefundPayServiceFactory.getService(RefundPayOptTypeEnum.CAR_DEPOSIT_REFUND_CALL_BACK.getCode());
        service.process(callBackParam);
        return WechatV3CallBackResult.success();
    }
    
    /**
     * 微信退款回调(租车租金)
     *
     * @param tenantId                         租户ID
     * @param request 微信回调参数
     * @return
     */
    @PostMapping("/outer/wechat/franchisee/refund/car/rent/notified/{tenantId}/{franchiseeId}")
    public WechatV3CallBackResult carRentRefundCallBackUrl(@PathVariable("tenantId") Integer tenantId, @PathVariable(value = "franchiseeId") Long franchiseeId,
            @RequestBody WechatV3RefundOrderCallBackRequest request) {
        if (Objects.isNull(franchiseeId)){
            log.warn("JsonOuterFranchiseeCallBackController.carRentRefundCallBackUrl :franchiseeId={}",franchiseeId);
            franchiseeId=0L;
        }
        request.setTenantId(tenantId);
        request.setFranchiseeId(franchiseeId);
        WechatJsapiRefundOrderCallBackResource callBackParam = handCallBackParam(request);
        RefundPayService service = RefundPayServiceFactory.getService(RefundPayOptTypeEnum.CAR_RENT_REFUND_CALL_BACK.getCode());
        service.process(callBackParam);
        return WechatV3CallBackResult.success();
    }
}
