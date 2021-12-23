package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.UserAmountHistoryQuery;
import com.xiliulou.electricity.service.UserAmountHistoryService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * 参与邀请活动记录(JoinShareActivityRecord)表控制层
 *
 * @author makejava
 * @since 2021-07-14 09:44:36
 */
@RestController
@Slf4j
public class JsonAdminUserAmountHistoryController {
	/**
	 * 服务对象
	 */
	@Autowired
	private UserAmountHistoryService userAmountHistoryService;



	/**
	 * 用户邀请记录
	 */
	@GetMapping(value = "/admin/userAmountHistory/list")
	public R queryList(@RequestParam("size") Long size,
			@RequestParam("offset") Long offset,
			@RequestParam(value = "uid", required = false) Long uid) {
		if (size < 0 || size > 50) {
			size = 10L;
		}

		if (offset < 0) {
			offset = 0L;
		}

		//租户
		Integer tenantId = TenantContextHolder.getTenantId();


		UserAmountHistoryQuery userAmountHistoryQuery = UserAmountHistoryQuery.builder()
				.offset(offset)
				.size(size)
				.tenantId(tenantId)
				.uid(uid).build();
		return userAmountHistoryService.queryList(userAmountHistoryQuery);
	}


	/**
	 * 用户邀请记录
	 */
	@GetMapping(value = "/admin/userAmountHistory/queryCount")
	public R queryCount(@RequestParam(value = "uid", required = false) Long uid) {

		//租户
		Integer tenantId = TenantContextHolder.getTenantId();


		UserAmountHistoryQuery userAmountHistoryQuery = UserAmountHistoryQuery.builder()
				.tenantId(tenantId)
				.uid(uid).build();
		return userAmountHistoryService.queryCount(userAmountHistoryQuery);
	}

}
