package com.xiliulou.electricity.controller.outer;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.UserNoticeQuery;
import com.xiliulou.electricity.service.UserNoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author : eclair
 * @date : 2021/9/26 4:45 下午
 */
@RestController
public class JsonOuterNoticeController extends BaseController {
    @Autowired
    UserNoticeService userNoticeService;

    @GetMapping("/outer/userNotice")
    public R queryUserNotice() {

        return userNoticeService.queryUserNotice();
    }



}
