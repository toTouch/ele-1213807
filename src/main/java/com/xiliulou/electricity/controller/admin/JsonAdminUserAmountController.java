package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.ElectricityCabinetQuery;
import com.xiliulou.electricity.query.UserAmountQuery;
import com.xiliulou.electricity.service.UserAmountService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 参与邀请活动记录(JoinShareActivityRecord)表控制层
 *
 * @author makejava
 * @since 2021-07-14 09:44:36
 */
@RestController
@Slf4j
public class JsonAdminUserAmountController {
	/**
	 * 服务对象
	 */
	@Resource
	private UserAmountService userAmountService;



	/**
	 * 用户余额列表
	 */
	@GetMapping(value = "/admin/userAmount/list")
	public R queryList(@RequestParam("size") Long size,
			@RequestParam("offset") Long offset,
			@RequestParam("phone") String phone) {

		if (size < 0 || size > 50) {
			size = 10L;
		}

		if (offset < 0) {
			offset = 0L;
		}

		//租户
		Integer tenantId = TenantContextHolder.getTenantId();

		UserAmountQuery userAmountQuery = UserAmountQuery.builder()
				.offset(offset)
				.size(size)
				.phone(phone)
				.tenantId(tenantId).build();

		return userAmountService.queryList(userAmountQuery);
	}

	/**
	 * 用户余额列表
	 */
	@GetMapping(value = "/admin/userAmount/queryCount")
	public R queryCount(@RequestParam("phone") String phone) {


		//租户
		Integer tenantId = TenantContextHolder.getTenantId();

		UserAmountQuery userAmountQuery = UserAmountQuery.builder()
				.phone(phone)
				.tenantId(tenantId).build();

		return userAmountService.queryCount(userAmountQuery);
	}

}
