package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.InvitationActivityRecordQuery;
import com.xiliulou.electricity.service.InvitationActivityRecordService;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-06-06-13:24
 */
@Slf4j
@RestController
public class JsonAdminInvitationActivityRecordController {

    @Autowired
    private InvitationActivityRecordService invitationActivityRecordService;

    @Autowired
    private UserDataScopeService userDataScopeService;

    @GetMapping("/admin/invitationActivityRecord/page")
    public R page(@RequestParam("size") long size, @RequestParam("offset") long offset,
                  @RequestParam(value = "beginTime", required = false) Long beginTime,
                  @RequestParam(value = "endTime", required = false) Long endTime,
                  @RequestParam(value = "uid", required = false) Long uid,
                  @RequestParam(value = "phone", required = false) String phone,
                  @RequestParam(value = "userName", required = false) String userName) {
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

        List<Long> storeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            storeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(org.apache.commons.collections.CollectionUtils.isEmpty(storeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(org.apache.commons.collections.CollectionUtils.isEmpty(franchiseeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        InvitationActivityRecordQuery query = InvitationActivityRecordQuery.builder()
                .size(size)
                .offset(offset)
                .userName(userName)
                .uid(uid)
                .tenantId(TenantContextHolder.getTenantId())
                .phone(phone)
                .beginTime(beginTime)
                .endTime(endTime)
                .storeIds(storeIds)
                .franchiseeIds(franchiseeIds)
                .build();

        return R.ok(invitationActivityRecordService.selectByPage(query));
    }

    @GetMapping("/admin/invitationActivityRecord/queryCount")
    public R count(@RequestParam(value = "phone", required = false) String phone,
                   @RequestParam(value = "beginTime", required = false) Long beginTime,
                   @RequestParam(value = "endTime", required = false) Long endTime,
                   @RequestParam(value = "uid", required = false) Long uid,
                   @RequestParam(value = "userName", required = false) String userName) {

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        List<Long> storeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            storeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(org.apache.commons.collections.CollectionUtils.isEmpty(storeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(org.apache.commons.collections.CollectionUtils.isEmpty(franchiseeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        InvitationActivityRecordQuery query = InvitationActivityRecordQuery.builder()
                .tenantId(TenantContextHolder.getTenantId())
                .beginTime(beginTime)
                .endTime(endTime)
                .uid(uid)
                .userName(userName)
                .phone(phone)
                .storeIds(storeIds)
                .franchiseeIds(franchiseeIds)
                .build();

        return R.ok(invitationActivityRecordService.selectByPageCount(query));
    }
    
    /**
     * @description 根据时间范围查询 邀请分析（邀请总数、邀请成功）、已获奖励（首次、非首次）
     * @param timeType 1-昨日（昨天0:00-23:59） 2-本月（当月一号0:00-当前时间，默认值） 3-自定义
     *                 timeType=3时，beginTime和endTime入参
     * 数据权限：详情列表（/admin/invitationActivityJoinHistory/page）没有加数据权限，此处与其保持一致
     * @date 2024/1/4 13:41:17
     * @author HeYafeng
     */
    @GetMapping("/admin/invitationActivityRecord/analysis")
    public R invitationAnalysis(@RequestParam("uid") Long uid, @RequestParam(value = "timeType") Integer timeType, @RequestParam(value = "beginTime", required = false) Long beginTime,
            @RequestParam(value = "beginTime", required = false) Long endTime) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
        InvitationActivityRecordQuery query = InvitationActivityRecordQuery.builder().uid(uid).tenantId(TenantContextHolder.getTenantId()).build();
    
        return R.ok(invitationActivityRecordService.queryInvitationAdminAnalysis(query, timeType, beginTime, endTime));
    }

}
