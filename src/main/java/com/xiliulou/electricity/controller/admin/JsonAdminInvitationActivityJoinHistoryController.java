package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.InvitationActivityJoinHistoryQuery;
import com.xiliulou.electricity.service.InvitationActivityJoinHistoryService;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-06-06-13:24
 */
@Slf4j
@RestController
public class JsonAdminInvitationActivityJoinHistoryController {

    @Autowired
    private InvitationActivityJoinHistoryService invitationActivityJoinHistoryService;

    @Autowired
    UserDataScopeService userDataScopeService;

    @GetMapping("/admin/invitationActivityJoinHistory/page")
    public R page(@RequestParam("size") long size, @RequestParam("offset") long offset,
                  @RequestParam(value = "beginTime", required = false) Long beginTime,
                  @RequestParam(value = "endTime", required = false) Long endTime,
                  @RequestParam(value = "activityId", required = false) Long activityId,
                  @RequestParam(value = "id", required = false) Long id,
                  @RequestParam(value = "status", required = false) Integer status,
                  @RequestParam(value = "joinUserPhone", required = false) String joinUserPhone,
                  @RequestParam(value = "joinUid", required = false) Long joinUid,
                  @RequestParam(value = "joinUserName", required = false) String joinUserName,
                  @RequestParam(value = "activityName", required = false) String activityName,
                  @RequestParam(value = "payCount", required = false) Integer payCount) {
        if (size < 0 || size > 50) {
            size = 10L;
        }

        if (offset < 0) {
            offset = 0L;
        }

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        InvitationActivityJoinHistoryQuery query = InvitationActivityJoinHistoryQuery.builder()
                .size(size)
                .offset(offset)
                .joinUid(joinUid)
                .userName(joinUserName)
                .recordId(id)
                .beginTime(beginTime)
                .endTime(endTime)
                .activityId(activityId)
                .activityName(activityName)
                .status(status)
                .payCount(payCount)
                .tenantId(TenantContextHolder.getTenantId())
                .phone(joinUserPhone).build();

        return R.ok(invitationActivityJoinHistoryService.selectByPage(query));
    }

    @GetMapping("/admin/invitationActivityJoinHistory/queryCount")
    public R count(@RequestParam(value = "activityId", required = false) Long activityId,
                   @RequestParam(value = "id", required = false) Long id,
                   @RequestParam(value = "beginTime", required = false) Long beginTime,
                   @RequestParam(value = "endTime", required = false) Long endTime,
                   @RequestParam(value = "status", required = false) Integer status,
                   @RequestParam(value = "joinUserPhone", required = false) String joinUserPhone,
                   @RequestParam(value = "joinUid", required = false) Long joinUid,
                   @RequestParam(value = "joinUserName", required = false) String joinUserName,
                   @RequestParam(value = "activityName", required = false) String activityName,
                   @RequestParam(value = "payCount", required = false) Integer payCount) {

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        InvitationActivityJoinHistoryQuery query = InvitationActivityJoinHistoryQuery.builder()
                .joinUid(joinUid)
                .userName(joinUserName)
                .recordId(id)
                .beginTime(beginTime)
                .endTime(endTime)
                .activityId(activityId)
                .activityName(activityName)
                .status(status)
                .payCount(payCount)
                .tenantId(TenantContextHolder.getTenantId())
                .phone(joinUserPhone)
                .build();

        return R.ok(invitationActivityJoinHistoryService.selectByPageCount(query));
    }

}
