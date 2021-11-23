package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.ShareMoneyActivityAddAndUpdateQuery;
import com.xiliulou.electricity.query.ShareMoneyActivityQuery;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.ShareMoneyActivityService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * 活动表(TActivity)表控制层
 *
 * @author makejava
 * @since 2021-04-14 09:27:12
 */
@RestController
@Slf4j
public class JsonAdminShareMoneyActivityController {
	/**
	 * 服务对象
	 */
	@Autowired
	private ShareMoneyActivityService shareMoneyActivityService;

	@Autowired
	FranchiseeService franchiseeService;

	//新增
	@PostMapping(value = "/admin/shareMoneyActivity")
	public R save(@RequestBody @Validated(value = CreateGroup.class) ShareMoneyActivityAddAndUpdateQuery shareMoneyActivityAddAndUpdateQuery) {
		return shareMoneyActivityService.insert(shareMoneyActivityAddAndUpdateQuery);
	}

	//修改--暂时无此功能
	@PutMapping(value = "/admin/shareMoneyActivity")
	public R update(@RequestBody @Validated(value = UpdateGroup.class) ShareMoneyActivityAddAndUpdateQuery shareMoneyActivityAddAndUpdateQuery) {
		return shareMoneyActivityService.update(shareMoneyActivityAddAndUpdateQuery);
	}

	//列表查询
	@GetMapping(value = "/admin/shareMoneyActivity/list")
	public R queryList(@RequestParam("size") Long size,
			@RequestParam("offset") Long offset,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
			@RequestParam(value = "type", required = false) String type) {
		if (size < 0 || size > 50) {
			size = 10L;
		}

		if (offset < 0) {
			offset = 0L;
		}

		//租户
		Integer tenantId = TenantContextHolder.getTenantId();


		ShareMoneyActivityQuery shareMoneyActivityQuery = ShareMoneyActivityQuery.builder()
				.offset(offset)
				.size(size)
				.name(name)
				.franchiseeId(franchiseeId)
				.tenantId(tenantId).build();

		return shareMoneyActivityService.queryList(shareMoneyActivityQuery);
	}


	//列表查询
	@GetMapping(value = "/admin/shareMoneyActivity/count")
	public R queryCount(@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
			@RequestParam(value = "type", required = false) String type) {

		//租户
		Integer tenantId = TenantContextHolder.getTenantId();


		ShareMoneyActivityQuery shareMoneyActivityQuery = ShareMoneyActivityQuery.builder()
				.name(name)
				.franchiseeId(franchiseeId)
				.tenantId(tenantId).build();

		return shareMoneyActivityService.queryCount(shareMoneyActivityQuery);
	}


	//根据id查询活动详情
	@GetMapping(value = "/admin/shareMoneyActivity/queryInfo/{id}")
	public R queryInfo(@PathVariable("id") Integer id) {
		if (Objects.isNull(id)) {
			return R.fail("ELECTRICITY.0007", "不合法的参数");
		}
		return shareMoneyActivityService.queryInfo(id);
	}
}
