package com.xiliulou.electricity.controller.admin;

import cn.hutool.json.JSONUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.NewUserActivityAddAndUpdateQuery;
import com.xiliulou.electricity.query.ShareActivityQuery;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.NewUserActivityService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import shaded.org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 活动表(TActivity)表控制层
 *
 * @author makejava
 * @since 2021-04-14 09:27:12
 */
@RestController
@Slf4j
public class JsonAdminNewUserActivityController {
	/**
	 * 服务对象
	 */
	@Autowired
	private NewUserActivityService newUserActivityService;

	@Autowired
	FranchiseeService franchiseeService;

	//新增
	@PostMapping(value = "/admin/newUserActivity")
	public R save(@RequestBody @Validated(value = CreateGroup.class) NewUserActivityAddAndUpdateQuery newUserActivityAddAndUpdateQuery) {
		return newUserActivityService.insert(newUserActivityAddAndUpdateQuery);
	}


	//列表查询
	@GetMapping(value = "/admin/newUserActivity/list")
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

		TokenUser user = SecurityUtils.getUserInfo();
		if (Objects.isNull(user)) {
			log.error("ELECTRICITY  ERROR! not found user ");
			return R.fail("ELECTRICITY.0001", "未找到用户");
		}

		if (Objects.equals(user.getType(), User.TYPE_USER_FRANCHISEE)) {
			Franchisee franchisee = franchiseeService.queryByUid(user.getUid());
			if (Objects.isNull(franchisee)) {
				return R.ok();
			}

			franchiseeId = franchisee.getId();
		}

		ShareActivityQuery shareActivityQuery = ShareActivityQuery.builder()
				.offset(offset)
				.size(size)
				.name(name)
				.franchiseeId(franchiseeId)
				.tenantId(tenantId).build();

		if (StringUtils.isNotEmpty(type)) {
			Integer[] types = (Integer[])
					JSONUtil.parseArray(type).toArray(Integer[].class);

			List<Integer> typeList = Arrays.asList(types);
			shareActivityQuery.setTypeList(typeList);
		}
		return newUserActivityService.queryList(shareActivityQuery);
	}

	//列表查询
	@GetMapping(value = "/admin/newUserActivity/count")
	public R queryCount(@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
			@RequestParam(value = "type", required = false) String type) {

		//租户
		Integer tenantId = TenantContextHolder.getTenantId();

		TokenUser user = SecurityUtils.getUserInfo();
		if (Objects.isNull(user)) {
			log.error("ELECTRICITY  ERROR! not found user ");
			return R.fail("ELECTRICITY.0001", "未找到用户");
		}

		if (Objects.equals(user.getType(), User.TYPE_USER_FRANCHISEE)) {
			Franchisee franchisee = franchiseeService.queryByUid(user.getUid());
			if (Objects.isNull(franchisee)) {
				return R.ok();
			}

			franchiseeId = franchisee.getId();
		}

		ShareActivityQuery shareActivityQuery = ShareActivityQuery.builder()
				.name(name)
				.franchiseeId(franchiseeId)
				.tenantId(tenantId).build();

		if (StringUtils.isNotEmpty(type)) {
			Integer[] types = (Integer[])
					JSONUtil.parseArray(type).toArray(Integer[].class);

			List<Integer> typeList = Arrays.asList(types);
			shareActivityQuery.setTypeList(typeList);
		}
		return newUserActivityService.queryCount(shareActivityQuery);
	}

	//根据id查询活动详情
	@GetMapping(value = "/admin/newUserActivity/queryInfo/{id}")
	public R queryInfo(@PathVariable("id") Integer id) {
		if (Objects.isNull(id)) {
			return R.fail("ELECTRICITY.0007", "不合法的参数");
		}
		return newUserActivityService.queryInfo(id);
	}
}
