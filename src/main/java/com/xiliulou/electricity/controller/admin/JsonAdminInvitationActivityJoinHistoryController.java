package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.InvitationActivityJoinHistoryQuery;
import com.xiliulou.electricity.query.InvitationActivityRecordQuery;
import com.xiliulou.electricity.request.activity.InvitationActivityAnalysisRequest;
import com.xiliulou.electricity.service.InvitationActivityJoinHistoryService;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

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

    @GetMapping("/admin/invitationActivityJoinHistory/page")
    public R page(@RequestParam("size") long size, @RequestParam("offset") long offset, @RequestParam(value = "beginTime", required = false) Long beginTime,
            @RequestParam(value = "endTime", required = false) Long endTime, @RequestParam(value = "activityId", required = false) Long activityId,
            @RequestParam(value = "id", required = false) Long id, @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "joinUid", required = false) Long joinUid, @RequestParam(value = "activityName", required = false) String activityName,
            @RequestParam(value = "payCount", required = false) Integer payCount) {
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
        
        InvitationActivityJoinHistoryQuery query = InvitationActivityJoinHistoryQuery.builder()
                .size(size)
                .offset(offset).joinUid(joinUid).recordId(id).beginTime(beginTime).endTime(endTime).activityId(activityId).activityName(activityName).status(status)
                .payCount(payCount)
                .tenantId(TenantContextHolder.getTenantId())
                .build();

        return R.ok(invitationActivityJoinHistoryService.selectByPage(query));
    }

    @GetMapping("/admin/invitationActivityJoinHistory/queryCount")
    public R count(@RequestParam(value = "activityId", required = false) Long activityId, @RequestParam(value = "id", required = false) Long id,
            @RequestParam(value = "beginTime", required = false) Long beginTime, @RequestParam(value = "endTime", required = false) Long endTime,
            @RequestParam(value = "status", required = false) Integer status, @RequestParam(value = "joinUid", required = false) Long joinUid,
            @RequestParam(value = "activityName", required = false) String activityName, @RequestParam(value = "payCount", required = false) Integer payCount) {

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        InvitationActivityJoinHistoryQuery query = InvitationActivityJoinHistoryQuery.builder().joinUid(joinUid).recordId(id).beginTime(beginTime).endTime(endTime)
                .activityId(activityId).activityName(activityName).status(status).payCount(payCount)
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
        
        InvitationActivityAnalysisRequest request = InvitationActivityAnalysisRequest.builder().uid(uid).activityId(activityId).timeType(timeType).beginTime(beginTime).endTime(endTime).build();
        
        return R.ok(invitationActivityJoinHistoryService.queryInvitationAdminAnalysis(request));
    }

}
