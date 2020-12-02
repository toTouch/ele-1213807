package com.xiliulou.electricity.pay.pay;

import com.jpay.ext.kit.PaymentKit;
import com.xiliulou.electricity.pay.entity.PayOrder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @program: XILIULOU
 * @description:
 * @author: Mr.YG
 * @create: 2020-12-02 13:53
 **/
@Service
@Slf4j
public abstract class AbstractWeiXinPayService implements WeiXinPayService {


    /**
     * 再签名
     *
     * @param payOrder
     * @param prepayId
     * @return
     */
    public Pair<Boolean, Object> reSign(PayOrder payOrder, String prepayId) {

        Map<String, String> packageParams = new HashMap<String, String>(6);


        packageParams.put("appId", payOrder.getAppId());
        packageParams.put("timeStamp", String.valueOf(System.currentTimeMillis() / 1000));
        packageParams.put("nonceStr", String.valueOf(System.currentTimeMillis()));
        packageParams.put("package", "prepay_id=" + prepayId);
        packageParams.put("signType", "MD5");
        String packageSign = PaymentKit.createSign(packageParams, payOrder.getPaternerKey());
        packageParams.put("paySign", packageSign);
        return Pair.of(true, packageParams);

    }

}
