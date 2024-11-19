/**
 *  Create date: 2024/8/8
 */

package com.xiliulou.electricity.service.impl.exrefund;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.constant.WechatPayConstant;
import com.xiliulou.electricity.enums.RefundPayOptTypeEnum;
import com.xiliulou.electricity.service.EleRefundOrderService;
import com.xiliulou.electricity.service.wxrefund.RefundPayService;
import com.xiliulou.pay.base.request.BaseOrderRefundCallBackResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * description: 电池押金退款
 *
 * @author caobotao.cbt
 * @date 2024/8/8 18:22
 */
@Service
public class RefundBatteryDepositServiceImpl implements RefundPayService {
    
    @Autowired
    EleRefundOrderService eleRefundOrderService;
    
    @Resource
    private RedisService redisService;
    
    @Override
    public void process(BaseOrderRefundCallBackResource callBackResource) {
        eleRefundOrderService.notifyDepositRefundOrder(callBackResource);
    }
    
    @Override
    public String getOptType() {
        return RefundPayOptTypeEnum.BATTERY_DEPOSIT_REFUND_CALL_BACK.getCode();
    }
}
