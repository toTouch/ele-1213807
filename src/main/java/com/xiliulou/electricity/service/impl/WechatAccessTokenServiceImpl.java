package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.google.gson.annotations.SerializedName;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.wp.beanposstprocessor.AbstractWechatStableAccessTokenService;
import com.xiliulou.core.wp.beanposstprocessor.WechatAccessTokenSevice;
import com.xiliulou.core.wp.constant.WeChatConstant;
import com.xiliulou.core.wp.entity.AccessTokenVo;
import com.xiliulou.core.wp.properties.WechatAccessTokenProperties;
import com.xiliulou.core.wp.service.WechatAccessTokenService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * @author Hardy
 * @date 2021/12/1 11:13
 * @mood
 */
@Service
@Slf4j
public class WechatAccessTokenServiceImpl extends AbstractWechatStableAccessTokenService {
    
    @Autowired
    private RedisService redisService;
    
    
    @Override
    public Pair<Boolean, Object> getAccessToken(String appId, String secret) {
        
        if (wechatAccessTokenProperties.getEnableStable()) {
            // 开启稳定模式的token获取
            return super.getAccessToken(appId, secret);
        }
        
        String accessToken;
        accessToken = redisService.get(WeChatConstant.ACCESS_TOKEN_KEY + appId);
        if (StringUtils.isEmpty(accessToken)) {
            String getAccessToken = WeChatConstant.ACCESS_TOKEN_URL + appId + "&secret=" + secret;
            String result = HttpUtil.get(getAccessToken);
            accessToken = JSONUtil.toBean(result, AccessTokenVo.class).getAccessToken();
            log.info("WeChat obtains the result of accessing Token : {}", result);
            if (ObjectUtil.isEmpty(accessToken)) {
                log.warn("GET ACCESS_TOKEN FAILED,RESULT:{},APPID:{},SECRET:{}", result, appId, secret);
                return Pair.of(false, "获取微信accessToken失败!");
            } else {
                //微信token有效期 7200秒
                redisService.set(WeChatConstant.ACCESS_TOKEN_KEY + appId, accessToken, wechatAccessTokenProperties.getExpiryTime(), TimeUnit.SECONDS);
            }
        }
        return Pair.of(true, accessToken);
    }
    
    @Override
    protected String getStableAccessTokenFromCache(String key) {
        return redisService.get(key);
    }
    
    @Override
    protected void saveStableCache(String key, String accessToken, Long expiryTime, TimeUnit seconds) {
        redisService.set(key, accessToken, expiryTime, seconds);
    }
    
    @Override
    public Pair<Boolean, String> getWechatTicket(String appId, String accessToken) {
        String ticketValues = redisService.get(WeChatConstant.CAHCE_TICKET_KEY + accessToken);
        if (StrUtil.isNotEmpty(ticketValues)) {
            return Pair.of(true, ticketValues);
        }
        
        String ticketUrl = String.format(WeChatConstant.WECHAT_TICKET, accessToken);
        
        String result = HttpUtil.get(ticketUrl);
        if (StrUtil.isEmpty(result)) {
            return Pair.of(false, null);
        }
        
        TicketRsp ticketRsp = JsonUtil.fromJson(result, TicketRsp.class);
        if (ticketRsp.getErrcode() != 0) {
            log.error("WETCHAT ERROR! get ticket error! appId={},msg={}", appId, ticketRsp.getErrmsg());
            return Pair.of(false, null);
        }
        
        redisService.set(WeChatConstant.CAHCE_TICKET_KEY + accessToken, ticketRsp.getTicket(), 4800L, TimeUnit.SECONDS);
        return Pair.of(true, ticketRsp.getTicket());
    }
}

@Data
class TicketRsp {
    
    private Integer errcode;
    
    private String errmsg;
    
    private String ticket;
    
    @SerializedName(value = "expires_in")
    private String expires;
}
