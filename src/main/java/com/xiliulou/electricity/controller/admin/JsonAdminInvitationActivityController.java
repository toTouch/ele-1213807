package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.InvitationActivityQuery;
import com.xiliulou.electricity.service.InvitationActivityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-06-01-15:57
 */
@Slf4j
@RestController
public class JsonAdminInvitationActivityController extends BaseController {

    @Autowired
    private InvitationActivityService invitationActivityService;


    @GetMapping("/admin/invitationActivity/page")
    public R page(@RequestParam("size") long size, @RequestParam("offset") long offset,
                  @RequestParam(value = "status", required = false) Integer status,
                  @RequestParam(value = "name", required = false) String name) {
        if (size < 0 || size > 50) {
            size = 10L;
        }

        if (offset < 0) {
            offset = 0L;
        }

        InvitationActivityQuery query = InvitationActivityQuery.builder().size(size).offset(offset).name(name).status(status).build();

        return R.ok(invitationActivityService.selectByPage(query));
    }

    @GetMapping("/admin/invitationActivity/count")
    public R count(@RequestParam(value = "status", required = false) Integer status,
                   @RequestParam(value = "name", required = false) String name) {

        InvitationActivityQuery query = InvitationActivityQuery.builder().name(name).status(status).build();

        return R.ok(invitationActivityService.selectByPageCount(query));
    }


}
