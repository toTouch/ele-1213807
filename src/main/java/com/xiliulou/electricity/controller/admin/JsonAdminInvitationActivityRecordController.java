package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.InvitationActivityRecordQuery;
import com.xiliulou.electricity.service.InvitationActivityRecordService;
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
public class JsonAdminInvitationActivityRecordController {

    @Autowired
    private InvitationActivityRecordService invitationActivityRecordService;

    @GetMapping("/admin/invitationActivityRecord/page")
    public R page(@RequestParam("size") long size, @RequestParam("offset") long offset,
                  @RequestParam(value = "beginTime", required = false) Long beginTime,
                  @RequestParam(value = "endTime", required = false) Long endTime,
                  @RequestParam(value = "phone", required = false) String phone,
                  @RequestParam(value = "userName", required = false) String userName) {
        if (size < 0 || size > 50) {
            size = 10L;
        }

        if (offset < 0) {
            offset = 0L;
        }

        InvitationActivityRecordQuery query = InvitationActivityRecordQuery.builder().size(size).offset(offset).userName(userName)
                .tenantId(TenantContextHolder.getTenantId()).phone(phone).beginTime(beginTime).endTime(endTime).build();

        return R.ok(invitationActivityRecordService.selectByPage(query));
    }

    @GetMapping("/admin/invitationActivityRecord/queryCount")
    public R count(@RequestParam(value = "phone", required = false) String phone,
                   @RequestParam(value = "beginTime", required = false) Long beginTime,
                   @RequestParam(value = "endTime", required = false) Long endTime,
                   @RequestParam(value = "userName", required = false) String userName) {

        InvitationActivityRecordQuery query = InvitationActivityRecordQuery.builder()
                .tenantId(TenantContextHolder.getTenantId()).beginTime(beginTime).endTime(endTime).userName(userName).phone(phone).build();

        return R.ok(invitationActivityRecordService.selectByPageCount(query));
    }


}
