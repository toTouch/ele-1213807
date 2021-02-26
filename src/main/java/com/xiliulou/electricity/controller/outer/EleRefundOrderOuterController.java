package com.xiliulou.electricity.controller.outer;
import cn.hutool.core.util.ObjectUtil;
import com.jpay.ext.kit.HttpKit;
import com.jpay.ext.kit.PaymentKit;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.electricity.service.EleRefundOrderService;
import com.xiliulou.electricity.service.ElectricityPayParamsService;
import com.xiliulou.pay.weixin.entity.WeiXinPayNotify;
import com.xiliulou.pay.weixin.entity.WeiXinRefundNotify;
import com.xiliulou.pay.weixin.notify.WeiXinRefundNotifyService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 退款订单表(TEleRefundOrder)表控制层
 *
 * @author makejava
 * @since 2021-02-22 10:17:06
 */
@RestController
@Slf4j
public class EleRefundOrderOuterController {
    /**
     * 服务对象
     */
    @Resource
    EleRefundOrderService eleRefundOrderService;
    @Autowired
    RedisService redisService;
    @Autowired
    ElectricityPayParamsService electricityPayParamsService;
    @Autowired
    WeiXinRefundNotifyService weiXinRefundNotifyService;


    @PostMapping("outer/refund/notify/weixin")
    public String WeiXinPayNotify(HttpServletRequest request) {
        String xmlMsg = HttpKit.readData(request);
        log.info("WEI_XIN PAY_NOTIFY MSG:{}", xmlMsg);
        //转换成map
        Map<String, String> params = PaymentKit.xmlToMap(xmlMsg);
        String orderNo = params.get("out_refund_no");
        String attach = params.get("attach");
        //去重
        if (!redisService.setNx("out_refund_no" + orderNo, String.valueOf(System.currentTimeMillis()), 10 * 1000L, false)) {
            return "FAILED";
        }

        ElectricityPayParams electricityPayParams
                = electricityPayParamsService.getElectricityPayParams();
        if (ObjectUtil.isEmpty(electricityPayParams)) {
            log.error("WEIXIN_PAY_NOTIFY  ERROR,NOT FOUND ELECTRICITY_PAY_PARAMS");
            return "FAILED";
        }
        Pair<Boolean, Object> paramPair = weiXinRefundNotifyService.handlerNotify(params, electricityPayParams.getPaternerKey());
        if (!paramPair.getLeft()) {
            return "FAILED";
        }
        WeiXinRefundNotify weiXinRefundNotify = (WeiXinRefundNotify) paramPair.getRight();
        Pair<Boolean, Object> notifyOrderPair=eleRefundOrderService.notifyDepositRefundOrder(weiXinRefundNotify);
        redisService.deleteKeys("out_refund_no" + orderNo);
        if (notifyOrderPair.getLeft()) {

            return "OK";
        } else {
            return "FAILED";

        }

    }


}