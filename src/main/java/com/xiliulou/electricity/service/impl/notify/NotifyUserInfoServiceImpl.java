/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/6/26
 */

package com.xiliulou.electricity.service.impl.notify;


import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.http.resttemplate.service.RestTemplateService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.config.WechatConfig;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.converter.notify.NotifyUserInfoConverter;
import com.xiliulou.electricity.dto.WxAuth2AccessTokenResult;
import com.xiliulou.electricity.entity.notify.NotifyUserInfo;
import com.xiliulou.electricity.mapper.notify.NotifyUserInfoMapper;
import com.xiliulou.electricity.request.notify.NotifyUserInfoOptRequest;
import com.xiliulou.electricity.service.notify.NotifyUserInfoService;
import com.xiliulou.electricity.vo.notify.NotifyUserInfoVO;
import com.xiliulou.electricity.vo.notify.NotifyUserInfoWechatResultVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
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
    
    
    @Qualifier("restTemplateServiceImpl")
    @Autowired
    RestTemplateService restTemplateService;
    
    
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
    @Override
    public NotifyUserInfo queryFromCacheByPhone(String phone) {
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
