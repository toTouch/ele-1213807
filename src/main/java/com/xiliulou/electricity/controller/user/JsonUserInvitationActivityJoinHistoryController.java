package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.InvitationActivityJoinHistoryQuery;
import com.xiliulou.electricity.service.InvitationActivityJoinHistoryService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-06-07-16:50
 */
@Slf4j
@RestController
public class JsonUserInvitationActivityJoinHistoryController extends BaseController {

    @Autowired
    private InvitationActivityJoinHistoryService invitationActivityJoinHistoryService;

    /**
     * 获取用户邀请记录
     */
    @GetMapping("/user/invitation/activity/join/list")
    public R selectUserInvitationDetail(@RequestParam("size") long size,
                                        @RequestParam("offset") long offset,
                                        @RequestParam(value = "activityId", required = false) Long activityId) {
        if (size < 0 || size > 50) {
            size = 10L;
        }

        if (offset < 0) {
            offset = 0L;
        }

        InvitationActivityJoinHistoryQuery query = InvitationActivityJoinHistoryQuery.builder()
                .offset(offset)
                .size(size)
                .tenantId(TenantContextHolder.getTenantId())
                .uid(SecurityUtils.getUid())
                .activityId(activityId)
                .build();

        return R.ok(invitationActivityJoinHistoryService.selectUserByPage(query));
    }


}
