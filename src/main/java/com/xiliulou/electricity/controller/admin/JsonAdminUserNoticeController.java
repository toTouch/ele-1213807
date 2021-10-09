package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.UserNoticeQuery;
import com.xiliulou.electricity.service.UserNoticeService;
import com.xiliulou.electricity.validator.UpdateGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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



    @PutMapping("/admin/userNotice")
    public R update(@Validated @RequestBody  UserNoticeQuery userNoticeQuery) {
        return returnTripleResult(userNoticeService.update(userNoticeQuery));

    }

}
