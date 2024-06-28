package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.JsonShareMoneyActivityHistoryQuery;
import com.xiliulou.electricity.service.JoinShareMoneyActivityHistoryService;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 参与邀请活动记录(JoinShareActivityRecord)表控制层
 *
 * @author makejava
 * @since 2021-07-14 09:44:36
 */
@RestController
@Slf4j
public class JsonAdminJoinShareMoneyActivityHistoryController {
    /**
     * 服务对象
     */
    @Resource
    private JoinShareMoneyActivityHistoryService joinShareMoneyActivityHistoryService;

    @Autowired
    private UserDataScopeService userDataScopeService;

    /**
     * 用户参与记录admin
     */
    @GetMapping(value = "/admin/joinShareMoneyActivityHistory/list")
    public R queryList(@RequestParam("size") Long size, @RequestParam("offset") Long offset,
            @RequestParam("id") Long id, @RequestParam(value = "joinName", required = false) String joinName,
            @RequestParam(value = "beginTime", required = false) Long beginTime,
            @RequestParam(value = "endTime", required = false) Long endTime,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "franchiseeId", required = false) Long franchiseeId) {

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
    
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            return R.ok();
        }
    
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok();
            }
        }
    
        JsonShareMoneyActivityHistoryQuery jsonShareMoneyActivityHistoryQuery = JsonShareMoneyActivityHistoryQuery.builder().offset(offset).size(size).id(id).joinName(joinName)
                .beginTime(beginTime).endTime(endTime).status(status).tenantId(TenantContextHolder.getTenantId()).franchiseeIds(franchiseeIds).franchiseeId(franchiseeId).build();
        return joinShareMoneyActivityHistoryService.queryList(jsonShareMoneyActivityHistoryQuery);
	}


    /**
     * 用户参与记录admin
     */
    @GetMapping(value = "/admin/joinShareMoneyActivityHistory/queryCount")
    public R queryCount(@RequestParam("id") Long id,
            @RequestParam(value = "joinName", required = false) String joinName,
            @RequestParam(value = "beginTime", required = false) Long beginTime,
            @RequestParam(value = "endTime", required = false) Long endTime,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "franchiseeId", required = false) Long franchiseeId) {
    
        JsonShareMoneyActivityHistoryQuery jsonShareMoneyActivityHistoryQuery = JsonShareMoneyActivityHistoryQuery
                .builder().id(id).joinName(joinName).beginTime(beginTime).endTime(endTime).status(status)
                .tenantId(TenantContextHolder.getTenantId()).build();
		return joinShareMoneyActivityHistoryService.queryCount(jsonShareMoneyActivityHistoryQuery);
	}
    
    @GetMapping(value = "/admin/joinShareMoneyActivityHistory/exportExcel")
    public void queryExportExcel(@RequestParam("id") Long id,
            @RequestParam(value = "joinName", required = false) String joinName,
            @RequestParam(value = "beginTime", required = false) Long beginTime,
            @RequestParam(value = "endTime", required = false) Long endTime,
            @RequestParam(value = "status", required = false) Integer status, HttpServletResponse response) {
        JsonShareMoneyActivityHistoryQuery jsonShareMoneyActivityHistoryQuery = JsonShareMoneyActivityHistoryQuery
                .builder().id(id).joinName(joinName).beginTime(beginTime).endTime(endTime).status(status)
                .tenantId(TenantContextHolder.getTenantId()).build();
    
        joinShareMoneyActivityHistoryService.queryExportExcel(jsonShareMoneyActivityHistoryQuery, response);
    }

    /**
     * 查询邀请返现参与记录列表信息
     * @param size
     * @param offset
     * @param joinName
     * @param activityName
     * @param beginTime
     * @param endTime
     * @param status
     * @return
     */
    @GetMapping(value = "/admin/joinShareMoneyActivityHistory/participationList")
    public R participationList(@RequestParam("size") Long size,
                               @RequestParam("offset") Long offset,
                               @RequestParam(value = "joinName", required = false) String joinName,
                               @RequestParam(value = "joinPhone", required = false) String joinPhone,
                               @RequestParam(value = "joinUid", required = false) Long joinUid,
                               @RequestParam(value = "activityName", required = false) String activityName,
                               @RequestParam(value = "beginTime", required = false) Long beginTime,
                               @RequestParam(value = "endTime", required = false) Long endTime,
                               @RequestParam(value = "status", required = false) Integer status,
                               @RequestParam(value = "franchiseeId", required = false) Long franchiseeId) {

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

        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        List<Long> storeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            storeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(storeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        JsonShareMoneyActivityHistoryQuery jsonShareMoneyActivityHistoryQuery = JsonShareMoneyActivityHistoryQuery.builder()
                .offset(offset)
                .size(size)
                .tenantId(tenantId)
                .joinName(joinName)
                .phone(joinPhone)
                .joinUid(joinUid)
                .activityName(activityName)
                .status(status)
                .beginTime(beginTime)
                .endTime(endTime)
                .storeIds(storeIds)
                .franchiseeIds(franchiseeIds)
                .franchiseeId(franchiseeId)
                .build();

        return joinShareMoneyActivityHistoryService.queryParticipantsRecord(jsonShareMoneyActivityHistoryQuery);
    }

    @GetMapping(value = "/admin/joinShareMoneyActivityHistory/participationCount")
    public R participationCount(@RequestParam(value = "joinName", required = false) String joinName,
                                @RequestParam(value = "joinPhone", required = false) String joinPhone,
                                @RequestParam(value = "joinUid", required = false) Long joinUid,
                                @RequestParam(value = "activityName", required = false) String activityName,
                                @RequestParam(value = "beginTime", required = false) Long beginTime,
                                @RequestParam(value = "endTime", required = false) Long endTime,
                                @RequestParam(value = "status", required = false) Integer status,
                                @RequestParam(value = "franchiseeId", required = false) Long franchiseeId) {

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (org.apache.commons.collections.CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        List<Long> storeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            storeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (org.springframework.util.CollectionUtils.isEmpty(storeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        Integer tenantId = TenantContextHolder.getTenantId();

        JsonShareMoneyActivityHistoryQuery jsonShareMoneyActivityHistoryQuery = JsonShareMoneyActivityHistoryQuery.builder()
                .tenantId(tenantId)
                .joinName(joinName)
                .phone(joinPhone)
                .joinUid(joinUid)
                .activityName(activityName)
                .status(status)
                .beginTime(beginTime)
                .endTime(endTime)
                .storeIds(storeIds)
                .franchiseeIds(franchiseeIds)
                .franchiseeId(franchiseeId)
                .build();

        return joinShareMoneyActivityHistoryService.queryParticipantsCount(jsonShareMoneyActivityHistoryQuery);
    }

}































