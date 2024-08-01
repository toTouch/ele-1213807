package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.InvitationActivityJoinHistoryQuery;
import com.xiliulou.electricity.query.InvitationActivityRecordQuery;
import com.xiliulou.electricity.request.InvitationActivityJoinHistoryRequest;
import com.xiliulou.electricity.request.activity.InvitationActivityAnalysisRequest;
import com.xiliulou.electricity.service.InvitationActivityJoinHistoryService;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.activity.InvitationActivityAnalysisAdminVO;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Objects;

import static org.bouncycastle.asn1.x500.style.RFC4519Style.uid;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-06-06-13:24
 */
@Slf4j
@RestController
public class JsonAdminInvitationActivityJoinHistoryController {

    @Autowired
    private InvitationActivityJoinHistoryService invitationActivityJoinHistoryService;

    @Autowired
    UserDataScopeService userDataScopeService;
    
    /**
     * 邀请人记录--查看参与详情
     *
     * @param size 默认10
     * @param offset 偏移量
     * @param beginTime
     * @param endTime
     * @param activityId 活动id
     * @param id
     * @param status 活动状态
     * @param joinUid 参与人uid
     * @param activityName
     * @param payCount
     * @return
     */
    @GetMapping("/admin/invitationActivityJoinHistory/page")
    public R page(@RequestParam("size") long size, @RequestParam("offset") long offset,
            InvitationActivityJoinHistoryRequest activityJoinHistoryRequest) {
        if (size < 0 || size > 50) {
            size = 10L;
        }

        if (offset < 0) {
            offset = 0L;
        }

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        if (Objects.isNull(activityJoinHistoryRequest.getUid())) {
            return R.ok(Collections.emptyList());
        }
        
        InvitationActivityJoinHistoryQuery query = InvitationActivityJoinHistoryQuery.builder()
                .size(size)
                .offset(offset)
                .joinUid(activityJoinHistoryRequest.getJoinUid())
                .recordId(activityJoinHistoryRequest.getId())
                .beginTime(activityJoinHistoryRequest.getBeginTime())
                .endTime(activityJoinHistoryRequest.getEndTime())
                .uid(activityJoinHistoryRequest.getUid())
                .activityId(activityJoinHistoryRequest.getActivityId())
                .activityName(activityJoinHistoryRequest.getActivityName())
                .status(activityJoinHistoryRequest.getStatus())
                .payCount(activityJoinHistoryRequest.getPayCount())
                .tenantId(TenantContextHolder.getTenantId())
                .build();

        return R.ok(invitationActivityJoinHistoryService.selectByPage(query));
    }
    
    /**
     * 邀请人记录--查看参与详情
     *
     * @param beginTime
     * @param endTime
     * @param activityId 活动id
     * @param id
     * @param status 活动状态
     * @param joinUid 参与人uid
     * @param activityName
     * @param payCount
     * @return
     */
    @GetMapping("/admin/invitationActivityJoinHistory/queryCount")
    public R count(@RequestParam(value = "activityId", required = false) Long activityId,
            InvitationActivityJoinHistoryRequest activityJoinHistoryRequest) {

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        if (Objects.isNull(activityJoinHistoryRequest.getUid())) {
            return R.ok(Collections.emptyList());
        }
        
        InvitationActivityJoinHistoryQuery query = InvitationActivityJoinHistoryQuery.builder()
                .joinUid(activityJoinHistoryRequest.getJoinUid())
                .recordId(activityJoinHistoryRequest.getId())
                .beginTime(activityJoinHistoryRequest.getBeginTime())
                .endTime(activityJoinHistoryRequest.getEndTime())
                .uid(activityJoinHistoryRequest.getUid())
                .activityId(activityJoinHistoryRequest.getActivityId())
                .activityName(activityJoinHistoryRequest.getActivityName())
                .status(activityJoinHistoryRequest.getStatus())
                .payCount(activityJoinHistoryRequest.getPayCount())
                .tenantId(TenantContextHolder.getTenantId())
                .build();

        return R.ok(invitationActivityJoinHistoryService.selectByPageCount(query));
    }
    
    /**
     * @description 根据时间范围查询 邀请分析（邀请总数、邀请成功）、已获奖励（首次、非首次）
     * @param timeType 1-昨日（昨天0:00-23:59） 2-本月（当月一号0:00-当前时间，默认值） 3-自定义
     *                 timeType=3时，beginTime和endTime入参
     * 数据权限：详情列表（/admin/invitationActivityJoinHistory/page）没有加数据权限，此处与其保持一致
     * @date 2024/1/4 13:41:17
     * @author HeYafeng
     */
    @GetMapping("/admin/invitationActivityJoinHistory/analysis")
    public R invitationAnalysis(@RequestParam("uid") Long uid, @RequestParam("activityId") Long activityId, @RequestParam(value = "timeType") Integer timeType, @RequestParam(value = "beginTime", required = false) Long beginTime,
            @RequestParam(value = "endTime", required = false) Long endTime) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        if (Objects.isNull(activityId)) {
            InvitationActivityAnalysisAdminVO invitationActivityAnalysisAdminVO = new InvitationActivityAnalysisAdminVO();
            invitationActivityAnalysisAdminVO.setTotalShareCount(0);
            invitationActivityAnalysisAdminVO.setTotalInvitationCount(0);
            invitationActivityAnalysisAdminVO.setFirstTotalIncome(BigDecimal.ZERO);
            invitationActivityAnalysisAdminVO.setRenewTotalIncome(BigDecimal.ZERO);
            invitationActivityAnalysisAdminVO.setTotalIncome(BigDecimal.ZERO);
            return R.ok();
        }
        
        InvitationActivityAnalysisRequest request = InvitationActivityAnalysisRequest.builder().uid(uid).activityId(activityId).timeType(timeType).beginTime(beginTime).endTime(endTime).build();
        
        return R.ok(invitationActivityJoinHistoryService.queryInvitationAdminAnalysis(request));
    }

}
