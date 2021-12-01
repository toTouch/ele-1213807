package com.xiliulou.electricity.service.impl;

import com.xiliulou.core.wp.beanposstprocessor.WechatAccessTokenSevice;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

/**
 * @author Hardy
 * @date 2021/12/1 11:13
 * @mood
 */
@Service
public class WechatAccessTokenSeviceImpl implements WechatAccessTokenSevice {
    @Override
    public Pair<Boolean, Object> getAccessToken(String appId, String secret) {
        return null;
    }

    @Override
    public Pair<Boolean, String> getWechatTicket(String appId, String accessToken) {
        return null;
    }
}
