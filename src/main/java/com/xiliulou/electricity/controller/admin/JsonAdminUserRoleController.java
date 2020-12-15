package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.RoleService;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import com.xiliulou.electricity.web.query.RoleQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * @author: eclair
 * @Date: 2020/12/11 09:54
 * @Description:
 */
@RestController
@RequestMapping(value = "/admin")
public class JsonAdminUserRoleController extends BaseController {
	@Autowired
	RoleService roleService;

	@GetMapping("/role/list")
	public R getRoleList() {
		return roleService.queryAll();
	}

	@PostMapping("/role/add")
	public R addRole(@Validated(value = CreateGroup.class) @RequestBody RoleQuery roleQuery) {
		return roleService.addRole(roleQuery);
	}

	@PutMapping("/role/update")
	public R updateRole(@Validated(value = UpdateGroup.class) @RequestBody RoleQuery roleQuery) {
		if (Objects.isNull(roleQuery.getId())) {
			return R.fail("SYSTEM.0002", "id不能为空");
		}

		return roleService.updateRole(roleQuery);
	}

	@DeleteMapping("/role/delete/{id}")
	public R deleteRole(@PathVariable Long id) {
		if (Objects.isNull(id)) {
			return R.fail("SYSTEM.0002", "id不能为空");
		}

		return returnPairResult(roleService.deleteRole(id));
	}

	@GetMapping("/role/uid/info/{uid}")
	public R queryRoleIds(@PathVariable("uid") Long uid) {
		if (Objects.isNull(uid)) {
			return R.fail("SYSTEM.0002", "参数错误");
		}

		return returnPairResult(roleService.queryBindUidRids(uid));
	}

}
