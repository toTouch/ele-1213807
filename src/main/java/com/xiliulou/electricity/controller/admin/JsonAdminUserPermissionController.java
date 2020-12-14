package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.PermissionResourceService;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import com.xiliulou.electricity.web.query.PermissionResourceQuery;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.ss.formula.functions.T;
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

import java.util.List;
import java.util.Objects;

/**
 * @author: eclair
 * @Date: 2020/12/10 18:01
 * @Description:
 */
@RestController
@RequestMapping("/admin")
public class JsonAdminUserPermissionController extends BaseController {
	@Autowired
	PermissionResourceService permissionResourceService;

	@GetMapping("/permission/list")
	public R getList() {
		return returnPairResult(permissionResourceService.getList());
	}

	@PostMapping("/permission/add")
	public R addPermission(@Validated(value = CreateGroup.class) @RequestBody PermissionResourceQuery permissionResourceQuery) {
		return returnPairResult(permissionResourceService.addPermissionResource(permissionResourceQuery));
	}

	@PutMapping("/permission/update")
	public R updatePermission(@Validated(value = UpdateGroup.class) @RequestBody PermissionResourceQuery permissionResourceQuery) {
		if (permissionResourceQuery.getId() == 0) {
			return R.fail("SYSTEM.0002", "id不能为空！");
		}
		return returnPairResult(permissionResourceService.updatePermissionResource(permissionResourceQuery));
	}

	@DeleteMapping("/permission/delete/{id}")
	public R deletePermission(@PathVariable("id") Long permissionId) {
		if (Objects.isNull(permissionId)) {
			return R.fail("SYSTEM.0002", "id不能为空");
		}

		return returnPairResult(permissionResourceService.deletePermission(permissionId));
	}

	/**
	 * 绑定权限
	 *
	 * @param roleId
	 * @param jsonPids
	 * @return
	 */
	@PostMapping("/permission/role/bind")
	public R bindPermissionToRole(@RequestParam("roleId") Long roleId, @RequestParam("pids") String jsonPids) {
		List<Long> pids = JsonUtil.fromJsonArray(jsonPids, Long.class);
		if (!DataUtil.collectionIsUsable(pids)) {
			return R.fail("SYSTEM.0002", "参数不合法");
		}
		return returnPairResult(permissionResourceService.bindPermissionToRole(roleId, pids));
	}

	@GetMapping("/permission/role/info/{id}")
	public R getPermissionsByRole(@PathVariable("id") Long rid) {
		if(Objects.isNull(rid)) {
			return R.fail("SYSTEM.0002", "参数不合法");
		}

		return returnPairResult(permissionResourceService.getPermissionsByRole(rid));
	}

}
