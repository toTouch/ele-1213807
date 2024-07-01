/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/6/27
 */

package com.xiliulou.electricity.mq.handler.message.wechat;

import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.dto.message.SendDTO;
import com.xiliulou.electricity.dto.message.SendReceiverDTO;
import com.xiliulou.electricity.entity.MqNotifyCommon;
import com.xiliulou.electricity.entity.notify.NotifyUserInfo;
import com.xiliulou.electricity.mq.handler.message.AbstractMessageSendHandler;
import com.xiliulou.electricity.service.notify.NotifyUserInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/6/27 20:04
 */
@Slf4j
public abstract class AbstractWechatOfficialAccountSendHandler extends AbstractMessageSendHandler {
    
    @Resource
    private NotifyUserInfoService notifyUserInfoService;
    
    
    public static final Integer WECHAT_SEND_CHANNEL = 3;
    
    @Override
    protected SendDTO getSendDTO(MqNotifyCommon mqNotifyCommon) {
        String phone = mqNotifyCommon.getPhone();
        
        if (StringUtils.isBlank(phone)) {
            log.warn("AbstractWechatOfficialAccountSendHandler.getSendDTO phone isBlank");
            return null;
        }
        if (Objects.isNull(mqNotifyCommon.getData())) {
            log.warn("AbstractWechatOfficialAccountSendHandler.getSendDTO data isBlank");
            return null;
        }
        
        
        Map<String, String> map = converterParamMap(JsonUtil.toJson(mqNotifyCommon.getData()));
        
        if (MapUtils.isEmpty(map)) {
            return null;
        }
        
        NotifyUserInfo notifyUserInfo = notifyUserInfoService.queryFromCacheByPhone(phone);
        if (Objects.isNull(notifyUserInfo)) {
            log.warn("AbstractWechatOfficialAccountSendHandler.getSendDTO phone={} not exist ", phone);
            return null;
        }
        
        SendDTO sendDTO = new SendDTO();
        sendDTO.setParamMap(map);
        SendReceiverDTO sendReceiverDTO = new SendReceiverDTO();
        sendReceiverDTO.setSendChannel(WECHAT_SEND_CHANNEL);
        sendReceiverDTO.setReceiver(Collections.singleton(notifyUserInfo.getOpenId()));
        sendDTO.setSendReceiverList(Collections.singletonList(sendReceiverDTO));
        return sendDTO;
    }
    
    
    /**
     * 参数转换
     *
     * @param data
     * @author caobotao.cbt
     * @date 2024/6/27 20:39
     */
    protected abstract Map<String, String> converterParamMap(String data);
    
}
