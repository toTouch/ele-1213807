package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.OldUserActivityAddAndUpdateQuery;
import com.xiliulou.electricity.query.OldUserActivityQuery;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.OldUserActivityService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.validator.CreateGroup;
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
public class JsonAdminOldUserActivityController {
	/**
	 * 服务对象
	 */
	@Autowired
	private OldUserActivityService oldUserActivityService;


	//新增
	@PostMapping(value = "/admin/oldUserActivity")
	public R save(@RequestBody @Validated(value = CreateGroup.class) OldUserActivityAddAndUpdateQuery oldUserActivityAddAndUpdateQuery) {
		return oldUserActivityService.insert(oldUserActivityAddAndUpdateQuery);
	}


	//编辑（暂时只支持上下架）
	@PutMapping(value = "/admin/oldUserActivity")
	public R update(@RequestBody @Validated(value = CreateGroup.class) OldUserActivityAddAndUpdateQuery oldUserActivityAddAndUpdateQuery) {
		return oldUserActivityService.update(oldUserActivityAddAndUpdateQuery);
	}


	//列表查询
	@GetMapping(value = "/admin/oldUserActivity/list")
	public R queryList(@RequestParam("size") Long size,
			@RequestParam("offset") Long offset,
			@RequestParam(value = "name", required = false) String name) {
		if (size < 0 || size > 50) {
			size = 10L;
		}

		if (offset < 0) {
			offset = 0L;
		}

		//租户
		Integer tenantId = TenantContextHolder.getTenantId();


		OldUserActivityQuery oldUserActivityQuery = OldUserActivityQuery.builder()
				.offset(offset)
				.size(size)
				.name(name)
				.tenantId(tenantId).build();


		return oldUserActivityService.queryList(oldUserActivityQuery);
	}


	//列表查询
	@GetMapping(value = "/admin/oldUserActivity/count")
	public R queryCount(@RequestParam(value = "name", required = false) String name) {

		//租户
		Integer tenantId = TenantContextHolder.getTenantId();


		OldUserActivityQuery oldUserActivityQuery = OldUserActivityQuery.builder()
				.name(name)
				.tenantId(tenantId).build();

		return oldUserActivityService.queryCount(oldUserActivityQuery);
	}


	//根据id查询活动详情
	@GetMapping(value = "/admin/oldUserActivity/queryInfo/{id}")
	public R queryInfo(@PathVariable("id") Integer id) {
		if (Objects.isNull(id)) {
			return R.fail("ELECTRICITY.0007", "不合法的参数");
		}
		return oldUserActivityService.queryInfo(id);
	}
}
