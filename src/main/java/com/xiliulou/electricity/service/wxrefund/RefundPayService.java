package com.xiliulou.electricity.service.wxrefund;

import com.xiliulou.pay.base.request.BaseOrderRefundCallBackResource;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiRefundOrderCallBackResource;

/**
 * 退款顶级 Service
 *
 * @author xiaohui.song
 **/
public interface RefundPayService {

    /**
     * 执行方法
     * @param callBackResource
     */
    void process(BaseOrderRefundCallBackResource callBackResource);

    /**
     * 获取操作类型
     * @return
     */
    String getOptType();

}
