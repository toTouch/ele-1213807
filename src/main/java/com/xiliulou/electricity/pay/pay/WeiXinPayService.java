package com.xiliulou.electricity.pay.pay;


import com.xiliulou.electricity.pay.entity.PayOrder;
import org.apache.commons.lang3.tuple.Pair;

public interface WeiXinPayService {

    /**
     * @param payOrder
     * @return
     */
    Pair<Boolean, Object> pay(PayOrder payOrder);


}
