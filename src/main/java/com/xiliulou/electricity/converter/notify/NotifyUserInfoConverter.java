/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/6/26
 */

package com.xiliulou.electricity.converter.notify;

import com.xiliulou.electricity.dto.WxAuth2AccessTokenResult;
import com.xiliulou.electricity.entity.notify.NotifyUserInfo;
import com.xiliulou.electricity.request.notify.NotifyUserInfoOptRequest;
import com.xiliulou.electricity.vo.notify.NotifyUserInfoVO;
import com.xiliulou.electricity.vo.notify.NotifyUserInfoWechatResultVO;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * description: 模型转换
 *
 * @author caobotao.cbt
 * @date 2024/6/26 15:00
 */
public class NotifyUserInfoConverter {
    
    /**
     * 操作参数转换
     *
     * @param request
     * @author caobotao.cbt
     * @date 2024/6/26 15:01
     */
    public static NotifyUserInfo optReqToDo(NotifyUserInfoOptRequest request) {
        if (Objects.isNull(request)) {
            return null;
        }
        NotifyUserInfo notifyUserInfo = new NotifyUserInfo();
        notifyUserInfo.setId(request.getId());
        notifyUserInfo.setUserName(request.getUserName());
        notifyUserInfo.setPhone(request.getPhone());
        notifyUserInfo.setNickName(request.getNickName());
        notifyUserInfo.setOpenId(request.getOpenId());
        return notifyUserInfo;
    }
    
    /**
     * 微信查询结果转换
     *
     * @param wxAuth2AccessTokenResult
     * @author caobotao.cbt
     * @date 2024/6/26 17:20
     */
    public static NotifyUserInfoWechatResultVO qryWxAuth2ToVO(WxAuth2AccessTokenResult wxAuth2AccessTokenResult) {
        if (Objects.isNull(wxAuth2AccessTokenResult)) {
            return null;
        }
        NotifyUserInfoWechatResultVO notifyUserInfoWechatResultVO = new NotifyUserInfoWechatResultVO();
        notifyUserInfoWechatResultVO.setOpenid(wxAuth2AccessTokenResult.getOpenid());
        notifyUserInfoWechatResultVO.setAccessToken(wxAuth2AccessTokenResult.getAccess_token());
        notifyUserInfoWechatResultVO.setErrcode(wxAuth2AccessTokenResult.getErrcode());
        notifyUserInfoWechatResultVO.setErrmsg(wxAuth2AccessTokenResult.getErrmsg());
        return notifyUserInfoWechatResultVO;
    }
    
    /**
     * do 转换 vo
     *
     * @param cache
     * @author caobotao.cbt
     * @date 2024/6/26 17:29
     */
    public static NotifyUserInfoVO qryNotifyUserInfoDOToVO(NotifyUserInfo cache) {
        if (Objects.isNull(cache)) {
            return null;
        }
        NotifyUserInfoVO notifyUserInfoVO = new NotifyUserInfoVO();
        notifyUserInfoVO.setId(cache.getId());
        notifyUserInfoVO.setUserName(cache.getUserName());
        notifyUserInfoVO.setPhone(cache.getPhone());
        notifyUserInfoVO.setNickName(cache.getNickName());
        notifyUserInfoVO.setOpenId(cache.getOpenId());
        notifyUserInfoVO.setCreateTime(cache.getCreateTime());
        notifyUserInfoVO.setUpdateTime(cache.getUpdateTime());
        return notifyUserInfoVO;
    }
    
    /**
     * do 转换 vo (批量)
     *
     * @param notifyUserInfos
     * @author caobotao.cbt
     * @date 2024/6/26 17:39
     */
    public static List<NotifyUserInfoVO> qryNotifyUserInfoDOToVOS(List<NotifyUserInfo> notifyUserInfos) {
        if (CollectionUtils.isEmpty(notifyUserInfos)) {
            return Collections.emptyList();
        }
        return notifyUserInfos.stream().map(v -> qryNotifyUserInfoDOToVO(v)).filter(Objects::nonNull).collect(Collectors.toList());
    }
}
