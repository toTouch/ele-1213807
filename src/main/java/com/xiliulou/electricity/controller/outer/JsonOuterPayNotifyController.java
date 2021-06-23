package com.xiliulou.electricity.controller.outer;

import cn.hutool.core.util.ObjectUtil;
import com.jpay.ext.kit.HttpKit;
import com.jpay.ext.kit.PaymentKit;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.electricity.entity.ElectricityTradeOrder;
import com.xiliulou.electricity.service.ElectricityPayParamsService;
import com.xiliulou.electricity.service.ElectricityTradeOrderService;
import com.xiliulou.pay.weixin.entity.WeiXinPayNotify;
import com.xiliulou.pay.weixin.notify.WeiXinPayNotifyService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Objects;

/**
 * @program: XILIULOU
 * @description:
 * @author: Mr.YG
 * @create: 2020-12-04 10:53
 **/
@RestController
@Slf4j
public class JsonOuterPayNotifyController {
    @Autowired
    WeiXinPayNotifyService weiXinPayNotifyService;

    @Autowired
    RedisService redisService;
    @Autowired
    ElectricityPayParamsService electricityPayParamsService;
    @Autowired
    ElectricityTradeOrderService electricityTradeOrderService;

    @PostMapping("outer/pay/notify/weixin/{tenantId}")
    public String payNotified(HttpServletRequest request) {
        String xmlMsg = HttpKit.readData(request);
        log.info("WEI_XIN PAY_NOTIFY MSG:{}", xmlMsg);
        //转换成map
        Map<String, String> params = PaymentKit.xmlToMap(xmlMsg);
        String orderNo = params.get("out_trade_no");
        String attach = params.get("attach");

        if (!redisService.setNx("notify_order_no" + orderNo, String.valueOf(System.currentTimeMillis()), 10 * 1000L, false)) {
            return "FAILED";
        }
        //去重
        ElectricityPayParams electricityPayParams
                = electricityPayParamsService.queryFromCache();
        if (ObjectUtil.isEmpty(electricityPayParams)) {
            log.error("WEIXIN_PAY_NOTIFY  ERROR,NOT FOUND ELECTRICITY_PAY_PARAMS");
            return "FAILED";
        }
        Pair<Boolean, Object> paramPair = weiXinPayNotifyService.handlerNotify(params, electricityPayParams.getPaternerKey());
        if (!paramPair.getLeft()) {
            return "FAILED";
        }
        WeiXinPayNotify weiXinPayNotify = (WeiXinPayNotify) paramPair.getRight();
        Pair<Boolean, Object> notifyOrderPair=null;
        if(Objects.equals(attach, ElectricityTradeOrder.ATTACH_DEPOSIT)){
            notifyOrderPair=electricityTradeOrderService.notifyDepositOrder(weiXinPayNotify);
        }else {
            notifyOrderPair=electricityTradeOrderService.notifyMemberOrder(weiXinPayNotify);
        }

        redisService.delete("notify_order_no" + orderNo);
        if (notifyOrderPair.getLeft()) {

            return "OK";
        } else {
            return "FAILED";

        }

    }

}
