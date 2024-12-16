package com.xiliulou.electricity.service.process.handler;

import com.xiliulou.electricity.entity.UserInfo;
import org.apache.commons.lang3.tuple.Triple;

public interface ExchangeBasicHandler {
    
    /**
     * 换电套餐校验handler
     *
     * @param userInfo
     * @return
     */
    Triple<Boolean, String, Object> handler(UserInfo userInfo);
}
