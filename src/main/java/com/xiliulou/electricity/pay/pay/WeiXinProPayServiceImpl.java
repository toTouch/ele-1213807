package com.xiliulou.electricity.pay.pay;

import cn.hutool.core.util.ObjectUtil;
import com.jpay.ext.kit.PaymentKit;
import com.jpay.weixin.api.WxPayApi;
import com.xiliulou.electricity.pay.config.WeiXinPayConfig;
import com.xiliulou.electricity.pay.constant.ChannelContant;
import com.xiliulou.electricity.pay.entity.PayOrder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @program: XILIULOU
 * @description:
 * @author: Mr.YG
 * @create: 2020-12-02 11:51
 **/
@Service("WX_PRO")
@Slf4j
public class WeiXinProPayServiceImpl extends AbstractWeiXinPayService {

    @Autowired
    WeiXinPayConfig weiXinPayConfig;


    @Override
    public Pair<Boolean, Object> pay(PayOrder payOrder) {
        Map<String, String> params = new HashMap<>();
        params.put("attach", StringUtils.isEmpty(payOrder.getAttach()) ?
                String.valueOf(System.currentTimeMillis()) : payOrder.getAttach());
        params.put("appid", payOrder.getAppId());
        params.put("mch_id", payOrder.getMchId());
        params.put("nonce_str", String.valueOf(System.currentTimeMillis()));
        params.put("body", payOrder.getBody());
        params.put("out_trade_no", payOrder.getOutTradeNo());
        params.put("total_fee", payOrder.getTotalFee().toString());
        params.put("spbill_create_ip", payOrder.getSpbillCreateIp());
        params.put("notify_url", weiXinPayConfig.getPayNotifyUrl());
        params.put("trade_type", "JSAPI");
        params.put("openid", payOrder.getOpenId());
        String sign = PaymentKit.createSign(params, payOrder.getPaternerKey());
        params.put("sign", sign);
        log.info("微信小程序支付请求xml参数:{}", params);

        String xmlResult = WxPayApi.pushOrder(false, params);
        log.info("微信小程序支付请求响应xml信息:{}", xmlResult);
        Map<String, String> resultMap = PaymentKit.xmlToMap(xmlResult);
        //支付失败
        if (ObjectUtil.equal(ChannelContant.WEI_XIN_PAY_RESULT_FAIL, resultMap.get("return_code"))) {
            String errmsg = resultMap.get("return_msg");
            if (org.springframework.util.StringUtils.isEmpty(errmsg)) {
                errmsg = "未知错误!";
            }
            return Pair.of(false, errmsg);
        }
        if (ObjectUtil.equal(ChannelContant.WEIXIN_PAY_RESULT_SUCCESS, resultMap.get("return_code"))) {
            if (ObjectUtil.equal(ChannelContant.WEI_XIN_PAY_RESULT_FAIL, resultMap.get("result_code"))) {
                log.error("pro pay error errorCode:{},errMsg:{}", resultMap.get("err_code"), resultMap.get("err_code_des"));
                return Pair.of(false, resultMap.get("err_code_des"));
            }
            String prepayId = resultMap.get("prepay_id");
            if (ObjectUtil.isEmpty(prepayId)) {
                return Pair.of(false, "未获取到预支付订单id");
            }
            return reSign(payOrder, prepayId);
        }
        return Pair.of(false, "未知响应信息!");
    }


}
