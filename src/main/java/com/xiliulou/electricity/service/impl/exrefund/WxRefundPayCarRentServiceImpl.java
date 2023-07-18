package com.xiliulou.electricity.service.impl.exrefund;

import org.springframework.stereotype.Service;

import com.xiliulou.electricity.enums.WxRefundPayOptTypeEnum;
import com.xiliulou.electricity.service.wxrefund.WxRefundPayService;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiRefundOrderCallBackResource;

import lombok.extern.slf4j.Slf4j;

/**
 * 微信退款-租车租金退款 ServiceImpl
 *
 * @author xiaohui.song
 **/
@Slf4j
@Service("wxRefundPayCarRentServiceImpl")
public class WxRefundPayCarRentServiceImpl implements WxRefundPayService {

    /**
     * 执行方法
     *
     * @param callBackResource
     */
    @Override
    public void process(WechatJsapiRefundOrderCallBackResource callBackResource) {

    }

    /**
     * 获取操作类型
     *
     * @return
     */
    @Override
    public String getOptType() {
        return WxRefundPayOptTypeEnum.CAR_RENT_REFUND_CALL_BACK.getCode();
    }
}
