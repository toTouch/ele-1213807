package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.InvitationActivityUser;
import com.xiliulou.electricity.service.ActivityService;
import com.xiliulou.electricity.service.InvitationActivityUserService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.ActivityUserInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-06-20-11:32
 */
@Slf4j
@Service("activityService")
public class ActivityServiceImpl implements ActivityService {

    @Autowired
    private InvitationActivityUserService invitationActivityUserService;

    /**
     * 用户是否有权限参加此活动
     *
     * @return
     */
    @Override
    public Triple<Boolean, String, Object> userActivityInfo() {
        ActivityUserInfoVO activityUserInfoVO = new ActivityUserInfoVO();

        InvitationActivityUser invitationActivityUser = invitationActivityUserService.selectByUid(SecurityUtils.getUid());
        activityUserInfoVO.setInvitationActivity(Objects.isNull(invitationActivityUser) ? Boolean.FALSE : Boolean.TRUE);

        return Triple.of(true, "", activityUserInfoVO);
    }
}
