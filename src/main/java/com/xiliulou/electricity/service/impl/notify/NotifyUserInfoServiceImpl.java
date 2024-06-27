/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/6/26
 */

package com.xiliulou.electricity.service.impl.notify;


import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.http.resttemplate.service.RestTemplateService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.config.WechatConfig;
import com.xiliulou.electricity.config.message.MessageCenterConfig;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.converter.notify.NotifyUserInfoConverter;
import com.xiliulou.electricity.converter.notify.SendWechatNotifyDataConverter;
import com.xiliulou.electricity.converter.notify.SendWechatNotifyDataConverterFactory;
import com.xiliulou.electricity.dto.WxAuth2AccessTokenResult;
import com.xiliulou.electricity.dto.message.SendDTO;
import com.xiliulou.electricity.dto.message.SendReceiverDTO;
import com.xiliulou.electricity.entity.notify.NotifyUserInfo;
import com.xiliulou.electricity.enums.notify.SendMessageTypeEnum;
import com.xiliulou.electricity.mapper.notify.NotifyUserInfoMapper;
import com.xiliulou.electricity.request.notify.NotifyUserInfoOptRequest;
import com.xiliulou.electricity.request.notify.SendNotifyMessageRequest;
import com.xiliulou.electricity.service.notify.NotifyUserInfoService;
import com.xiliulou.electricity.ttl.TtlTraceIdSupport;
import com.xiliulou.electricity.ttl.TtlXllThreadPoolExecutorServiceWrapper;
import com.xiliulou.electricity.ttl.TtlXllThreadPoolExecutorsSupport;
import com.xiliulou.electricity.vo.notify.NotifyUserInfoVO;
import com.xiliulou.electricity.vo.notify.NotifyUserInfoWechatResultVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

import static com.xiliulou.electricity.constant.CacheConstant.CACHE_NOTIFY_USER_INFO_LOCK;
import static com.xiliulou.electricity.constant.WechatUrlConstant.WECHAT_OAUTH2_ACCESS_TOKEN_URL;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/6/26 15:07
 */
@Slf4j
@Service
public class NotifyUserInfoServiceImpl implements NotifyUserInfoService {
    
    @Resource
    private NotifyUserInfoMapper notifyUserInfoMapper;
    
    
    @Resource
    private RedisService redisService;
    
    
    @Resource
    private WechatConfig wechatConfig;
    
    @Resource
    private MessageCenterConfig messageCenterConfig;
    
    
    @Qualifier("restTemplateServiceImpl")
    @Autowired
    RestTemplateService restTemplateService;
    
    @Resource
    private SendWechatNotifyDataConverterFactory sendWechatNotifyDataConverterFactory;
    
    
    private final TtlXllThreadPoolExecutorServiceWrapper serviceWrapper = TtlXllThreadPoolExecutorsSupport
            .get(XllThreadPoolExecutors.newFixedThreadPool("MESSAGE_NOTIFY", 20, "message-notify-executor"));
    
    
    /**
     * 微信公众号渠道
     */
    private static final Integer WECHAT_SEND_CHANNEL = 3;
    
    @Override
    public <T> boolean asyncSendMessage(SendNotifyMessageRequest<T> request) {
        boolean traceIdExist = StringUtils.isBlank(TtlTraceIdSupport.get());
        try {
            if (!traceIdExist) {
                TtlTraceIdSupport.set();
            }
            serviceWrapper.execute(() -> sendMessage(request));
        } finally {
            if (!traceIdExist) {
                TtlTraceIdSupport.clear();
            }
        }
        return true;
    }
    
    @Override
    public <T> boolean sendMessage(SendNotifyMessageRequest<T> request) {
        
        String phone = request.getPhone();
        SendMessageTypeEnum type = request.getType();
        if (StringUtils.isBlank(phone)) {
            log.warn("NotifyUserInfoServiceImpl.sendMessage phone isBlank");
            return false;
        }
        NotifyUserInfo notifyUserInfo = queryFromCacheByPhone(phone);
        if (Objects.isNull(notifyUserInfo)) {
            log.warn("NotifyUserInfoServiceImpl.sendMessage phone={} not exist ", phone);
            return false;
        }
        
        String messageId = UUID.randomUUID().toString().replace("-", "");
        
        SendWechatNotifyDataConverter<T> converterByType = sendWechatNotifyDataConverterFactory.getConverterByType(type.getType());
        
        Map<String, String> map = converterByType.converterParamMap(request.getData());
        
        SendDTO sendDTO = new SendDTO();
        sendDTO.setMessageTemplateCode(converterByType.converterTemplateCode());
        sendDTO.setTenantId(request.getTenantId());
        sendDTO.setMessageId(messageId);
        sendDTO.setParamMap(map);
        SendReceiverDTO sendReceiverDTO = new SendReceiverDTO();
        sendReceiverDTO.setSendChannel(WECHAT_SEND_CHANNEL);
        sendReceiverDTO.setReceiver(Collections.singleton(notifyUserInfo.getOpenId()));
        sendDTO.setSendReceiverList(Collections.singletonList(sendReceiverDTO));
        
        ResponseEntity<String> responseEntity = restTemplateService.postJsonForResponseEntity(messageCenterConfig.getUrl(), JsonUtil.toJson(sendDTO), null);
        if (Objects.isNull(responseEntity)) {
            log.warn("send warn to message center warn! failure warn send note result is null, messageId={}", messageId);
            return false;
        }
        
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            log.warn("send warn to message center warn! failure warn send note error, sessionId={}, msg = {}", messageId, responseEntity.getBody());
            return false;
        }
        
