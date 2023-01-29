package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.JsonShareActivityHistoryQuery;
import com.xiliulou.electricity.service.JoinShareActivityHistoryService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
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



	/**
	 * 用户参与记录admin
	 */
	@GetMapping(value = "/admin/joinShareActivityHistory/list")
	public R joinActivity(@RequestParam("size") Long size,
			@RequestParam("offset") Long offset,
			@RequestParam( "uid") Long uid, @RequestParam("activityId") Integer activityId,
            @RequestParam(value = "joinName", required = false) String joinName,
            @RequestParam(value = "startTime", required = false) Long startTime,
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
				.size(size)
				.tenantId(tenantId)
				.uid(uid).activityId(activityId).joinName(joinName).status(status).startTime(startTime).endTime(endTime)
                .build();
		return joinShareActivityHistoryService.queryList(jsonShareActivityHistoryQuery);
	}

}































