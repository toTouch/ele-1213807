package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.InvitationActivityJoinHistoryQuery;
import com.xiliulou.electricity.service.InvitationActivityJoinHistoryService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/admin/invitationActivityJoinHistory/page")
    public R page(@RequestParam("size") long size, @RequestParam("offset") long offset,
                  @RequestParam(value = "beginTime", required = false) Long beginTime,
                  @RequestParam(value = "endTime", required = false) Long endTime,
                  @RequestParam(value = "activityId", required = false) Long activityId,
                  @RequestParam(value = "id", required = false) Long id,
                  @RequestParam(value = "status", required = false) Integer status,
                  @RequestParam(value = "phone", required = false) String phone,
                  @RequestParam(value = "joinUserName", required = false) String joinUserName) {
        if (size < 0 || size > 50) {
            size = 10L;
        }

        if (offset < 0) {
            offset = 0L;
        }

        InvitationActivityJoinHistoryQuery query = InvitationActivityJoinHistoryQuery.builder().size(size).offset(offset).userName(joinUserName).recordId(id)
                .beginTime(beginTime).endTime(endTime).activityId(activityId).status(status).tenantId(TenantContextHolder.getTenantId()).phone(phone).build();

        return R.ok(invitationActivityJoinHistoryService.selectByPage(query));
    }

    @GetMapping("/admin/invitationActivityJoinHistory/queryCount")
    public R count(@RequestParam(value = "activityId", required = false) Long activityId,
                   @RequestParam(value = "id", required = false) Long id,
                   @RequestParam(value = "beginTime", required = false) Long beginTime,
                   @RequestParam(value = "endTime", required = false) Long endTime,
                   @RequestParam(value = "status", required = false) Integer status,
                   @RequestParam(value = "phone", required = false) String phone,
                   @RequestParam(value = "joinUserName", required = false) String joinUserName) {

        InvitationActivityJoinHistoryQuery query = InvitationActivityJoinHistoryQuery.builder().userName(joinUserName).recordId(id)
                .beginTime(beginTime).endTime(endTime).activityId(activityId).status(status).tenantId(TenantContextHolder.getTenantId()).phone(phone).build();

        return R.ok(invitationActivityJoinHistoryService.selectByPageCount(query));
    }

}
