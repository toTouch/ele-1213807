package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.JsonShareActivityHistoryQuery;
import com.xiliulou.electricity.service.JoinShareActivityHistoryService;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
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
public class JsonAdminJoinShareActivityHistoryController {
    /**
     * 服务对象
     */
    @Resource
    private JoinShareActivityHistoryService joinShareActivityHistoryService;

    @Autowired
	private UserDataScopeService userDataScopeService;

	/**
	 * 用户参与记录admin
	 */
	@GetMapping(value = "/admin/joinShareActivityHistory/list")
	public R joinActivity(@RequestParam("size") Long size,
			@RequestParam("offset") Long offset, @RequestParam("id") Long id,
            @RequestParam(value = "joinName", required = false) String joinName,
            @RequestParam(value = "beginTime", required = false) Long beginTime,
            @RequestParam(value = "endTime", required = false) Long endTime,
            @RequestParam(value = "status", required = false) Integer status) {

		if (size < 0 || size > 50) {
			size = 10L;
		}

		if (offset < 0) {
			offset = 0L;
		}

		//租户
		Integer tenantId = TenantContextHolder.getTenantId();

		JsonShareActivityHistoryQuery jsonShareActivityHistoryQuery = JsonShareActivityHistoryQuery.builder()
				.offset(offset)
				.size(size).tenantId(tenantId).id(id).joinName(joinName).status(status)
                .startTime(beginTime).endTime(endTime)
                .build();
		return joinShareActivityHistoryService.queryList(jsonShareActivityHistoryQuery);
	}
    
    
    @GetMapping(value = "/admin/joinShareActivityHistory/queryCount")
    public R joinActivityCount(@RequestParam("id") Long id,
            @RequestParam(value = "joinName", required = false) String joinName,
            @RequestParam(value = "beginTime", required = false) Long beginTime,
            @RequestParam(value = "endTime", required = false) Long endTime,
            @RequestParam(value = "status", required = false) Integer status) {
        
        //租户
        Integer tenantId = TenantContextHolder.getTenantId();
        
        JsonShareActivityHistoryQuery jsonShareActivityHistoryQuery = JsonShareActivityHistoryQuery.builder()
                .tenantId(tenantId).id(id).joinName(joinName).status(status)
                .startTime(beginTime).endTime(endTime).build();
        return joinShareActivityHistoryService.queryCount(jsonShareActivityHistoryQuery);
    }

	/**
	 * 邀请活动参与人列表查询
	 * @param size
	 * @param offset
	 * @param joinName
	 * @param phone
	 * @param activityName
	 * @param beginTime
	 * @param endTime
	 * @param status
	 * @return
	 */
	@GetMapping(value = "/admin/joinShareActivityHistory/participationList")
	public R participationList(@RequestParam("size") Long size,
								  @RequestParam("offset") Long offset,
								  @RequestParam(value = "joinName", required = false) String joinName,
								  @RequestParam(value = "joinPhone", required = false) String joinPhone,
							      @RequestParam(value = "joinUid", required = false) Long joinUid,
								  @RequestParam(value = "activityName", required = false) String activityName,
								  @RequestParam(value = "beginTime", required = false) Long beginTime,
								  @RequestParam(value = "endTime", required = false) Long endTime,
								  @RequestParam(value = "status", required = false) Integer status) {

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


		//租户
		Integer tenantId = TenantContextHolder.getTenantId();

		JsonShareActivityHistoryQuery jsonShareActivityHistoryQuery = JsonShareActivityHistoryQuery.builder()
				.offset(offset)
				.size(size)
				.tenantId(tenantId)
				.joinName(joinName)
				.phone(joinPhone)
				.joinUid(joinUid)
				.activityName(activityName)
				.status(status)
				.startTime(beginTime)
				.endTime(endTime)
				.storeIds(storeIds)
				.franchiseeIds(franchiseeIds)
				.build();

		return joinShareActivityHistoryService.queryParticipants(jsonShareActivityHistoryQuery);
	}

	@GetMapping(value = "/admin/joinShareActivityHistory/participationCount")
	public R participationCount(@RequestParam(value = "joinName", required = false) String joinName,
								  @RequestParam(value = "joinPhone", required = false) String joinPhone,
								  @RequestParam(value = "joinUid", required = false) Long joinUid,
								  @RequestParam(value = "activityName", required = false) String activityName,
								  @RequestParam(value = "beginTime", required = false) Long beginTime,
								  @RequestParam(value = "endTime", required = false) Long endTime,
								  @RequestParam(value = "status", required = false) Integer status) {

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

		JsonShareActivityHistoryQuery jsonShareActivityHistoryQuery = JsonShareActivityHistoryQuery.builder()
				.tenantId(tenantId)
				.joinName(joinName)
				.phone(joinPhone)
				.joinUid(joinUid)
				.activityName(activityName)
				.status(status)
				.startTime(beginTime)
				.endTime(endTime)
				.storeIds(storeIds)
				.franchiseeIds(franchiseeIds)
				.build();

		return joinShareActivityHistoryService.queryParticipantsCount(jsonShareActivityHistoryQuery);
	}

    @GetMapping(value = "/admin/joinShareActivityHistory/exportExcel")
    public void joinActivityExportExcel(@RequestParam("id") Long id,
            @RequestParam(value = "joinName", required = false) String joinName,
            @RequestParam(value = "beginTime", required = false) Long beginTime,
            @RequestParam(value = "endTime", required = false) Long endTime,
            @RequestParam(value = "status", required = false) Integer status, HttpServletResponse response) {
        
        //租户
        Integer tenantId = TenantContextHolder.getTenantId();
        
        JsonShareActivityHistoryQuery jsonShareActivityHistoryQuery = JsonShareActivityHistoryQuery.builder()
                .tenantId(tenantId).id(id).joinName(joinName).status(status).startTime(beginTime).endTime(endTime)
                .build();
        joinShareActivityHistoryService.queryExportExcel(jsonShareActivityHistoryQuery, response);
    }
}































