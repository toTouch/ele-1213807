package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.annotation.Log;
import com.xiliulou.electricity.entity.ElectricityCabinetModel;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.ElectricityCabinetModelQuery;
import com.xiliulou.electricity.service.ElectricityCabinetModelService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * 换电柜型号表(TElectricityCabinetModel)表控制层
 *
 * @author makejava
 * @since 2020-11-25 11:01:04
 */
@RestController
public class JsonAdminElectricityCabinetModelController {
	/**
	 * 服务对象
	 */
	@Autowired
	ElectricityCabinetModelService electricityCabinetModelService;

	@Autowired
	ElectricityCabinetService electricityCabinetService;

	//新增换电柜型号
	@PostMapping(value = "/admin/electricityCabinetModel")
	public R save(@RequestBody @Validated ElectricityCabinetModel electricityCabinetModel) {
		return electricityCabinetModelService.save(electricityCabinetModel);
	}

	//修改换电柜型号
	@PutMapping(value = "/admin/electricityCabinetModel")
	@Log(title = "修改换电柜型号")
	public R update(@RequestBody ElectricityCabinetModel electricityCabinetModel) {
		return electricityCabinetModelService.edit(electricityCabinetModel);
	}

	//删除换电柜型号
	@DeleteMapping(value = "/admin/electricityCabinetModel/{id}")
	@Log(title = "删除换电柜型号")
	public R delete(@PathVariable("id") Integer id) {
		if (Objects.isNull(id)) {
			return R.fail("ELECTRICITY.0007", "不合法的参数");
		}
		return electricityCabinetModelService.delete(id);
	}

	//列表查询
	@GetMapping(value = "/admin/electricityCabinetModel/list")
	public R queryList(@RequestParam("size") Long size,
			@RequestParam("offset") Long offset,
			@RequestParam(value = "name", required = false) String name) {
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

		if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
			return R.ok();
		}

		Integer tenantId = TenantContextHolder.getTenantId();

		ElectricityCabinetModelQuery electricityCabinetModelQuery = ElectricityCabinetModelQuery.builder()
				.offset(offset)
				.size(size)
				.name(name)
				.tenantId(tenantId).build();

		return electricityCabinetModelService.queryList(electricityCabinetModelQuery);
	}

	//列表查询
	@GetMapping(value = "/admin/electricityCabinetModel/queryCount")
	public R queryCount(@RequestParam(value = "name", required = false) String name) {
		TokenUser user = SecurityUtils.getUserInfo();
		if (Objects.isNull(user)) {
			return R.fail("ELECTRICITY.0001", "未找到用户");
		}

		if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
			return R.ok();
		}

		Integer tenantId = TenantContextHolder.getTenantId();

		ElectricityCabinetModelQuery electricityCabinetModelQuery = ElectricityCabinetModelQuery.builder()
				.name(name)
				.tenantId(tenantId).build();

		return electricityCabinetModelService.queryCount(electricityCabinetModelQuery);
	}

	/**
	 * 柜机型号search
	 * @param size
	 * @param offset
	 * @param name
	 * @return
	 */
	@GetMapping("/admin/cabinetModel/search")
	public R cabinetSearch(@RequestParam("size") Long size, @RequestParam("offset") Long offset,
						   @RequestParam(value = "name", required = false) String name) {
		if (size < 0 || size > 20) {
			size = 20L;
		}

		if (offset < 0) {
			offset = 0L;
		}

		return electricityCabinetService.cabinetSearch(size, offset, name, TenantContextHolder.getTenantId());
	}

}
