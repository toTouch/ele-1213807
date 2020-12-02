package com.xiliulou.electricity.pay.pay;

import com.xiliulou.electricity.pay.entity.PayOrder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

/**
 * @program: XILIULOU
 * @description:
 * @author: Mr.YG
 * @create: 2020-12-02 11:48
 **/
@Service
@Slf4j
public class PayAdapterHandler {
    @Autowired
    Map<String, WeiXinPayService> weiXinPayServiceMap;


    /**
     * @param payOrder
     * @return
     */
    public Pair<Boolean, Object> AdaptAndPay(PayOrder payOrder) {

        WeiXinPayService weiXinPayService = weiXinPayServiceMap.get(payOrder.getChannelId());
        if (Objects.isNull(weiXinPayService)) {
            log.error("ADAPT_AND_PAY ERROR   ,NOT MATCHED PAY_HANDLER ,CHANNEL_ID:{}", payOrder.getChannelId());
            return Pair.of(false, "未找到支付处理器!");
        }
        return weiXinPayService.pay(payOrder);
    }
}
