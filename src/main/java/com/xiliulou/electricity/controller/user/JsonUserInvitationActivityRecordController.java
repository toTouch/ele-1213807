package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.InvitationActivityQuery;
import com.xiliulou.electricity.request.activity.InvitationActivityAnalysisRequest;
import com.xiliulou.electricity.service.InvitationActivityRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
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
     * 获取用户邀请信息(多活动)
     */
    @GetMapping("/user/invitation/activity/record/detail/v2")
    public R selectUserInvitationDetailV2() {
        return returnTripleResult(invitationActivityRecordService.selectUserInvitationDetailV2());
    }
    
    /**
     * 生成邀请活动二维码
     */
    @GetMapping("/user/invitation/activity/generateCode")
    public R generateCode() {
        return returnTripleResult(invitationActivityRecordService.generateCode());
    }
    
    /**
     * 生成邀请活动二维码（多个活动）
     */
    @GetMapping("/user/invitation/activity/generateCode/v2")
    public R generateCodeV2() {
        return returnTripleResult(invitationActivityRecordService.generateCodeV2());
    }

    /**
     * 参与活动
     */
    @PostMapping("/user/invitation/activity/joinActivity")
    public R joinActivity(@RequestBody InvitationActivityQuery query) {
        return returnTripleResult(invitationActivityRecordService.joinActivity(query));
    }
    
    /**
     * 我的战绩，获取 累计成功邀请人数、我的收入
     */
    @GetMapping("/user/invitation/activity/record/statics")
    public R countByStatics() {
        return returnTripleResult(invitationActivityRecordService.countByStatics());
    }
    
    /**
     * 邀请分析-折线图
     */
    @GetMapping("/user/invitation/activity/record/analysis/lineData")
    public R listInvitationLineData() {
        return returnTripleResult(invitationActivityRecordService.listInvitationLineData());
    }
    
    /**
     * 邀请分析
     *          -时间范围（昨日/本月/累计  统计邀请总人数及成功邀请总人数）
     *          -邀请明细
     */
    @GetMapping("/user/invitation/activity/record/analysis")
    public R listInvitationAnalysis(@RequestBody @Validated InvitationActivityAnalysisRequest request) {
        return returnTripleResult(invitationActivityRecordService.listInvitationAnalysis(request));
    }
    
    /**
     * 收入明细
     */
    @GetMapping("/user/invitation/activity/record/income/detail")
    public R listInvitationIncomeDetail(@RequestBody InvitationActivityAnalysisRequest request) {
        return returnTripleResult(invitationActivityRecordService.listInvitationIncomeDetail(request));
    }
    
}
