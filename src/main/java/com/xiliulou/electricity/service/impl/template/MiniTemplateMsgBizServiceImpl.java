/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/7/23
 */

package com.xiliulou.electricity.service.impl.template;

import cn.hutool.core.util.RandomUtil;
import com.xiliulou.core.alipay.request.AlipayMessageRequest;
import com.xiliulou.core.base.MiniTemplateMessageFactory;
import com.xiliulou.core.base.request.BaseMsgRequest;
import com.xiliulou.core.base.service.MiniTemplateMessageService;
import com.xiliulou.core.wp.entity.AppTemplateQuery;
import com.xiliulou.electricity.bo.base.BasePayConfig;
import com.xiliulou.electricity.bo.pay.AlipayAppConfigBizDetails;
import com.xiliulou.electricity.bo.wechat.WechatPayParamsDetails;
import com.xiliulou.electricity.constant.MultiFranchiseeConstant;
import com.xiliulou.electricity.dto.BatteryPowerNotifyDto;
import com.xiliulou.electricity.entity.TemplateConfigEntity;
import com.xiliulou.electricity.entity.UserOauthBind;
import com.xiliulou.electricity.service.UserOauthBindService;
import com.xiliulou.electricity.service.pay.PayConfigBizService;
import com.xiliulou.electricity.service.template.MiniTemplateMsgBizService;
import com.xiliulou.electricity.service.template.TemplateConfigService;
import com.xiliulou.core.base.enums.ChannelEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/7/23 18:25
 */
@Slf4j
@Service
public class MiniTemplateMsgBizServiceImpl implements MiniTemplateMsgBizService {
    
    @Resource
    private UserOauthBindService userOauthBindService;
    
    @Resource
    private PayConfigBizService payConfigBizService;
    
    @Resource
    private TemplateConfigService templateConfigService;
    
    @Resource
    private MiniTemplateMessageFactory miniTemplateMessageFactory;
    
    @Override
    public boolean sendLowBatteryReminder(Integer tenantId, Long uid, String soc, String sn) {
        GetBaseMsgRequest getBaseMsgRequest = (payConfig, tmpConfig, userOauthBind) -> {
            if (ChannelEnum.WECHAT.getCode().equals(tmpConfig.getChannel())) {
                Map<String, Object> data = new HashMap<>();
                data.put("character_string1", soc);
                data.put("character_string2", sn);
                data.put("thing3", "当前电量较低，请及时换电。");
                AppTemplateQuery appTemplateQuery = initWechat(uid, payConfig, userOauthBind);
                appTemplateQuery.setTemplateId(tmpConfig.getElectricQuantityRemindTemplate());
                appTemplateQuery.setData(data);
                return appTemplateQuery;
            } else if (ChannelEnum.ALIPAY.getCode().equals(tmpConfig.getChannel())) {
                Map<String, Object> data = new HashMap<>();
                data.put("keyword1", new AlipayDataValue(soc));
                data.put("keyword2", new AlipayDataValue(sn));
                data.put("keyword3", new AlipayDataValue("当前电量较低，请及时换电。"));
                AlipayMessageRequest alipayMessageRequest = initAlipay(uid, payConfig, userOauthBind);
                alipayMessageRequest.setTemplateId(tmpConfig.getElectricQuantityRemindTemplate());
                alipayMessageRequest.setData(data);
                return alipayMessageRequest;
            } else {
                return null;
            }
        };
        return sendMsg(tenantId, uid, getBaseMsgRequest);
    }
    
