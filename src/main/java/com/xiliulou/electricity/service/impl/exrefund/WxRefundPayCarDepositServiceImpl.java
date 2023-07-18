package com.xiliulou.electricity.service.impl.exrefund;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.enums.WxRefundStatusEnum;
import org.springframework.stereotype.Service;

import com.xiliulou.electricity.constant.WechatPayConstant;
import com.xiliulou.electricity.enums.WxRefundPayOptTypeEnum;
import com.xiliulou.electricity.service.wxrefund.WxRefundPayService;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiRefundOrderCallBackResource;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

/**
 * 微信退款-租车押金退款 ServiceImpl
 *
 * @author xiaohui.song
 **/
@Slf4j
@Service("wxRefundPayCarDepositService")
public class WxRefundPayCarDepositServiceImpl implements WxRefundPayService {

    @Resource
    private RedisService redisService;

    /**
     * 执行方法
     * @param callBackResource
     */
    @Override
    public void process(WechatJsapiRefundOrderCallBackResource callBackResource) {
        String outTradeNo = callBackResource.getOutTradeNo();
        String outRefundNo = callBackResource.getOutRefundNo();
        String refundStatus = callBackResource.getRefundStatus();

        String redisLockKey = WechatPayConstant.REFUND_ORDER_ID_CALL_BACK + outTradeNo;

        try {
            if (!redisService.setNx(redisLockKey, outTradeNo, 10 * 1000L, false)) {
                return;
            }

            if (WxRefundStatusEnum.SUCCESS.getCode().equals(refundStatus)) {

            }



        } catch (Exception e) {

        } finally {
            redisService.delete(redisLockKey);
        }
    }

    /**
     * 获取操作类型
     *
     * @return
     */
    @Override
    public String getOptType() {
        return WxRefundPayOptTypeEnum.CAR_DEPOSIT_REFUND_CALL_BACK.getCode();
    }
}
