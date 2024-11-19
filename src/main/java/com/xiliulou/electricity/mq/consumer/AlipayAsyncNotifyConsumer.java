/**
 *  Create date: 2024/7/31
 */

package com.xiliulou.electricity.mq.consumer;

import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.factory.paycallback.RefundPayServiceFactory;
import com.xiliulou.electricity.mq.constant.MqConsumerConstant;
import com.xiliulou.electricity.mq.constant.MqProducerConstant;;
import com.xiliulou.electricity.service.wxrefund.RefundPayService;
import com.xiliulou.electricity.ttl.TtlTraceIdSupport;
import com.xiliulou.pay.alipay.dto.AliPayRefundOrderDTO;
import com.xiliulou.pay.alipay.request.ApiPayRefundOrderCallBackResource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * description: 支付宝异步通知消费端
 *
 * @author caobotao.cbt
 * @date 2024/7/31 08:45
 */

@Slf4j
@Component
@RocketMQMessageListener(topic = MqProducerConstant.ALIPAY_REFUND_NOTIFY_TOPIC, consumerGroup = MqConsumerConstant.ALIPAY_REFUND_NOTIFY_GROUP, consumeThreadMax = 20)
public class AlipayAsyncNotifyConsumer implements RocketMQListener<String> {
    
    
    @Override
    public void onMessage(String message) {
        TtlTraceIdSupport.set();
        log.info("ALIPAY_REFUND_NOTIFY_TOPIC !received msg={}", message);
        try {
            AliPayRefundOrderDTO refundOrderDTO = JsonUtil.fromJson(message, AliPayRefundOrderDTO.class);
            // 成功
            ApiPayRefundOrderCallBackResource backResource = new ApiPayRefundOrderCallBackResource();
            backResource.setTradeStatus(refundOrderDTO.getRefundStatus());
            backResource.setOutTradeNo(refundOrderDTO.getOutTradeNo());
            backResource.setOutBizNo(refundOrderDTO.getRefundNo());
            RefundPayService service = RefundPayServiceFactory.getService(refundOrderDTO.getRefundType());
            if (Objects.isNull(service)) {
                log.warn("RefundPayService is null RefundType={}", refundOrderDTO.getRefundType());
                return;
            }
            service.process(backResource);
        } finally {
            TtlTraceIdSupport.clear();
        }
    }
}