    @Override
    public boolean sendBatteryMemberCardExpiring(Integer tenantId, Long uid, String cardName, String memberCardExpireTimeStr) {
        GetBaseMsgRequest getBaseMsgRequest = (payConfig, tmpConfig, userOauthBind) -> {
            if (ChannelEnum.WECHAT.getCode().equals(tmpConfig.getChannel())) {
                Map<String, Object> data = new HashMap<>();
                data.put("thing2", cardName);
                data.put("date4", memberCardExpireTimeStr);
                data.put("thing3", "电池套餐即将过期，请及时续费。");
                AppTemplateQuery appTemplateQuery = initWechat(uid, payConfig, userOauthBind);
                appTemplateQuery.setTemplateId(tmpConfig.getBatteryMemberCardExpiringTemplate());
                appTemplateQuery.setData(data);
                return appTemplateQuery;
            } else if (ChannelEnum.ALIPAY.getCode().equals(tmpConfig.getChannel())) {
                Map<String, Object> data = new HashMap<>();
                data.put("keyword1", new AlipayDataValue(cardName));
                data.put("keyword2", new AlipayDataValue(memberCardExpireTimeStr));
                data.put("keyword3", new AlipayDataValue("电池套餐即将过期，请及时续费。"));
                AlipayMessageRequest alipayMessageRequest = initAlipay(uid, payConfig, userOauthBind);
                alipayMessageRequest.setTemplateId(tmpConfig.getBatteryMemberCardExpiringTemplate());
                alipayMessageRequest.setData(data);
                return alipayMessageRequest;
            } else {
                return null;
            }
        };
        return sendMsg(tenantId, uid, getBaseMsgRequest);
    }
    
    
    private boolean sendMsg(Integer tenantId, Long uid, GetBaseMsgRequest getBaseMsgRequest) {
        
        try {
            UserOauthBind userOauthBind = userOauthBindService.queryUserOauthBySysId(uid, tenantId);
            
            if (Objects.isNull(userOauthBind)) {
                log.warn("MiniTemplateMsgBizServiceImpl.sendMsg WARN! UserOauthBind is null tenantId={}, uid={}", tenantId, uid);
                return false;
            }
            
            List<String> channels = new ArrayList<>();
            
            if (StringUtils.isNotBlank(userOauthBind.getThirdId())) {
                channels.add(ChannelEnum.WECHAT.getCode());
            }
            
            // TODO: 2024/7/23 CBT 支付宝openid
            
            if (CollectionUtils.isEmpty(channels)) {
                log.warn("MiniTemplateMsgBizServiceImpl.sendMsg WARN! channels is null tenantId={}, uid={}", tenantId, uid);
                return false;
            }
            
            List<TemplateConfigEntity> configEntities = templateConfigService.queryByTenantIdAndChannelListFromCache(tenantId, channels);
            
            if (CollectionUtils.isEmpty(configEntities)) {
                log.warn("MiniTemplateMsgBizServiceImpl.sendMsg WARN! configEntities is isEmpty tenantId={}", tenantId);
                return false;
            }
            
            for (TemplateConfigEntity configEntity : configEntities) {
                String channel = configEntity.getChannel();
                
                // 获取渠道service
                MiniTemplateMessageService messageService = miniTemplateMessageFactory.getChannel(channel);
                if (Objects.isNull(messageService)) {
                    log.warn("MiniTemplateMsgBizServiceImpl.sendMsg WARN! channel={},tenantId={} MiniTemplateMessageService is null", channel, tenantId);
                    continue;
                }
                // 查询配置
                BasePayConfig config = payConfigBizService.queryPayParams(channel, tenantId, MultiFranchiseeConstant.DEFAULT_FRANCHISEE);
                if (Objects.isNull(config)) {
                    log.warn("MiniTemplateMsgBizServiceImpl.sendMsg WARN! channel={},tenantId={} BasePayConfig is null", channel, tenantId);
                    continue;
                }
                
                BaseMsgRequest request = getBaseMsgRequest.get(config, configEntity, userOauthBind);
                if (Objects.isNull(request)) {
                    log.warn("MiniTemplateMsgBizServiceImpl.sendMsg WARN! request is null ");
                    return false;
                }
                Pair<Boolean, Object> pair = messageService.sendMsg(request);
                if (!pair.getLeft()) {
                    log.warn("MiniTemplateMsgBizServiceImpl.sendMsg WARN! tenantId={},channel-{}, Send failure ", tenantId, channel);
                }
            }
            return true;
        } catch (Exception e) {
            log.warn("MiniTemplateMsgBizServiceImpl.sendMsg WARN! exception:", e);
            return false;
        }
    }
    
    private AppTemplateQuery initWechat(Long uid, BasePayConfig payConfig, UserOauthBind userOauthBind) {
        WechatPayParamsDetails wechatPayParamsDetails = (WechatPayParamsDetails) payConfig;
        AppTemplateQuery appTemplateQuery = new AppTemplateQuery();
        appTemplateQuery.setPage("/pages/start/index");
        appTemplateQuery.setFormId(RandomUtil.randomString(20));
        appTemplateQuery.setUid(uid);
        //            appTemplateQuery.setOpenId(userOauthBind);
        appTemplateQuery.setAppId(wechatPayParamsDetails.getMerchantMinProAppId());
        appTemplateQuery.setSecret(wechatPayParamsDetails.getMerchantMinProAppSecert());
        return appTemplateQuery;
    }
    
    private AlipayMessageRequest initAlipay(Long uid, BasePayConfig payConfig, UserOauthBind userOauthBind) {
        AlipayAppConfigBizDetails alipayAppConfigBizDetails = (AlipayAppConfigBizDetails) payConfig;
        AlipayMessageRequest alipayMessageRequest = new AlipayMessageRequest();
        alipayMessageRequest.setAppId(alipayAppConfigBizDetails.getAppId());
        alipayMessageRequest.setAppPrivateKey(alipayAppConfigBizDetails.getAppPrivateKey());
        alipayMessageRequest.setAlipayPublicKey(alipayAppConfigBizDetails.getPublicKey());
        alipayMessageRequest.setPage("/pages/start/index");
        //            alipayMessageRequest.setOpenId(userOauthBind);
        return alipayMessageRequest;
    }
    
    
    @FunctionalInterface
    public interface GetBaseMsgRequest {
        
        BaseMsgRequest get(BasePayConfig config, TemplateConfigEntity configEntity, UserOauthBind userOauthBind);
    }
    
    
    @Data
    @AllArgsConstructor
    public static class AlipayDataValue {
        
        private String value;
    }
}
