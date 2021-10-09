package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.FaqQuery;
import com.xiliulou.electricity.query.UserNoticeQuery;
import com.xiliulou.electricity.query.VersionNotificationQuery;
import com.xiliulou.electricity.service.FaqService;
import com.xiliulou.electricity.service.UserNoticeService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : eclair
 * @date : 2021/9/26 4:45 下午
 */
@RestController
public class JsonAdminUserNoticeController extends BaseController {
    @Autowired
    UserNoticeService userNoticeService;

    @GetMapping("/admin/userNotice")
    public R queryUserNotice() {

        return R.ok(userNoticeService.queryUserNotice());
    }


    @PostMapping("/admin/userNotice")
    public R insert( @Validated(value = CreateGroup.class) @RequestBody  UserNoticeQuery userNoticeQuery) {
        return returnTripleResult(userNoticeService.insert(userNoticeQuery));

    }

    @PutMapping("/admin/userNotice")
    public R update(@Validated(value = UpdateGroup.class) @RequestBody  UserNoticeQuery userNoticeQuery) {
        return returnTripleResult(userNoticeService.update(userNoticeQuery));

    }

}
