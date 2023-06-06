package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.InvitationActivityRecordService;
import com.xiliulou.electricity.service.InvitationActivityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-06-05-15:14
 */
@Slf4j
@RestController
public class JsonUserInvitationActivityController extends BaseController {

    @Autowired
    private InvitationActivityRecordService invitationActivityRecordService;

    /**
     * 生成邀请活动二维码
     */
    @GetMapping("/user/invitation/activity/generateCode")
    public R generateCode() {
        return returnTripleResult(invitationActivityRecordService.generateCode());
    }

    /**
     * 参与活动
     */
    @PostMapping("/user/invitation/activity/joinActivity")
    public R joinActivity(@RequestParam("code") String code) {
        return returnTripleResult(invitationActivityRecordService.joinActivity(code));
    }

}
