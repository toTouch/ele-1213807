package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.UserNoticeQuery;
import com.xiliulou.electricity.service.UserNoticeService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

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

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.ok();
        }
        return userNoticeService.queryUserNotice();
    }


    @PutMapping("/admin/userNotice")
    public R update(@Validated @RequestBody UserNoticeQuery userNoticeQuery) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        return returnTripleResult(userNoticeService.update(userNoticeQuery, user.getUid()));
    }

}
