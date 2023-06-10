package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.InvitationActivityQuery;
import com.xiliulou.electricity.service.InvitationActivityRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-06-07-14:57
 */
@Slf4j
@RestController
public class JsonUserInvitationActivityRecordController extends BaseController {

    @Autowired
    private InvitationActivityRecordService invitationActivityRecordService;

    /**
     * 获取用户邀请信息
     */
    @GetMapping("/user/invitation/activity/record/detail")
    public R selectUserInvitationDetail() {
        return returnTripleResult(invitationActivityRecordService.selectUserInvitationDetail());
    }

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
    public R joinActivity(@RequestBody InvitationActivityQuery query) {
        return returnTripleResult(invitationActivityRecordService.joinActivity(query));
    }

}