        return true;
    }
    
    @Override
    public R queryWechatOpenIdByCode(String code) {
        String url = String.format(WECHAT_OAUTH2_ACCESS_TOKEN_URL, wechatConfig.getNotifyUserInfoAppId(), wechatConfig.getNotifyUserInfoSecret(), code);
        String bodyStr = restTemplateService.getForString(url, null);
        if (StringUtils.isBlank(bodyStr)) {
            log.warn("NotifyUserInfoServiceImpl.queryWechatOpenId wechat result is empty! url={}", url);
            return R.fail("300873", "微信返回异常");
        }
        
        WxAuth2AccessTokenResult wxAuth2AccessTokenResult = JsonUtil.fromJson(bodyStr, WxAuth2AccessTokenResult.class);
        
        NotifyUserInfoWechatResultVO vo = NotifyUserInfoConverter.qryWxAuth2ToVO(wxAuth2AccessTokenResult);
        
        return R.ok(vo);
    }
    
    @Override
    public R insert(NotifyUserInfoOptRequest request) {
        if (!checkIdempotent(request)) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }
        
        NotifyUserInfo existByOpenId = notifyUserInfoMapper.selectByOpenId(request.getOpenId());
        if (Objects.nonNull(existByOpenId)) {
            return R.fail("300870", "用户已存在");
        }
        
        NotifyUserInfo existByPhone = notifyUserInfoMapper.selectByPhone(request.getPhone());
        if (Objects.nonNull(existByPhone)) {
            return R.fail("300871", "手机号已存在");
        }
        
        NotifyUserInfo notifyUserInfo = NotifyUserInfoConverter.optReqToDo(request);
        notifyUserInfoMapper.insert(notifyUserInfo);
        
        return R.ok(true);
    }
    
    @Override
    public R update(NotifyUserInfoOptRequest request) {
        if (!checkIdempotent(request)) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }
        
        NotifyUserInfo oldUserInfo = notifyUserInfoMapper.selectById(request.getId());
        if (Objects.isNull(oldUserInfo)) {
            return R.fail("300872", "信息不存在");
        }
        
        String oldPhone = oldUserInfo.getPhone();
        
        NotifyUserInfo existByPhone = notifyUserInfoMapper.selectByPhone(request.getPhone());
        if (Objects.nonNull(existByPhone) && !existByPhone.getId().equals(request.getId())) {
            return R.fail("300871", "手机号已存在");
        }
        
        NotifyUserInfo notifyUserInfo = NotifyUserInfoConverter.optReqToDo(request);
        notifyUserInfoMapper.update(notifyUserInfo);
        // 删除缓存
        String openIdKey = String.format(CacheConstant.CACHE_NOTIFY_USER_INFO_OPENID, oldUserInfo.getOpenId());
        String phoneKey = String.format(CacheConstant.CACHE_NOTIFY_USER_INFO_PHONE, oldPhone);
        redisService.delete(Arrays.asList(openIdKey, phoneKey));
        return R.ok(true);
    }
    
    @Override
    public R queryByOpenIdFromCache(String openId) {
        NotifyUserInfo notifyUserInfo = queryFromCache(openId, p -> String.format(CacheConstant.CACHE_NOTIFY_USER_INFO_OPENID, p), p -> notifyUserInfoMapper.selectByOpenId(p));
        
        NotifyUserInfoVO vo = NotifyUserInfoConverter.qryNotifyUserInfoDOToVO(notifyUserInfo);
        return R.ok(vo);
    }
    
    @Override
    public R queryByPhoneFromCache(String phone) {
        // 查缓存
        NotifyUserInfo notifyUserInfo = queryFromCacheByPhone(phone);
        
        NotifyUserInfoVO vo = NotifyUserInfoConverter.qryNotifyUserInfoDOToVO(notifyUserInfo);
        
        return R.ok(vo);
    }
    
    @Slave
    @Override
    public R queryAll(Integer offset, Integer size) {
        List<NotifyUserInfo> notifyUserInfos = notifyUserInfoMapper.selectList(offset, size);
        List<NotifyUserInfoVO> vos = NotifyUserInfoConverter.qryNotifyUserInfoDOToVOS(notifyUserInfos);
        return R.ok(vos);
    }
    
    
    /**
     * 根据手机号查询
     *
     * @param phone
     * @author caobotao.cbt
     * @date 2024/6/26 20:49
     */
    private NotifyUserInfo queryFromCacheByPhone(String phone) {
        return queryFromCache(phone, p -> String.format(CacheConstant.CACHE_NOTIFY_USER_INFO_PHONE, p), p -> notifyUserInfoMapper.selectByPhone(p));
    }
    
    
    /**
     * 幂等性校验
     *
     * @param request
     * @return
     * @author caobotao.cbt
     * @date 2024/6/26 15:09
     */
    private boolean checkIdempotent(NotifyUserInfoOptRequest request) {
        return redisService.setNx(String.format(CACHE_NOTIFY_USER_INFO_LOCK, request.getPhone()), String.valueOf(System.currentTimeMillis()), 3 * 1000L, false);
    }
    
    
    /**
     * 先查缓存，不存在再查询数据库添加缓存
     *
     * @param param
     * @param getKey
     * @param qryDB
     * @author caobotao.cbt
     * @date 2024/6/26 18:37
     */
    private <T> NotifyUserInfo queryFromCache(T param, Function<T, String> getKey, Function<T, NotifyUserInfo> qryDB) {
        String key = getKey.apply(param);
        NotifyUserInfo cache = redisService.getWithHash(key, NotifyUserInfo.class);
        if (Objects.nonNull(cache)) {
            return cache;
        }
        NotifyUserInfo dbNotifyUserInfo = qryDB.apply(param);
        if (Objects.isNull(dbNotifyUserInfo)) {
            return null;
        }
        redisService.saveWithHash(key, dbNotifyUserInfo);
        return dbNotifyUserInfo;
    }
}
