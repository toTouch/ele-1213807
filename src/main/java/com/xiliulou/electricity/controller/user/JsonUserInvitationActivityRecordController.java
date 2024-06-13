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
import org.springframework.web.bind.annotation.RequestParam;
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
    public R invitationLineData() {
        return returnTripleResult(invitationActivityRecordService.listInvitationLineData());
    }
    
    /**
     * 邀请分析-时间范围（昨日/本月/累计  统计邀请总人数及成功邀请总人数）
     *
     * @param timeType 1-昨日（昨天0:00-23:59） 2-本月（当月一号0:00-当前时间，默认值） 3-累计
     */
    @GetMapping("/user/invitation/activity/record/analysis")
    public R invitationAnalysis(@RequestParam("timeType") Integer timeType) {
        return returnTripleResult(invitationActivityRecordService.queryInvitationAnalysis(timeType));
    }
    
    /**
     * 邀请分析-邀请明细
     *
     * @param timeType 1-昨日（昨天0:00-23:59） 2-本月（当月一号0:00-当前时间，默认值） 3-累计
     * @param status   0-全部(默认)，1--已参与，2--邀请成功，3--已过期， 4--被替换， 5--活动已下架
     */
    @GetMapping("/user/invitation/activity/record/analysis/detail")
    public R invitationDetails(@RequestParam("size") long size, @RequestParam("offset") long offset, @RequestParam("timeType") Integer timeType,
            @RequestParam("status") Integer status) {
        if (size < 0 || size > 50) {
            size = 10L;
        }
        
        if (offset < 0) {
            offset = 0L;
        }
        
        InvitationActivityAnalysisRequest request = InvitationActivityAnalysisRequest.builder().size(size).offset(offset).timeType(timeType).status(status).build();
        
        return returnTripleResult(invitationActivityRecordService.queryInvitationDetail(request));
    }
    
    /**
     * 已获奖励
     *
     * @param timeType 1-昨日（昨天0:00-23:59） 2-本月（当月一号0:00-当前时间，默认值） 3-自定义
     */
    @GetMapping("/user/invitation/activity/record/income/analysis")
    public R invitationIncomeAnalysis(@RequestParam("timeType") Integer timeType) {
        return returnTripleResult(invitationActivityRecordService.queryInvitationIncomeAnalysis(timeType));
    }
    
    /**
     * 收入明细
     *
     * @param timeType 1-昨日（昨天0:00-23:59） 2-本月（当月一号0:00-当前时间，默认值） 3-自定义
     * @param status   0-全部(默认)，1--已参与，2--邀请成功，3--已过期， 4--被替换， 5--活动已下架
     */
    @GetMapping("/user/invitation/activity/record/income/detail")
    public R invitationIncomeDetail(@RequestParam("size") long size, @RequestParam("offset") long offset, @RequestParam("timeType") Integer timeType,
            @RequestParam("status") Integer status) {
        if (size < 0 || size > 50) {
            size = 10L;
        }
        
        if (offset < 0) {
            offset = 0L;
        }
        
        InvitationActivityAnalysisRequest request = InvitationActivityAnalysisRequest.builder().size(size).offset(offset).timeType(timeType).status(status).build();
        
        return returnTripleResult(invitationActivityRecordService.queryInvitationIncomeDetail(request));
    }
    
}
