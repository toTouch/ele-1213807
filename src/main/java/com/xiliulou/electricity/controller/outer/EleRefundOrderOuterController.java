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
    ElectricityPayParamsService electricityPayParamsService;
    @Autowired
    WeiXinRefundNotifyService weiXinRefundNotifyService;


    @PostMapping("outer/refund/notify/weixin")
    public String WeiXinPayNotify(HttpServletRequest request) {
        String xmlMsg = HttpKit.readData(request);
        log.info("WEI_XIN REFUND_NOTIFY MSG:{}", xmlMsg);
        //转换成map
        Map<String, String> refundMap = PaymentKit.xmlToMap(xmlMsg);
        //验签
        ElectricityPayParams electricityPayParams
                = electricityPayParamsService.getElectricityPayParams();
        if (ObjectUtil.isEmpty(electricityPayParams)) {
            log.error("WEIXIN_PAY_NOTIFY  ERROR,NOT FOUND ELECTRICITY_PAY_PARAMS");
            return "fail";
        }
        Pair<Boolean, Object> verifyNotifyPair = weiXinRefundNotifyService.handlerNotify(refundMap,electricityPayParams.getPaternerKey());
        if (!verifyNotifyPair.getLeft()) {
            return "fail";
        }
        Map<String, String> afterDecryptRefundMap = (Map<String, String>) verifyNotifyPair.getRight();
        log.info("afterDecryptRefundMap:{}",afterDecryptRefundMap);
        //验重
        if (!weiXinRefundNotifyService.Deduplication(afterDecryptRefundMap.get("transaction_id"))) {
            return "fail";
        }

        Pair<Boolean, Object> notifyOrderPair=eleRefundOrderService.notifyDepositRefundOrder(afterDecryptRefundMap);
        if (notifyOrderPair.getLeft()) {

            return "ok";
        } else {
            return "fail";

        }

    }


}