package com.xiliulou.electricity.service.wxrefund;

import com.xiliulou.pay.weixinv3.dto.WechatJsapiRefundOrderCallBackResource;

/**
 * 微信退款顶级 Service
 *
 * @author xiaohui.song
 **/
public interface WxRefundPayService {

    /**
     * 执行方法
     * @param callBackResource
     */
    void process(WechatJsapiRefundOrderCallBackResource callBackResource);

    /**
     * 获取操作类型
     * @return
     */
    String getOptType();

}
