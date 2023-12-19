package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.ActivityShareInvitationRef;
import com.xiliulou.electricity.mapper.ActivityShareInvitationRefMapper;
import com.xiliulou.electricity.service.ActivityShareInvitationRefService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author HeYafeng
 * @description 邀请返券活动和套餐返现活动映射业务 用于 悦来能源需求：邀请返券迁移到邀请返现，并使相关邀请人的返券二维码跳转到邀请返现的业务逻辑中
 * @date 2023/12/19 10:05:28
 */
@Service
public class ActivityShareInvitationRefServiceImpl implements ActivityShareInvitationRefService {
    
    @Resource
    private ActivityShareInvitationRefMapper activityShareInvitationRefMapper;
    
    @Override
    public ActivityShareInvitationRef selectByInviterAndShareActivityId(Integer tenantId, Long inviterUid, Long shareActivityId) {
        return activityShareInvitationRefMapper.selectByInviterAndShareActivityId(tenantId, inviterUid, shareActivityId);
    }
}
